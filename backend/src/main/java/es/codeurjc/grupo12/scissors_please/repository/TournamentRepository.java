package es.codeurjc.grupo12.scissors_please.repository;

import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.TournamentStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
  Page<Tournament> findByNameContainingIgnoreCase(String name, Pageable pageable);

  Page<Tournament> findDistinctByParticipantsOwnerId(Long ownerId, Pageable pageable);

  Page<Tournament> findDistinctByParticipantsOwnerIdAndNameContainingIgnoreCase(
      Long ownerId, String name, Pageable pageable);

  List<Tournament> findAllByOrderByStartDateAsc();

  List<Tournament> findDistinctByParticipantsOwnerIdOrderByStartDateAsc(Long ownerId);

  List<Tournament> findByStatusAndStartDateLessThanEqualOrderByStartDateAsc(
      TournamentStatus status, LocalDate startDate);
}
