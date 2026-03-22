package es.codeurjc.grupo12.scissors_please.service.admin;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.TournamentStatus;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository.MonthlyUserCount;
import es.codeurjc.grupo12.scissors_please.security.ActiveSessionService;
import es.codeurjc.grupo12.scissors_please.service.bot.BotService;
import es.codeurjc.grupo12.scissors_please.service.chart.ChartService;
import es.codeurjc.grupo12.scissors_please.service.tournament.TournamentAutomationService;
import es.codeurjc.grupo12.scissors_please.service.tournament.TournamentAutomationService.RunNowResult;
import es.codeurjc.grupo12.scissors_please.service.tournament.TournamentService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService.UserStatusFilter;
import es.codeurjc.grupo12.scissors_please.views.AdminTournamentDetail;
import es.codeurjc.grupo12.scissors_please.views.AdminUserView;
import es.codeurjc.grupo12.scissors_please.views.TournamentListItem;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import es.codeurjc.grupo12.scissors_please.views.WebPageView;
import es.codeurjc.grupo12.scissors_please.views.WebRedirectView;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

@Service
public class AdminWebHandlerService {

  private static final int MIN_PLAYERS = 4;
  private static final int MAX_PLAYERS = 128;
  private static final int MAX_TITLE_LENGTH = 80;
  private static final int MAX_DESCRIPTION_LENGTH = 500;
  private static final int MAX_PRIZE_LENGTH = 120;

  @Autowired private TournamentService tournamentService;
  @Autowired private ChartService chartService;
  @Autowired private TournamentAutomationService tournamentAutomationService;
  @Autowired private UserService userService;
  @Autowired private ActiveSessionService activeSessionService;
  @Autowired private BotService botService;

  public WebFlowView adminTournamentsHandler(Pageable pageable, String query, Integer processed) {
    String normalizedQuery = normalizeQuery(query);
    Page<TournamentListItem> tournamentPage =
        tournamentService.getTournamentPage(normalizedQuery, pageable);
    int fromItem =
        tournamentPage.isEmpty() ? 0 : (int) tournamentPage.getPageable().getOffset() + 1;
    int toItem = tournamentPage.isEmpty() ? 0 : fromItem + tournamentPage.getNumberOfElements() - 1;

    WebPageView view =
        WebPageView.of("admin-tournaments")
            .attribute("tournaments", tournamentPage.getContent())
            .attribute("showEmpty", pageable.getPageNumber() == 0 && tournamentPage.isEmpty())
            .attribute("nextPage", tournamentPage.getNumber() + 1)
            .attribute("hasMore", tournamentPage.hasNext())
            .attribute("totalElements", tournamentPage.getTotalElements())
            .attribute("fromItem", fromItem)
            .attribute("toItem", toItem)
            .attribute("size", Math.max(pageable.getPageSize(), 1))
            .attribute("searchQuery", normalizedQuery);

    if (processed != null) {
      view.attribute(
          "successMessage",
          processed > 0
              ? "Processed " + processed + " tournament(s)."
              : "No upcoming tournaments to process.");
    }
    return view;
  }

  public WebFlowView adminTournamentsPageHandler(Pageable pageable, String query) {
    String normalizedQuery = normalizeQuery(query);
    Page<TournamentListItem> tournamentPage =
        tournamentService.getTournamentPage(normalizedQuery, pageable);
    int fromItem =
        tournamentPage.isEmpty() ? 0 : (int) tournamentPage.getPageable().getOffset() + 1;
    int toItem = tournamentPage.isEmpty() ? 0 : fromItem + tournamentPage.getNumberOfElements() - 1;

    return WebPageView.of("components/admin-tournament-page-chunk")
        .attribute("tournaments", tournamentPage.getContent())
        .attribute("showEmpty", pageable.getPageNumber() == 0 && tournamentPage.isEmpty())
        .attribute("nextPage", tournamentPage.getNumber() + 1)
        .attribute("hasMore", tournamentPage.hasNext())
        .attribute("totalElements", tournamentPage.getTotalElements())
        .attribute("fromItem", fromItem)
        .attribute("toItem", toItem)
        .attribute("searchQuery", normalizedQuery);
  }

