package com.octo.keip.flow.xml.spring;

import static com.octo.keip.flow.xml.spring.SpringIntegrationAttrNames.CHANNEL;
import static com.octo.keip.flow.xml.spring.SpringIntegrationAttrNames.DISCARD_CHANNEL;
import static com.octo.keip.flow.xml.spring.SpringIntegrationAttrNames.ID;
import static com.octo.keip.flow.xml.spring.SpringIntegrationAttrNames.INPUT_CHANNEL;
import static com.octo.keip.flow.xml.spring.SpringIntegrationAttrNames.OUTPUT_CHANNEL;

import com.octo.keip.flow.model.ConnectionType;
import com.octo.keip.flow.model.EdgeProps;
import com.octo.keip.flow.model.EdgeProps.EdgeType;
import com.octo.keip.flow.model.EipGraph;
import com.octo.keip.flow.model.EipId;
import com.octo.keip.flow.model.EipNode;
import com.octo.keip.flow.model.Role;
import com.octo.keip.flow.xml.NodeTransformer;
import com.octo.keip.flow.xml.XmlElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DefaultNodeTransformer implements NodeTransformer {

  private static final EipId DIRECT_CHANNEL = new EipId("integration", "channel");

  private final EipGraph graph;

  public DefaultNodeTransformer(EipGraph graph) {
    this.graph = graph;
  }

  @Override
  public List<XmlElement> apply(EipNode node) {
    if (!validate(node, graph.predecessors(node), graph.successors(node))) {
      throw new IllegalArgumentException(
          "The default node to xml transformer only handles single input/output components");
    }

    XmlElement element = BaseNodeTransformation.toXmlElement(node);
    element = updateAttributes(node, element);

    List<XmlElement> elements = new ArrayList<>();
    elements.add(element);
    elements.addAll(createAdditionalElements(node));
    return elements;
  }

  public XmlElement updateAttributes(EipNode node, XmlElement element) {
    Map<String, Object> updatedAttrs = new LinkedHashMap<>();
    updatedAttrs.put(ID, node.id());
    addChannelAttributes(updatedAttrs, node);
    updatedAttrs.putAll(element.attributes());
    return new XmlElement(element.prefix(), element.localName(), updatedAttrs, element.children());
  }

  // TODO: Consider rolling into toXml method
  public List<XmlElement> createAdditionalElements(EipNode node) {
    if (Role.CHANNEL.equals(node.role())) {
      return Collections.emptyList();
    }

    // Create downstream channels
    return this.graph.successors(node).stream()
        .map(s -> getChannelId(node, s))
        .map(
            channelId ->
                new XmlElement(
                    DIRECT_CHANNEL.namespace(),
                    DIRECT_CHANNEL.name(),
                    Map.of(ID, channelId),
                    Collections.emptyList()))
        .toList();
  }

  private void addChannelAttributes(Map<String, Object> attributes, EipNode node) {
    if (Role.CHANNEL.equals(node.role())) {
      return;
    }
    Optional<EipNode> predecessor = this.graph.predecessors(node).stream().findFirst();
    Optional<EipNode> successor = this.graph.successors(node).stream().findFirst();

    switch (node.connectionType()) {
      case PASSTHRU -> {
        predecessor.ifPresent(p -> attributes.put(INPUT_CHANNEL, getChannelId(p, node)));
        successor.ifPresent(s -> attributes.put(OUTPUT_CHANNEL, getChannelId(node, s)));
      }
      case SINK -> predecessor.ifPresent(p -> attributes.put(CHANNEL, getChannelId(p, node)));
      case SOURCE -> successor.ifPresent(s -> attributes.put(CHANNEL, getChannelId(node, s)));
      case TEE -> {
        // Assumes exactly two successors - an output-channel and a discard-channel
        predecessor.ifPresent(p -> attributes.put(INPUT_CHANNEL, getChannelId(p, node)));
        attributes.putAll(getTeeOutgoingChannelAttrs(node));
      }
      default ->
          throw new IllegalStateException("Unexpected connectionType: " + node.connectionType());
    }
  }

  private Map<String, Object> getTeeOutgoingChannelAttrs(EipNode node) {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    for (EipNode successor : this.graph.successors(node)) {
      EdgeType type =
          this.graph.getEdgeProps(node, successor).map(EdgeProps::type).orElse(EdgeType.DEFAULT);
      if (type.equals(EdgeType.DISCARD)) {
        map.put(DISCARD_CHANNEL, getChannelId(node, successor));
      } else {
        map.put(OUTPUT_CHANNEL, getChannelId(node, successor));
      }
    }
    return map;
  }

  // TODO: Might be used by other transformers. Consider extracting.
  public String getChannelId(EipNode source, EipNode target) {
    if (Role.CHANNEL.equals(source.role())) {
      return source.id();
    } else if (Role.CHANNEL.equals(target.role())) {
      return target.id();
    } else {
      return this.graph.getEdgeProps(source, target).map(EdgeProps::id).orElseThrow();
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
