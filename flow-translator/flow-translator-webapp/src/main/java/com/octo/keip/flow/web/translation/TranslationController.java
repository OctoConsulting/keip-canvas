package com.octo.keip.flow.web.translation;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.octo.keip.flow.model.Flow;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: Add json validation endpoint
@RestController
@RequestMapping("/")
class TranslationController {

  private final TranslationService flowTranslationService;

  TranslationController(TranslationService flowTranslationService) {
    this.flowTranslationService = flowTranslationService;
  }

  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  ResponseEntity<Object> flowToXml(@RequestBody Flow eipFlow) {
    TranslationResponse response = this.flowTranslationService.toXml(eipFlow);
    if (response.error() == null) {
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}
