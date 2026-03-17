package es.codeurjc.grupo12.scissors_please.views;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public final class WebRedirectView implements WebFlowView {

  private final String viewName;
  private final Map<String, Object> flashAttributes = new LinkedHashMap<>();

  private WebRedirectView(String viewName) {
    this.viewName = viewName;
  }

  public static WebRedirectView to(String path) {
    return new WebRedirectView("redirect:" + path);
  }

  public WebRedirectView flash(String key, Object value) {
    flashAttributes.put(key, value);
    return this;
  }

  @Override
  public String viewName() {
    return viewName;
  }

  @Override
  public void toRedirectAttributes(RedirectAttributes redirectAttributes) {
    flashAttributes.forEach(redirectAttributes::addFlashAttribute);
  }
}
