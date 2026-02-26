package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.security.ActiveSessionService;
import es.codeurjc.grupo12.scissors_please.service.TournamentService;
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
  public String adminTournamentCreate() {
    return "admin-tournament-create";
  }

  @GetMapping("/tournament/edit")
  public String adminTournamentEdit() {
    return "admin-tournament-edit";
  }

  @GetMapping("/tournament/detail")
  public String adminTournamentDetail() {
    return "admin-tournament-detail";
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
