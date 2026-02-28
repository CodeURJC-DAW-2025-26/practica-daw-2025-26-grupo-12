package es.codeurjc.grupo12.scissors_please.repository;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {
  List<Bot> findByIsPublicTrue();

  List<Bot> findByOwnerId(Long ownerId);

  List<Bot> findByOwnerIdAndIsPublicTrue(Long ownerId);

  Page<Bot> findByOwnerIdOrderByIdDesc(Long ownerId, Pageable pageable);

  Page<Bot> findByOwnerIdAndIsPublicTrueOrderByIdDesc(Long ownerId, Pageable pageable);
}
