package com.octo.keip.flow.web.translation;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.octo.keip.flow.dto.Flow;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: Add json validation endpoint
@RestController
@RequestMapping("/")
class TranslationController {

  private final TranslationService flowTranslationService;

  public TranslationController(TranslationService flowTranslationService) {
    this.flowTranslationService = flowTranslationService;
  }

  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  Map<String, Object> flowToXml(@RequestBody Flow eipFlow) {
    String xml = this.flowTranslationService.toXml(eipFlow);
    return Map.of("data", xml);
  }
}
