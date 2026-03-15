package es.codeurjc.grupo12.scissors_please.views;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.ui.Model;

public final class WebPageView implements WebFlowView {

  private final String viewName;
  private final Map<String, Object> attributes = new LinkedHashMap<>();

  private WebPageView(String viewName) {
    this.viewName = viewName;
  }

  public static WebPageView of(String viewName) {
    return new WebPageView(viewName);
  }

  public WebPageView attribute(String key, Object value) {
    attributes.put(key, value);
    return this;
  }

  @Override
  public String viewName() {
    return viewName;
  }

  @Override
  public void toModel(Model model) {
    attributes.forEach(model::addAttribute);
  }
}
