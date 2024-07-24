package com.octo.keip.flow.web.translation;

import com.octo.keip.flow.FlowTransformer;
import com.octo.keip.flow.dto.Flow;
import java.io.StringWriter;
import javax.xml.transform.TransformerException;
import org.springframework.stereotype.Service;

@Service
class TranslationService {

  private final FlowTransformer flowTransformer;

  TranslationService(FlowTransformer flowTransformer) {
    this.flowTransformer = flowTransformer;
  }

  // TODO: Register an error handler
  String toXml(Flow flow) {
    StringWriter output = new StringWriter();
    try {
      this.flowTransformer.toXml(flow, output);
      return output.toString();
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
  }
}