  public WebFlowView adminTournamentCreatePageHandler(String success) {
    WebPageView view = createTournamentFormView("", "", "", "", "", "");
    if (success != null) {
      view.attribute("successMessage", "Tournament created successfully.");
    }
    return view;
  }

  public WebFlowView createTournamentHandler(
      String adminTitle,
      String adminMaxPlayers,
      String adminRegistrationStart,
      String adminStartDate,
      String adminDescription,
      String adminPrize,
      MultipartFile image) {
    List<String> errors = new ArrayList<>();

    String title = safeTrim(adminTitle);
    String maxPlayersRaw = safeTrim(adminMaxPlayers);
    String registrationStartRaw = safeTrim(adminRegistrationStart);
    String startDateRaw = safeTrim(adminStartDate);
    String description = safeTrim(adminDescription);
    String prize = safeTrim(adminPrize);

    if (title.isBlank()) {
      errors.add("Title is required.");
    } else if (title.length() > MAX_TITLE_LENGTH) {
      errors.add("Title cannot exceed " + MAX_TITLE_LENGTH + " characters.");
    }

    Integer maxPlayers = parseMaxPlayers(maxPlayersRaw, errors);
    LocalDate registrationStart =
        parseDate("Registration opens date is invalid.", registrationStartRaw, errors);
    LocalDate startDate = parseDate("Start date is invalid.", startDateRaw, errors);

    if (description.length() > MAX_DESCRIPTION_LENGTH) {
      errors.add("Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters.");
    }
    if (prize.length() > MAX_PRIZE_LENGTH) {
      errors.add("Prize cannot exceed " + MAX_PRIZE_LENGTH + " characters.");
    }
    if (registrationStart != null && startDate != null && registrationStart.isAfter(startDate)) {
      errors.add("Registration opens date must be before or equal to start date.");
    }

    if (!errors.isEmpty()) {
      return createTournamentFormView(
              title, maxPlayersRaw, registrationStartRaw, startDateRaw, description, prize)
          .attribute("errorMessages", errors);
    }

    Image tournamentImage = null;
    if (image != null && !image.isEmpty()) {
      try {
        tournamentImage = new Image();
        tournamentImage.setFilename(image.getOriginalFilename());
        tournamentImage.setContentType(image.getContentType());
        tournamentImage.setData(image.getBytes());
      } catch (IOException exception) {
        return errorView(ResponseConstants.IMAGE_ERROR_UPLOAD, ResponseConstants.BAD_REQUEST_CODE);
      }
    }

    tournamentService.createTournament(
        title, tournamentImage, description, maxPlayers, registrationStart, startDate, prize);
    return WebRedirectView.to("/admin/tournaments/create?success");
  }

  public WebFlowView editTournamentPageHandler(Long id) {
    Optional<Tournament> tournament = tournamentService.getTournamentById(id);
    if (tournament.isEmpty()) {
      return errorView(ResponseConstants.TOURNAMENT_NOT_FOUND, ResponseConstants.NOT_FOUND_CODE);
    }
    return WebPageView.of("admin-tournament-edit").attribute("tournament", tournament.get());
  }

  public WebFlowView editTournamentHandler(
      Long id,
      String name,
      String description,
      String startDate,
      int slots,
      MultipartFile image,
      String status) {
    Optional<Tournament> tournamentOp = tournamentService.getTournamentById(id);
    if (tournamentOp.isEmpty()) {
      return errorView(ResponseConstants.TOURNAMENT_NOT_FOUND, ResponseConstants.NOT_FOUND_CODE);
    }

    Tournament tournament = tournamentOp.get();
    if (!handleImageUpload(tournament, image)) {
      return errorView(ResponseConstants.IMAGE_ERROR_UPLOAD, ResponseConstants.BAD_REQUEST_CODE);
    }

    tournament.setName(name);
    tournament.setDescription(description != null ? description : "");
    tournament.setSlots(slots);
    tournament.setStatus(TournamentStatus.fromDisplayName(status));

    if (startDate != null && !startDate.isBlank()) {
      try {
        tournament.setStartDate(LocalDate.parse(startDate));
      } catch (Exception exception) {
        return errorView(ResponseConstants.DATE_INVALID, ResponseConstants.BAD_REQUEST_CODE);
      }
    }

    tournamentService.save(tournament);
    return WebRedirectView.to("/admin/tournaments");
  }

