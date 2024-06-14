package com.octo.keip.flow.xml;

import java.util.List;
import java.util.Map;

public record XmlElement(
    String prefix, String localName, Map<String, String> attributes, List<XmlElement> children) {}
