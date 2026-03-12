package es.codeurjc.grupo12.scissors_please.repository;

import es.codeurjc.grupo12.scissors_please.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

  @Query(
      "SELECT u FROM User u "
          + "WHERE u.deleteDate IS NULL "
          + "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) "
          + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) "
          + "ORDER BY u.username ASC")
  Page<User> searchActiveUsers(@Param("query") String query, Pageable pageable);

  Page<User> findByBlockedAndDeleteDateIsNullOrderByUsernameAsc(boolean blocked, Pageable pageable);

  @Query(
      "SELECT u FROM User u "
          + "WHERE u.deleteDate IS NULL "
          + "AND u.blocked = :blocked "
          + "AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) "
          + "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) "
          + "ORDER BY u.username ASC")
  Page<User> searchActiveUsersByBlocked(
      @Param("blocked") boolean blocked, @Param("query") String query, Pageable pageable);

  @Query(
      "SELECT YEAR(u.createdAt), MONTH(u.createdAt), COUNT(u) "
          + "FROM User u "
          + "WHERE u.deleteDate IS NULL "
          + "GROUP BY YEAR(u.createdAt), MONTH(u.createdAt) "
          + "ORDER BY YEAR(u.createdAt) ASC, MONTH(u.createdAt) ASC")
  List<MonthlyUserCount> countUsersByMonth();

  public record MonthlyUserCount(Integer year, Integer month, Long count) {}
}
