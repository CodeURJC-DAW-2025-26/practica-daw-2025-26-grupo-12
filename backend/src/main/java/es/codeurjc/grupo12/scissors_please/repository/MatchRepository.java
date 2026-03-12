package es.codeurjc.grupo12.scissors_please.repository;

import es.codeurjc.grupo12.scissors_please.model.Match;
import java.util.List;
import java.util.Optional;
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
			  (
			    case
			      when (b1.elo < b2.elo and m.bot1Score > m.bot2Score)
			        or (b2.elo < b1.elo and m.bot2Score > m.bot1Score)
			      then least(abs(b1.elo - b2.elo) / 400, 1.0)
			      else -least(abs(b1.elo - b2.elo) / 400, 1.0)
			    end

			    +

			    least(((coalesce(b1.elo, 0) + coalesce(b2.elo, 0)) / 2.0) / 4000.0, 1.0)

			    +

			    least(
			      (
			        select count(m2)
			        from Match m2
			        where
			        (m2.bot1 = b1 and m2.bot2 = b2)
			        or
			        (m2.bot1 = b2 and m2.bot2 = b1)
			      ) / 10.0,
			      1.0
			    )
			  ) desc,
			  m.timestamp desc,
			  m.id desc
			""")
  Page<Match> findBestMatches(Pageable pageable);

  List<Match> findDistinctByBot1OwnerIdOrBot2OwnerIdOrderByTimestampDesc(
      Long bot1OwnerId, Long bot2OwnerId);

  Optional<Match> findTopByOrderByTimestampDesc();
}
