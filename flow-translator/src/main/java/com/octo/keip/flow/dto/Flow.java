package com.octo.keip.flow.dto;

import com.octo.keip.flow.model.EipNode;
import java.util.List;

// TODO: Validate that node ids are unique. (Might want to do this in Graph impl)
// TODO: Validate required fields.
public record Flow(List<EipNode> nodes, List<FlowEdge> edges) {}
