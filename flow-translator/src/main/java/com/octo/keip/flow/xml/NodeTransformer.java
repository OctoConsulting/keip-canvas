package com.octo.keip.flow.xml;

import com.octo.keip.flow.model.EipNode;
import java.util.List;

/**
 * Implementations of this interface transform an {@link EipNode} into one or more {@link
 * XmlElement}. The transformed elements are then composed together to generate the output XML.
 */
@FunctionalInterface
public interface NodeTransformer {

  List<XmlElement> apply(EipNode node);
}
