package es.codeurjc.grupo12.scissors_please.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

  @ModelAttribute
  public void addAttributes(Model model, HttpServletRequest request) {
    model.addAttribute("logged", request.getUserPrincipal() != null);
    model.addAttribute("admin", request.isUserInRole("ADMIN"));

    Object csrf = request.getAttribute("_csrf");
    model.addAttribute("_csrf", Optional.ofNullable(csrf).orElseGet(CsrfDummy::new));

    model.addAttribute("title", "Welcome");
  }

  public static class CsrfDummy {
    public String getToken() {
      return "";
    }

    public String getParameterName() {
      return "_csrf";
    }

    public String getHeaderName() {
      return "X-CSRF-TOKEN";
    }
  }
}
