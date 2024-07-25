package com.octo.keip.flow.web.translation;

import com.octo.keip.flow.error.TransformationError;

record TranslationApiError(String nodeId, String message) {

  static TranslationApiError from(TransformationError error) {
    return new TranslationApiError(error.source(), error.exception().getMessage());
  }
}
