package es.codeurjc.grupo12.scissors_please.repository;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {
  List<Bot> findByIsPublicTrue();

  List<Bot> findByOwner(User owner);

  List<Bot> findByOwnerAndIsPublicTrue(User owner);
}
