package es.codeurjc.grupo12.scissors_please.controller.api.v1.exceptions;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ResponseDto;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseDto handleInvalidFormat(HttpMessageNotReadableException ex) {
    return new ResponseDto(
        true, ResponseConstants.BAD_REQUEST_CODE_INT, ResponseConstants.IMAGE_ERROR_UPLOAD, null);
  }

  @ExceptionHandler({IllegalArgumentException.class, java.io.IOException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseDto handleImageUploadException(Exception ex) {
    return new ResponseDto(
        true, ResponseConstants.BAD_REQUEST_CODE_INT, ResponseConstants.IMAGE_ERROR_UPLOAD, null);
  }

  @ExceptionHandler({NoSuchElementException.class, java.io.IOException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseDto elementNotFound(Exception ex) {
    return new ResponseDto(
        true, ResponseConstants.NOT_FOUND_CODE_INT, ResponseConstants.ELEMENT_NOT_FOUND, null);
  }
}
