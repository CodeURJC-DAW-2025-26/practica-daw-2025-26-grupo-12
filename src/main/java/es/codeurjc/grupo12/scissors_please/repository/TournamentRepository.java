package es.codeurjc.grupo12.scissors_please.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.grupo12.scissors_please.model.Tournament;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
  Page<Tournament> findAllByOrderByStartDateAsc(Pageable pageable);

  List<Tournament> findAllByOrderByStartDateAsc();

  List<Tournament> findDistinctByParticipantsOwnerIdOrderByStartDateAsc(Long ownerId);

  List<Tournament> findByStatusIgnoreCaseAndStartDateLessThanEqualOrderByStartDateAsc(
      String status, LocalDate startDate);
}
