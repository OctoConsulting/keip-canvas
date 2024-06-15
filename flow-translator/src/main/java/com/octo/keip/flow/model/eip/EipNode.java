package com.octo.keip.flow.model.eip;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Represents a specific instantiation of an EIP component in the graph (e.g. a message filter or
// router). Ids must be unique across the flow graph.

// TODO: Are we going to need a builder?
// TODO: Deserialize missing fields as empty containers rather than nulls
public record EipNode(
    String id,
    EipId eipId,
    String label,
    String description,
    Role role,
    Map<String, Object> attributes,
    List<EipChild> children) {

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
