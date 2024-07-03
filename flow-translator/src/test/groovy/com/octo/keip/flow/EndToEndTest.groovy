package com.octo.keip.flow

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.octo.keip.flow.dto.Flow
import com.octo.keip.flow.graph.GuavaGraph
import com.octo.keip.flow.xml.spring.IntegrationGraphTransformer
import spock.lang.Specification

import java.nio.file.Path

class EndToEndTest extends Specification {
    private static final NAMESPACES = [
            "beans"      : "http://www.springframework.org/schema/beans",
            "integration": "http://www.springframework.org/schema/integration",
            "jms"        : "http://www.springframework.org/schema/integration/jms"]


    def mapper = JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build()

    def "draft"() {
        given:
        // TODO: Directly create a graph fake
        def flow = mapper.readValue(getFlowJson(), Flow.class)
        def graph = GuavaGraph.from(flow)
        def xmlTransformer = new IntegrationGraphTransformer(NAMESPACES)

        when:
        def xml = xmlTransformer.prettyPrintXml(graph)

        then:
        println xml
    }

    static BufferedReader getFlowJson() {
        Path path = Path.of("tmp").resolve("flowGraph.json")
        return EndToEndTest.class.getClassLoader().getResource(path.toString()).newReader()
    }
}