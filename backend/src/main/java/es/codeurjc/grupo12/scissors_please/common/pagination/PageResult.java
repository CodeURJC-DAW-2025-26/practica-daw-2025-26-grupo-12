package es.codeurjc.grupo12.scissors_please.common.pagination;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResult<T>(
    List<T> items, int nextPage, boolean hasMore, long totalElements, int fromItem, int toItem) {

  public static <T> PageResult<T> from(Page<T> page) {
    List<T> items = page.getContent();
    int fromItem = items.isEmpty() ? 0 : (int) page.getPageable().getOffset() + 1;
    int toItem = items.isEmpty() ? 0 : fromItem + items.size() - 1;

    return new PageResult<>(
        items, page.getNumber() + 1, page.hasNext(), page.getTotalElements(), fromItem, toItem);
  }
}
