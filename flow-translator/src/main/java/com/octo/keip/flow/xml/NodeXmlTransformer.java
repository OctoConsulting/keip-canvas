package com.octo.keip.flow.xml;

import com.octo.keip.flow.model.eip.EipNode;
import java.util.Set;

public interface NodeXmlTransformer {

  XmlElement apply(EipNode node, Set<EipNode> predecessors, Set<EipNode> successors);
}
