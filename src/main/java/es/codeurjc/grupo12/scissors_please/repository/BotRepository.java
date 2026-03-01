package es.codeurjc.grupo12.scissors_please.repository;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {
  List<Bot> findByIsPublicTrue();

  List<Bot> findByOwnerId(Long ownerId);

  List<Bot> findByOwnerIdAndIsPublicTrue(Long ownerId);

  List<Bot> findByNameContainingIgnoreCase(String name);

  List<Bot> findByNameContainingIgnoreCaseAndIsPublic(String name, boolean isPublic);

  List<Bot> findByIsPublic(boolean isPublic);

  Page<Bot> findAllByOrderByIdDesc(Pageable pageable);

  Page<Bot> findByNameContainingIgnoreCaseOrderByIdDesc(String name, Pageable pageable);

  Page<Bot> findByNameContainingIgnoreCaseAndIsPublicOrderByIdDesc(
      String name, boolean isPublic, Pageable pageable);

  Page<Bot> findByIsPublicOrderByIdDesc(boolean isPublic, Pageable pageable);

  Page<Bot> findByOwnerIdOrderByIdDesc(Long ownerId, Pageable pageable);

  Page<Bot> findByOwnerIdAndIsPublicTrueOrderByIdDesc(Long ownerId, Pageable pageable);

  @Query(
      "SELECT COUNT(b) + 1 FROM Bot b WHERE b.isPublic = true AND b.elo > (SELECT b2.elo FROM Bot b2 WHERE b2.id = :botId)")
  Long findRankingPositionById(@Param("botId") Long botId);

  @Query(
      "SELECT SUM(b.wins) as totalWins, SUM(b.losses) as totalLosses, "
          + "SUM(b.draws) as totalDraws FROM Bot b WHERE b.ownerId = :ownerId")
  StatsProjection aggregateStatsByOwnerId(@Param("ownerId") Long ownerId);

  @Query("SELECT MAX(b.elo) FROM Bot b WHERE b.ownerId = :ownerId")
  Integer findMaxEloByOwnerId(@Param("ownerId") Long ownerId);

  public interface StatsProjection {
    Long getTotalWins();

    Long getTotalLosses();

    Long getTotalDraws();
  }
}
