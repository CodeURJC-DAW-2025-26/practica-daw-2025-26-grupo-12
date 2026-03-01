package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.config.ErrorConstants;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository.MonthlyUserCount;
import es.codeurjc.grupo12.scissors_please.security.ActiveSessionService;
import es.codeurjc.grupo12.scissors_please.service.ChartService;
import es.codeurjc.grupo12.scissors_please.service.TournamentAutomationService;
import es.codeurjc.grupo12.scissors_please.service.TournamentService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import es.codeurjc.grupo12.scissors_please.service.UserService.UserStatusFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

@Controller
@RequestMapping("/admin")
public class AdminController {

  private static final int MIN_PLAYERS = 4;
  private static final int MAX_PLAYERS = 128;
  private static final int MAX_TITLE_LENGTH = 80;
  private static final int MAX_DESCRIPTION_LENGTH = 500;
  private static final int MAX_PRIZE_LENGTH = 120;
  private static final Set<String> ALLOWED_FORMATS =
      Set.of("Single Elimination", "Double Elimination", "Round Robin");

  @Autowired private TournamentService tournamentService;
  @Autowired private ChartService chartService;
  @Autowired private TournamentAutomationService tournamentAutomationService;
  @Autowired private UserService userService;
  @Autowired private ActiveSessionService activeSessionService;

  @GetMapping("/panel")
  public String adminPanel(
      @PageableDefault(size = 5) Pageable pageable,
      @RequestParam(required = false) Integer processed,
      Model model) {

    List<MonthlyUserCount> data = userService.getMonthlyUserCount();
    byte[] barChart = chartService.generateUserHistory(data);

    String base64Chart = Base64.getEncoder().encodeToString(barChart);
    model.addAttribute("barChart", base64Chart);
    model.addAttribute("size", Math.max(pageable.getPageSize(), 1));
    model.addAttribute("fromItem", 0);
    model.addAttribute("toItem", 0);
    model.addAttribute("totalElements", 0);

    if (processed != null) {
      if (processed > 0) {
        model.addAttribute("successMessage", "Processed " + processed + " tournament(s).");
      } else {
        model.addAttribute("successMessage", "No upcoming tournaments to process.");
      }
    }
    return "admin-panel";
  }

  @GetMapping("/panel/page")
  public String adminPanelPage(@PageableDefault(size = 5) Pageable pageable, Model model) {
    TournamentService.TournamentPage tournamentPage = tournamentService.getTournamentPage(pageable);
    model.addAttribute("tournaments", tournamentPage.tournaments());
    model.addAttribute(
        "showEmpty", pageable.getPageNumber() == 0 && tournamentPage.tournaments().isEmpty());
    model.addAttribute("nextPage", tournamentPage.nextPage());
    model.addAttribute("hasMore", tournamentPage.hasMore());
    model.addAttribute("totalElements", tournamentPage.totalElements());
    model.addAttribute("fromItem", tournamentPage.fromItem());
    model.addAttribute("toItem", tournamentPage.toItem());
    return "components/admin-tournament-page-chunk";
  }

  @GetMapping("/tournaments/create")
  public String adminTournamentCreate(@RequestParam(required = false) String success, Model model) {
    if (success != null) {
      model.addAttribute("successMessage", "Tournament created successfully.");
    }
    setCreateFormModel(model, "", "", "", "", "", "");
    return "admin-tournament-create";
  }

  @PostMapping("/tournaments/create")
  public String createTournament(
      @RequestParam(required = false) String adminTitle,
      @RequestParam(required = false) String adminMaxPlayers,
      @RequestParam(required = false) String adminRegistrationStart,
      @RequestParam(required = false) String adminStartDate,
      @RequestParam(required = false) String adminDescription,
      @RequestParam(required = false) String adminPrize,
      @RequestParam(required = false) MultipartFile image,
      Model model) {
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
      model.addAttribute("errorMessages", errors);
      setCreateFormModel(
          model, title, maxPlayersRaw, registrationStartRaw, startDateRaw, description, prize);
      return "admin-tournament-create";
    }
    Image tournamentImage = null;
    if (image != null && !image.isEmpty()) {
      try {
        tournamentImage = new Image();
        tournamentImage.setFilename(image.getOriginalFilename());
        tournamentImage.setContentType(image.getContentType());
        tournamentImage.setData(image.getBytes());
      } catch (IOException e) {
        model.addAttribute("errorMessage", ErrorConstants.IMAGE_ERROR_UPLOAD);
        return "error";
      }
    }

