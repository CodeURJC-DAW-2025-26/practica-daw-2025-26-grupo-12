package es.codeurjc.grupo12.scissors_please.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.repository.TournamentRepository;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TournamentServiceTests {

  @Autowired private TournamentService tournamentService;
  @Autowired private TournamentRepository tournamentRepository;
  @Autowired private BotRepository botRepository;
  @Autowired private UserRepository userRepository;

  @Test
  void getRegistrationStateShowsJoinWhenTournamentHasSlotsAndDatesAreValid() {
    User user = saveUser("eligibility-open");
    saveBot(user, "Eligibility Open Bot", 1700);
    Tournament tournament =
        saveTournament(
            "Open Eligibility Cup",
            4,
            LocalDate.now().plusDays(3),
            LocalDate.now().minusDays(1),
            List.of());

    TournamentService.TournamentRegistrationState state =
        tournamentService.getRegistrationState(tournament, user);

    assertTrue(state.registrationOpen());
    assertTrue(state.showJoinButton());
    assertEquals(0, state.registeredParticipants());
  }

  @Test
  void getRegistrationStateHidesJoinWhenTournamentIsFull() {
    User currentUser = saveUser("eligibility-full");
    saveBot(currentUser, "Eligibility Full Bot", 1650);

    User registeredUser = saveUser("already-in");
    Bot registeredBot = saveBot(registeredUser, "Registered Bot", 1800);

    Tournament tournament =
        saveTournament(
            "Full Cup",
            1,
            LocalDate.now().plusDays(2),
            LocalDate.now().minusDays(2),
            List.of(registeredBot));

    TournamentService.TournamentRegistrationState state =
        tournamentService.getRegistrationState(tournament, currentUser);

    assertFalse(state.registrationOpen());
    assertFalse(state.showJoinButton());
    assertFalse(state.hasAvailableSlots());
    assertEquals(1, state.registeredParticipants());
  }

  @Test
  void getRegistrationStateHidesJoinWhenRegistrationHasNotStarted() {
    User user = saveUser("eligibility-future");
    saveBot(user, "Eligibility Future Bot", 1750);

    Tournament tournament =
        saveTournament(
            "Future Registration Cup",
            8,
            LocalDate.now().plusDays(5),
            LocalDate.now().plusDays(1),
            List.of());

    TournamentService.TournamentRegistrationState state =
        tournamentService.getRegistrationState(tournament, user);

    assertFalse(state.registrationOpen());
    assertFalse(state.showJoinButton());
    assertFalse(state.registrationStarted());
  }

  @Test
  void joinTournamentAddsTheSelectedBotToParticipants() {
    User user = saveUser("join-success");
    Bot bot = saveBot(user, "Join Success Bot", 1900);
    Tournament tournament =
        saveTournament(
            "Join Success Cup",
            4,
            LocalDate.now().plusDays(4),
            LocalDate.now().minusDays(1),
            List.of());

    TournamentService.JoinTournamentResult result =
        tournamentService.joinTournament(tournament.getId(), bot.getId(), user);
    Tournament updatedTournament = tournamentRepository.findById(tournament.getId()).orElseThrow();

    assertEquals(TournamentService.JoinTournamentStatus.JOINED, result.status());
    assertEquals(1, updatedTournament.getParticipants().size());
    assertEquals(bot.getId(), updatedTournament.getParticipants().get(0).getId());
  }

  private User saveUser(String suffix) {
    User user = new User();
    user.setUsername("test_" + suffix + "_" + System.nanoTime());
    user.setEmail("test_" + suffix + "_" + System.nanoTime() + "@mail.com");
    user.setPassword("secret");
    user.setBlocked(false);
    user.setRoles(List.of("USER"));
    return userRepository.save(user);
  }

  private Bot saveBot(User owner, String name, int elo) {
    Bot bot = new Bot();
    bot.setName(name);
    bot.setDescription("Test bot");
    bot.setCode("return 'rock';");
    bot.setPublic(true);
    bot.setElo(elo);
    bot.setOwnerId(owner.getId());
    return botRepository.save(bot);
  }

  private Tournament saveTournament(
      String name,
      int slots,
      LocalDate startDate,
      LocalDate registrationOpenDate,
      List<Bot> participants) {
    Tournament tournament = new Tournament();
    tournament.setName(name);
    tournament.setStatus("Upcoming");
    tournament.setSlots(slots);
    tournament.setStartDate(startDate);
    tournament.setDescription(
        "Test tournament");
    tournament.setParticipants(new ArrayList<>(participants));
    tournament.setMatches(new ArrayList<>());
    return tournamentRepository.save(tournament);
  }
}
