package com.octo.keip.flow.web.translation;

import com.octo.keip.flow.error.TransformationError;
import java.util.List;

record TranslationResult(String data, List<TransformationError> errors) {}
