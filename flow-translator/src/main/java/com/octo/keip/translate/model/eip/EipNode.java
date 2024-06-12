package com.octo.keip.translate.model.eip;

import java.util.List;
import java.util.Map;
import java.util.Objects;

// Represents a specific instantiation of an EIP component in the graph (e.g. a message filter or
// router). Ids must be unique across the flow graph.
public record EipNode(
    String id,
    EipId eipId,
    String label,
    String description,
    Role role,
    Map<String, Object> attributes,
    List<EipChild> children) {
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EipNode eipNode)) {
      return false;
    }
    return Objects.equals(id, eipNode.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return id;
  }
}
