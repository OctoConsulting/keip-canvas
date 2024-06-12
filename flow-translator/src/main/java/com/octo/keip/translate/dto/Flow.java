package com.octo.keip.translate.dto;

import com.octo.keip.translate.model.eip.EipNode;
import java.util.List;

// TODO: Validate that node ids are unique.
public record Flow(List<EipNode> nodes, List<FlowEdge> edges) {}
