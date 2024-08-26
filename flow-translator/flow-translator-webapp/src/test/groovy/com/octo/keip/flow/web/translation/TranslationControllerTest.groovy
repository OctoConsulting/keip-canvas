package com.octo.keip.flow.web.translation

import com.fasterxml.jackson.databind.json.JsonMapper
import com.octo.keip.flow.model.Flow
import com.octo.keip.flow.web.error.ApiError
import com.octo.keip.flow.web.error.DefaultErrorResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
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

    private static final String OUTPUT_XML = "<test>canned</test>"

    @Autowired
    MockMvc mvc

    @Autowired
    TranslationService translationService

    def "valid flow json to XML -> returns ok response with body"() {
        given:
        def translationResult = new TranslationResponse(OUTPUT_XML, null)
        translationService.toXml(_ as Flow) >> translationResult

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
        def errDetails = new TranslationErrorDetail("node1", "unknown node")
        def err = ApiError.of(new TransformerException("unsupported node type"), [errDetails])
        def translationResult = new TranslationResponse(OUTPUT_XML, err)
        translationService.toXml(_ as Flow) >> translationResult

        expect:
        MvcResult mvcResult = mvc.perform(post("/")
                .contentType(APPLICATION_JSON_VALUE)
                .content(readFlowJson("sample-flow.json")))
                                 .andExpect(status().isInternalServerError())
                                 .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                                 .andReturn()

        verifyTranslationResult(mvcResult, translationResult)
    }

    def "flow json to XML with fatal transformation errors -> returns error response with no body"(Exception ex, int httpStatusCode) {
        given:
        translationService.toXml(_ as Flow) >> { throw ex }

        expect:
        MvcResult mvcResult = mvc.perform(post("/")
                .contentType(APPLICATION_JSON_VALUE)
                .content(readFlowJson("sample-flow.json")))
                                 .andExpect(status().is(httpStatusCode))
                                 .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                                 .andReturn()

        verifyTranslationResult(mvcResult, new TranslationResponse(null, ApiError.of(ex)))

        where:
        ex                                        | httpStatusCode
        new IllegalArgumentException("bad input") | HttpStatus.BAD_REQUEST.value()
        new RuntimeException("unkown")            | HttpStatus.INTERNAL_SERVER_ERROR.value()
    }

    def "malformed flow json -> deserialization error -> returns error response with no body"() {
        given:
        def translationResult = new TranslationResponse(OUTPUT_XML, null)
        translationService.toXml(_ as Flow) >> translationResult

        expect:
        MvcResult mvcResult = mvc.perform(post("/")
                .contentType(APPLICATION_JSON_VALUE)
                .content(readFlowJson("invalid-role-flow.json")))
                                 .andExpect(status().isBadRequest())
                                 .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                                 .andReturn()
        // Verify expected API error format is returned
        MAPPER.readValue(mvcResult.getResponse().getContentAsString(), DefaultErrorResponse.class)
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
