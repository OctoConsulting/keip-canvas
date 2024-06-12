package com.octo.keip.translate.graph;

import com.google.common.collect.Streams;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.ImmutableGraph.Builder;
import com.google.common.graph.Traverser;
import com.octo.keip.translate.dto.Flow;
import com.octo.keip.translate.dto.FlowEdge;
import com.octo.keip.translate.model.eip.EipGraph;
import com.octo.keip.translate.model.eip.EipNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

// Guava Graph API is still marked as beta
@SuppressWarnings("UnstableApiUsage")
public class GuavaGraph implements EipGraph {

  private final ImmutableGraph<EipNode> graph;

  private GuavaGraph(ImmutableGraph<EipNode> graph) {
    if (Graphs.hasCycle(graph)) {
      throw new IllegalArgumentException("Graphs with cycles are not allowed");
    }
    this.graph = graph;
  }

  public static GuavaGraph from(Flow flow) {
    Map<String, EipNode> visitedNodes = new HashMap<>();
    Builder<EipNode> builder = GraphBuilder.directed().immutable();
    addNodes(flow.nodes(), builder, visitedNodes);
    addEdges(flow.edges(), builder, visitedNodes);
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

  private static void addNodes(
      List<EipNode> nodes, Builder<EipNode> builder, Map<String, EipNode> visitedNodes) {
    for (EipNode node : nodes) {
      if (visitedNodes.containsKey(node.id())) {
        throw new IllegalArgumentException(String.format("Duplicate node id: %s", node.id()));
      }
      visitedNodes.put(node.id(), node);
      builder.addNode(node);
    }
  }

  private static void addEdges(
      List<FlowEdge> edges, Builder<EipNode> builder, Map<String, EipNode> visitedNodes) {
    for (FlowEdge edge : edges) {
      EipNode source = visitedNodes.get(edge.source());
      EipNode target = visitedNodes.get(edge.target());
      if (source == null || target == null) {
        throw new IllegalArgumentException(String.format("A graph edge is detached: %s", edge));
      }
      builder.putEdge(source, target);
    }
  }
}
