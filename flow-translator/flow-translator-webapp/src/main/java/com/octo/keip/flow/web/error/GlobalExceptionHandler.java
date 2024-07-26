package com.octo.keip.flow.web.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> malformedInput(HttpMessageNotReadableException ex) {
    return buildResponse(ApiError.of(ex), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> illegalArgument(IllegalArgumentException ex) {
    return buildResponse(ApiError.of(ex), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> defaultExceptionHandler(Exception ex) {
    return buildResponse(ApiError.of(ex), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public ResponseEntity<Object> buildResponse(ApiError<Object> error, HttpStatus status) {
    return ResponseEntity.status(status).body(new DefaultErrorResponse(error));
  }
}
