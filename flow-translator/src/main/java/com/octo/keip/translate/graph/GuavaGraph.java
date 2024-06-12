package com.octo.keip.translate.graph;

import com.google.common.collect.Streams;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.ImmutableGraph.Builder;
import com.google.common.graph.Traverser;
import com.octo.keip.translate.dto.Flow;
import com.octo.keip.translate.dto.FlowEdge;
import com.octo.keip.translate.model.EipGraph;
import com.octo.keip.translate.model.EipNode;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

// Guava Graph API is still marked as beta
@SuppressWarnings("UnstableApiUsage")
public class GuavaGraph implements EipGraph {

  private final ImmutableGraph<EipNode> graph;

  private GuavaGraph(ImmutableGraph<EipNode> graph) {
    this.graph = graph;
  }

  public static GuavaGraph from(Flow flow) {
    Map<String, EipNode> idToNode = new HashMap<>();
    Builder<EipNode> builder = GraphBuilder.directed().immutable();
    flow.nodes()
        .forEach(
            node -> {
              idToNode.put(node.id(), node);
              builder.addNode(node);
            });

    for (FlowEdge edge : flow.edges()) {
      EipNode source = idToNode.get(edge.source());
      EipNode target = idToNode.get(edge.target());

      if (source == null || target == null) {
        throw new IllegalArgumentException(String.format("A graph edge is detached: %s", edge));
      }
      builder.putEdge(source, target);
    }
    return new GuavaGraph(builder.build());
  }

  @Override
  public Stream<EipNode> traverse() {
    Stream<EipNode> roots = findRoots();
    Iterable<EipNode> nodes = Traverser.forGraph(this.graph).depthFirstPreOrder(roots.toList());
    return Streams.stream(nodes);
  }

  @Override
  public Set<EipNode> predecessors(EipNode node) {
    return this.graph.predecessors(node);
  }

  @Override
  public Set<EipNode> successors(EipNode node) {
    return this.graph.successors(node);
  }

  /** Finds the nodes in the graph that have no incoming edges (indicating the start of a flow). */
  private Stream<EipNode> findRoots() {
    return graph.nodes().stream().filter(node -> graph.inDegree(node) == 0);
  }
}
