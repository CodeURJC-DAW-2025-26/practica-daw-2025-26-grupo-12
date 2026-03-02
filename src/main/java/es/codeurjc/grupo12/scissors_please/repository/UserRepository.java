package es.codeurjc.grupo12.scissors_please.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import es.codeurjc.grupo12.scissors_please.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  Optional<User> findByUsernameAndDeleteDateIsNull(String username);

  Optional<User> findByEmailAndDeleteDateIsNull(String email);

  List<User> findAllByDeleteDateIsNull();

  List<User> findTop25ByDeleteDateIsNullOrderByUsernameAsc();

  List<User>
      findTop25ByDeleteDateIsNullAndUsernameContainingIgnoreCaseOrDeleteDateIsNullAndEmailContainingIgnoreCaseOrderByUsernameAsc(
          String usernameQuery, String emailQuery);

  List<User> findTop25ByDeleteDateIsNullAndBlockedOrderByUsernameAsc(boolean blocked);

  List<User>
      findTop25ByDeleteDateIsNullAndBlockedAndUsernameContainingIgnoreCaseOrDeleteDateIsNullAndBlockedAndEmailContainingIgnoreCaseOrderByUsernameAsc(
          boolean usernameBlocked, String usernameQuery, boolean emailBlocked, String emailQuery);

  Page<User> findAllByDeleteDateIsNullOrderByUsernameAsc(Pageable pageable);

  Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByUsernameAsc(
      String usernameQuery, String emailQuery, Pageable pageable);

  Page<User> findByBlockedAndDeleteDateIsNullOrderByUsernameAsc(boolean blocked, Pageable pageable);

  Page<User>
      findByBlockedAndUsernameContainingIgnoreCaseOrBlockedAndEmailContainingIgnoreCaseOrderByUsernameAsc(
          boolean usernameBlocked,
          String usernameQuery,
          boolean emailBlocked,
          String emailQuery,
          Pageable pageable);

  @Query(
      "SELECT YEAR(u.createdAt), MONTH(u.createdAt), COUNT(u) "
          + "FROM User u "
          + "WHERE u.deleteDate IS NULL "
          + "GROUP BY YEAR(u.createdAt), MONTH(u.createdAt) "
          + "ORDER BY YEAR(u.createdAt) ASC, MONTH(u.createdAt) ASC")
  List<MonthlyUserCount> countUsersByMonth();

  public record MonthlyUserCount(Integer year, Integer month, Long count) {}
}
