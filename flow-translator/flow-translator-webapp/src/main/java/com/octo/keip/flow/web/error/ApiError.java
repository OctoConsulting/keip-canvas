package com.octo.keip.flow.web.error;

import java.util.Collections;
import java.util.List;

public record ApiError(String message, String type, List<Object> details) {

  public static ApiError of(Exception ex) {
    return new ApiError(ex.getMessage(), getCause(ex), Collections.emptyList());
  }

  public static ApiError of(Exception ex, List<Object> details) {
    return new ApiError(ex.getMessage(), getCause(ex), details);
  }

  private static String getCause(Exception ex) {
    return ex.getCause() == null ? "INTERNAL" : ex.getCause().getClass().getSimpleName();
  }
}
