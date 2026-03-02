package es.codeurjc.grupo12.scissors_please;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ScissorsPleaseApplication {

  public static void main(String[] args) {
    SpringApplication.run(ScissorsPleaseApplication.class, args);
  }
}
