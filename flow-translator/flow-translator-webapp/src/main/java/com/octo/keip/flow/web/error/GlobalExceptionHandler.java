package com.octo.keip.flow.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(HttpMessageNotReadableException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<DefaultErrorResponse> malformedInput(HttpMessageNotReadableException ex) {
    return buildResponse(ApiError.of(ex), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<DefaultErrorResponse> illegalArgument(IllegalArgumentException ex) {
    return buildResponse(ApiError.of(ex), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<DefaultErrorResponse> defaultExceptionHandler(Exception ex) {
    return buildResponse(ApiError.of(ex), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public ResponseEntity<DefaultErrorResponse> buildResponse(
      ApiError<Object> error, HttpStatus status) {
    return ResponseEntity.status(status).body(new DefaultErrorResponse(error));
  }
}
