package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BotService {
  private static final int MAX_PAGE_SIZE = 20;
  private final BotRepository botRepository;

  @Transactional(readOnly = true)
  public BotPage getBotPage(User user, boolean includePrivate, Pageable pageable) {
    Long ownerId = requireOwnerId(user);
    int safePage = Math.max(pageable.getPageNumber(), 0);
    int safeSize = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
    Pageable safePageable = PageRequest.of(safePage, safeSize);

    Page<Bot> pageResult =
        includePrivate
            ? botRepository.findByOwnerIdOrderByIdDesc(ownerId, safePageable)
            : botRepository.findByOwnerIdAndIsPublicTrueOrderByIdDesc(ownerId, safePageable);

    List<Bot> bots = pageResult.getContent();
    long totalElements = pageResult.getTotalElements();
    int fromItem = bots.isEmpty() ? 0 : (safePage * safeSize) + 1;
    int toItem = bots.isEmpty() ? 0 : fromItem + bots.size() - 1;

    return new BotPage(bots, safePage + 1, pageResult.hasNext(), totalElements, fromItem, toItem);
  }

  @Transactional(readOnly = true)
  public List<Bot> getBotsForUser(User user, boolean includePrivate) {
    Long ownerId = requireOwnerId(user);
    if (includePrivate) {
      return new ArrayList<>(botRepository.findByOwnerId(ownerId));
    }

    return new ArrayList<>(botRepository.findByOwnerIdAndIsPublicTrue(ownerId));
  }

  @Transactional(readOnly = true)
  public List<Bot> getTopBotsForUser(User user, boolean includePrivate, int limit) {
    if (limit <= 0) {
      return new ArrayList<>();
    }

    List<Bot> bots = getBotsForUser(user, includePrivate);
    bots.sort(Comparator.comparingInt(Bot::getElo).reversed());
    int end = Math.min(limit, bots.size());
    return new ArrayList<>(bots.subList(0, end));
  }

  public Bot createBot(Bot bot, User owner) {
    if (owner.getRoles() != null && owner.getRoles().contains("ADMIN")) {
      throw new IllegalArgumentException("Admin users cannot own bots");
    }
    bot.setOwnerId(requireOwnerId(owner));
    return botRepository.save(bot);
  }

  public void deleteBot(Long id) {
    botRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public java.util.Optional<Bot> getBotById(Long id) {
    return botRepository.findById(id);
  }

  private Long requireOwnerId(User user) {
    if (user.getId() == null) {
      throw new IllegalArgumentException("User ID is required");
    }
    return user.getId();
  }

  public record BotPage(
      List<Bot> bots,
      int nextPage,
      boolean hasMore,
      long totalElements,
      int fromItem,
      int toItem) {}
}
