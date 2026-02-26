package es.codeurjc.grupo12.scissors_please.repository;

import es.codeurjc.grupo12.scissors_please.model.Tournament;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
  Page<Tournament> findAllByOrderByStartDateAsc(Pageable pageable);

  List<Tournament> findByStatusIgnoreCaseAndStartDateLessThanEqualOrderByStartDateAsc(
      String status, LocalDate startDate);
}
