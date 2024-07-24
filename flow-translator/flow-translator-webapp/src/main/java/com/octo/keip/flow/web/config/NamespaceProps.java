package com.octo.keip.flow.web.config;

import com.octo.keip.flow.xml.NamespaceSpec;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keip")
public record NamespaceProps(List<NamespaceSpec> namespaces) {}
