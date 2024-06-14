package com.octo.keip.flow.dto;

import com.octo.keip.flow.model.eip.EipNode;
import java.util.List;

// TODO: Validate that node ids are unique.
public record Flow(List<EipNode> nodes, List<FlowEdge> edges) {}
