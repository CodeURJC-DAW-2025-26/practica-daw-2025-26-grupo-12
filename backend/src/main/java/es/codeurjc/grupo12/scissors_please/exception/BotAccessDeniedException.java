package es.codeurjc.grupo12.scissors_please.exception;

public class BotAccessDeniedException extends RuntimeException {

  public BotAccessDeniedException(String message) {
    super(message);
  }
}
