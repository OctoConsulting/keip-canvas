package com.octo.keip.flow.dto


import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import spock.lang.Specification

import java.nio.file.Path

// TODO: Add more unit tests
class SerializationTest extends Specification {

    def mapper = JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build()

    def "Test Serialization"() {
        when:
        Flow flow = mapper.readValue(getFlowJson(), Flow.class)
        then:
        println flow
    }

    static BufferedReader getFlowJson() {
        Path path = Path.of("tmp").resolve("flowGraph.json")
        return SerializationTest.class.getClassLoader().getResource(path.toString()).newReader()
    }
}
