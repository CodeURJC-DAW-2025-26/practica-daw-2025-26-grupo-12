package es.codeurjc.grupo12.scissors_please.service.tournament;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.TournamentStatus;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.repository.TournamentRepository;
import es.codeurjc.grupo12.scissors_please.views.AdminTournamentDetail;
import es.codeurjc.grupo12.scissors_please.views.BotOptionView;
import es.codeurjc.grupo12.scissors_please.views.JoinTournamentResult;
import es.codeurjc.grupo12.scissors_please.views.JoinTournamentStatus;
import es.codeurjc.grupo12.scissors_please.views.TournamentJoinPage;
import es.codeurjc.grupo12.scissors_please.views.TournamentListItem;
import es.codeurjc.grupo12.scissors_please.views.TournamentRegistrationState;
import es.codeurjc.grupo12.scissors_please.views.UserTournamentItem;
import es.codeurjc.grupo12.scissors_please.views.UserTournamentSection;

@Service
public class TournamentService {

  private static final int MAX_PAGE_SIZE = 20;
  private static final TournamentStatus STATUS_UPCOMING = TournamentStatus.UPCOMING;
  private static final Sort TOURNAMENT_PAGE_SORT = Sort.by(Sort.Direction.ASC, "startDate");
  private static final DateTimeFormatter TOURNAMENT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

  private static final Pattern REGISTRATION_OPEN_PATTERN =
      Pattern.compile("Registration opens:\\s*(\\d{4}-\\d{2}-\\d{2})", Pattern.CASE_INSENSITIVE);

  @Autowired private TournamentRepository tournamentRepository;
  @Autowired private BotRepository botRepository;

  public void deleteTournament(Long id) {
    Tournament tournament =
        tournamentRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Tournament not found with id: " + id));
    tournamentRepository.delete(tournament);
  }

  public Tournament createTournament(
      String title,
      Image image,
      String description,
      int maxPlayers,
      LocalDate registrationStart,
      LocalDate startDate,
      String prize) {
    Tournament tournament = new Tournament();
    tournament.setImage(image);
    tournament.setName(title);
    tournament.setStartDate(startDate);
    tournament.setSlots(maxPlayers);
    tournament.setStatus(resolveTournamentStatus(startDate));
    tournament.setDescription(buildDescription(description, maxPlayers, registrationStart, prize));
    return tournamentRepository.save(tournament);
  }

  public Tournament save(Tournament tournament) {
    return tournamentRepository.save(tournament);
  }

  public Optional<Tournament> updateTournament(
      Long id,
      String name,
      Image image,
      String description,
      TournamentStatus status,
      int slots,
      LocalDate registrationStart,
      LocalDate startDate,
      String prize) {
    return tournamentRepository
        .findById(id)
        .map(
            tournament -> {
              tournament.setName(name);
              tournament.setDescription(
                  buildDescription(description, slots, registrationStart, prize));
              tournament.setStatus(resolveTournamentStatus(startDate));
              tournament.setStartDate(startDate);
              tournament.setSlots(slots);
              if (image != null) {
                tournament.setImage(image);
              }
              return tournamentRepository.save(tournament);
            });
  }