    tournamentService.createTournament(
        title, tournamentImage, description, maxPlayers, registrationStart, startDate, prize);
    return "redirect:/admin/tournaments/create?success";
  }

  @GetMapping("/tournaments/edit/{id}")
  public String editTournament(@PathVariable Long id, Model model) {
    Optional<Tournament> opTournament = tournamentService.getTournamentById(id);

    if (opTournament.isPresent()) {
      Tournament tournament = opTournament.get();
      model.addAttribute("tournament", tournament);
      return "admin-tournament-edit";
    }
    model.addAttribute("errorMessage", ErrorConstants.TOURNAMENT_NOT_FOUND);
    return "error";
  }

  @PostMapping("/tournaments/edit/{id}")
  public String editTournament(
      @PathVariable Long id,
      @RequestParam String name,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) int slots,
      @RequestParam(required = false) MultipartFile image,
      @RequestParam String status,
      Model model,
      Authentication authentication) {

    Optional<Tournament> opTournament = tournamentService.getTournamentById(id);

    if (opTournament.isPresent()) {
      Tournament tournament = opTournament.get();
      if (!handleImageUpload(tournament, image)) {
        model.addAttribute("errorMessage", ErrorConstants.IMAGE_ERROR_UPLOAD);
        return "error";
      }
      tournament.setName(name);
      tournament.setDescription(description != null ? description : "");
      tournament.setSlots(slots);
      tournament.setStatus(status);

      if (startDate != null && !startDate.isBlank()) {
        try {
          tournament.setStartDate(LocalDate.parse(startDate));
        } catch (Exception e) {
          model.addAttribute("errorMessage", ErrorConstants.DATE_INVALID);
          return "error";
        }
      }

      tournamentService.save(tournament);

      return "redirect:/admin/panel";
    }

    return "error";
  }

  @GetMapping("/tournaments/detail")
  public String adminTournamentDetail(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String runResult,
      Model model) {
    TournamentService.AdminTournamentDetail tournament =
        tournamentService.getAdminTournamentDetail(id);
    model.addAttribute("tournament", tournament);
    if (runResult != null) {
      switch (runResult) {
        case "executed" ->
            model.addAttribute("successMessage", "Tournament executed successfully.");
        case "not-upcoming" ->
            model.addAttribute("errorMessage", "Only upcoming tournaments can be executed now.");
        case "not-found" -> model.addAttribute("errorMessage", "Tournament not found.");
        default -> {
          // no-op
        }
      }
    }
    return "admin-tournament-detail";
  }

  @PostMapping("/tournaments/process-due")
  public String processDueTournamentsManually() {
    int processed = tournamentAutomationService.processDueUpcomingTournamentsNow();
    return "redirect:/admin/panel?processed=" + processed;
  }

  @PostMapping("/tournaments/{id}/run-now")
  public String runTournamentNow(@org.springframework.web.bind.annotation.PathVariable Long id) {
    TournamentAutomationService.RunNowResult result =
        tournamentAutomationService.runTournamentNow(id);
    return switch (result) {
      case EXECUTED -> "redirect:/admin/tournaments/detail?id=" + id + "&runResult=executed";
      case NOT_UPCOMING ->
          "redirect:/admin/tournaments/detail?id=" + id + "&runResult=not-upcoming";
      case NOT_FOUND -> "redirect:/admin/tournaments/detail?runResult=not-found";
    };
  }

  @PostMapping("/tournaments/{id}/delete")
  public String deleteTournament(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
      tournamentService.deleteTournament(id);
      redirectAttributes.addFlashAttribute("successMessage", "Tournament deleted successfully.");
    } catch (NoSuchElementException e) {
      redirectAttributes.addFlashAttribute("errorMessage", "Tournament not found.");
    }
    return "redirect:/admin/panel";
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
    } catch (NumberFormatException ex) {
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
    } catch (DateTimeParseException ex) {
      errors.add(invalidMessage);
      return null;
    }
  }

  private String safeTrim(String value) {
    return value == null ? "" : value.trim();
  }

  private void setCreateFormModel(
      Model model,
      String title,
      String maxPlayers,
      String registrationStart,
      String startDate,
      String description,
      String prize) {
    model.addAttribute("adminTitle", title);
    model.addAttribute("adminMaxPlayers", maxPlayers);
    model.addAttribute("adminRegistrationStart", registrationStart);
    model.addAttribute("adminStartDate", startDate);
    model.addAttribute("adminDescription", description);
    model.addAttribute("adminPrize", prize);
  }

  @GetMapping("/users")
  public String adminUsers(
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(name = "status", required = false) String status,
      Authentication authentication,
      Model model) {
    String normalizedQuery = normalizeQuery(query);
    UserStatusFilter statusFilter = UserStatusFilter.fromValue(status);
    User currentAdmin = userService.getCurrentUser(authentication);
    populateUsersSearchModel(model, normalizedQuery, statusFilter, currentAdmin);
    return "admin-users";
  }

  @GetMapping("/users/table")
  public String adminUsersTable(
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(name = "status", required = false) String status,
      Authentication authentication,
      Model model) {
    String normalizedQuery = normalizeQuery(query);
    UserStatusFilter statusFilter = UserStatusFilter.fromValue(status);
    User currentAdmin = userService.getCurrentUser(authentication);
    populateUsersSearchModel(model, normalizedQuery, statusFilter, currentAdmin);
    return "components/admin-user-rows";
  }

  @PostMapping("/users/{id}/block")
  public String blockUser(
      @PathVariable Long id,
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(name = "status", required = false) String status,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {
    return updateBlockedStatus(id, query, status, authentication, redirectAttributes, true);
  }

  @PostMapping("/users/{id}/unblock")
  public String unblockUser(
      @PathVariable Long id,
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(name = "status", required = false) String status,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {
    return updateBlockedStatus(id, query, status, authentication, redirectAttributes, false);
  }

  @PostMapping("/users/{id}/delete")
  public String deleteUser(
      @PathVariable Long id,
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(name = "status", required = false) String status,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {
    String normalizedQuery = normalizeQuery(query);
    UserStatusFilter statusFilter = UserStatusFilter.fromValue(status);
    try {
      User currentAdmin = userService.getCurrentUser(authentication);
      User deletedUser = userService.deleteUser(id, currentAdmin);
      activeSessionService.expireSessions(deletedUser);
      redirectAttributes.addFlashAttribute(
          "successMessage", "User " + deletedUser.getUsername() + " was deleted successfully.");
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
    }
    return buildUsersRedirect(normalizedQuery, statusFilter);
  }

  private String updateBlockedStatus(
      Long userId,
      String query,
      String status,
      Authentication authentication,
      RedirectAttributes redirectAttributes,
      boolean blocked) {
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
      redirectAttributes.addFlashAttribute(
          "successMessage",
          "User " + targetUser.getUsername() + " was " + action + " successfully.");
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
    }
    return buildUsersRedirect(normalizedQuery, statusFilter);
  }

  private AdminUserView toAdminUserView(User user, User currentAdmin) {
    boolean isSameUser = user.getId() != null && user.getId().equals(currentAdmin.getId());
    boolean adminRole = userService.isAdmin(user);
    boolean manageable = !isSameUser && !adminRole;
    boolean hasImage = user.getImage() != null;
    String profileHref =
        "/user/profile?user="
            + UriUtils.encodeQueryParam(user.getUsername(), StandardCharsets.UTF_8);
    return new AdminUserView(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        resolveProvider(user.getOauthProvider()),
        user.isBlocked(),
        manageable,
        adminRole,
        hasImage,
        resolveInitial(user.getUsername()),
        profileHref);
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

  private void populateUsersSearchModel(
      Model model, String searchQuery, UserStatusFilter statusFilter, User currentAdmin) {
    List<AdminUserView> users =
        userService.searchUsers(searchQuery, statusFilter).stream()
            .map(user -> toAdminUserView(user, currentAdmin))
            .toList();

    model.addAttribute("searchQuery", searchQuery);
    model.addAttribute("statusFilter", statusFilter.value());
    model.addAttribute("statusAll", statusFilter == UserStatusFilter.ALL);
    model.addAttribute("statusActive", statusFilter == UserStatusFilter.ACTIVE);
    model.addAttribute("statusBlocked", statusFilter == UserStatusFilter.BLOCKED);
    model.addAttribute("resultCount", users.size());
    model.addAttribute("users", users);
  }

  private String buildUsersRedirect(String query, UserStatusFilter statusFilter) {
    StringBuilder redirectBuilder = new StringBuilder("redirect:/admin/users");
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
      Image img = new Image();
      img.setFilename(imageFile.getOriginalFilename());
      img.setContentType(contentType);
      img.setData(imageFile.getBytes());

      tournament.setImage(img);
      return true;

    } catch (IOException e) {
      return false;
    }
  }

  private record AdminUserView(
      Long id,
      String username,
      String email,
      String provider,
      boolean blocked,
      boolean manageable,
      boolean adminRole,
      boolean hasImage,
      String initial,
      String profileHref) {}
}
