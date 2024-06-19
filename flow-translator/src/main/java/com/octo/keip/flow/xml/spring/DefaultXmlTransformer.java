package com.octo.keip.flow.xml.spring;

import com.octo.keip.flow.model.ConnectionType;
import com.octo.keip.flow.model.EdgeProps;
import com.octo.keip.flow.model.EdgeProps.EdgeType;
import com.octo.keip.flow.model.EipChild;
import com.octo.keip.flow.model.EipGraph;
import com.octo.keip.flow.model.EipId;
import com.octo.keip.flow.model.EipNode;
import com.octo.keip.flow.model.Role;
import com.octo.keip.flow.xml.NodeXmlTransformer;
import com.octo.keip.flow.xml.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DefaultXmlTransformer implements NodeXmlTransformer {

  // TODO: Check which of these needs to be made configurable/extracted.
  private static final String ID = "id";
  private static final String CHANNEL = "channel";
  private static final String INPUT_CHANNEL = "input-channel";
  private static final String OUTPUT_CHANNEL = "output-channel";

  private static final String DISCARD_CHANNEL = "discard-channel";

  private static final EipId DIRECT_CHANNEL = new EipId("integration", "channel");

  @Override
  public List<XmlElement> apply(EipNode node, EipGraph graph) {
    if (!validate(node, graph.predecessors(node), graph.successors(node))) {
      throw new IllegalArgumentException(
          "The default node to xml transformer only handles single input/output components");
    }

    List<XmlElement> elements = new ArrayList<>();
    elements.add(toXmlElement(node, graph));
    elements.addAll(createDownstreamChannels(node, graph));
    return elements;
  }

  private XmlElement toXmlElement(EipNode node, EipGraph graph) {
    List<XmlElement> children =
        node.children().stream().map(c -> toXmlElement(c, node.eipId().namespace())).toList();

    Map<String, Object> updatedAttrs = new LinkedHashMap<>();
    updatedAttrs.put(ID, node.id());
    addChannelAttributes(updatedAttrs, node, graph);
    updatedAttrs.putAll(node.attributes());

    return new XmlElement(node.eipId().namespace(), node.eipId().name(), updatedAttrs, children);
  }

  private XmlElement toXmlElement(EipChild child, String prefix) {
    List<XmlElement> children =
        child.children().stream().map(c -> this.toXmlElement(c, prefix)).toList();
    return new XmlElement(prefix, child.name(), child.attributes(), children);
  }

  private void addChannelAttributes(Map<String, Object> attributes, EipNode node, EipGraph graph) {
    if (Role.CHANNEL.equals(node.role())) {
      return;
    }
    Optional<EipNode> predecessor = graph.predecessors(node).stream().findFirst();
    Optional<EipNode> successor = graph.successors(node).stream().findFirst();

    switch (node.connectionType()) {
      case PASSTHRU -> {
        predecessor.ifPresent(p -> attributes.put(INPUT_CHANNEL, getChannelId(p, node, graph)));
        successor.ifPresent(s -> attributes.put(OUTPUT_CHANNEL, getChannelId(node, s, graph)));
      }
      case SINK ->
          predecessor.ifPresent(p -> attributes.put(CHANNEL, getChannelId(p, node, graph)));
      case SOURCE ->
          successor.ifPresent(s -> attributes.put(CHANNEL, getChannelId(node, s, graph)));
      case TEE -> {
        // Assumes exactly two successors - an output-channel and a discard-channel
        predecessor.ifPresent(p -> attributes.put(INPUT_CHANNEL, getChannelId(p, node, graph)));
        attributes.putAll(getTeeOutgoingChannelAttrs(node, graph));
      }
      default ->
          throw new IllegalStateException("Unexpected connectionType: " + node.connectionType());
    }
  }

  private Map<String, Object> getTeeOutgoingChannelAttrs(EipNode node, EipGraph graph) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    for (EipNode successor : graph.successors(node)) {
      EdgeType type =
          graph.getEdgeProps(node, successor).map(EdgeProps::type).orElse(EdgeType.DEFAULT);
      if (type.equals(EdgeType.DISCARD)) {
        map.put(DISCARD_CHANNEL, getChannelId(node, successor, graph));
      } else {
        map.put(OUTPUT_CHANNEL, getChannelId(node, successor, graph));
      }
    }
    return map;
  }

  private List<XmlElement> createDownstreamChannels(EipNode node, EipGraph graph) {
    if (Role.CHANNEL.equals(node.role())) {
      return Collections.emptyList();
    }

    return graph.successors(node).stream()
        .map(s -> getChannelId(node, s, graph))
        .map(
            channelId ->
                new XmlElement(
                    DIRECT_CHANNEL.namespace(),
                    DIRECT_CHANNEL.name(),
                    Map.of(ID, channelId),
                    Collections.emptyList()))
        .toList();
  }

  // TODO: This will likely be shared by multiple components
  // TODO: Consider making a ChannelId Record
  private String getChannelId(EipNode source, EipNode target, EipGraph graph) {
    if (Role.CHANNEL.equals(source.role())) {
      return source.id();
    } else if (Role.CHANNEL.equals(target.role())) {
      return target.id();
    } else {
      return graph.getEdgeProps(source, target).map(EdgeProps::id).orElseThrow();
    }
  }

  // This transformer handles components with at most a single input and a single output, with an
  // optional discard channel.
  private boolean validate(EipNode node, Set<EipNode> predecessors, Set<EipNode> successors) {
    boolean atMostOnePredecessor = predecessors.size() <= 1;
    boolean atMostOneSuccessor = successors.size() <= 1;

    if (ConnectionType.TEE.equals(node.connectionType())) {
      return atMostOnePredecessor && successors.size() <= 2;
    }
    return atMostOnePredecessor && atMostOneSuccessor;
  }
}
