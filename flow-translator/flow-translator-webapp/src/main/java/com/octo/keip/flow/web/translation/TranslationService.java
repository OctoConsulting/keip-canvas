package com.octo.keip.flow.web.translation;

import com.octo.keip.flow.FlowTransformer;
import com.octo.keip.flow.error.TransformationError;
import com.octo.keip.flow.model.Flow;
import com.octo.keip.flow.web.error.ApiError;
import java.io.StringWriter;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.springframework.stereotype.Service;

@Service
class TranslationService {

  private final FlowTransformer flowTransformer;

  TranslationService(FlowTransformer flowTransformer) {
    this.flowTransformer = flowTransformer;
  }

  TranslationResponse toXml(Flow flow) {
    StringWriter output = new StringWriter();
    try {
      List<TransformationError> errors = this.flowTransformer.toXml(flow, output);
      return new TranslationResponse(output.toString(), toApiError(errors));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
  }

  private ApiError<TranslationErrorDetail> toApiError(List<TransformationError> errors) {
    if (errors.isEmpty()) {
      return null;
    }

    List<TranslationErrorDetail> errorDetails =
        errors.stream().map(TranslationErrorDetail::from).toList();
    return new ApiError<>(
        "Failed to transform one or more nodes", "PARTIAL_TRANSFORM", errorDetails);
  }
}
