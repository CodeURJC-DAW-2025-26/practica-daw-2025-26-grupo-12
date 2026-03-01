package es.codeurjc.grupo12.scissors_please.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "server.ssl.enabled", havingValue = "true")
public class HttpsRedirectConfig {

  @Bean
  public TomcatServletWebServerFactory tomcatServletWebServerFactory(
      @Value("${server.http.port:8080}") int httpPort,
      @Value("${server.port:8443}") int httpsPort) {
    TomcatServletWebServerFactory factory =
        new TomcatServletWebServerFactory() {
          @Override
          protected void postProcessContext(Context context) {
            SecurityConstraint securityConstraint = new SecurityConstraint();
            securityConstraint.setUserConstraint("CONFIDENTIAL");
            SecurityCollection collection = new SecurityCollection();
            collection.addPattern("/*");
            securityConstraint.addCollection(collection);
            context.addConstraint(securityConstraint);
          }
        };

    Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
    connector.setScheme("http");
    connector.setPort(httpPort);
    connector.setSecure(false);
    connector.setRedirectPort(httpsPort);
    factory.addAdditionalConnectors(connector);

    return factory;
  }
}
