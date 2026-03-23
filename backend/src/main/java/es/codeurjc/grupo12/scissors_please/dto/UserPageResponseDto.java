package es.codeurjc.grupo12.scissors_please.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record UserPageResponseDto(
    List<UserResponseDto> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious,
    boolean first,
    boolean last) {
  public static UserPageResponseDto fromPage(Page<UserResponseDto> page) {
    return new UserPageResponseDto(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.hasNext(),
        page.hasPrevious(),
        page.isFirst(),
        page.isLast());
  }
}
