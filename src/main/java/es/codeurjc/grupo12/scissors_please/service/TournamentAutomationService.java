package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Match;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.TournamentStatus;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.repository.TournamentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TournamentAutomationService {

  private static final TournamentStatus STATUS_UPCOMING = TournamentStatus.UPCOMING;
  private static final TournamentStatus STATUS_IN_PROGRESS = TournamentStatus.IN_PROGRESS;
  private static final TournamentStatus STATUS_COMPLETED = TournamentStatus.COMPLETED;

  @Autowired private TournamentRepository tournamentRepository;
  @Autowired private BotRepository botRepository;
  @Autowired private BotService botService;

  public enum RunNowResult {
    EXECUTED,
    NOT_UPCOMING,
    NOT_FOUND
  }

  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  public void processUpcomingTournamentsDaily() {
    processDueUpcomingTournaments(LocalDate.now());
  }

  @Transactional
  public int processDueUpcomingTournamentsNow() {
    return processDueUpcomingTournaments(LocalDate.now());
  }

  @Transactional
  public RunNowResult runTournamentNow(Long tournamentId) {
    Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
    if (tournament == null) {
      return RunNowResult.NOT_FOUND;
    }

    TournamentStatus status = tournament.getStatus();
    if (STATUS_UPCOMING != status) {
      return RunNowResult.NOT_UPCOMING;
    }

    runTournament(tournament);
    return RunNowResult.EXECUTED;
  }

  int processDueUpcomingTournaments(LocalDate today) {
    List<Tournament> dueTournaments =
        tournamentRepository.findByStatusAndStartDateLessThanEqualOrderByStartDateAsc(
            STATUS_UPCOMING, today);

    if (dueTournaments.isEmpty()) {
      return 0;
    }

    for (Tournament tournament : dueTournaments) {
      runTournament(tournament);
    }
    return dueTournaments.size();
  }

  private void runTournament(Tournament tournament) {
    List<Bot> participants = resolveParticipants(tournament);

    tournament.setStatus(STATUS_IN_PROGRESS);
    tournamentRepository.saveAndFlush(tournament);

    if (participants.size() < 2) {
      tournament.setMatches(new ArrayList<>());
      tournament.setStatus(STATUS_COMPLETED);
      tournamentRepository.save(tournament);
      log.info("Tournament {} completed with less than 2 participants", tournament.getId());
      return;
    }

    TournamentSimulationResult simulationResult = simulateTournament(participants);
    tournament.setMatches(simulationResult.matches());
    tournament.setStatus(STATUS_COMPLETED);
    tournament.setDescription(
        appendWinnerToDescription(tournament.getDescription(), simulationResult.winnerName()));
    tournamentRepository.save(tournament);

    log.info(
        "Tournament {} completed. Winner: {}. Matches: {}",
        tournament.getId(),
        simulationResult.winnerName(),
        simulationResult.matches().size());
  }

  private List<Bot> resolveParticipants(Tournament tournament) {
    List<Bot> participants =
        deduplicateById(tournament.getParticipants()).stream().filter(b -> !b.isDeleted()).toList();
    if (participants.size() >= 2) {
      return participants;
    }

    List<Bot> publicBots = deduplicateById(botRepository.findByIsPublicTrueAndDeletedFalse());
    if (publicBots.size() >= 2) {
      tournament.setParticipants(publicBots);
      return publicBots;
    }

    List<Bot> allBots = deduplicateById(botRepository.findByDeletedFalse());
    tournament.setParticipants(allBots);
    return allBots;
  }

  private List<Bot> deduplicateById(List<Bot> bots) {
    if (bots == null || bots.isEmpty()) {
      return new ArrayList<>();
    }

    Map<Long, Bot> uniqueBots = new LinkedHashMap<>();
    for (Bot bot : bots) {
      if (bot == null || bot.getId() == null) {
        continue;
      }
      uniqueBots.putIfAbsent(bot.getId(), bot);
    }
    return new ArrayList<>(uniqueBots.values());
  }

  private TournamentSimulationResult simulateTournament(List<Bot> participants) {
    List<Bot> currentRound = new ArrayList<>(participants);
    Collections.shuffle(currentRound);

    List<Match> matches = new ArrayList<>();
    while (currentRound.size() > 1) {
      List<Bot> nextRound = new ArrayList<>();

      for (int i = 0; i < currentRound.size(); i += 2) {
        Bot bot1 = currentRound.get(i);
        if (i + 1 >= currentRound.size()) {
          nextRound.add(bot1);
          continue;
        }

        Bot bot2 = currentRound.get(i + 1);
        boolean bot1Wins = ThreadLocalRandom.current().nextBoolean();
        Bot winner = bot1Wins ? bot1 : bot2;
        nextRound.add(winner);

        Match match = new Match();
        match.setBot1(bot1);
        match.setBot2(bot2);
        match.setBot1Score(bot1Wins ? 1 : 0);
        match.setBot2Score(bot1Wins ? 0 : 1);
        match.setResult(bot1Wins ? "Win" : "Loss");
        match.setRounds(new ArrayList<>());
        match.setTimestamp(LocalDateTime.now());
        matches.add(match);
        botService.recordMatchResult(bot1, bot2, match.getBot1Score(), match.getBot2Score());
      }

      currentRound = nextRound;
    }

    Bot winner = currentRound.get(0);
    return new TournamentSimulationResult(matches, winner.getName());
  }

  private String appendWinnerToDescription(String currentDescription, String winnerName) {
    String winnerSection = "Winner: " + winnerName;
    if (currentDescription == null || currentDescription.isBlank()) {
      return winnerSection;
    }
    int winnerIndex = currentDescription.indexOf("Winner:");
    if (winnerIndex >= 0) {
      return currentDescription.substring(0, winnerIndex).trim() + " - " + winnerSection;
    }
    return currentDescription + " - " + winnerSection;
  }

  private record TournamentSimulationResult(List<Match> matches, String winnerName) {}
}
