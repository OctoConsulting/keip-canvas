package com.octo.keip.flow.web.translation

import com.fasterxml.jackson.databind.json.JsonMapper
import com.octo.keip.flow.web.error.ApiError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import javax.xml.transform.TransformerException
import java.nio.file.Path

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = TranslationController)
class TranslationControllerTest extends Specification {

    private static final JsonMapper MAPPER = JsonMapper.builder().build()

    @Autowired
    MockMvc mvc

    @Autowired
    TranslationService translationService

    def "valid flow json to XML -> returns ok response with body"() {
        given:
        def outputXml = "<test>canned</test>"
        def translationResult = new TranslationResponse(outputXml, null)
        translationService.toXml(_) >> translationResult
        expect:
        MvcResult mvcResult = mvc.perform(post("/")
                .contentType(APPLICATION_JSON_VALUE)
                .content(readFlowJson("sample-flow.json")))
                                 .andExpect(status().isOk())
                                 .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                                 .andReturn()

        verifyTranslationResult(mvcResult, translationResult)
    }

    def "flow json to XML with non-critical transformation errors -> returns error response with partial body"() {
        given:
        def outputXml = "<test>canned</test>"
        def errDetails = new TranslationErrorDetail("node1", "unknown node")
        def err = ApiError.of(new TransformerException("unsupported node type"), [errDetails])
        def translationResult = new TranslationResponse(outputXml, err)
        translationService.toXml(_) >> translationResult
        expect:
        MvcResult mvcResult = mvc.perform(post("/")
                .contentType(APPLICATION_JSON_VALUE)
                .content(readFlowJson("sample-flow.json")))
                                 .andExpect(status().isInternalServerError())
                                 .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                                 .andReturn()

        verifyTranslationResult(mvcResult, translationResult)
    }

    // TODO: Test different exception types
    def "flow json to XML with fatal transformation errors -> returns error response with no body"() {
        given:
        Exception ex = new IllegalArgumentException("bad input")
        translationService.toXml(_) >> { throw ex }
        expect:
        MvcResult mvcResult = mvc.perform(post("/")
                .contentType(APPLICATION_JSON_VALUE)
                .content(readFlowJson("sample-flow.json")))
                                 .andExpect(status().isBadRequest())
                                 .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                                 .andReturn()

        verifyTranslationResult(mvcResult, new TranslationResponse(null, ApiError.of(ex)))
    }

    static void verifyTranslationResult(MvcResult actual, Object expected) {
        def responseJson = actual.getResponse().getContentAsString()
        assert MAPPER.readValue(responseJson, TranslationResponse.class) == expected
    }

    static String readFlowJson(String filename) {
        Path path = Path.of("json").resolve(filename)
        return TranslationControllerTest.class.getClassLoader()
                                        .getResource(path.toString()).text
    }

    @TestConfiguration
    static class MockConfig {
        def detachedMockFactory = new DetachedMockFactory()

        @Bean
        TranslationService translationService() {
            return detachedMockFactory.Stub(TranslationService)
        }
    }
}
