package es.codeurjc.grupo12.scissors_please.dto.tournaments;

import es.codeurjc.grupo12.scissors_please.views.TournamentListItem;
import java.util.List;
import org.springframework.data.domain.Page;

public record TournamentPageDto(
    List<TournamentListItemDto> content,
    int number,
    int size,
    int totalPages,
    long totalElements,
    boolean last) {
  public static TournamentPageDto fromPage(Page<TournamentListItem> page) {
    List<TournamentListItemDto> content =
        page.getContent().stream().map(TournamentListItemDto::from).toList();
    return new TournamentPageDto(
        content,
        page.getNumber(),
        page.getSize(),
        page.getTotalPages(),
        page.getTotalElements(),
        page.isLast());
  }
}