  public WebFlowView adminTournamentDetailHandler(Long id, String runResult) {
    AdminTournamentDetail tournament = tournamentService.getAdminTournamentDetail(id);
    WebPageView view =
        WebPageView.of("admin-tournament-detail").attribute("tournament", tournament);

    if (runResult == null) {
      return view;
    }

    return switch (runResult) {
      case "executed" -> view.attribute("successMessage", "Tournament executed successfully.");
      case "not-upcoming" ->
          view.attribute("errorMessage", "Only upcoming tournaments can be executed now.");
      case "not-found" -> view.attribute("errorMessage", "Tournament not found.");
      default -> view;
    };
  }

  public WebFlowView processDueTournamentsManuallyHandler() {
    int processed = tournamentAutomationService.processDueUpcomingTournamentsNow();
    return WebRedirectView.to("/admin/tournaments?processed=" + processed);
  }

  public WebFlowView runTournamentNowHandler(Long id) {
    RunNowResult result = tournamentAutomationService.runTournamentNow(id);
    return switch (result) {
      case EXECUTED ->
          WebRedirectView.to("/admin/tournaments/detail?id=" + id + "&runResult=executed");
      case NOT_UPCOMING ->
          WebRedirectView.to("/admin/tournaments/detail?id=" + id + "&runResult=not-upcoming");
      case NOT_FOUND -> WebRedirectView.to("/admin/tournaments/detail?runResult=not-found");
    };
  }

  public WebFlowView deleteTournamentHandler(Long id) {
    try {
      tournamentService.deleteTournament(id);
      return WebRedirectView.to("/admin/tournaments")
          .flash("successMessage", "Tournament deleted successfully.");
    } catch (NoSuchElementException exception) {
      return WebRedirectView.to("/admin/tournaments")
          .flash("errorMessage", "Tournament not found.");
    }
  }

  public WebFlowView adminUsersHandler(
      Pageable pageable, String query, String status, Authentication authentication) {
    List<MonthlyUserCount> data = userService.getMonthlyUserCount();
    byte[] barChart = chartService.generateUserHistory(data);

    WebPageView view =
        buildUsersSearchView(
            "admin-users", pageable, query, status, userService.getCurrentUser(authentication));
    view.attribute("barChart", Base64.getEncoder().encodeToString(barChart));
    return view;
  }

  public WebFlowView adminUsersTableHandler(
      Pageable pageable, String query, String status, Authentication authentication) {
    return buildUsersSearchView(
        "components/admin-user-page-chunk",
        pageable,
        query,
        status,
        userService.getCurrentUser(authentication));
  }

  public WebFlowView blockUserHandler(
      Long id, String query, String status, Authentication authentication) {
    return updateBlockedStatusHandler(id, query, status, authentication, true);
  }

  public WebFlowView unblockUserHandler(
      Long id, String query, String status, Authentication authentication) {
    return updateBlockedStatusHandler(id, query, status, authentication, false);
  }

  public WebFlowView adminBotsHandler(Pageable pageable, String query, String visibility) {
    return buildBotsSearchView("admin-bots", pageable, query, visibility);
  }

  public WebFlowView adminBotsTableHandler(Pageable pageable, String query, String visibility) {
    return buildBotsSearchView("components/admin-bot-page-chunk", pageable, query, visibility);
  }

