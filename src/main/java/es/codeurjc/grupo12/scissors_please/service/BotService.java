package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BotService {
  private final BotRepository botRepository;

  @Transactional(readOnly = true)
  public List<Bot> getBotsWithinRange(User user, boolean includePrivate, int from, int to) {
    if (includePrivate) {
      return sliceByRange(botRepository.findByOwner(user), from, to);
    }

    return sliceByRange(botRepository.findByOwnerAndIsPublicTrue(user), from, to);
  }

  @Transactional(readOnly = true)
  public List<Bot> getBotsForUser(User user, boolean includePrivate) {
    if (includePrivate) {
      return new ArrayList<>(botRepository.findByOwner(user));
    }

    return new ArrayList<>(botRepository.findByOwnerAndIsPublicTrue(user));
  }

  private List<Bot> sliceByRange(List<Bot> bots, int from, int to) {
    int normalizedFrom = Math.max(0, from);
    int normalizedTo = Math.max(0, to);
    if (normalizedFrom >= bots.size() || normalizedFrom >= normalizedTo) {
      return new ArrayList<>();
    }

    int end = Math.min(normalizedTo, bots.size());
    return new ArrayList<>(bots.subList(normalizedFrom, end));
  }

  public Bot createBot(Bot bot, User owner) {
    bot.setOwner(owner);
    return botRepository.save(bot);
  }
}
