package es.codeurjc.grupo12.scissors_please.dto;

import es.codeurjc.grupo12.scissors_please.views.TournamentListItem;
import java.util.List;
import org.springframework.data.domain.Page;

public record TournamentPageDto(
    List<TournamentListItemDto> content, int pageNumber, int totalPages, long totalElements) {
  public static TournamentPageDto fromPage(Page<TournamentListItem> page) {
    List<TournamentListItemDto> content =
        page.getContent().stream().map(TournamentListItemDto::from).toList();
    return new TournamentPageDto(
        content, page.getNumber(), page.getTotalPages(), page.getTotalElements());
  }
}
