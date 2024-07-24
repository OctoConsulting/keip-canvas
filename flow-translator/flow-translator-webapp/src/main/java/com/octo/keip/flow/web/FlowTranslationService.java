package com.octo.keip.flow.web;

import com.octo.keip.flow.FlowTransformer;
import com.octo.keip.flow.SpringIntegrationFlowTransformer;
import com.octo.keip.flow.dto.Flow;
import com.octo.keip.flow.xml.NamespaceSpec;
import java.io.StringWriter;
import java.util.List;
import javax.xml.transform.TransformerException;
import org.springframework.stereotype.Service;

@Service
class FlowTranslationService {

  // TODO: externalize namespace config
  private final List<NamespaceSpec> namespaces =
      List.of(
          new NamespaceSpec(
              "integration",
              "http://www.springframework.org/schema/integration",
              "https://www.springframework.org/schema/integration/spring-integration.xsd"),
          new NamespaceSpec(
              "jms",
              "http://www.springframework.org/schema/integration/jms",
              "https://www.springframework.org/schema/integration/jms/spring-integration-jms.xsd"));

  private final FlowTransformer flowTransformer;

  // TODO: Use DI instead
  FlowTranslationService() {
    this.flowTransformer = new SpringIntegrationFlowTransformer(namespaces);
  }

  // TODO: Register an error handler
  String toXml(Flow flow) throws TransformerException {
    StringWriter output = new StringWriter();
    this.flowTransformer.toXml(flow, output);
    return output.toString();
  }
}
