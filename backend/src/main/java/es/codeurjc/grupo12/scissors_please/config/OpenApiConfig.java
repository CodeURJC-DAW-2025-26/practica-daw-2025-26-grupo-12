package es.codeurjc.grupo12.scissors_please.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "Scissors, Please! API",
            version = "v1",
            description = "Documentacion OpenAPI de los endpoints REST de Scissors, Please!",
            contact = @Contact(name = "Grupo 12")))
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {

  @Bean
  OpenAPI scissorsPleaseOpenApi() {
    return new OpenAPI()
        .info(
            new io.swagger.v3.oas.models.info.Info()
                .title("Scissors, Please! API")
                .version("v1")
                .description("Documentacion OpenAPI generada a partir de los controllers REST.")
                .license(
                    new License()
                        .name("CC BY-SA 4.0")
                        .url("https://creativecommons.org/licenses/by-sa/4.0/deed.es")));
  }
}
