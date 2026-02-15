package es.codeurjc.grupo12.scissors_please.config;

import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Slf4j
public class DataInitializer {

  @Bean
  public CommandLineRunner initData(
      UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      if (userRepository.count() == 0) {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@scissors-please.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRoles(List.of("ADMIN", "USER"));

        User regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setEmail("user@scissors-please.com");
        regularUser.setPassword(passwordEncoder.encode("user123"));
        regularUser.setRoles(List.of("USER"));

        userRepository.save(adminUser);
        userRepository.save(regularUser);

        log.info("admin user created admin:admin123");
        log.info("ordinary user created user:user123");
      }
    };
  }
}
