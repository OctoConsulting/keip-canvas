package com.octo.keip.flow.dto;

import com.octo.keip.flow.model.EdgeProps.EdgeType;

public record FlowEdge(String id, String source, String target, EdgeType sourceHandle) {

  public FlowEdge(String id, String source, String target) {
    this(id, source, target, EdgeType.DEFAULT);
  }

  @Override
  public EdgeType sourceHandle() {
    return (sourceHandle == null) ? EdgeType.DEFAULT : sourceHandle;
  }
}
