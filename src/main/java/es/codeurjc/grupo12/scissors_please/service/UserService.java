package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public User registerUser(String username, String email, String password) {
    if (userRepository.findByUsername(username).isPresent()) {
      log.warn("Registration attempt with existing username: {}", username);
      throw new IllegalArgumentException("Username already exists");
    }
    if (userRepository.findByEmail(email).isPresent()) {
      log.warn("Registration attempt with existing email: {}", email);
      throw new IllegalArgumentException("Email already exists");
    }

    User user = new User();
    user.setUsername(username);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.setRoles(List.of("USER"));

    User savedUser = userRepository.save(user);
    log.info("User registered successfully: {}", username);
    return savedUser;
  }

  @Transactional(readOnly = true)
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Transactional(readOnly = true)
  public User getUserById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
  }

  public User updateUser(User user) {
    log.info("Updating user: {}", user.getUsername());
    return userRepository.save(user);
  }

  public void deleteUser(Long id) {
    log.info("Deleting user with id: {}", id);
    userRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }
}
