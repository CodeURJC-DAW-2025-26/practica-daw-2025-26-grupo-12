package es.codeurjc.grupo12.scissors_please.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Match;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.repository.MatchRepository;
import es.codeurjc.grupo12.scissors_please.repository.TournamentRepository;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserServiceTests {

  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private BotRepository botRepository;
  @Autowired private MatchRepository matchRepository;
  @Autowired private TournamentRepository tournamentRepository;

  @Test
  void deleteUserRemovesOwnedBotsMatchesAndTournamentReferences() {
    String suffix = Long.toString(System.nanoTime());
    User actingAdmin = saveUser("admin_delete_" + suffix, "admin_delete_" + suffix, true);
    User targetUser = saveUser("delete_me_" + suffix, "delete_me_" + suffix, false);
    User opponentOwner = saveUser("opponent_" + suffix, "opponent_" + suffix, false);

    Bot targetBot = saveBot(targetUser, "Target Bot " + suffix);
    Bot opponentBot = saveBot(opponentOwner, "Opponent Bot " + suffix);

    Match match = new Match();
    match.setBot1(targetBot);
    match.setBot2(opponentBot);
    match.setBot1Score(2);
    match.setBot2Score(1);
    match.setResult("Win");
    match.setTimestamp(LocalDateTime.now());
    match.setRounds(new ArrayList<>());
    Match savedMatch = matchRepository.saveAndFlush(match);

    Tournament tournament = new Tournament();
    tournament.setName("Tournament " + suffix);
    tournament.setDescription("Integration test tournament");
    tournament.setStatus("Upcoming");
    tournament.setStartDate(LocalDate.now().plusDays(7));
    tournament.setSlots(2);
    tournament.setParticipants(new ArrayList<>(List.of(targetBot, opponentBot)));
    tournament.setMatches(new ArrayList<>(List.of(savedMatch)));
    Tournament savedTournament = tournamentRepository.saveAndFlush(tournament);

    User deletedUser = userService.deleteUser(targetUser.getId(), actingAdmin);

    assertEquals(targetUser.getId(), deletedUser.getId());
    assertTrue(userRepository.findById(targetUser.getId()).isEmpty());
    assertTrue(botRepository.findById(targetBot.getId()).isEmpty());
    assertTrue(matchRepository.findById(savedMatch.getId()).isEmpty());
    assertTrue(botRepository.findById(opponentBot.getId()).isPresent());

    Tournament reloadedTournament =
        tournamentRepository.findById(savedTournament.getId()).orElseThrow();
    assertEquals(1, reloadedTournament.getParticipants().size());
    assertEquals(opponentBot.getId(), reloadedTournament.getParticipants().get(0).getId());
    assertTrue(reloadedTournament.getMatches().isEmpty());
  }

  @Test
  void deleteUserRejectsDeletingOwnAdminAccount() {
    String suffix = Long.toString(System.nanoTime());
    User actingAdmin = saveUser("self_admin_" + suffix, "self_admin_" + suffix, true);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.deleteUser(actingAdmin.getId(), actingAdmin));

    assertEquals("You cannot delete your own account", exception.getMessage());
    assertTrue(userRepository.findById(actingAdmin.getId()).isPresent());
  }

  @Test
  void deleteUserRejectsDeletingAnotherAdminAccount() {
    String suffix = Long.toString(System.nanoTime());
    User actingAdmin = saveUser("admin_actor_" + suffix, "admin_actor_" + suffix, true);
    User targetAdmin = saveUser("admin_target_" + suffix, "admin_target_" + suffix, true);

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> userService.deleteUser(targetAdmin.getId(), actingAdmin));

    assertEquals("Admin accounts cannot be deleted", exception.getMessage());
    assertTrue(userRepository.findById(targetAdmin.getId()).isPresent());
  }

  private User saveUser(String username, String emailPrefix, boolean admin) {
    User user = new User();
    user.setUsername(username);
    user.setEmail(emailPrefix + "@example.com");
    user.setBlocked(false);
    user.setRoles(admin ? List.of("ADMIN", "USER") : List.of("USER"));
    return userRepository.saveAndFlush(user);
  }

  private Bot saveBot(User owner, String name) {
    Bot bot = new Bot();
    bot.setName(name);
    bot.setDescription("Integration test bot");
    bot.setCode("return 'rock';");
    bot.setPublic(true);
    bot.setElo(1200);
    bot.setOwnerId(owner.getId());
    return botRepository.saveAndFlush(bot);
  }
}
