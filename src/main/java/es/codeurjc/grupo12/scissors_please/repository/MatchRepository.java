package es.codeurjc.grupo12.scissors_please.repository;

import es.codeurjc.grupo12.scissors_please.model.Match;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

  @Query(
      """
      select m
      from Match m
      left join m.bot1 b1
      left join m.bot2 b2
      order by
        case
          when coalesce(b1.elo, 0) >= coalesce(b2.elo, 0) then coalesce(b1.elo, 0)
          else coalesce(b2.elo, 0)
        end desc,
        m.timestamp desc,
        m.id desc
      """)
  Page<Match> findBestMatches(Pageable pageable);

  List<Match> findDistinctByBot1OwnerIdOrBot2OwnerIdOrderByTimestampDesc(
      Long bot1OwnerId, Long bot2OwnerId);

  List<Match> findAllByOrderByTimestampDesc();
}
