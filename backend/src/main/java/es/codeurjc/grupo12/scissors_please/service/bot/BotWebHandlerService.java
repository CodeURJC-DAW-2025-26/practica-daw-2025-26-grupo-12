package es.codeurjc.grupo12.scissors_please.service.bot;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.chart.ChartService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import es.codeurjc.grupo12.scissors_please.views.WebPageView;
import es.codeurjc.grupo12.scissors_please.views.WebRedirectView;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BotWebHandlerService {

  @Autowired private BotService botService;
  @Autowired private UserService userService;
  @Autowired private ChartService chartService;

  public WebFlowView createBotHandler(
      String name,
      String description,
      String code,
      String tags,
      MultipartFile image,
      boolean isPublic,
      Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    Bot bot = botService.createBot(currentUser, name, description, code, tags, image, isPublic);
    return WebRedirectView.to("/bots/" + bot.getId() + "/edit");
  }

  public WebFlowView botDetailHandler(Long id, Authentication authentication) {
    User currentUser =
        isAuthenticated(authentication) ? userService.getCurrentUser(authentication) : null;
    Bot bot = botService.getUserBot(Optional.ofNullable(currentUser), id);
    boolean canManage = botService.canManageBot(currentUser, bot);

    byte[] pieChartBytes =
        chartService.generateResultsPieChart(bot.getWins(), bot.getLosses(), bot.getDraws());
    byte[] eloChartBytes = chartService.generateEloLineChart(bot.getEloHistory());

    int total = bot.getWins() + bot.getLosses() + bot.getDraws();
    double winRate = total > 0 ? (bot.getWins() * 100.0 / total) : 0;
    int startElo = bot.getEloHistory().isEmpty() ? bot.getElo() : bot.getEloHistory().get(0);
    int trend = bot.getElo() - startElo;
    boolean isOwner = currentUser != null && Objects.equals(currentUser.getId(), bot.getOwnerId());

    return WebPageView.of("bot-detail")
        .attribute("rankingPosition", botService.findRankingPositionById(bot.getId()))
        .attribute("bot", bot)
        .attribute("username", resolveOwnerUsername(bot))
        .attribute("initial", resolveInitial(bot))
        .attribute("totalMatches", total)
        .attribute("winRateFormatted", String.format("%.1f", winRate))
        .attribute("pieChart", Base64.getEncoder().encodeToString(pieChartBytes))
        .attribute("eloChart", Base64.getEncoder().encodeToString(eloChartBytes))
        .attribute("startElo", startElo)
        .attribute("eloTrend", (trend >= 0 ? "+" : "") + trend)
        .attribute("trendClass", trend >= 0 ? "bg-success" : "bg-danger")
        .attribute("canManage", canManage)
        .attribute("isOwner", isOwner)
        .attribute("showCode", bot.isPublic() || canManage);
  }

  public WebFlowView updateBotHandler(
      Long id,
      String name,
      String description,
      String code,
      MultipartFile image,
      String tags,
      boolean isPublic,
      Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    botService.updateBot(currentUser, id, name, description, code, image, tags, isPublic);
    return WebRedirectView.to("/bots/" + id);
  }

  public WebFlowView deleteBotHandler(Long id, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    botService.deleteBot(currentUser, id);
    if (userService.isAdmin(currentUser)) {
      return WebRedirectView.to("/admin/bots");
    }
    return WebRedirectView.to("/bots/user-bots");
  }

  public WebFlowView createBotPageHandler() {
    return WebPageView.of("bot-create");
  }

  public WebFlowView editBotHandler(Long botId, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    Bot bot = botService.getEditableBotOrThrow(botId, currentUser);
    return WebPageView.of("bot-edit")
        .attribute("initial", resolveInitial(bot))
        .attribute("bot", bot);
  }

  public WebFlowView userBotsPageHandler(
      String username, Pageable pageable, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    User targetUser = resolveTargetUser(username, currentUser);
    if (targetUser == null) {
      return WebPageView.of("error")
          .attribute("errorMessage", "The user is no longer in the database.")
          .attribute("errorCode", ResponseConstants.NOT_FOUND_CODE);
    }

    boolean canManage = userService.canViewPrivateBots(currentUser, targetUser);
    boolean ownBotsView = isOwnBotsView(currentUser, targetUser);
    List<Bot> visibleBots = botService.getBotsForUser(targetUser, canManage);

    int bestElo = visibleBots.stream().mapToInt(Bot::getElo).max().orElse(0);
    Long latestBotId =
        visibleBots.stream()
            .map(Bot::getId)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(null);

    return WebPageView.of("user-bots")
        .attribute("latestBotId", latestBotId)
        .attribute("hasLatestBot", latestBotId != null)
        .attribute("totalBots", visibleBots.size())
        .attribute("bestElo", bestElo)
        .attribute("username", targetUser.getUsername())
        .attribute("canManage", canManage)
        .attribute("ownBotsView", ownBotsView)
        .attribute("size", Math.max(pageable.getPageSize(), 1))
        .attribute("fromItem", 0)
        .attribute("toItem", 0)
        .attribute("totalElements", 0);
  }

  public WebFlowView userBotsChunkHandler(
      String username, Pageable pageable, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    User targetUser = resolveTargetUser(username, currentUser);
    if (targetUser == null) {
      throw new IllegalArgumentException("User not found");
    }

    boolean canManage = userService.canViewPrivateBots(currentUser, targetUser);
    Page<Bot> botPage =
        botService.getUserBots(Optional.of(currentUser.getId()), targetUser.getId(), pageable);
    int fromItem = botPage.isEmpty() ? 0 : (int) botPage.getPageable().getOffset() + 1;
    int toItem = botPage.isEmpty() ? 0 : fromItem + botPage.getNumberOfElements() - 1;

    return WebPageView.of("components/bot-page-chunk")
        .attribute("bots", botPage.getContent())
        .attribute("showEmpty", pageable.getPageNumber() == 0 && botPage.isEmpty())
        .attribute("canManage", canManage)
        .attribute("nextPage", botPage.getNumber() + 1)
        .attribute("hasMore", botPage.hasNext())
        .attribute("totalElements", botPage.getTotalElements())
        .attribute("fromItem", fromItem)
        .attribute("toItem", toItem);
  }

  private boolean isAuthenticated(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }

  private User resolveTargetUser(String username, User currentUser) {
    if (username == null || username.isBlank()) {
      return currentUser;
    }
    return userService.findByUsername(username.trim()).orElse(null);
  }

  private boolean isOwnBotsView(User currentUser, User targetUser) {
    return currentUser != null
        && currentUser.getId() != null
        && currentUser.getId().equals(targetUser.getId());
  }

  private char resolveInitial(Bot bot) {
    if (bot == null || bot.getName() == null || bot.getName().isBlank()) {
      return '?';
    }
    return bot.getName().charAt(0);
  }

  private String resolveOwnerUsername(Bot bot) {
    if (bot == null || bot.getOwnerId() == null) {
      return "Unknown";
    }
    try {
      User owner = userService.getUserById(bot.getOwnerId());
      String username = owner == null ? null : owner.getUsername();
      return (username == null || username.isBlank()) ? "Unknown" : username;
    } catch (IllegalArgumentException exception) {
      return "Unknown";
    }
  }
}
