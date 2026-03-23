package es.codeurjc.grupo12.scissors_please.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired private MatchmakingLockInterceptor matchmakingLockInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(matchmakingLockInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/css/**",
            "/js/**",
            "/images/**",
            "/bot-images/**",
            "/user-images/**",
            "/tournament-images/**",
            "/api/v1/images/**");
  }
}
