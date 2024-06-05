package model;

import java.util.Map;

public record EipComponent(String id, EipId eipId, Map<String, Object> attributes) {}
