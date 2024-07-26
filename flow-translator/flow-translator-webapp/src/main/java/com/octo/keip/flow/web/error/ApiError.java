package com.octo.keip.flow.web.error;

import java.util.Collections;
import java.util.List;

public record ApiError<T>(String message, String type, List<T> details) {

  public static <T> ApiError<T> of(Exception ex) {
    return new ApiError<>(ex.getMessage(), getCause(ex), Collections.emptyList());
  }

  public static <T> ApiError<T> of(Exception ex, List<T> details) {
    return new ApiError<>(ex.getMessage(), getCause(ex), details);
  }

  private static String getCause(Exception ex) {
    return ex.getCause() == null ? "INTERNAL" : ex.getCause().getClass().getSimpleName();
  }
}