  public WebFlowView deleteUserHandler(
      Long id, String query, String status, Authentication authentication) {
    String normalizedQuery = normalizeQuery(query);
    UserStatusFilter statusFilter = UserStatusFilter.fromValue(status);
    try {
      User currentAdmin = userService.getCurrentUser(authentication);
      User targetUser = userService.deleteUser(id, currentAdmin);
      activeSessionService.expireSessions(targetUser);
      return WebRedirectView.to(buildUsersRedirect(normalizedQuery, statusFilter))
          .flash(
              "successMessage", "User " + targetUser.getUsername() + " was deleted successfully.");
    } catch (IllegalArgumentException exception) {
      return WebRedirectView.to(buildUsersRedirect(normalizedQuery, statusFilter))
          .flash("errorMessage", exception.getMessage());
    }
  }

  private WebFlowView updateBlockedStatusHandler(
      Long userId, String query, String status, Authentication authentication, boolean blocked) {
    String normalizedQuery = normalizeQuery(query);
    UserStatusFilter statusFilter = UserStatusFilter.fromValue(status);
    try {
      User currentAdmin = userService.getCurrentUser(authentication);
      User targetUser =
          blocked
              ? userService.blockUser(userId, currentAdmin)
              : userService.unblockUser(userId, currentAdmin);

      if (blocked) {
        activeSessionService.expireSessions(targetUser);
      }

      String action = blocked ? "blocked" : "unblocked";
      return WebRedirectView.to(buildUsersRedirect(normalizedQuery, statusFilter))
          .flash(
              "successMessage",
              "User " + targetUser.getUsername() + " was " + action + " successfully.");
    } catch (IllegalArgumentException exception) {
      return WebRedirectView.to(buildUsersRedirect(normalizedQuery, statusFilter))
          .flash("errorMessage", exception.getMessage());
    }
  }

  private WebPageView buildBotsSearchView(
      String template, Pageable pageable, String query, String visibility) {
    String normalizedQuery = normalizeQuery(query);
    Page<Bot> botPage = botService.getAdminBotPage(normalizedQuery, visibility, pageable);
    int fromItem = botPage.isEmpty() ? 0 : (int) botPage.getPageable().getOffset() + 1;
    int toItem = botPage.isEmpty() ? 0 : fromItem + botPage.getNumberOfElements() - 1;

    return WebPageView.of(template)
        .attribute("searchQuery", normalizedQuery)
        .attribute("visibilityFilter", visibility)
        .attribute("visAll", "all".equals(visibility))
        .attribute("visPublic", "public".equals(visibility))
        .attribute("visPrivate", "private".equals(visibility))
        .attribute("bots", botPage.getContent())
        .attribute("nextPage", botPage.getNumber() + 1)
        .attribute("hasMore", botPage.hasNext())
        .attribute("totalElements", botPage.getTotalElements())
        .attribute("fromItem", fromItem)
        .attribute("toItem", toItem)
        .attribute("resultCount", botPage.getTotalElements())
        .attribute("size", Math.max(pageable.getPageSize(), 1));
  }

  private WebPageView buildUsersSearchView(
      String template, Pageable pageable, String query, String status, User currentAdmin) {
    String normalizedQuery = normalizeQuery(query);
    UserStatusFilter statusFilter = UserStatusFilter.fromValue(status);
    Page<User> userPage = userService.getUserPage(normalizedQuery, statusFilter, pageable);
    List<AdminUserView> users =
        userPage.getContent().stream().map(user -> toAdminUserView(user, currentAdmin)).toList();
    int fromItem = userPage.isEmpty() ? 0 : (int) userPage.getPageable().getOffset() + 1;
    int toItem = userPage.isEmpty() ? 0 : fromItem + userPage.getNumberOfElements() - 1;

    return WebPageView.of(template)
        .attribute("searchQuery", normalizedQuery)
        .attribute("statusFilter", statusFilter.value())
        .attribute("statusAll", statusFilter == UserStatusFilter.ALL)
        .attribute("statusActive", statusFilter == UserStatusFilter.ACTIVE)
        .attribute("statusBlocked", statusFilter == UserStatusFilter.BLOCKED)
        .attribute("users", users)
        .attribute("nextPage", userPage.getNumber() + 1)
        .attribute("hasMore", userPage.hasNext())
        .attribute("totalElements", userPage.getTotalElements())
        .attribute("fromItem", fromItem)
        .attribute("toItem", toItem)
        .attribute("resultCount", userPage.getTotalElements())
        .attribute("size", Math.max(pageable.getPageSize(), 1));
  }

