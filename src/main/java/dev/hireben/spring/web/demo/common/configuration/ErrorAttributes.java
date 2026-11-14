package dev.hireben.spring.web.demo.common.configuration;

import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import dev.hireben.spring.web.demo.common.utility.RequestUtil;
import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
final class ErrorAttributes extends DefaultErrorAttributes {

  private final ObjectMapper objectMapper;
  private final MessageSource messageSource;

  @Override
  public Map<String, @Nullable Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {

    String code = "UNCAUGHT";

    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR,
        messageSource.getMessage(
            String.join(".", "common", "error", code.toLowerCase()),
            null,
            webRequest.getLocale()));

    Object attr = webRequest.getAttribute(RequestUtil.ATTRIBUTE_REQUEST_ID, RequestAttributes.SCOPE_REQUEST);

    pd.setProperty("code", code);
    pd.setProperty("id", (attr instanceof String s) ? s : RequestUtil.generateRequestId());

    return objectMapper.convertValue(pd, new TypeReference<Map<String, Object>>() {
    });
  }

}
