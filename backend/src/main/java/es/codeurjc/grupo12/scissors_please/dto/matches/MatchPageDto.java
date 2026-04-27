package es.codeurjc.grupo12.scissors_please.dto.matches;

import es.codeurjc.grupo12.scissors_please.views.MatchListItem;
import java.util.List;
import org.springframework.data.domain.Page;

public record MatchPageDto(
    List<MatchListItemDto> content,
    int number,
    int size,
    int totalPages,
    long totalElements,
    boolean last) {
  public static MatchPageDto fromPage(Page<MatchListItem> page) {
    return new MatchPageDto(
        page.getContent().stream().map(MatchListItemDto::from).toList(),
        page.getNumber(),
        page.getSize(),
        page.getTotalPages(),
        page.getTotalElements(),
        page.isLast());
  }
}
