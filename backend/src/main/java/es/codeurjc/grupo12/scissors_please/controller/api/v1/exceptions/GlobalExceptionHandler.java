package es.codeurjc.grupo12.scissors_please.controller.api.v1.exceptions;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ExceptionResponseDto;
import es.codeurjc.grupo12.scissors_please.exception.BotAccessDeniedException;
import es.codeurjc.grupo12.scissors_please.exception.BotImageUploadException;
import es.codeurjc.grupo12.scissors_please.exception.BotNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ExceptionResponseDto> handleInvalidJson(
      HttpMessageNotReadableException ex, HttpServletRequest request) {

    ExceptionResponseDto error =
        new ExceptionResponseDto(ResponseConstants.BAD_JSON, LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(java.io.IOException.class)
  public ResponseEntity<ExceptionResponseDto> handleImageUploadException(
      Exception ex, HttpServletRequest request) {

    ExceptionResponseDto error =
        new ExceptionResponseDto(ResponseConstants.IMAGE_ERROR_UPLOAD, LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(BotImageUploadException.class)
  public ResponseEntity<ExceptionResponseDto> handleBotImageUploadException(
      BotImageUploadException ex, HttpServletRequest request) {

    ExceptionResponseDto error = new ExceptionResponseDto(ex.getMessage(), LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(BotNotFoundException.class)
  public ResponseEntity<ExceptionResponseDto> handleBotNotFound(
      BotNotFoundException ex, HttpServletRequest request) {

    ExceptionResponseDto error = new ExceptionResponseDto(ex.getMessage(), LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(BotAccessDeniedException.class)
  public ResponseEntity<ExceptionResponseDto> handleBotAccessDenied(
      BotAccessDeniedException ex, HttpServletRequest request) {

    ExceptionResponseDto error = new ExceptionResponseDto(ex.getMessage(), LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  @ExceptionHandler(InsufficientAuthenticationException.class)
  public ResponseEntity<ExceptionResponseDto> handleInsufficientAuthentication(
      InsufficientAuthenticationException ex, HttpServletRequest request) {

    ExceptionResponseDto error = new ExceptionResponseDto(ex.getMessage(), LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ExceptionResponseDto> handleIllegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {

    ExceptionResponseDto error = new ExceptionResponseDto(ex.getMessage(), LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ExceptionResponseDto> handleElementNotFound(
      NoSuchElementException ex, HttpServletRequest request) {

    ExceptionResponseDto error =
        new ExceptionResponseDto(ResponseConstants.ELEMENT_NOT_FOUND, LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ExceptionResponseDto> handleMissingParams(
      MissingServletRequestParameterException ex, HttpServletRequest request) {

    ExceptionResponseDto error =
        new ExceptionResponseDto(
            "Missing parameter: " + ex.getParameterName(), LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExceptionResponseDto> handleGenericException(
      Exception ex, HttpServletRequest request) {

    ExceptionResponseDto error =
        new ExceptionResponseDto(ResponseConstants.INTERNAL_SERVER_ERROR, LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
