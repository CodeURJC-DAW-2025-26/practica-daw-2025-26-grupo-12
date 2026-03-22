package es.codeurjc.grupo12.scissors_please.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ResponseDto {

  private boolean isError;
  private int statusCode;
  private String message;
  private Object data;
}
