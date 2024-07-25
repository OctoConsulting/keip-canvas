package com.octo.keip.flow.web.translation;

import com.octo.keip.flow.FlowTransformer;
import com.octo.keip.flow.error.TransformationError;
import com.octo.keip.flow.model.Flow;
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

  TranslationResult toXml(Flow flow) {
    StringWriter output = new StringWriter();
    try {
      List<TransformationError> errors = this.flowTransformer.toXml(flow, output);
      return new TranslationResult(output.toString(), errors);
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
  }
}
