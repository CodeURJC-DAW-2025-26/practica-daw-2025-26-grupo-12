package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository.MonthlyUserCount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class UserService {

  private static final int MAX_PAGE_SIZE = 25;
  @Autowired private UserRepository userRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private BotRepository botRepository;

  public record UserPage(
      List<User> users,
      int nextPage,
      boolean hasMore,
      long totalElements,
      int fromItem,
      int toItem) {}

  public boolean canViewPrivateBots(User requester, User target) {
    if (requester == null || target == null) return false;

    boolean isOwner = requester.getId() != null && requester.getId().equals(target.getId());
    boolean isAdmin = requester.getRoles() != null && requester.getRoles().contains("ADMIN");

    return isOwner || isAdmin;
  }

  @Transactional(readOnly = true)
  public UserPage getUserPage(Pageable pageable) {
    return getUserPage("", UserStatusFilter.ALL, pageable);
  }

  @Transactional(readOnly = true)
  public UserPage getUserPage(String query, UserStatusFilter statusFilter, Pageable pageable) {
    int safePage = Math.max(pageable.getPageNumber(), 0);
    int safeSize = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
    Pageable safePageable = PageRequest.of(safePage, safeSize);

    UserStatusFilter effectiveStatusFilter =
        statusFilter == null ? UserStatusFilter.ALL : statusFilter;
    String normalizedQuery = query == null ? "" : query.trim();

    Page<User> pageResult;
    if (normalizedQuery.isBlank()) {
      pageResult =
          switch (effectiveStatusFilter) {
            case ALL -> userRepository.findAllByDeleteDateIsNullOrderByUsernameAsc(safePageable);
            case BLOCKED ->
                userRepository.findByBlockedAndDeleteDateIsNullOrderByUsernameAsc(
                    true, safePageable);
            case ACTIVE ->
                userRepository.findByBlockedAndDeleteDateIsNullOrderByUsernameAsc(
                    false, safePageable);
          };
    } else {
      pageResult =
          switch (effectiveStatusFilter) {
            case ALL ->
                userRepository
                    .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByUsernameAsc(
                        normalizedQuery, normalizedQuery, safePageable);
            case BLOCKED ->
                userRepository
                    .findByBlockedAndUsernameContainingIgnoreCaseOrBlockedAndEmailContainingIgnoreCaseOrderByUsernameAsc(
                        true, normalizedQuery, true, normalizedQuery, safePageable);
            case ACTIVE ->
                userRepository
                    .findByBlockedAndUsernameContainingIgnoreCaseOrBlockedAndEmailContainingIgnoreCaseOrderByUsernameAsc(
                        false, normalizedQuery, false, normalizedQuery, safePageable);
          };
    }

    List<User> users = pageResult.getContent();
    long totalElements = pageResult.getTotalElements();
    int fromItem = users.isEmpty() ? 0 : (safePage * safeSize) + 1;
    int toItem = users.isEmpty() ? 0 : fromItem + users.size() - 1;

    return new UserPage(users, safePage + 1, pageResult.hasNext(), totalElements, fromItem, toItem);
  }

  public User registerUser(String username, String email, String password) {
    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Username cannot be empty");
    }
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email cannot be empty");
    }
    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      throw new IllegalArgumentException("Invalid email format");
    }
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("Password cannot be empty");
    }
    if (password.length() < 8) {
      throw new IllegalArgumentException("Password must be at least 8 characters long");
    }

    if (userRepository.findByUsername(username).isPresent()) {
      log.warn("Registration attempt with existing username: {}", username);
      throw new IllegalArgumentException("Username already exists");
    }
    if (userRepository.findByEmail(email).isPresent()) {
      log.warn("Registration attempt with existing email: {}", email);
      throw new IllegalArgumentException("Email already exists");
    }

    User user = new User();
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.setOauthProvider("local");
    user.setBlocked(false);
    user.setRoles(List.of("USER"));

    User savedUser = userRepository.save(user);
    log.info("User registered successfully: {}", username);
    return savedUser;
  }

  @Transactional(readOnly = true)
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsernameAndDeleteDateIsNull(username);
  }

  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmailAndDeleteDateIsNull(email);
  }

  @Transactional(readOnly = true)
  public Optional<User> findByUsernameIncludingDeleted(String username) {
    return userRepository.findByUsername(username);
  }

  @Transactional(readOnly = true)
  public Optional<User> findByEmailIncludingDeleted(String email) {
    return userRepository.findByEmail(email);
  }

  @Transactional(readOnly = true)
  public User getUserById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public User getUserByIdExcludingDeleted(Long id) {
    return userRepository
        .findById(id)
        .filter(user -> user.getDeleteDate() == null)
        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
  }

  public User updateUser(User user) {
    log.info("Updating user: {}", user.getUsername());
    return userRepository.save(user);
  }

  public List<MonthlyUserCount> getMonthlyUserCount() {
    return userRepository.countUsersByMonth();
  }

  @Transactional(readOnly = true)
  public List<User> getAllUsers() {
    return userRepository.findAllByDeleteDateIsNull();
  }

  @Transactional(readOnly = true)
  public List<User> searchUsers(String query) {
    return searchUsers(query, UserStatusFilter.ALL);
  }

  @Transactional(readOnly = true)
  public List<User> searchUsers(String query, UserStatusFilter statusFilter) {
    UserStatusFilter effectiveStatusFilter =
        statusFilter == null ? UserStatusFilter.ALL : statusFilter;
    if (query == null || query.isBlank()) {
      return switch (effectiveStatusFilter) {
        case ALL -> userRepository.findTop25ByDeleteDateIsNullOrderByUsernameAsc();
        case BLOCKED ->
            userRepository.findTop25ByDeleteDateIsNullAndBlockedOrderByUsernameAsc(true);
        case ACTIVE ->
            userRepository.findTop25ByDeleteDateIsNullAndBlockedOrderByUsernameAsc(false);
      };
    }

    String normalizedQuery = query.trim();
    return switch (effectiveStatusFilter) {
      case ALL ->
          userRepository
              .findTop25ByDeleteDateIsNullAndUsernameContainingIgnoreCaseOrDeleteDateIsNullAndEmailContainingIgnoreCaseOrderByUsernameAsc(
                  normalizedQuery, normalizedQuery);
      case BLOCKED ->
          userRepository
              .findTop25ByDeleteDateIsNullAndBlockedAndUsernameContainingIgnoreCaseOrDeleteDateIsNullAndBlockedAndEmailContainingIgnoreCaseOrderByUsernameAsc(
                  true, normalizedQuery, true, normalizedQuery);
      case ACTIVE ->
          userRepository
              .findTop25ByDeleteDateIsNullAndBlockedAndUsernameContainingIgnoreCaseOrDeleteDateIsNullAndBlockedAndEmailContainingIgnoreCaseOrderByUsernameAsc(
                  false, normalizedQuery, false, normalizedQuery);
    };
  }

  @Transactional(readOnly = true)
  public User getCurrentUser(Authentication authentication) {
    if (authentication == null) {
      throw new IllegalArgumentException("User not authenticated");
    }

    Optional<User> byUsername =
        userRepository.findByUsernameAndDeleteDateIsNull(authentication.getName());
    if (byUsername.isPresent()) {
      return byUsername.get();
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof OAuth2User oauth2User) {
      Object emailAttribute = oauth2User.getAttributes().get("email");
      if (emailAttribute != null) {
        return userRepository
            .findByEmailAndDeleteDateIsNull(emailAttribute.toString())
            .orElseThrow(() -> new IllegalArgumentException("User not found with email"));
      }
    }

    throw new IllegalArgumentException("User not found for current authentication");
  }

  public boolean isAdmin(User user) {
    return user.getRoles() != null && user.getRoles().contains("ADMIN");
  }

  public User blockUser(Long userId, User actingAdmin) {
    return changeBlockedStatus(userId, actingAdmin, true);
  }

  public User unblockUser(Long userId, User actingAdmin) {
    return changeBlockedStatus(userId, actingAdmin, false);
  }

  public User deleteUser(Long userId, User actingAdmin) {
    User targetUser = getUserByIdExcludingDeleted(userId);
    if (targetUser.getId().equals(actingAdmin.getId())) {
      throw new IllegalArgumentException("You cannot delete your own account");
    }
    if (isAdmin(targetUser)) {
      throw new IllegalArgumentException("Admin accounts cannot be deleted");
    }

    if (targetUser.getDeleteDate() == null) {
      markUserAsDeleted(targetUser);
    }
    return targetUser;
  }

  private User changeBlockedStatus(Long userId, User actingAdmin, boolean blocked) {
    User targetUser = getUserByIdExcludingDeleted(userId);
    if (targetUser.getId().equals(actingAdmin.getId())) {
      throw new IllegalArgumentException("You cannot block your own account");
    }
    if (isAdmin(targetUser)) {
      throw new IllegalArgumentException("Admin accounts cannot be blocked");
    }

    if (targetUser.isBlocked() == blocked) {
      return targetUser;
    }

    targetUser.setBlocked(blocked);
    User updatedUser = userRepository.save(targetUser);
    log.info("Updated blocked status for user {}: {}", updatedUser.getUsername(), blocked);
    return updatedUser;
  }

  private User markUserAsDeleted(User targetUser) {
    targetUser.setDeleteDate(LocalDateTime.now());
    targetUser.setBlocked(true);
    User deletedUser = userRepository.save(targetUser);
    log.info("Logically deleted user {}", deletedUser.getUsername());
    return deletedUser;
  }

  @Transactional(readOnly = true)
  public int getMaxElo(User user) {
    Integer maxElo = botRepository.findMaxEloByOwnerId(user.getId());
    return (maxElo != null) ? maxElo : 0;
  }

  @Transactional(readOnly = true)
  public UserStats getTotalStats(User user) {
    BotRepository.StatsProjection stats = botRepository.aggregateStatsByOwnerId(user.getId());

    if (stats == null || stats.getTotalWins() == null) {
      return new UserStats(0, 0);
    }

    int totalWins = stats.getTotalWins().intValue();
    int totalMatches =
        totalWins + stats.getTotalLosses().intValue() + stats.getTotalDraws().intValue();

    return new UserStats(totalWins, totalMatches);
  }

  public record UserStats(int totalWins, int totalMatches) {
    public int getWinRate() {
      return totalMatches == 0 ? 0 : (int) ((totalWins * 100.0) / totalMatches);
    }
  }

  public enum UserStatusFilter {
    ALL("all"),
    ACTIVE("active"),
    BLOCKED("blocked");

    private final String value;

    UserStatusFilter(String value) {
      this.value = value;
    }

    public static UserStatusFilter fromValue(String rawValue) {
      if (rawValue == null || rawValue.isBlank()) {
        return ALL;
      }
      for (UserStatusFilter statusFilter : values()) {
        if (statusFilter.value.equalsIgnoreCase(rawValue.trim())) {
          return statusFilter;
        }
      }
      return ALL;
    }

    public String value() {
      return value;
    }
  }
}
