package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.BotService;
import es.codeurjc.grupo12.scissors_please.service.MatchService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

@Controller
@RequestMapping("/user")
public class UserController {

  private static final int TOP_BOTS_LIMIT = 5;
  private static final int RECENT_MATCHES_LIMIT = 5;

  @Autowired private UserService userService;
  @Autowired private BotService botService;
  @Autowired private MatchService matchService;

  @GetMapping("/profile")
  public String userProfile(
      @RequestParam(required = false) String user, Authentication authentication, Model model) {
    User currentUser = userService.getCurrentUser(authentication);
    User targetUser = resolveTargetUser(user, currentUser);

    boolean ownProfile = isOwnProfile(currentUser, targetUser);
    boolean includePrivateBots = ownProfile || userService.isAdmin(currentUser);

    List<Bot> allBots = botService.getBotsForUser(targetUser, includePrivateBots);
    List<ProfileBotView> topBots =
        botService.getTopBotsForUser(targetUser, includePrivateBots, TOP_BOTS_LIMIT).stream()
            .map(this::toProfileBotView)
            .toList();

    List<MatchService.UserMatchItem> userMatches =
        matchService.getUserHomeMatches(requireUserId(targetUser), Integer.MAX_VALUE);
    int wins =
        (int) userMatches.stream().filter(match -> "win".equalsIgnoreCase(match.result())).count();
    int winRate = userMatches.isEmpty() ? 0 : (int) Math.round((wins * 100.0) / userMatches.size());

    model.addAttribute("userId", currentUser.getId());
    model.addAttribute("hasProfilePhoto", currentUser.getImage() != null);
    model.addAttribute("profileHeading", ownProfile ? "My Profile" : "User Profile");
    model.addAttribute("profileInitial", resolveInitial(targetUser.getUsername()));
    model.addAttribute("profileName", targetUser.getUsername());
    model.addAttribute("profileEmail", targetUser.getEmail());
    model.addAttribute("providerLabel", resolveProvider(targetUser.getOauthProvider()));
    model.addAttribute("rolesLabel", resolveRoles(targetUser.getRoles()));
    model.addAttribute("showPhotoActions", ownProfile);
    model.addAttribute("showBackToMyProfile", !ownProfile);
    model.addAttribute("myProfileHref", "/user/profile");
    model.addAttribute("botsPageHref", buildBotsPageHref(currentUser, targetUser));

    model.addAttribute("totalBots", allBots.size());
    model.addAttribute("totalMatches", userMatches.size());
    model.addAttribute("winRate", winRate);
    model.addAttribute("wins", wins);
    model.addAttribute("topBots", topBots);
    model.addAttribute("recentMatches", userMatches.stream().limit(RECENT_MATCHES_LIMIT).toList());

    return "user-detail";
  }

  @PostMapping("/profile/update-photo")
  public String updatePhoto(
      @RequestParam("image") MultipartFile image, Authentication authentication)
      throws IOException {
    User currentUser = userService.getCurrentUser(authentication);

    if (!image.isEmpty()) {
      if (!handleImageUpload(currentUser, image)) {
        return "error";
      }
      userService.updateUser(currentUser);
    }

    return "redirect:/user/profile";
  }

  private User resolveTargetUser(String username, User currentUser) {
    if (username == null || username.isBlank()) {
      return currentUser;
    }
    return userService.findByUsername(username.trim()).orElse(currentUser);
  }

  private boolean isOwnProfile(User currentUser, User targetUser) {
    return targetUser.getId() != null && targetUser.getId().equals(currentUser.getId());
  }

  private Long requireUserId(User user) {
    if (user.getId() == null) {
      throw new IllegalArgumentException("User id is required to load profile");
    }
    return user.getId();
  }

  private ProfileBotView toProfileBotView(Bot bot) {

    return new ProfileBotView(bot.getName(), bot.getElo(), bot.isPublic());
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
      return "/bots/my-bots";
    }
    String encodedUsername =
        UriUtils.encodeQueryParam(targetUser.getUsername(), StandardCharsets.UTF_8);
    return "/bots/my-bots?user=" + encodedUsername;
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

    } catch (IOException e) {
      return false;
    }
  }

  private record ProfileBotView(String name, int elo, boolean publicBot) {}
}
