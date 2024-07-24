package com.octo.keip.flow.web.translation;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

import com.octo.keip.flow.dto.Flow;
import javax.xml.transform.TransformerException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
class TranslationController {

  private final TranslationService flowTranslationService;

  public TranslationController(TranslationService flowTranslationService) {
    this.flowTranslationService = flowTranslationService;
  }

  // TODO: Exception handling
  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_XML_VALUE)
  String flowToXml(@RequestBody Flow eipFlow) {
    try {
      return this.flowTranslationService.toXml(eipFlow);
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
  }
}
