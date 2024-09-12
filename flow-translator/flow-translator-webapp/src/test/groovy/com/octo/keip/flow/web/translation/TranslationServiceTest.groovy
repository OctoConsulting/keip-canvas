package com.octo.keip.flow.web.translation

import com.octo.keip.flow.FlowTransformer
import com.octo.keip.flow.error.TransformationError
import com.octo.keip.flow.model.Flow
import spock.lang.Specification

import javax.xml.transform.TransformerException
import java.nio.file.Path

class TranslationServiceTest extends Specification {

    private static final String OUTPUT_XML = "<test>canned</test>"

    FlowTransformer flowTransformer = Stub()

    def translationSvc = new TranslationService(flowTransformer)

    def "error-free transformation -> transformed data plus null error field"() {
        given:
        flowTransformer.toXml(_ as Flow, _ as Writer) >> {
            args ->
                {
                    Writer w = args[1]
                    w.write(OUTPUT_XML)
                    return []
                }
        }

        when:
        def response = translationSvc.toXml(new Flow([], []))

        then:
        response == new TranslationResponse(OUTPUT_XML, null)
    }

    def "transformation with non-critical errors -> transformed partial data plus detailed error field"() {
        given:
        flowTransformer.toXml(_ as Flow, _ as Writer) >> {
            args ->
                {
                    Writer w = args[1]
                    w.write(OUTPUT_XML)
                    return [new TransformationError("node1", new TransformerException("oops"))]
                }
        }

        when:
        def response = translationSvc.toXml(new Flow([], []))

        then:
        with(response) {
            data() == OUTPUT_XML
            error() != null
            error().details().size() == 1
        }
    }

    def "transformation with critical error -> throw runtime exception"() {
        given:
        flowTransformer.toXml(_ as Flow, _ as Writer) >> { throw new TransformerException("oops") }

        when:
        translationSvc.toXml(new Flow([], []))

        then:
        thrown(RuntimeException)
    }

    def "transformation with pretty print -> transformed data is formatted, no errors"() {
        given:
        flowTransformer.toXml(_ as Flow, _ as Writer) >> {
            args ->
                {
                    Writer w = args[1]
                    w.write(readXml("sample-integration-route.xml"))
                    return []
                }
        }

        when:
        def response = translationSvc.toXml(new Flow([], []), true)

        then:
        response == new TranslationResponse(readXml("formatted-sample.xml"), null)
    }

    static String readXml(String filename) {
        Path path = Path.of("xml").resolve(filename)
        return TranslationServiceTest.class.getClassLoader()
                                     .getResource(path.toString()).text
    }
}
