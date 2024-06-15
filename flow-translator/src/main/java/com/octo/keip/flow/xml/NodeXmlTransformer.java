package com.octo.keip.flow.xml;

import com.octo.keip.flow.model.eip.EipNode;
import java.util.List;
import java.util.Set;

@FunctionalInterface
public interface NodeXmlTransformer {

  List<XmlElement> apply(EipNode node, Set<EipNode> predecessors, Set<EipNode> successors);
}
