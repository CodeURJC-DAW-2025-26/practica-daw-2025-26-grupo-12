package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.security.ActiveSessionService;
import es.codeurjc.grupo12.scissors_please.service.TournamentService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import es.codeurjc.grupo12.scissors_please.service.UserService.UserStatusFilter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private static final int MIN_PLAYERS = 4;
  private static final int MAX_PLAYERS = 128;
  private static final int MAX_TITLE_LENGTH = 80;
  private static final int MAX_DESCRIPTION_LENGTH = 500;
  private static final int MAX_PRIZE_LENGTH = 120;
  private static final Set<String> ALLOWED_FORMATS =
      Set.of("Single Elimination", "Double Elimination", "Round Robin");

  private final TournamentService tournamentService;
  private final UserService userService;
  private final ActiveSessionService activeSessionService;

  @GetMapping("/panel")
  public String adminPanel(@PageableDefault(size = 5) Pageable pageable, Model model) {
    model.addAttribute("size", Math.max(pageable.getPageSize(), 1));
    model.addAttribute("fromItem", 0);
    model.addAttribute("toItem", 0);
    model.addAttribute("totalElements", 0);
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

  @GetMapping("/tournament/create")
  public String adminTournamentCreate(@RequestParam(required = false) String success, Model model) {
    if (success != null) {
      model.addAttribute("successMessage", "Tournament created successfully.");
    }
    setCreateFormModel(model, "", "", "", "", "Single Elimination", "", "");
    return "admin-tournament-create";
  }

  @PostMapping("/tournament/create")
  public String createTournament(
      @RequestParam(required = false) String adminTitle,
      @RequestParam(required = false) String adminMaxPlayers,
      @RequestParam(required = false) String adminRegistrationStart,
      @RequestParam(required = false) String adminStartDate,
      @RequestParam(required = false) String adminFormat,
      @RequestParam(required = false) String adminDescription,
      @RequestParam(required = false) String adminPrize,
      Model model) {
    List<String> errors = new ArrayList<>();

    String title = safeTrim(adminTitle);
    String maxPlayersRaw = safeTrim(adminMaxPlayers);
    String registrationStartRaw = safeTrim(adminRegistrationStart);
    String startDateRaw = safeTrim(adminStartDate);
    String format = safeTrim(adminFormat);
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

    if (!ALLOWED_FORMATS.contains(format)) {
      errors.add("Format is required.");
    }
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
          model,
          title,
          maxPlayersRaw,
          registrationStartRaw,
          startDateRaw,
          format,
          description,
          prize);
      return "admin-tournament-create";
    }

    tournamentService.createTournament(
        title, description, maxPlayers, registrationStart, startDate, format, prize);
    return "redirect:/admin/tournament/create?success";
  }

  @GetMapping("/tournament/edit")
  public String adminTournamentEdit() {
    return "admin-tournament-edit";
  }

  @GetMapping("/tournament/detail")
  public String adminTournamentDetail() {
    return "admin-tournament-detail";
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
      String format,
      String description,
      String prize) {
    model.addAttribute("adminTitle", title);
    model.addAttribute("adminMaxPlayers", maxPlayers);
    model.addAttribute("adminRegistrationStart", registrationStart);
    model.addAttribute("adminStartDate", startDate);
    model.addAttribute("adminDescription", description);
    model.addAttribute("adminPrize", prize);
    model.addAttribute("formatSingleSelected", "Single Elimination".equals(format));
    model.addAttribute("formatDoubleSelected", "Double Elimination".equals(format));
    model.addAttribute("formatRoundRobinSelected", "Round Robin".equals(format));
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
    return new AdminUserView(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        resolveProvider(user.getOauthProvider()),
        user.isBlocked(),
        manageable,
        adminRole);
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

  private record AdminUserView(
      Long id,
      String username,
      String email,
      String provider,
      boolean blocked,
      boolean manageable,
      boolean adminRole) {}
}