  @Transactional(readOnly = true)
  public TournamentRegistrationState getRegistrationState(Tournament tournament, User currentUser) {
    List<Bot> participantList = tournament.getParticipants();
    Set<Long> participantIds = getParticipantIds(participantList);
    int registeredParticipants = participantIds.size();
    int slots = Math.max(tournament.getSlots(), 0);

    LocalDate today = LocalDate.now();
    LocalDate registrationOpenDate =
        extractRegistrationOpenDate(tournament.getDescription()).orElse(null);
    boolean registrationStarted =
        registrationOpenDate == null || !today.isBefore(registrationOpenDate);
    boolean startsInFuture =
        tournament.getStartDate() != null && today.isBefore(tournament.getStartDate());
    boolean upcoming = STATUS_UPCOMING == tournament.getStatus();
    boolean hasAvailableSlots = slots > 0 && registeredParticipants < slots;

    List<Bot> ownedBots = getOwnedBots(currentUser);
    List<Bot> selectableBots =
        ownedBots.stream()
            .filter(bot -> bot.getId() != null && !participantIds.contains(bot.getId()))
            .sorted(Comparator.comparingInt(Bot::getElo).reversed())
            .toList();

    boolean isAdmin = currentUser != null && isAdmin(currentUser);
    boolean alreadyRegistered =
        currentUser != null
            && participantList != null
            && participantList.stream()
                .filter(bot -> bot != null && !bot.isDeleted())
                .anyMatch(
                    bot ->
                        currentUser.getId() != null
                            && currentUser.getId().equals(bot.getOwnerId()));

    boolean registrationOpen =
        upcoming && startsInFuture && registrationStarted && hasAvailableSlots;
    boolean hasOwnedBots = !ownedBots.isEmpty();
    boolean hasSelectableBots = !selectableBots.isEmpty();
    boolean showJoinButton =
        currentUser != null
            && !isAdmin
            && registrationOpen
            && !alreadyRegistered
            && hasSelectableBots;

    String joinMessage = null;
    String joinMessageClass = null;

    if (!isAdmin) {
      if (alreadyRegistered) {
        joinMessage = "You are already registered in this tournament.";
        joinMessageClass = "alert-info";
      } else if (!upcoming || !startsInFuture) {
        joinMessage = "Registration is closed for this tournament.";
        joinMessageClass = "alert-warning";
      } else if (!registrationStarted) {
        joinMessage = "Registration opens on " + formatDate(registrationOpenDate) + ".";
        joinMessageClass = "alert-info";
      } else if (!hasAvailableSlots) {
        joinMessage = "Tournament is full.";
        joinMessageClass = "alert-warning";
      } else if (currentUser == null) {
        joinMessage = "Log in to join this tournament.";
        joinMessageClass = "alert-info";
      } else if (!hasOwnedBots) {
        joinMessage = "Create a bot before joining this tournament.";
        joinMessageClass = "alert-warning";
      } else if (!hasSelectableBots) {
        joinMessage = "You do not have any available bots for this tournament.";
        joinMessageClass = "alert-warning";
      } else if (registrationOpen) {
        joinMessage = "Registration is open. Choose a bot to join.";
        joinMessageClass = "alert-success";
      }
    }

    return new TournamentRegistrationState(
        registrationOpen,
        hasAvailableSlots,
        registrationStarted,
        startsInFuture,
        upcoming,
        alreadyRegistered,
        hasOwnedBots,
        hasSelectableBots,
        showJoinButton,
        registeredParticipants,
        joinMessage,
        joinMessageClass);
  }

  @Transactional(readOnly = true)
  public TournamentJoinPage getTournamentJoinPage(Long tournamentId, User currentUser) {
    Tournament tournament = tournamentRepository.findById(tournamentId).orElseThrow();
    TournamentRegistrationState registrationState = getRegistrationState(tournament, currentUser);

    Set<Long> participantIds = getParticipantIds(tournament.getParticipants());
    List<BotOptionView> botOptions =
        getOwnedBots(currentUser).stream()
            .filter(bot -> bot.getId() != null && !participantIds.contains(bot.getId()))
            .sorted(Comparator.comparingInt(Bot::getElo).reversed())
            .map(bot -> new BotOptionView(bot.getId(), bot.getName(), bot.getElo(), false))
            .toList();

    List<BotOptionView> resolvedBotOptions = selectFirstBot(botOptions);
    return new TournamentJoinPage(
        tournament,
        resolvedBotOptions,
        registrationState.showJoinButton(),
        registrationState.registeredParticipants(),
        formatDate(tournament.getStartDate()),
        formatDate(extractRegistrationOpenDate(tournament.getDescription()).orElse(null)),
        extractFormat(tournament.getDescription()),
        registrationState.joinMessage(),
        registrationState.joinMessageClass(),
        !registrationState.hasOwnedBots());
  }

