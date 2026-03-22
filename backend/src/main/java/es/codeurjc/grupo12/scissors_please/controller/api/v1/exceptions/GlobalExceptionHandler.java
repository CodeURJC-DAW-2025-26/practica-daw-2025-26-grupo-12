package es.codeurjc.grupo12.scissors_please.controller.api.v1.exceptions;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseDto handleInvalidJson(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    log.error("JSON syntax error at {}: {}", request.getRequestURI(), ex.getMessage());
    return new ResponseDto(
        true, ResponseConstants.BAD_REQUEST_CODE_INT, ResponseConstants.BAD_JSON, null);
  }

  @ExceptionHandler({IllegalArgumentException.class, java.io.IOException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseDto handleImageUploadException(Exception ex, HttpServletRequest request) {
    log.error("Image upload error at {}: {}", request.getRequestURI(), ex.getMessage());
    return new ResponseDto(
        true, ResponseConstants.BAD_REQUEST_CODE_INT, ResponseConstants.IMAGE_ERROR_UPLOAD, null);
  }

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseDto handleElementNotFound(NoSuchElementException ex, HttpServletRequest request) {
    log.error("Element not found at {}: {}", request.getRequestURI(), ex.getMessage());
    return new ResponseDto(
        true, ResponseConstants.NOT_FOUND_CODE_INT, ResponseConstants.ELEMENT_NOT_FOUND, null);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseDto handleGenericException(Exception ex, HttpServletRequest request) {
    log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
    return new ResponseDto(
        true,
        ResponseConstants.INTERNAL_SERVER_ERROR_CODE_INT,
        ResponseConstants.INTERNAL_SERVER_ERROR,
        null);
  }
}
