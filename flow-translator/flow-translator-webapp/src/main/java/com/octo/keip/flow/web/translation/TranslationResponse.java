package com.octo.keip.flow.web.translation;

import com.octo.keip.flow.web.error.ApiError;

record TranslationResponse(String data, ApiError<TranslationErrorDetail> error) {}
