package org.codice.keip.flow.model;

import org.codice.keip.flow.model.EdgeProps.EdgeType;

public record FlowEdge(String id, String source, String target, EdgeType type) {

  public FlowEdge(String id, String source, String target) {
    this(id, source, target, EdgeType.DEFAULT);
  }

  public EdgeType type() {
    return (type == null) ? EdgeType.DEFAULT : type;
  }
}
