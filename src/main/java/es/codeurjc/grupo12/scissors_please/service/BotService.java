package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BotService {
  private static final int MAX_PAGE_SIZE = 20;
  @Autowired private BotRepository botRepository;

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
  public BotPage getAdminBotPage(String query, String visibility, Pageable pageable) {
    int safePage = Math.max(pageable.getPageNumber(), 0);
    int safeSize = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
    Pageable safePageable = PageRequest.of(safePage, safeSize);

    String normalizedQuery = (query != null) ? query.trim() : "";
    boolean hasQuery = !normalizedQuery.isBlank();
    Page<Bot> pageResult;

    if ("public".equalsIgnoreCase(visibility)) {
      pageResult =
          hasQuery
              ? botRepository.findByNameContainingIgnoreCaseAndIsPublicOrderByIdDesc(
                  normalizedQuery, true, safePageable)
              : botRepository.findByIsPublicOrderByIdDesc(true, safePageable);
    } else if ("private".equalsIgnoreCase(visibility)) {
      pageResult =
          hasQuery
              ? botRepository.findByNameContainingIgnoreCaseAndIsPublicOrderByIdDesc(
                  normalizedQuery, false, safePageable)
              : botRepository.findByIsPublicOrderByIdDesc(false, safePageable);
    } else {
      pageResult =
          hasQuery
              ? botRepository.findByNameContainingIgnoreCaseOrderByIdDesc(
                  normalizedQuery, safePageable)
              : botRepository.findAllByOrderByIdDesc(safePageable);
    }

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

  @Transactional
  public void recordMatchResult(Bot b1, Bot b2, int score1, int score2) {
    if (b1 == null || b2 == null || b1.getId() == null || b2.getId() == null) {
      return;
    }

    Bot bot1 = botRepository.findById(b1.getId()).orElseThrow();
    Bot bot2 = botRepository.findById(b2.getId()).orElseThrow();

    double s1;
    double s2;

    if (score1 > score2) {
      s1 = 1.0;
      s2 = 0.0;
      bot1.setWins(bot1.getWins() + 1);
      bot2.setLosses(bot2.getLosses() + 1);
    } else if (score1 < score2) {
      s1 = 0.0;
      s2 = 1.0;
      bot1.setLosses(bot1.getLosses() + 1);
      bot2.setWins(bot2.getWins() + 1);
    } else {
      s1 = 0.5;
      s2 = 0.5;
      bot1.setDraws(bot1.getDraws() + 1);
      bot2.setDraws(bot2.getDraws() + 1);
    }

    int r1 = bot1.getElo();
    int r2 = bot2.getElo();

    double e1 = 1.0 / (1.0 + Math.pow(10, (r2 - r1) / 400.0));
    double e2 = 1.0 / (1.0 + Math.pow(10, (r1 - r2) / 400.0));

    int k = 32;
    int newR1 = (int) Math.round(r1 + k * (s1 - e1));
    int newR2 = (int) Math.round(r2 + k * (s2 - e2));

    bot1.updateElo(newR1);
    bot2.updateElo(newR2);

    botRepository.save(bot1);
    botRepository.save(bot2);
  }

  @Transactional(readOnly = true)
  public UserGlobalRanking getUserGlobalRanking(User user) {
    Long ownerId = requireOwnerId(user);
    List<Bot> allBots = botRepository.findAll();
    Map<Long, Integer> bestEloByOwner = new HashMap<>();

    for (Bot bot : allBots) {
      Long botOwnerId = bot.getOwnerId();
      if (botOwnerId == null) {
        continue;
      }
      bestEloByOwner.merge(botOwnerId, bot.getElo(), Math::max);
    }

    Integer userBestElo = bestEloByOwner.get(ownerId);
    if (userBestElo == null) {
      return new UserGlobalRanking(false, 0, bestEloByOwner.size(), 0);
    }

    long usersWithHigherElo =
        bestEloByOwner.values().stream().filter(elo -> elo > userBestElo).count();
    int rank = (int) usersWithHigherElo + 1;
    return new UserGlobalRanking(true, rank, bestEloByOwner.size(), userBestElo);
  }

  public Bot createBot(Bot bot, User owner) {
    if (owner.getRoles() != null && owner.getRoles().contains("ADMIN")) {
      throw new IllegalArgumentException("Admin users cannot own bots");
    }
    bot.setOwnerId(requireOwnerId(owner));
    return botRepository.save(bot);
  }

  // This methods are identical but conceptually they might be different in a near
  // future
  public Bot updateBot(Bot bot, User actingUser) {
    if (bot.getOwnerId() == null) {
      if (actingUser.getRoles() != null && actingUser.getRoles().contains("ADMIN")) {
        throw new IllegalArgumentException("Admin users cannot own bots");
      }
      bot.setOwnerId(requireOwnerId(actingUser));
    }
    return botRepository.save(bot);
  }

  public void deleteBot(Long id) {
    botRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public Optional<Bot> getBotById(Long id) {
    return botRepository.findById(id);
  }

  private Long requireOwnerId(User user) {
    if (user.getId() == null) {
      throw new IllegalArgumentException("User ID is required");
    }
    return user.getId();
  }

  public Long findRankingPositionById(Long id) {
    return botRepository.findRankingPositionById(id);
  }

  @Transactional(readOnly = true)
  public List<Bot> searchBots(String query, String visibility) {
    boolean hasQuery = query != null && !query.isBlank();

    if ("public".equalsIgnoreCase(visibility)) {
      return hasQuery
          ? botRepository.findByNameContainingIgnoreCaseAndIsPublic(query, true)
          : botRepository.findByIsPublic(true);
    }

    if ("private".equalsIgnoreCase(visibility)) {
      return hasQuery
          ? botRepository.findByNameContainingIgnoreCaseAndIsPublic(query, false)
          : botRepository.findByIsPublic(false);
    }

    return hasQuery ? botRepository.findByNameContainingIgnoreCase(query) : botRepository.findAll();
  }

  @Transactional(readOnly = true)
  public record BotPage(
      List<Bot> bots,
      int nextPage,
      boolean hasMore,
      long totalElements,
      int fromItem,
      int toItem) {}

  public record UserGlobalRanking(boolean ranked, int rank, int totalUsers, int bestElo) {}
}
