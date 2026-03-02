package es.codeurjc.grupo12.scissors_please.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("h2")
public class H2ConsoleConfig {

  @Bean
  public ServletRegistrationBean<JakartaWebServlet> h2Console() {
    ServletRegistrationBean<JakartaWebServlet> reg =
        new ServletRegistrationBean<>(new JakartaWebServlet(), "/h2-console/*");
    return reg;
  }
}
