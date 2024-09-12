package com.octo.keip.flow.web.config;

import com.octo.keip.flow.xml.NamespaceSpec;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "keip")
@Validated
public record NamespaceProps(@NotEmpty List<NamespaceSpec> namespaceMappings) {}
