package com.octo.keip.flow.xml;

import java.util.List;
import java.util.Map;

// TODO: Is this intermediary class required?
public record XmlElement(
    String prefix, String localName, Map<String, Object> attributes, List<XmlElement> children) {}
