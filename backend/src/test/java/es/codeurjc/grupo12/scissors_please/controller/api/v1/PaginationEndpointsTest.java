package es.codeurjc.grupo12.scissors_please.controller.api.v1;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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

  private User createUser(String username, String email) {
    User user = new User();
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword("encoded-password");
    user.setOauthProvider("local");
    user.setBlocked(false);
    user.setRoles(List.of("USER"));
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
}
