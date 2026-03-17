package es.codeurjc.grupo12.scissors_please.views;

import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface WebFlowView {

  String viewName();

  default void toModel(Model model) {}

  default void toRedirectAttributes(RedirectAttributes redirectAttributes) {}
}