  @Transactional
  public JoinTournamentResult joinTournament(Long tournamentId, Long botId, User currentUser) {
    Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
    if (tournament == null) {
      return new JoinTournamentResult(
          JoinTournamentStatus.TOURNAMENT_NOT_FOUND, "Tournament not found.");
    }

    if (currentUser == null || currentUser.getId() == null) {
      return new JoinTournamentResult(
          JoinTournamentStatus.INVALID_USER, "You must be logged in to join a tournament.");
    }

    if (isAdmin(currentUser)) {
      return new JoinTournamentResult(
          JoinTournamentStatus.ADMIN_NOT_ALLOWED, "Administrators cannot join tournaments.");
    }

    if (botId == null) {
      return new JoinTournamentResult(
          JoinTournamentStatus.INVALID_BOT, "Select a bot to complete your registration.");
    }

    Bot selectedBot = botRepository.findById(botId).orElse(null);
    if (selectedBot == null || !currentUser.getId().equals(selectedBot.getOwnerId())) {
      return new JoinTournamentResult(
          JoinTournamentStatus.INVALID_BOT, "The selected bot does not belong to your account.");
    }

    TournamentRegistrationState registrationState = getRegistrationState(tournament, currentUser);
    if (registrationState.alreadyRegistered()) {
      return new JoinTournamentResult(
          JoinTournamentStatus.ALREADY_REGISTERED,
          "You are already registered in this tournament.");
    }

    if (!registrationState.upcoming() || !registrationState.startsInFuture()) {
      return new JoinTournamentResult(
          JoinTournamentStatus.REGISTRATION_CLOSED, "Registration is closed for this tournament.");
    }

    if (!registrationState.registrationStarted()) {
      LocalDate registrationOpenDate =
          extractRegistrationOpenDate(tournament.getDescription()).orElse(null);
      return new JoinTournamentResult(
          JoinTournamentStatus.REGISTRATION_NOT_OPEN,
          "Registration opens on " + formatDate(registrationOpenDate) + ".");
    }

    if (!registrationState.hasAvailableSlots()) {
      return new JoinTournamentResult(JoinTournamentStatus.TOURNAMENT_FULL, "Tournament is full.");
    }

    Set<Long> participantIds = getParticipantIds(tournament.getParticipants());
    if (selectedBot.getId() != null && participantIds.contains(selectedBot.getId())) {
      return new JoinTournamentResult(
          JoinTournamentStatus.BOT_ALREADY_REGISTERED,
          "The selected bot is already registered in this tournament.");
    }

    List<Bot> participants =
        tournament.getParticipants() == null
            ? new ArrayList<>()
            : new ArrayList<>(tournament.getParticipants());
    participants.add(selectedBot);
    tournament.setParticipants(participants);
    tournamentRepository.save(tournament);

    return new JoinTournamentResult(JoinTournamentStatus.JOINED, "Bot registered successfully.");
  }

  public Page<TournamentListItem> getTournamentPage(String query, Pageable pageable) {
    int safePage = Math.max(pageable.getPageNumber(), 0);
    int safeSize = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
    Pageable safePageable = PageRequest.of(safePage, safeSize, TOURNAMENT_PAGE_SORT);

    String normalizedQuery = query == null ? "" : query.trim();
    Page<Tournament> pageResult =
        normalizedQuery.isBlank()
            ? tournamentRepository.findAll(safePageable)
            : tournamentRepository.findByNameContainingIgnoreCase(normalizedQuery, safePageable);
    return pageResult.map(this::toListItem);
  }

  public Page<TournamentListItem> getUserTournamentPage(
      Long userId, String query, Pageable pageable) {
    int safePage = Math.max(pageable.getPageNumber(), 0);
    int safeSize = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
    Pageable safePageable = PageRequest.of(safePage, safeSize, TOURNAMENT_PAGE_SORT);

    String normalizedQuery = query == null ? "" : query.trim();
    Page<Tournament> pageResult =
        normalizedQuery.isBlank()
            ? tournamentRepository.findDistinctByParticipantsOwnerId(userId, safePageable)
            : tournamentRepository.findDistinctByParticipantsOwnerIdAndNameContainingIgnoreCase(
                userId, normalizedQuery, safePageable);
    return pageResult.map(this::toListItem);
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
        .map(this::toUserTournamentItem)
        .toList();
  }

  public UserTournamentSection getUserTournamentSection(Long userId, String query) {
    String normalizedQuery = normalizeQuery(query);

    List<Tournament> registeredTournaments =
        tournamentRepository.findDistinctByParticipantsOwnerIdOrderByStartDateAsc(userId);

    List<UserTournamentItem> tournaments =
        registeredTournaments.stream()
            .filter(tournament -> matchesQuery(tournament, normalizedQuery))
            .map(this::toUserTournamentItem)
            .toList();

    String search = query == null ? "" : query.trim();
    return new UserTournamentSection(tournaments, search);
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

  private UserTournamentItem toUserTournamentItem(Tournament tournament) {
    TournamentListItem listItem = toListItem(tournament);

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
        listItem.hasImage());
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

    TournamentStatus status = tournament.getStatus();
    String rawStatus = status == null ? "" : status.getDisplayName();
    String statusLower = rawStatus.toLowerCase();

    String label = rawStatus.isBlank() ? "Unknown" : rawStatus;
    String badgeClass = "bg-secondary";
    String actionLabel = "View";
    String actionHref = "/tournaments/detail/" + tournament.getId();
    boolean actionDisabled = false;

    if (statusLower.contains("progress")) {
      badgeClass = "bg-warning text-dark";
      actionLabel = "In Progress";
      actionHref = "";
      actionDisabled = true;
    } else if (statusLower.contains("finish") || statusLower.contains("complete")) {
      badgeClass = "bg-success";
      actionLabel = "View Results";
      actionHref = "/tournaments/detail/" + tournament.getId();
    } else if (statusLower.contains("upcoming")) {
      badgeClass = "bg-info text-dark";
      actionLabel = "Details";
    }

    String summary =
        tournament.getDescription() == null || tournament.getDescription().isBlank()
            ? "No description available."
            : tournament.getDescription();
    int occupiedSlots = getParticipantIds(tournament.getParticipants()).size();
    int totalSlots = Math.max(tournament.getSlots(), 0);

    return new TournamentListItem(
        tournament.getId(),
        tournament.getName(),
        summary,
        label,
        badgeClass,
        actionLabel,
        actionHref,
        actionDisabled,
        tournament.getImage() != null,
        occupiedSlots,
        totalSlots);
  }

