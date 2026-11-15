package dev.hireben.spring.web.demo.common.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import dev.hireben.spring.web.demo.common.exception.api.ApplicationException;
import dev.hireben.spring.web.demo.common.utility.ExceptionUtil;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
final class CommonExceptionHandler extends ResponseEntityExceptionHandler {

  private final Tracer tracer;

  @Override
  protected ProblemDetail createProblemDetail(
      Exception ex,
      HttpStatusCode status,
      String defaultDetail,
      @Nullable String detailMessageCode,
      Object @Nullable [] detailMessageArguments,
      WebRequest request) {

    String code = "UNHANDLED";

    if (ex instanceof ApplicationException appEx) {
      code = appEx.getCode();
    }

    ProblemDetail pd = super.createProblemDetail(
        ex,
        status,
        defaultDetail,
        detailMessageCode,
        detailMessageArguments,
        request);

    pd.setProperty("code", code);

    TraceContext context = tracer.currentTraceContext().context();
    if (context != null) {
      pd.setProperty("trace", context.traceId());
    }

    return pd;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<Object> handleConstraintViolationException(
      ConstraintViolationException ex,
      WebRequest request) {

    Collection<Map<String, String>> errors = ex.getConstraintViolations().stream()
        .map(violation -> Map.of(
            "field", clientFieldName(violation.getPropertyPath()),
            "message", violation.getMessage()))
        .toList();

    HttpStatus status = HttpStatus.BAD_REQUEST;

    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
        status,
        getMessageSource().getMessage("common.error.validation_default",
            null,
            request.getLocale()));
    pd.setProperty("errors", errors);

    return super.createResponseEntity(pd, HttpHeaders.EMPTY, status, request);
  }

  @ExceptionHandler(ApplicationException.class)
  ResponseEntity<Object> handleApplicationException(
      ApplicationException ex,
      HttpServletRequest request,
      HttpServletResponse response) {

    if (response.isCommitted()) {
      throw ex;
    }

    ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);

    HttpStatus status = responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;

    WebRequest webRequest = new ServletWebRequest(request, response);

    ProblemDetail pd = createProblemDetail(
        ex,
        status,
        null,
        ExceptionUtil.errorMessageSourceKey(ex.getMessageCodePrefix(), ex.getCode().toLowerCase()),
        ex.getArguments(),
        webRequest);

    return super.createResponseEntity(pd, HttpHeaders.EMPTY, status, webRequest);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<Object> catchAllException(
      Exception ex,
      WebRequest request) {

    String code = "UNCAUGHT";

    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    ProblemDetail pd = createProblemDetail(
        ex,
        status,
        null,
        String.join(".", "common", "error", code.toLowerCase()),
        null,
        request);

    log.error("Unhandle exception:", ex);

    return super.createResponseEntity(pd, HttpHeaders.EMPTY, status, request);
  }

  private static String clientFieldName(Path path) {
    List<String> names = new ArrayList<>();
    for (Path.Node node : path) {
      ElementKind kind = node.getKind();
      if (kind == ElementKind.PARAMETER || kind == ElementKind.PROPERTY) {
        names.add(node.getName());
      }
    }
    return String.join(".", names);
  }

}
