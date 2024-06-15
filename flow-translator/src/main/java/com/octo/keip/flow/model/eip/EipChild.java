package com.octo.keip.flow.model.eip;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record EipChild(String name, Map<String, Object> attributes, List<EipChild> children) {
  @Override
  public Map<String, Object> attributes() {
    if (attributes == null) {
      return Collections.emptyMap();
    }
    return attributes;
  }

  @Override
  public List<EipChild> children() {
    if (children == null) {
      return Collections.emptyList();
    }
    return children;
  }
}
