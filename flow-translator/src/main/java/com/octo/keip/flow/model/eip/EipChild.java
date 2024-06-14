package com.octo.keip.flow.model.eip;

import java.util.List;
import java.util.Map;

public record EipChild(String name, Map<String, Object> attributes, List<EipChild> children) {}
