package es.codeurjc.grupo12.scissors_please.controller.advice;

import es.codeurjc.grupo12.scissors_please.config.ErrorConstants;
import es.codeurjc.grupo12.scissors_please.exception.BotAccessDeniedException;
import es.codeurjc.grupo12.scissors_please.exception.BotNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

  @ExceptionHandler(BotNotFoundException.class)
  public String handleBotNotFound(
      BotNotFoundException exception, Model model, HttpServletResponse response) {
    response.setStatus(HttpStatus.NOT_FOUND.value());
    model.addAttribute("errorMessage", exception.getMessage());
    model.addAttribute("errorCode", ErrorConstants.NOT_FOUND_CODE);
    return "error";
  }

  @ExceptionHandler(BotAccessDeniedException.class)
  public String handleBotAccessDenied(
      BotAccessDeniedException exception, Model model, HttpServletResponse response) {
    response.setStatus(HttpStatus.FORBIDDEN.value());
    model.addAttribute("errorMessage", exception.getMessage());
    model.addAttribute("errorCode", ErrorConstants.FORBIDDEN_CODE);
    return "error";
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
