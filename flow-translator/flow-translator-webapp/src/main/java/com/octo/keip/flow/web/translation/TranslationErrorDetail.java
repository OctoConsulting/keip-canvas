package com.octo.keip.flow.web.translation;

import com.octo.keip.flow.error.TransformationError;

record TranslationErrorDetail(String nodeId, String message) {

  static TranslationErrorDetail from(TransformationError error) {
    return new TranslationErrorDetail(error.source(), error.exception().getMessage());
  }
}