  private AdminUserView toAdminUserView(User user, User currentAdmin) {
    boolean isSameUser =
        currentAdmin != null && user.getId() != null && user.getId().equals(currentAdmin.getId());
    boolean adminRole = userService.isAdmin(user);
    return new AdminUserView(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        buildUserProfileHref(user.getUsername()),
        resolveProvider(user.getOauthProvider()),
        user.isBlocked(),
        !isSameUser && !adminRole,
        adminRole,
        user.getImage() != null,
        resolveInitial(user.getUsername()));
  }

  private Integer parseMaxPlayers(String value, List<String> errors) {
    if (value.isBlank()) {
      errors.add("Max players is required.");
      return null;
    }
    try {
      int maxPlayers = Integer.parseInt(value);
      if (maxPlayers < MIN_PLAYERS || maxPlayers > MAX_PLAYERS) {
        errors.add("Max players must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS + ".");
      }
      return maxPlayers;
    } catch (NumberFormatException exception) {
      errors.add("Max players must be a valid number.");
      return null;
    }
  }

  private LocalDate parseDate(String invalidMessage, String value, List<String> errors) {
    if (value.isBlank()) {
      errors.add(invalidMessage.replace(" is invalid.", " is required."));
      return null;
    }
    try {
      return LocalDate.parse(value);
    } catch (DateTimeParseException exception) {
      errors.add(invalidMessage);
      return null;
    }
  }

  private String safeTrim(String value) {
    return value == null ? "" : value.trim();
  }

  private WebPageView createTournamentFormView(
      String title,
      String maxPlayers,
      String registrationStart,
      String startDate,
      String description,
      String prize) {
    return WebPageView.of("admin-tournament-create")
        .attribute("adminTitle", title)
        .attribute("adminMaxPlayers", maxPlayers)
        .attribute("adminRegistrationStart", registrationStart)
        .attribute("adminStartDate", startDate)
        .attribute("adminDescription", description)
        .attribute("adminPrize", prize);
  }

  private String buildUserProfileHref(String username) {
    if (username == null || username.isBlank()) {
      return "/user/profile";
    }
    return "/user/profile?user="
        + UriUtils.encodeQueryParam(username.trim(), StandardCharsets.UTF_8);
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

  private String normalizeQuery(String query) {
    return query == null ? "" : query.trim();
  }

  private String buildUsersRedirect(String query, UserStatusFilter statusFilter) {
    StringBuilder redirectBuilder = new StringBuilder("/admin/users");
    boolean hasQueryParam = false;
    if (!query.isBlank()) {
      redirectBuilder
          .append("?q=")
          .append(UriUtils.encodeQueryParam(query, StandardCharsets.UTF_8));
      hasQueryParam = true;
    }
    if (statusFilter != UserStatusFilter.ALL) {
      redirectBuilder
          .append(hasQueryParam ? "&" : "?")
          .append("status=")
          .append(statusFilter.value());
    }
    return redirectBuilder.toString();
  }

  private boolean handleImageUpload(Tournament tournament, MultipartFile imageFile) {
    if (imageFile == null || imageFile.isEmpty()) {
      return true;
    }

    String contentType = imageFile.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      return false;
    }

    try {
      Image image = new Image();
      image.setFilename(imageFile.getOriginalFilename());
      image.setContentType(contentType);
      image.setData(imageFile.getBytes());
      tournament.setImage(image);
      return true;
    } catch (IOException exception) {
      return false;
    }
  }

  private WebPageView errorView(String errorMessage, String errorCode) {
    return WebPageView.of("error")
        .attribute("errorMessage", errorMessage)
        .attribute("errorCode", errorCode);
  }
}
