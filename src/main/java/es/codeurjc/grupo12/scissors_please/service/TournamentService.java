package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.repository.TournamentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TournamentService {

  private static final int MAX_PAGE_SIZE = 20;
  private final TournamentRepository tournamentRepository;

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
        tournament.getName(), summary, label, badgeClass, actionLabel, actionHref, actionDisabled);
  }

  public record TournamentListItem(
      String name,
      String summary,
      String status,
      String badgeClass,
      String actionLabel,
      String actionHref,
      boolean actionDisabled) {}

  public record TournamentPage(
      List<TournamentListItem> tournaments,
      int nextPage,
      boolean hasMore,
      long totalElements,
      int fromItem,
      int toItem) {}
}