  public AdminTournamentDetail getAdminTournamentDetail(Long id) {
    Tournament tournament =
        id == null
            ? tournamentRepository.findAll().stream()
                .min(Comparator.comparing(Tournament::getStartDate))
                .orElseThrow()
            : tournamentRepository.findById(id).orElseThrow();

    TournamentStatus status = tournament.getStatus();
    int slots = tournament.getSlots();
    int participants = getParticipantIds(tournament.getParticipants()).size();
    return new AdminTournamentDetail(
        tournament.getId(),
        tournament.getName(),
        tournament.getDescription(),
        status,
        tournament.getStartDate(),
        slots,
        participants,
        TournamentStatus.UPCOMING == status,
        tournament.getImage() != null);
  }

  private String buildDescription(
      String description, int maxPlayers, LocalDate registrationStart, String prize) {
    List<String> sections = new ArrayList<>();
    if (description != null && !description.isBlank()) {
      sections.add(description);
    }
    sections.add("Max players: " + maxPlayers);
    sections.add("Registration opens: " + registrationStart);
    if (prize != null && !prize.isBlank()) {
      sections.add("Prize: " + prize);
    }
    return String.join(" - ", sections);
  }

  private TournamentStatus resolveTournamentStatus(LocalDate startDate) {
    if (startDate == null) {
      return TournamentStatus.UPCOMING;
    }
    return startDate.isAfter(LocalDate.now())
        ? TournamentStatus.UPCOMING
        : TournamentStatus.IN_PROGRESS;
  }

  public Optional<Tournament> getTournamentById(Long id) {
    return tournamentRepository.findById(id);
  }

  private List<Bot> getOwnedBots(User currentUser) {
    if (currentUser == null || currentUser.getId() == null || isAdmin(currentUser)) {
      return List.of();
    }
    return botRepository.findByOwnerIdAndDeletedFalse(currentUser.getId()).stream()
        .sorted(Comparator.comparingInt(Bot::getElo).reversed())
        .toList();
  }

  private boolean isAdmin(User user) {
    return user.getRoles() != null && user.getRoles().contains("ADMIN");
  }

  private Optional<LocalDate> extractRegistrationOpenDate(String description) {
    if (description == null || description.isBlank()) {
      return Optional.empty();
    }

    var matcher = REGISTRATION_OPEN_PATTERN.matcher(description);
    if (!matcher.find()) {
      return Optional.empty();
    }

    try {
      return Optional.of(LocalDate.parse(matcher.group(1)));
    } catch (Exception ex) {
      return Optional.empty();
    }
  }

  private Set<Long> getParticipantIds(List<Bot> participants) {
    if (participants == null || participants.isEmpty()) {
      return Set.of();
    }

    Set<Long> participantIds = new LinkedHashSet<>();
    for (Bot participant : participants) {
      if (participant != null && participant.getId() != null && !participant.isDeleted()) {
        participantIds.add(participant.getId());
      }
    }
    return participantIds;
  }

  private List<BotOptionView> selectFirstBot(List<BotOptionView> botOptions) {
    if (botOptions.isEmpty()) {
      return List.of();
    }

    List<BotOptionView> resolvedBotOptions = new ArrayList<>(botOptions.size());
    boolean selectedAssigned = false;
    for (BotOptionView botOption : botOptions) {
      boolean selected = !selectedAssigned;
      resolvedBotOptions.add(
          new BotOptionView(botOption.id(), botOption.name(), botOption.elo(), selected));
      selectedAssigned = true;
    }
    return resolvedBotOptions;
  }
}
