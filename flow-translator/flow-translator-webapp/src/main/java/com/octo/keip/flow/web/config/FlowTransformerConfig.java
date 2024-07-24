package com.octo.keip.flow.web.config;

import com.octo.keip.flow.FlowTransformer;
import com.octo.keip.flow.SpringIntegrationFlowTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowTransformerConfig {

  private final NamespaceProps namespaceProps;

  public FlowTransformerConfig(NamespaceProps namespaceProps) {
    this.namespaceProps = namespaceProps;
  }

  @Bean
  public FlowTransformer springIntegrationFlowTransformer() {
    return new SpringIntegrationFlowTransformer(this.namespaceProps.namespaces());
  }
}
