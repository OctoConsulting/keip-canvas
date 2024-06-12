package com.octo.keip.translate;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.Traverser;
import java.util.ArrayList;
import java.util.List;

// Guava Graph API is still marked as beta
@SuppressWarnings("UnstableApiUsage")
public class GraphsTest {
  public static void main(String[] args) {
    ImmutableGraph<String> graph =
        GraphBuilder.directed()
            .<String>immutable()
            .addNode("a")
            .addNode("b")
            .addNode("c")
            .addNode("d")
            .addNode("e")
            .addNode("f")
            .putEdge("a", "b")
            .putEdge("a", "c")
            .putEdge("b", "c")
            .putEdge("b", "d")
            .putEdge("c", "d")
            .putEdge("b", "f")
            .putEdge("d", "e")
            .build();

    System.out.println(graph.predecessors("d"));
  }
}
