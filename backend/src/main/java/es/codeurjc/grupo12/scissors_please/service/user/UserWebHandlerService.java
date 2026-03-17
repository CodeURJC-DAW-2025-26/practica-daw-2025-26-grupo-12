package es.codeurjc.grupo12.scissors_please.service.user;

import es.codeurjc.grupo12.scissors_please.config.ErrorConstants;
import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.bot.BotService;
import es.codeurjc.grupo12.scissors_please.service.match.MatchService;
import es.codeurjc.grupo12.scissors_please.views.ProfileBotView;
import es.codeurjc.grupo12.scissors_please.views.UserMatchItem;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import es.codeurjc.grupo12.scissors_please.views.WebPageView;
import es.codeurjc.grupo12.scissors_please.views.WebRedirectView;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

@Service
public class UserWebHandlerService {

  private static final int TOP_BOTS_LIMIT = 5;
  private static final int RECENT_MATCHES_LIMIT = 5;
  private static final DateTimeFormatter DELETE_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  @Autowired private UserService userService;
  @Autowired private BotService botService;
  @Autowired private MatchService matchService;

  public WebFlowView userProfileHandler(String targetUsername, Authentication authentication) {
    User currentUser = null;
    try {
      if (authentication != null && authentication.isAuthenticated()) {
        currentUser = userService.getCurrentUser(authentication);
      }
    } catch (Exception exception) {
      // Ignore exceptions and treat as unauthenticated
    }

    User targetUser = resolveTargetUser(targetUsername, currentUser);
    if (targetUser == null) {
      return WebPageView.of("error")
          .attribute("errorMessage", "The user is no longer in the database.")
          .attribute("errorCode", ErrorConstants.NOT_FOUND_CODE);
    }

    boolean ownProfile = isOwnProfile(currentUser, targetUser);
    boolean isAdmin = currentUser != null && userService.isAdmin(currentUser);

    WebPageView view =
        WebPageView.of("user-detail")
            .attribute("userId", targetUser.getId())
            .attribute("loggedIn", currentUser != null)
            .attribute("hasProfilePhoto", targetUser.getImage() != null)
            .attribute("profileHeading", ownProfile ? "My Profile" : "User Profile")
            .attribute("profileInitial", resolveInitial(targetUser.getUsername()))
            .attribute("profileName", targetUser.getUsername())
            .attribute("profileEmail", targetUser.getEmail())
            .attribute("providerLabel", resolveProvider(targetUser.getOauthProvider()))
            .attribute("rolesLabel", resolveRoles(targetUser.getRoles()))
            .attribute("profileDeleted", targetUser.getDeleteDate() != null)
            .attribute("profileDeleteDate", formatDeleteDate(targetUser))
            .attribute("showPhotoActions", ownProfile)
            .attribute("showBackToMyProfile", !ownProfile && currentUser != null)
            .attribute("myProfileHref", "/user/profile")
            .attribute("isAdmin", isAdmin);

    boolean includePrivateBots = ownProfile || isAdmin;

    // Should change this to only load the count, we dont need all the info in memory just to count
    // it
    List<Bot> allBots = botService.getBotsForUser(targetUser, includePrivateBots);
    List<ProfileBotView> topBots =
        botService.getTopBotsForUser(targetUser, includePrivateBots, TOP_BOTS_LIMIT).stream()
            .map(this::toProfileBotView)
            .toList();
    List<UserMatchItem> userMatches =
        matchService.getUserHomeMatches(requireUserId(targetUser), Integer.MAX_VALUE);
    // Same here, we should only load the count and not all the matches, should be done in db
    int wins =
        (int) userMatches.stream().filter(match -> "win".equalsIgnoreCase(match.result())).count();
    int winRate = userMatches.isEmpty() ? 0 : (int) Math.round((wins * 100.0) / userMatches.size());

    return view.attribute("botsPageHref", buildBotsPageHref(currentUser, targetUser))
        .attribute("totalBots", allBots.size())
        .attribute("totalMatches", userMatches.size())
        .attribute("winRate", winRate)
        .attribute("wins", wins)
        .attribute("topBots", topBots)
        .attribute("recentMatches", userMatches.stream().limit(RECENT_MATCHES_LIMIT).toList());
  }

  public WebFlowView updatePhotoHandler(MultipartFile image, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);

    if (!image.isEmpty()) {
      if (!handleImageUpload(currentUser, image)) {
        return WebPageView.of("error")
            .attribute("errorMessage", ErrorConstants.IMAGE_ERROR_UPLOAD)
            .attribute("errorCode", ErrorConstants.BAD_REQUEST_CODE);
      }
      userService.updateUser(currentUser);
    }

    return WebRedirectView.to("/user/profile");
  }

  private User resolveTargetUser(String username, User currentUser) {
    if (username == null || username.isBlank()) {
      return currentUser;
    }
    return userService.findByUsername(username.trim()).orElse(null);
  }

  private boolean isOwnProfile(User currentUser, User targetUser) {
    return currentUser != null
        && targetUser.getId() != null
        && targetUser.getId().equals(currentUser.getId());
  }

  private Long requireUserId(User user) {
    if (user.getId() == null) {
      throw new IllegalArgumentException("User id is required to load profile");
    }
    return user.getId();
  }

  private ProfileBotView toProfileBotView(Bot bot) {
    return new ProfileBotView(bot.getId(), bot.getName(), bot.getElo(), bot.isPublic());
  }

  private String resolveInitial(String value) {
    if (value == null || value.isBlank()) {
      return "?";
    }
    return value.substring(0, 1).toUpperCase();
  }

  private String resolveProvider(String oauthProvider) {
    if (oauthProvider == null || oauthProvider.isBlank()) {
      return "local";
    }
    return oauthProvider;
  }

  private String resolveRoles(List<String> roles) {
    if (roles == null || roles.isEmpty()) {
      return "USER";
    }
    return String.join(", ", roles);
  }

  private String buildBotsPageHref(User currentUser, User targetUser) {
    if (isOwnProfile(currentUser, targetUser)) {
      return "/bots/user-bots";
    }
    String encodedUsername =
        UriUtils.encodeQueryParam(targetUser.getUsername(), StandardCharsets.UTF_8);
    return "/bots/user-bots?username=" + encodedUsername;
  }

  private String formatDeleteDate(User user) {
    if (user.getDeleteDate() == null) {
      return "";
    }
    return user.getDeleteDate().format(DELETE_DATE_FORMAT);
  }

  private boolean handleImageUpload(User user, MultipartFile imageFile) {
    if (imageFile == null || imageFile.isEmpty()) {
      return true;
    }

    String contentType = imageFile.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      return false;
    }

    try {
      Image img = new Image();
      img.setFilename(imageFile.getOriginalFilename());
      img.setContentType(contentType);
      img.setData(imageFile.getBytes());
      user.setImage(img);
      return true;
    } catch (IOException exception) {
      return false;
    }
  }
}
