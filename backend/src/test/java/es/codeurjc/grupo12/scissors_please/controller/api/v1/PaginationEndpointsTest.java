package es.codeurjc.grupo12.scissors_please.controller.api.v1;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.TournamentStatus;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.repository.TournamentRepository;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository;
import es.codeurjc.grupo12.scissors_please.security.CustomUserDetailsService;
import es.codeurjc.grupo12.scissors_please.security.jwt.JwtTokenProvider;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("h2")
@Transactional
class PaginationEndpointsTest {

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private FilterChainProxy springSecurityFilterChain;
  @Autowired private UserRepository userRepository;
  @Autowired private BotRepository botRepository;
  @Autowired private TournamentRepository tournamentRepository;
  @Autowired private CustomUserDetailsService userDetailsService;
  @Autowired private JwtTokenProvider jwtTokenProvider;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilters(springSecurityFilterChain)
            .build();
  }

  @Test
  void getUsersFiltersByUsername() throws Exception {
    String suffix = UUID.randomUUID().toString();
    User matchingUser = createUser("page-user-" + suffix, suffix + "@mail.com");
    createUser("other-user-" + suffix, "other-" + suffix + "@mail.com");

    mockMvc
        .perform(get("/api/v1/users").param("query", matchingUser.getUsername()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].id").value(matchingUser.getId()))
        .andExpect(jsonPath("$.content[0].username").value(matchingUser.getUsername()));
  }

  @Test
  void getBotsFiltersByNameAndHidesPrivateBotsForAnonymousRequests() throws Exception {
    String suffix = UUID.randomUUID().toString();
    User owner = createUser("bot-owner-" + suffix, "bot-owner-" + suffix + "@mail.com");

    Bot publicBot = createBot(owner.getId(), "shared-bot-" + suffix, true);
    createBot(owner.getId(), "shared-bot-" + suffix + "-private", false);

    mockMvc
        .perform(get("/api/v1/bots").param("query", "shared-bot-" + suffix))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].id").value(publicBot.getId()))
        .andExpect(jsonPath("$.content[0].name").value(publicBot.getName()))
        .andExpect(jsonPath("$.content[0].isPublic").value(true));
  }

  @Test
  void putUserProfileWithoutImageKeepsWorking() throws Exception {
    String suffix = UUID.randomUUID().toString();
    User user = createUser("user-" + suffix, "user-" + suffix + "@mail.com");
    String accessToken =
        jwtTokenProvider.generateAccessToken(userDetailsService.loadUserByUsername(user.getUsername()));
    MockMultipartFile requestPart =
        new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            """
            {"username":"updated-%s","email":"updated-%s@mail.com","password":"newpassword123"}
            """
                .formatted(suffix, suffix)
                .getBytes());
    MockMultipartHttpServletRequestBuilder requestBuilder =
        multipart(HttpMethod.PUT, "/api/v1/users/{id}", user.getId()).file(requestPart);

    mockMvc
        .perform(requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId()))
        .andExpect(jsonPath("$.username").value("updated-" + suffix))
        .andExpect(jsonPath("$.email").value("updated-" + suffix + "@mail.com"));
  }

  @Test
  void putTournamentAcceptsJsonRequestPartAsJson() throws Exception {
    String suffix = UUID.randomUUID().toString();
    User admin = createUser("admin-" + suffix, "admin-" + suffix + "@mail.com", List.of("ADMIN"));
    Tournament tournament = createTournament("tournament-" + suffix);
    String accessToken =
        jwtTokenProvider.generateAccessToken(userDetailsService.loadUserByUsername(admin.getUsername()));
    MockMultipartFile requestPart =
        new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            """
            {"name":"updated-tournament-%s","description":"updated description","status":"REGISTRATION_OPEN","slots":24,"registrationStarts":"2026-04-10","startDate":"2026-05-10","price":"20€"}
            """
                .formatted(suffix)
                .getBytes());
    MockMultipartHttpServletRequestBuilder requestBuilder =
        multipart(HttpMethod.PUT, "/api/v1/tournaments/{id}", tournament.getId()).file(requestPart);

    mockMvc
        .perform(requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(tournament.getId()))
        .andExpect(jsonPath("$.name").value("updated-tournament-" + suffix));
  }

  private User createUser(String username, String email) {
    return createUser(username, email, List.of("USER"));
  }

  private User createUser(String username, String email, List<String> roles) {
    User user = new User();
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword("encoded-password");
    user.setOauthProvider("local");
    user.setBlocked(false);
    user.setRoles(roles);
    return userRepository.save(user);
  }

  private Bot createBot(Long ownerId, String name, boolean isPublic) {
    Bot bot = new Bot();
    bot.setOwnerId(ownerId);
    bot.setName(name);
    bot.setDescription("test");
    bot.setPublic(isPublic);
    return botRepository.save(bot);
  }

  private Tournament createTournament(String name) {
    Tournament tournament = new Tournament();
    tournament.setName(name);
    tournament.setDescription("test");
    tournament.setStatus(TournamentStatus.UPCOMING);
    tournament.setSlots(8);
    tournament.setStartDate(LocalDate.of(2026, 5, 1));
    return tournamentRepository.save(tournament);
  }
}
