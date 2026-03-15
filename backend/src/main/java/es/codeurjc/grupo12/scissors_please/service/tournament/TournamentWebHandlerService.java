package es.codeurjc.grupo12.scissors_please.service.tournament;

import es.codeurjc.grupo12.scissors_please.config.ErrorConstants;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.views.JoinTournamentResult;
import es.codeurjc.grupo12.scissors_please.views.TournamentJoinPage;
import es.codeurjc.grupo12.scissors_please.views.TournamentListItem;
import es.codeurjc.grupo12.scissors_please.views.TournamentRegistrationState;
import es.codeurjc.grupo12.scissors_please.views.UserTournamentSection;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import es.codeurjc.grupo12.scissors_please.views.WebPageView;
import es.codeurjc.grupo12.scissors_please.views.WebRedirectView;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class TournamentWebHandlerService {

  @Autowired private TournamentService tournamentService;
  @Autowired private UserService userService;

  public WebFlowView tournamentListHandler(Pageable pageable, String query) {
    String normalizedQuery = query == null ? "" : query.trim();
    Page<TournamentListItem> tournamentPage =
        tournamentService.getTournamentPage(normalizedQuery, pageable);
    int fromItem =
        tournamentPage.isEmpty() ? 0 : (int) tournamentPage.getPageable().getOffset() + 1;
    int toItem = tournamentPage.isEmpty() ? 0 : fromItem + tournamentPage.getNumberOfElements() - 1;

    return WebPageView.of("tournament-list")
        .attribute("tournaments", tournamentPage.getContent())
        .attribute("showEmpty", pageable.getPageNumber() == 0 && tournamentPage.isEmpty())
        .attribute("nextPage", tournamentPage.getNumber() + 1)
        .attribute("hasMore", tournamentPage.hasNext())
        .attribute("totalElements", tournamentPage.getTotalElements())
        .attribute("fromItem", fromItem)
        .attribute("toItem", toItem)
        .attribute("size", Math.max(pageable.getPageSize(), 1))
        .attribute("searchQuery", normalizedQuery);
  }

  public WebFlowView tournamentListPageHandler(Pageable pageable, String query) {
    String normalizedQuery = query == null ? "" : query.trim();
    Page<TournamentListItem> tournamentPage =
        tournamentService.getTournamentPage(normalizedQuery, pageable);
    int fromItem =
        tournamentPage.isEmpty() ? 0 : (int) tournamentPage.getPageable().getOffset() + 1;
    int toItem = tournamentPage.isEmpty() ? 0 : fromItem + tournamentPage.getNumberOfElements() - 1;

    return WebPageView.of("components/tournament-page-chunk")
        .attribute("tournaments", tournamentPage.getContent())
        .attribute("showEmpty", pageable.getPageNumber() == 0 && tournamentPage.isEmpty())
        .attribute("nextPage", tournamentPage.getNumber() + 1)
        .attribute("hasMore", tournamentPage.hasNext())
        .attribute("totalElements", tournamentPage.getTotalElements())
        .attribute("fromItem", fromItem)
        .attribute("toItem", toItem)
        .attribute("searchQuery", normalizedQuery);
  }

  public WebFlowView tournamentDetailHandler(Long id, Authentication authentication) {
    User currentUser =
        isAuthenticated(authentication) ? userService.getCurrentUser(authentication) : null;
    UserType type = resolveUser(currentUser);
    Optional<Tournament> tournamentOp = tournamentService.getTournamentById(id);
    if (tournamentOp.isEmpty()) {
      return WebPageView.of("error")
          .attribute("errorMessage", ErrorConstants.TOURNAMENT_NOT_FOUND)
          .attribute("errorCode", ErrorConstants.NOT_FOUND_CODE);
    }

    Tournament tournament = tournamentOp.get();
    TournamentRegistrationState registrationState =
        tournamentService.getRegistrationState(tournament, currentUser);

    return WebPageView.of("tournament-detail")
        .attribute("isAdmin", type == UserType.ADMIN)
        .attribute("logged", currentUser != null)
        .attribute("open", registrationState.registrationOpen())
        .attribute("showJoinButton", registrationState.showJoinButton())
        .attribute("joinMessage", registrationState.joinMessage())
        .attribute("joinMessageClass", registrationState.joinMessageClass())
        .attribute("hasTournamentPhoto", tournament.getImage() != null)
        .attribute("participants", registrationState.registeredParticipants())
        .attribute("tournament", tournament);
  }

  public WebFlowView tournamentCreateHandler() {
    return WebPageView.of("tournament-create");
  }

  public WebFlowView tournamentJoinRedirectHandler() {
    return WebRedirectView.to("/tournaments");
  }

  public WebFlowView tournamentJoinHandler(Long id, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    if (userService.isAdmin(currentUser)) {
      return WebRedirectView.to("/tournaments/detail/" + id);
    }

    Optional<Tournament> tournamentOp = tournamentService.getTournamentById(id);
    if (tournamentOp.isEmpty()) {
      return WebPageView.of("error")
          .attribute("errorMessage", ErrorConstants.TOURNAMENT_NOT_FOUND)
          .attribute("errorCode", ErrorConstants.NOT_FOUND_CODE);
    }

    TournamentJoinPage joinPage = tournamentService.getTournamentJoinPage(id, currentUser);
    return WebPageView.of("tournament-join").attribute("joinPage", joinPage);
  }

  public WebFlowView joinTournamentHandler(
      Long tournamentId, Long botId, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    JoinTournamentResult result =
        tournamentService.joinTournament(tournamentId, botId, currentUser);

    return switch (result.status()) {
      case JOINED ->
          WebRedirectView.to("/tournaments/detail/" + tournamentId)
              .flash("successMessage", result.message());
      case INVALID_BOT ->
          WebRedirectView.to("/tournaments/join/" + tournamentId)
              .flash("errorMessage", result.message());
      case TOURNAMENT_NOT_FOUND ->
          WebRedirectView.to("/tournaments").flash("errorMessage", result.message());
      default ->
          WebRedirectView.to("/tournaments/detail/" + tournamentId)
              .flash("errorMessage", result.message());
    };
  }

  // Should look into making it pageable
  public WebFlowView myTournamentsHandler(Authentication authentication, String search) {
    Long userId = userService.getCurrentUser(authentication).getId();
    UserTournamentSection section = tournamentService.getUserTournamentSection(userId, search);

    return WebPageView.of("my-tournaments")
        .attribute("tournaments", section.tournaments())
        .attribute("hasTournaments", !section.tournaments().isEmpty())
        .attribute("search", section.search());
  }

  private boolean isAuthenticated(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }

  private boolean hasRole(User user, String role) {
    return user.getRoles() != null && user.getRoles().contains(role);
  }

  private UserType resolveUser(User user) {
    if (user == null) {
      return UserType.GUEST;
    }
    if (hasRole(user, "ADMIN")) {
      return UserType.ADMIN;
    }
    if (hasRole(user, "USER")) {
      return UserType.USER;
    }
    return UserType.GUEST;
  }

  private enum UserType {
    GUEST,
    USER,
    ADMIN
  }
}
