package com.octo.keip.flow.xml

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.octo.keip.flow.dto.Flow
import com.octo.keip.flow.graph.GuavaGraph
import spock.lang.Specification

import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import java.nio.file.Path

class GraphXmlTransformerTest extends Specification {

    private static final NAMESPACES = [
            "beans"      : "http://www.springframework.org/schema/beans",
            "integration": "http://www.springframework.org/schema/integration",
            "jms"        : "http://www.springframework.org/schema/integration/jms"]


    def mapper = JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build()

    def "quick test"() {
        given:
        // TODO: Directly create a graph fake
        def flow = mapper.readValue(getFlowJson(), Flow.class)
        def graph = GuavaGraph.from(flow)
        def xmlTransformer = new GraphXmlTransformer(NAMESPACES)
        def baos = new ByteArrayOutputStream()
        def writer = new BufferedWriter(new OutputStreamWriter(baos))

        when:
        xmlTransformer.toXml(graph, writer)

        then:
        println formatXml(baos)
    }

    // TODO: remove
    static BufferedReader getFlowJson() {
        Path path = Path.of("tmp").resolve("flowGraph.json")
        return GraphXmlTransformerTest.class.getClassLoader().getResource(path.toString()).newReader()
    }

    private static String formatXml(ByteArrayOutputStream byteStream) throws TransformerException {
        var transformerFactory = TransformerFactory.newInstance();
        var transformer = transformerFactory.newTransformer();

        // pretty print by indention
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        // add standalone="yes", add line break before the root element
//        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

        var source = new StreamSource(new ByteArrayInputStream(byteStream.toByteArray()));
        var result = new StringWriter();
        transformer.transform(source, new StreamResult(result));
        return result.toString();
    }

}
