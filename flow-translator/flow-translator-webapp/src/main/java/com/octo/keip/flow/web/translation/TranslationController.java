package com.octo.keip.flow.web.translation;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.octo.keip.flow.model.Flow;
import com.octo.keip.flow.web.error.ApiError;
import java.util.List;
import java.util.Map;
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
    TranslationResult transformed = this.flowTranslationService.toXml(eipFlow);
    if (transformed.errors().isEmpty()) {
      return ResponseEntity.ok(transformed);
    } else {
      var body = Map.of("data", transformed.data(), "error", toApiError(transformed));
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
  }

  private ApiError toApiError(TranslationResult result) {
    List<Object> errors =
        result.errors().stream().map(e -> (Object) TranslationApiError.from(e)).toList();
    return new ApiError("Failed to transform one or more nodes", "PARTIAL_TRANSFORM", errors);
  }
}
