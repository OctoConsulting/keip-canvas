package com.octo.keip.flow.xml.spring;

import com.octo.keip.flow.model.eip.EipChild;
import com.octo.keip.flow.model.eip.EipNode;
import com.octo.keip.flow.xml.NodeXmlTransformer;
import com.octo.keip.flow.xml.XmlElement;
import java.util.List;
import java.util.Set;

public class DefaultXmlTransformer implements NodeXmlTransformer {
  @Override
  public List<XmlElement> apply(EipNode node, Set<EipNode> predecessors, Set<EipNode> successors) {
    return List.of(toXmlElement(node));
  }

  private XmlElement toXmlElement(EipNode node) {
    List<XmlElement> children = node.children().stream().map(this::toXmlElement).toList();
    return new XmlElement(
        node.eipId().namespace(), node.eipId().name(), node.attributes(), children);
  }

  // TODO: Make EipChild and EipNode return empty containers rather than null
  private XmlElement toXmlElement(EipChild child) {
    List<XmlElement> children = child.children().stream().map(this::toXmlElement).toList();
    return new XmlElement(null, child.name(), child.attributes(), children);
  }
}
