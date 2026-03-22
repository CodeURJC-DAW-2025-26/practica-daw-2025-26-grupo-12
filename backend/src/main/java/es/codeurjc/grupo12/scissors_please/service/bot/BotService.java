package es.codeurjc.grupo12.scissors_please.service.bot;

import es.codeurjc.grupo12.scissors_please.common.pagination.PageableUtils;
import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.exception.BotAccessDeniedException;
import es.codeurjc.grupo12.scissors_please.exception.BotImageUploadException;
import es.codeurjc.grupo12.scissors_please.exception.BotNotFoundException;
import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.views.UserGlobalRanking;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class BotService {
  private static final int MAX_PAGE_SIZE = 20;
  @Autowired private BotRepository botRepository;
  @Autowired private UserService userService;
  @Autowired private ImageService imageService;

  public boolean canManageBot(User user, Bot bot) {
    if (user == null || bot == null) {
      return false;
    }
    if (userService.isAdmin(user)) {
      return true;
    }
    return bot.getOwnerId().equals(user.getId());
  }

  public Bot getUserBot(Optional<User> requesterUser, Long botId) {
    Bot bot =
        botRepository
            .findById(botId)
            .orElseThrow(() -> new BotNotFoundException(ResponseConstants.BOT_NOT_FOUND));

    if (bot.isDeleted()) {
      throw new BotNotFoundException(ResponseConstants.BOT_NOT_FOUND);
    }

    if (bot.isPublic()) {
      return bot;
    }

    if (requesterUser.isEmpty()) {
      throw new BotAccessDeniedException(ResponseConstants.ACCESS_DENIED);
    }

    User targetUser = userService.getUserById(bot.getOwnerId());

    if (userService.canViewPrivateBots(requesterUser.get(), targetUser)) {
      return bot;
    }

    throw new BotAccessDeniedException(ResponseConstants.ACCESS_DENIED);
  }

  @Transactional(readOnly = true)
  public Bot getEditableBotOrThrow(Long botId, User actingUser) {
    Bot bot =
        getBotById(botId).orElseThrow(() -> new BotNotFoundException(ResponseConstants.BOT_NOT_FOUND));

    if (userService.isAdmin(actingUser)) {
      return bot;
    }

    Long actingUserId = requireOwnerId(actingUser);
    if (bot.getOwnerId() == null || !bot.getOwnerId().equals(actingUserId)) {
      throw new BotAccessDeniedException(ResponseConstants.ACCESS_DENIED);
    }

    return bot;
  }

  @Transactional(readOnly = true)
  public Page<Bot> getUserBots(Optional<Long> requesterId, Long targetId, Pageable pageable) {

    User targetUser = userService.getUserById(targetId);
    if (targetUser == null) throw new IllegalArgumentException("Target user does not exist");

    User requesterUser = requesterId.map(userService::getUserById).orElse(null);
    if (requesterId.isPresent() && requesterUser == null)
      throw new IllegalArgumentException("Requester user does not exist");

    boolean canViewPrivate = userService.canViewPrivateBots(requesterUser, targetUser);

    return getBotPage(targetUser, canViewPrivate, pageable);
  }

  @Transactional(readOnly = true)
  public List<Bot> getBotsForUser(User user, boolean includePrivate) {
    Long ownerId = requireOwnerId(user);
    if (includePrivate) {
      return new ArrayList<>(botRepository.findByOwnerIdAndDeletedFalse(ownerId));
    }

    return new ArrayList<>(botRepository.findByOwnerIdAndIsPublicTrueAndDeletedFalse(ownerId));
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

  @Transactional(readOnly = true)
  public int getMaxEloForUser(Long userId) {
    Integer maxElo = botRepository.findMaxEloByOwnerId(userId);
    return (maxElo != null) ? maxElo : 0;
  }

  @Transactional(readOnly = true)
  public int getTotalBotsForUser(Long userId) {
    return botRepository.countByOwnerId(userId);
  }

  @Transactional(readOnly = true)
  public Long getLatestBotIdForUser(Long userId) {
    Page<Bot> pageResult =
        botRepository.findByOwnerIdAndDeletedFalseOrderByIdDesc(userId, Pageable.ofSize(1));
    return pageResult.hasContent() ? pageResult.getContent().get(0).getId() : null;
  }

  @Transactional(readOnly = true)
  public Page<Bot> getAdminBotPage(String query, String visibility, Pageable pageable) {
    Pageable safePageable = PageableUtils.sanitize(pageable, MAX_PAGE_SIZE);

    String normalizedQuery = (query != null) ? query.trim() : "";
    boolean hasQuery = !normalizedQuery.isBlank();
    Page<Bot> pageResult;

    if ("public".equalsIgnoreCase(visibility)) {
      pageResult =
          hasQuery
              ? botRepository.findByNameContainingIgnoreCaseAndIsPublicAndDeletedFalseOrderByIdDesc(
                  normalizedQuery, true, safePageable)
              : botRepository.findByIsPublicAndDeletedFalseOrderByIdDesc(true, safePageable);
    } else if ("private".equalsIgnoreCase(visibility)) {
      pageResult =
          hasQuery
              ? botRepository.findByNameContainingIgnoreCaseAndIsPublicAndDeletedFalseOrderByIdDesc(
                  normalizedQuery, false, safePageable)
              : botRepository.findByIsPublicAndDeletedFalseOrderByIdDesc(false, safePageable);
    } else {
      pageResult =
          hasQuery
              ? botRepository.findByNameContainingIgnoreCaseAndDeletedFalseOrderByIdDesc(
                  normalizedQuery, safePageable)
              : botRepository.findAllByDeletedFalseOrderByIdDesc(safePageable);
    }

    return pageResult;
  }

  @Transactional(readOnly = true)
  public Long findRankingPositionById(Long id) {
    return botRepository.findRankingPositionById(id);
  }

  @Transactional(readOnly = true)
  public UserGlobalRanking getUserGlobalRanking(User user) {
    Long ownerId = requireOwnerId(user);
    List<Bot> allBots = botRepository.findByDeletedFalse();
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

  public Bot createBot(
      User actingUser,
      String name,
      String description,
      String tags,
      MultipartFile image,
      boolean isPublic) {
    Bot bot = new Bot();

    bot.setName(name);
    bot.setDescription(description);
    bot.setOwnerId(requireOwnerId(actingUser));
    bot.setPublic(isPublic);
    bot.setTags(parseTags(tags));
    if (!imageService.handleImageUpload(bot, image)) {
      throw new BotImageUploadException(ResponseConstants.IMAGE_ERROR_UPLOAD);
    }

    return botRepository.save(bot);
  }

  public Bot updateBot(
      User actingUser,
      long botId,
      String name,
      String description,
      String code,
      MultipartFile image,
      String tags,
      boolean isPublic) {
    Bot bot = getEditableBotOrThrow(botId, actingUser);

    bot.setName(name != null ? name : "");
    bot.setDescription(description != null ? description : "");
    bot.setCode(code != null ? code : "");
    bot.setTags(parseTags(tags));
    bot.setPublic(isPublic);
    if (!imageService.handleImageUpload(bot, image)) {
      throw new BotImageUploadException(ResponseConstants.IMAGE_ERROR_UPLOAD);
    }

    return botRepository.save(bot);
  }

  public void deleteBot(User requester, Long botId) {
    Bot bot = botRepository.findById(botId).orElseThrow();
    if (canManageBot(requester, bot)) {
      bot.setDeleted(true);
      botRepository.save(bot);
    } else {
      throw new BotAccessDeniedException(ResponseConstants.ACCESS_DENIED);
    }
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
  private Page<Bot> getBotPage(User user, boolean includePrivate, Pageable pageable) {
    Long ownerId = requireOwnerId(user);
    Pageable safePageable = PageableUtils.sanitize(pageable, MAX_PAGE_SIZE);

    Page<Bot> pageResult =
        includePrivate
            ? botRepository.findByOwnerIdAndDeletedFalseOrderByIdDesc(ownerId, safePageable)
            : botRepository.findByOwnerIdAndIsPublicTrueAndDeletedFalseOrderByIdDesc(
                ownerId, safePageable);

    return pageResult;
  }

  private Optional<Bot> getBotById(Long id) {
    return botRepository.findById(id).filter(bot -> !bot.isDeleted());
  }

  private Long requireOwnerId(User user) {
    if (user.getId() == null) {
      throw new IllegalArgumentException("User ID is required");
    }
    return user.getId();
  }

  private List<String> parseTags(String tags) {
    if (tags == null || tags.isBlank()) {
      return new ArrayList<>();
    }
    List<String> parsedTags = new ArrayList<>();
    for (String tag : tags.split(",")) {
      String trimmedTag = tag.trim();
      if (!trimmedTag.isEmpty()) {
        parsedTags.add(trimmedTag);
      }
    }
    return parsedTags;
  }
}
