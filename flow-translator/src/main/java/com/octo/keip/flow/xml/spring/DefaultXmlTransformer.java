package com.octo.keip.flow.xml.spring;

import com.octo.keip.flow.model.eip.EipChild;
import com.octo.keip.flow.model.eip.EipId;
import com.octo.keip.flow.model.eip.EipNode;
import com.octo.keip.flow.model.eip.Role;
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

  private static final EipId DIRECT_CHANNEL = new EipId("integration", "channel");

  @Override
  public List<XmlElement> apply(EipNode node, Set<EipNode> predecessors, Set<EipNode> successors) {
    if (predecessors.size() > 1 || successors.size() > 1) {
      throw new IllegalArgumentException(
          "The default node to xml transformer only handles single input/output components");
    }

    List<XmlElement> elements = new ArrayList<>();
    elements.add(toXmlElement(node, predecessors, successors));

    XmlElement channel = createDownstreamChannel(node, successors);
    if (channel != null) {
      elements.add(channel);
    }

    return elements;
  }

  private XmlElement toXmlElement(
      EipNode node, Set<EipNode> predecessors, Set<EipNode> successors) {
    List<XmlElement> children =
        node.children().stream().map(c -> toXmlElement(c, node.eipId().namespace())).toList();

    Map<String, Object> updatedAttrs = new LinkedHashMap<>();
    updatedAttrs.put(ID, node.id());
    addChannelAttributes(updatedAttrs, node, predecessors, successors);
    updatedAttrs.putAll(node.attributes());

    return new XmlElement(node.eipId().namespace(), node.eipId().name(), updatedAttrs, children);
  }

  // TODO: Make EipChild and EipNode return empty containers rather than null
  private XmlElement toXmlElement(EipChild child, String prefix) {
    List<XmlElement> children =
        child.children().stream().map(c -> this.toXmlElement(c, prefix)).toList();
    return new XmlElement(prefix, child.name(), child.attributes(), children);
  }

  private void addChannelAttributes(
      Map<String, Object> attributes,
      EipNode node,
      Set<EipNode> predecessors,
      Set<EipNode> successors) {
    if (Role.CHANNEL.equals(node.role())) {
      return;
    }

    Optional<EipNode> predecessor = predecessors.stream().findFirst();
    Optional<EipNode> successor = successors.stream().findFirst();

    switch (node.flowType()) {
      case SOURCE -> successor.ifPresent(s -> attributes.put(CHANNEL, getChannelId(node, s)));
      case SINK -> predecessor.ifPresent(p -> attributes.put(CHANNEL, getChannelId(p, node)));
      case PASSTHRU -> {
        predecessor.ifPresent(p -> attributes.put(INPUT_CHANNEL, getChannelId(p, node)));
        successor.ifPresent(s -> attributes.put(OUTPUT_CHANNEL, getChannelId(node, s)));
      }
    }
  }

  private XmlElement createDownstreamChannel(EipNode node, Set<EipNode> successors) {
    if (successors.isEmpty() || Role.CHANNEL.equals(node.role())) {
      return null;
    }

    String channelId = getChannelId(node, successors.iterator().next());
    return new XmlElement(
        DIRECT_CHANNEL.namespace(),
        DIRECT_CHANNEL.name(),
        Map.of(ID, channelId),
        Collections.emptyList());
  }

  // TODO: This will likely be shared by multiple components
  private String getChannelId(EipNode source, EipNode target) {
    if (Role.CHANNEL.equals(source.role())) {
      return source.id();
    } else if (Role.CHANNEL.equals(target.role())) {
      return target.id();
    } else {
      return source.id() + "-" + target.id();
    }
  }
}
