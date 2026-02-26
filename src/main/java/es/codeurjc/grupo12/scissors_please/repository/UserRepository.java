package es.codeurjc.grupo12.scissors_please.repository;

import es.codeurjc.grupo12.scissors_please.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  List<User> findTop25ByOrderByUsernameAsc();

  List<User> findTop25ByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByUsernameAsc(
      String usernameQuery, String emailQuery);

  List<User> findTop25ByBlockedOrderByUsernameAsc(boolean blocked);

  List<User>
      findTop25ByBlockedAndUsernameContainingIgnoreCaseOrBlockedAndEmailContainingIgnoreCaseOrderByUsernameAsc(
          boolean usernameBlocked, String usernameQuery, boolean emailBlocked, String emailQuery);
}
