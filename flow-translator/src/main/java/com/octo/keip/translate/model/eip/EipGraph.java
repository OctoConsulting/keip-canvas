package com.octo.keip.translate.model.eip;

import java.util.Set;
import java.util.stream.Stream;

public interface EipGraph {
  Stream<EipNode> traverse();

  Set<EipNode> predecessors(EipNode node);

  Set<EipNode> successors(EipNode node);
}
