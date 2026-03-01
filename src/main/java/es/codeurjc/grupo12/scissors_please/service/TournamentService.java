package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.repository.TournamentRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TournamentService {

  private static final int MAX_PAGE_SIZE = 20;
  private static final DateTimeFormatter TOURNAMENT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

  private static final Pattern SLOT_PATTERN =
      Pattern.compile("(\\d+)\\s+slots", Pattern.CASE_INSENSITIVE);

  @Autowired private TournamentRepository tournamentRepository;

  public Tournament createTournament(
      String title,
      Image image,
      String description,
      int maxPlayers,
      LocalDate registrationStart,
      LocalDate startDate,
      String format,
      String prize) {
    Tournament tournament = new Tournament();
    tournament.setImage(image);
    tournament.setName(title);
    tournament.setStartDate(startDate);
    tournament.setSlots(maxPlayers);
    tournament.setStatus(startDate.isAfter(LocalDate.now()) ? "Upcoming" : "In Progress");
    tournament.setDescription(
        buildDescription(description, maxPlayers, registrationStart, format, prize));
    return tournamentRepository.save(tournament);
  }

  public Tournament save(Tournament tournament) {
    return tournamentRepository.save(tournament);
  }

  public TournamentPage getTournamentPage(Pageable pageable) {
    int safePage = Math.max(pageable.getPageNumber(), 0);
    int safeSize = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
    Pageable safePageable = PageRequest.of(safePage, safeSize);

    Page<Tournament> pageResult = tournamentRepository.findAllByOrderByStartDateAsc(safePageable);
    List<TournamentListItem> tournaments =
        pageResult.getContent().stream().map(this::toListItem).toList();
    long totalElements = pageResult.getTotalElements();
    int fromItem = tournaments.isEmpty() ? 0 : (safePage * safeSize) + 1;
    int toItem = tournaments.isEmpty() ? 0 : fromItem + tournaments.size() - 1;

    return new TournamentPage(
        tournaments, safePage + 1, pageResult.hasNext(), totalElements, fromItem, toItem);
  }

  public List<UserTournamentItem> getUserHomeTournaments(Long userId, int limit) {
    int safeLimit = Math.max(limit, 0);
    if (safeLimit == 0) {
      return List.of();
    }

    return tournamentRepository
        .findDistinctByParticipantsOwnerIdOrderByStartDateAsc(userId)
        .stream()
        .limit(safeLimit)
        .map(tournament -> toUserTournamentItem(tournament, true))
        .toList();
  }

  public UserTournamentSection getUserTournamentSection(
      Long userId, String registrationFilterParam, String query) {
    RegistrationFilter registrationFilter = RegistrationFilter.fromParam(registrationFilterParam);
    String normalizedQuery = normalizeQuery(query);

    List<Tournament> allTournaments = tournamentRepository.findAllByOrderByStartDateAsc();
    Set<Long> registeredTournamentIds =
        tournamentRepository.findDistinctByParticipantsOwnerIdOrderByStartDateAsc(userId).stream()
            .map(Tournament::getId)
            .collect(Collectors.toSet());

    List<UserTournamentItem> tournaments =
        allTournaments.stream()
            .filter(
                tournament ->
                    matchesRegistrationFilter(
                        tournament.getId(), registeredTournamentIds, registrationFilter))
            .filter(tournament -> matchesQuery(tournament, normalizedQuery))
            .map(
                tournament ->
                    toUserTournamentItem(
                        tournament, registeredTournamentIds.contains(tournament.getId())))
            .toList();

    String search = query == null ? "" : query.trim();
    return new UserTournamentSection(
        tournaments,
        search,
        registrationFilter == RegistrationFilter.ALL,
        registrationFilter == RegistrationFilter.REGISTERED,
        registrationFilter == RegistrationFilter.NOT_REGISTERED);
  }

  private boolean matchesRegistrationFilter(
      Long tournamentId, Set<Long> registeredTournamentIds, RegistrationFilter registrationFilter) {
    boolean isRegistered = registeredTournamentIds.contains(tournamentId);
    if (registrationFilter == RegistrationFilter.REGISTERED) {
      return isRegistered;
    }
    if (registrationFilter == RegistrationFilter.NOT_REGISTERED) {
      return !isRegistered;
    }
    return true;
  }

  private boolean matchesQuery(Tournament tournament, String normalizedQuery) {
    if (normalizedQuery.isBlank()) {
      return true;
    }
    String tournamentName = tournament.getName() == null ? "" : tournament.getName().toLowerCase();
    return tournamentName.contains(normalizedQuery);
  }

  private String normalizeQuery(String query) {
    return query == null ? "" : query.trim().toLowerCase();
  }

  private UserTournamentItem toUserTournamentItem(Tournament tournament, boolean isRegistered) {
    TournamentListItem listItem = toListItem(tournament);

    String registrationLabel = isRegistered ? "Registered" : "Not Registered";
    String registrationBadgeClass =
        isRegistered ? "bg-secondary" : "bg-dark border border-secondary text-secondary";

    return new UserTournamentItem(
        listItem.id(),
        listItem.name(),
        formatDate(tournament.getStartDate()),
        extractFormat(tournament.getDescription()),
        listItem.status(),
        listItem.badgeClass(),
        listItem.actionLabel(),
        listItem.actionHref(),
        listItem.actionDisabled(),
        isRegistered,
        registrationLabel,
        registrationBadgeClass);
  }

  private String formatDate(LocalDate startDate) {
    if (startDate == null) {
      return "Unknown";
    }
    return startDate.format(TOURNAMENT_DATE_FORMATTER);
  }

  private String extractFormat(String description) {
    if (description == null || description.isBlank()) {
      return "Unknown";
    }

    String descriptionLower = description.toLowerCase();
    int markerIndex = descriptionLower.indexOf("format:");
    if (markerIndex < 0) {
      return "Unknown";
    }

    int formatStart = markerIndex + "format:".length();
    int formatEnd = description.indexOf(" - ", formatStart);
    String rawFormat =
        formatEnd >= 0
            ? description.substring(formatStart, formatEnd)
            : description.substring(formatStart);
    String format = rawFormat.trim();
    return format.isBlank() ? "Unknown" : format;
  }

  private TournamentListItem toListItem(Tournament tournament) {

    String rawStatus = tournament.getStatus() == null ? "" : tournament.getStatus().trim();
    String statusLower = rawStatus.toLowerCase();

    String label = rawStatus.isBlank() ? "Unknown" : rawStatus;
    String badgeClass = "bg-secondary";
    String actionLabel = "View";
    String actionHref = "/tournaments/detail";
    boolean actionDisabled = false;

    if (statusLower.contains("progress")) {
      badgeClass = "bg-warning text-dark";
      actionLabel = "In Progress";
      actionHref = "";
      actionDisabled = true;
    } else if (statusLower.contains("finish") || statusLower.contains("complete")) {
      badgeClass = "bg-success";
      actionLabel = "View Results";
      actionHref = "/tournaments/results";
    } else if (statusLower.contains("upcoming")) {
      badgeClass = "bg-info text-dark";
      actionLabel = "Details";
    }

    String summary =
        tournament.getDescription() == null || tournament.getDescription().isBlank()
            ? "No description available."
            : tournament.getDescription();

    return new TournamentListItem(
        tournament.getId(),
        tournament.getName(),
        summary,
        label,
        badgeClass,
        actionLabel,
        actionHref,
        actionDisabled);
  }

  public AdminTournamentDetail getAdminTournamentDetail(Long id) {
    Tournament tournament =
        id == null
            ? tournamentRepository.findAll().stream()
                .min(Comparator.comparing(Tournament::getStartDate))
                .orElseThrow()
            : tournamentRepository.findById(id).orElseThrow();

    String status = normalizeStatus(tournament.getStatus());
    int slots = extractSlots(tournament.getDescription());
    int participants =
        tournament.getParticipants() == null ? 0 : tournament.getParticipants().size();
    return new AdminTournamentDetail(
        tournament.getId(),
        tournament.getName(),
        tournament.getDescription(),
        status,
        tournament.getStartDate(),
        slots,
        participants,
        "Upcoming".equalsIgnoreCase(status));
  }

  private String normalizeStatus(String status) {
    if (status == null || status.isBlank()) {
      return "Unknown";
    }
    return status.trim();
  }

  private int extractSlots(String description) {
    if (description == null || description.isBlank()) {
      return 0;
    }
    Matcher matcher = SLOT_PATTERN.matcher(description);
    if (!matcher.find()) {
      return 0;
    }
    try {
      return Integer.parseInt(matcher.group(1));
    } catch (NumberFormatException ex) {
      return 0;
    }
  }

  private String buildDescription(
      String description,
      int maxPlayers,
      LocalDate registrationStart,
      String format,
      String prize) {
    List<String> sections = new ArrayList<>();
    if (description != null && !description.isBlank()) {
      sections.add(description);
    }
    sections.add("Format: " + format);
    sections.add("Max players: " + maxPlayers);
    sections.add("Registration opens: " + registrationStart);
    if (prize != null && !prize.isBlank()) {
      sections.add("Prize: " + prize);
    }
    return String.join(" - ", sections);
  }

  public Optional<Tournament> getTournamentById(Long id) {
    return tournamentRepository.findById(id);
  }

  public record TournamentListItem(
      Long id,
      String name,
      String summary,
      String status,
      String badgeClass,
      String actionLabel,
      String actionHref,
      boolean actionDisabled) {}

  public record UserTournamentItem(
      Long id,
      String name,
      String date,
      String format,
      String status,
      String statusBadgeClass,
      String actionLabel,
      String actionHref,
      boolean actionDisabled,
      boolean registered,
      String registrationLabel,
      String registrationBadgeClass) {}

  public record UserTournamentSection(
      List<UserTournamentItem> tournaments,
      String search,
      boolean selectedAll,
      boolean selectedRegistered,
      boolean selectedNotRegistered) {}

  public record AdminTournamentDetail(
      Long id,
      String name,
      String description,
      String status,
      LocalDate startDate,
      int slots,
      int participants,
      boolean canRunNow) {}

  public record TournamentPage(
      List<TournamentListItem> tournaments,
      int nextPage,
      boolean hasMore,
      long totalElements,
      int fromItem,
      int toItem) {}

  private enum RegistrationFilter {
    ALL,
    REGISTERED,
    NOT_REGISTERED;

    static RegistrationFilter fromParam(String value) {
      if (value == null) {
        return ALL;
      }

      String normalizedValue = value.trim().toLowerCase();
      if ("registered".equals(normalizedValue)) {
        return REGISTERED;
      }
      if ("not-registered".equals(normalizedValue) || "not_registered".equals(normalizedValue)) {
        return NOT_REGISTERED;
      }
      return ALL;
    }
  }
}
