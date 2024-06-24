package com.octo.keip.flow.xml;

import com.octo.keip.flow.model.EipGraph;
import com.octo.keip.flow.model.EipId;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NodeTransformerFactory {

  private final Map<EipId, NodeTransformer> cache = new HashMap<>();

  private final EipGraph graph;

  private NodeTransformer defaultTransformer;

  public NodeTransformerFactory(EipGraph graph) {
    this.graph = graph;
  }

  public void setDefaultTransformer(Function<EipGraph, NodeTransformer> createDefaultTransformer) {
    this.defaultTransformer = createDefaultTransformer.apply(graph);
  }

  public void register(EipId id, Function<EipGraph, NodeTransformer> createTransformer) {
    cache.put(id, createTransformer.apply(this.graph));
  }

  public NodeTransformer getTransformer(EipId id) {
    return cache.getOrDefault(id, defaultTransformer);
  }
}
