package es.codeurjc.grupo12.scissors_please.common.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class PageableUtils {

  private PageableUtils() {}

  public static Pageable sanitize(Pageable pageable, int maxPageSize) {
    int safePage = Math.max(pageable.getPageNumber(), 0);
    int safeSize = Math.min(Math.max(pageable.getPageSize(), 1), maxPageSize);
    return PageRequest.of(safePage, safeSize, pageable.getSort());
  }
}
