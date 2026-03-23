package es.codeurjc.grupo12.scissors_please.service.match;

import es.codeurjc.grupo12.scissors_please.dto.MatchBattleDto;
import es.codeurjc.grupo12.scissors_please.dto.MatchStartResultDto;
import es.codeurjc.grupo12.scissors_please.dto.MatchStatsDto;
import es.codeurjc.grupo12.scissors_please.dto.MatchmakingStatusDto;
import es.codeurjc.grupo12.scissors_please.dto.RecentMatchesDto;
import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.bot.BotService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.views.BotOptionView;
import es.codeurjc.grupo12.scissors_please.views.MatchListItem;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import es.codeurjc.grupo12.scissors_please.views.WebPageView;
import es.codeurjc.grupo12.scissors_please.views.WebRedirectView;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class MatchWebHandlerService {

  @Autowired private BotService botService;
  @Autowired private MatchService matchService;
  @Autowired private UserService userService;

  public WebFlowView matchListHandler(Pageable pageable) {
    return WebPageView.of("match-list")
        .attribute("size", Math.max(pageable.getPageSize(), 1))
        .attribute("fromItem", 0)
        .attribute("toItem", 0)
        .attribute("totalElements", 0);
  }

  public WebFlowView matchListPageHandler(Pageable pageable) {
    Page<MatchListItem> matchPage = matchService.getBestMatchPage(pageable);
    int fromItem = matchPage.isEmpty() ? 0 : (int) matchPage.getPageable().getOffset() + 1;
    int toItem = matchPage.isEmpty() ? 0 : fromItem + matchPage.getNumberOfElements() - 1;

    return WebPageView.of("components/match-page-chunk")
        .attribute("matches", matchPage.getContent())
        .attribute("showEmpty", pageable.getPageNumber() == 0 && matchPage.isEmpty())
        .attribute("nextPage", matchPage.getNumber() + 1)
        .attribute("hasMore", matchPage.hasNext())
        .attribute("totalElements", matchPage.getTotalElements())
        .attribute("fromItem", fromItem)
        .attribute("toItem", toItem);
  }

  public WebFlowView matchStatsHandler(Long matchId, Authentication authentication) {
    try {
      Long currentUserId =
          isAuthenticated(authentication)
              ? userService.getCurrentUser(authentication).getId()
              : null;
      MatchStatsDto matchStats = matchService.getMatchStatsView(matchId, currentUserId);
      WebPageView view =
          WebPageView.of("match-stats")
              .attribute("stats", matchStats)
              .attribute("hasRounds", !matchStats.rounds().isEmpty());
      if (isAuthenticated(authentication)) {
        matchService.acknowledgeReadyMatch(currentUserId, matchId);
        view.attribute("canRematch", matchService.canRequestRematch(matchId, currentUserId));
      }
      return view;
    } catch (IllegalArgumentException exception) {
      return WebRedirectView.to("/matches/search").flash("errorMessage", exception.getMessage());
    }
  }

  public WebFlowView matchBattleHandler(Long matchId, Authentication authentication) {
    try {
      MatchBattleDto battleView = matchService.getMatchBattleView(matchId);
      Long userId = userService.getCurrentUser(authentication).getId();
      matchService.acknowledgeReadyMatch(userId, matchId);
      return WebPageView.of("match-battle").attribute("battle", battleView);
    } catch (IllegalArgumentException exception) {
      return WebRedirectView.to("/matches/search").flash("errorMessage", exception.getMessage());
    }
  }

  public WebFlowView matchSearchHandler(Authentication authentication, Long selectedBotId) {
    User currentUser = userService.getCurrentUser(authentication);
    MatchmakingStatusDto matchmakingStatus = matchService.getMatchmakingStatus(currentUser.getId());
    List<Bot> userBots = botService.getBotsForUser(currentUser, true);
    Long effectiveBotId =
        (matchmakingStatus.searching() || matchmakingStatus.matched()) && selectedBotId == null
            ? resolveSelectedBotId(matchmakingStatus.selectedBotId(), userBots)
            : resolveSelectedBotId(selectedBotId, userBots);

    List<BotOptionView> botOptions =
        userBots.stream()
            .sorted(java.util.Comparator.comparingInt(Bot::getElo).reversed())
            .map(
                bot ->
                    new BotOptionView(
                        bot.getId(),
                        bot.getName(),
                        bot.getElo(),
                        bot.getId() != null && bot.getId().equals(effectiveBotId)))
            .toList();

    return WebPageView.of("match-search")
        .attribute("hasBots", !botOptions.isEmpty())
        .attribute("botOptions", botOptions)
        .attribute("searching", matchmakingStatus.searching() || matchmakingStatus.matched())
        .attribute("waitingBotName", matchmakingStatus.selectedBotName())
        .attribute("waitingBotElo", matchmakingStatus.selectedBotElo())
        .attribute("waitingBotDescription", matchmakingStatus.selectedBotDescription())
        .attribute("waitingSeconds", matchmakingStatus.waitSeconds())
        .attribute("playersSearching", matchmakingStatus.playersSearching());
  }

  public WebFlowView startMatchHandler(Authentication authentication, Long botId) {
    User currentUser = userService.getCurrentUser(authentication);
    try {
      MatchStartResultDto startResult =
          matchService.startMatchmaking(currentUser.getId(), currentUser.getUsername(), botId);
      if (startResult.matched()) {
        return WebRedirectView.to("/matches/battle?id=" + startResult.matchId());
      }
      return WebRedirectView.to("/matches/search")
          .flash(
              "successMessage",
              "Searching opponent for "
                  + startResult.myBotName()
                  + ". Waiting for another player...");
    } catch (IllegalArgumentException exception) {
      return WebRedirectView.to("/matches/search").flash("errorMessage", exception.getMessage());
    }
  }

  public WebFlowView cancelMatchmakingHandler(Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    try {
      matchService.cancelMatchmaking(userId);
      return WebRedirectView.to("/matches/search")
          .flash("successMessage", "Matchmaking cancelled.");
    } catch (IllegalArgumentException exception) {
      return WebRedirectView.to("/matches/search").flash("errorMessage", exception.getMessage());
    }
  }

  public MatchmakingStatusDto matchmakingStatusHandler(Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    return matchService.getMatchmakingStatus(userId);
  }

  public WebFlowView requestRematchHandler(Authentication authentication, Long matchId) {
    User currentUser = userService.getCurrentUser(authentication);
    try {
      matchService.requestRematch(matchId, currentUser);
      return WebRedirectView.to("/matches/search");
    } catch (IllegalArgumentException exception) {
      return WebRedirectView.to("/matches/stats?id=" + matchId)
          .flash("errorMessage", exception.getMessage());
    }
  }

  public WebFlowView acceptRematchHandler(Authentication authentication, String invitationId) {
    User currentUser = userService.getCurrentUser(authentication);
    try {
      return WebRedirectView.to(matchService.acceptRematch(invitationId, currentUser));
    } catch (IllegalArgumentException exception) {
      return WebRedirectView.to("/home").flash("errorMessage", exception.getMessage());
    }
  }

  public WebFlowView recentMatchesHandler(Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    RecentMatchesDto section = matchService.getUserRecentMatchSection(userId);

    return WebPageView.of("recent-matches")
        .attribute("matches", section.matches())
        .attribute("hasMatches", !section.matches().isEmpty());
  }

  private Long resolveSelectedBotId(Long requestedBotId, List<Bot> userBots) {
    if (userBots.isEmpty()) {
      return null;
    }
    if (requestedBotId != null
        && userBots.stream().anyMatch(bot -> requestedBotId.equals(bot.getId()))) {
      return requestedBotId;
    }
    return userBots.get(0).getId();
  }

  private boolean isAuthenticated(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }
}
