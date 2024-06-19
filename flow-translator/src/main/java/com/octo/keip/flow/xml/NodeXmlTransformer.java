package com.octo.keip.flow.xml;

import com.octo.keip.flow.model.EipGraph;
import com.octo.keip.flow.model.EipNode;
import java.util.List;

@FunctionalInterface
public interface NodeXmlTransformer {

  List<XmlElement> apply(EipNode node, EipGraph graph);
}
