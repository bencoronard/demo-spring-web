package dev.hireben.spring.web.demo.common.annotation;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import dev.hireben.spring.web.demo.common.annotation.api.AuthorizationHeader;
import dev.hireben.spring.web.demo.common.dto.Client;
import dev.hireben.spring.web.demo.common.exception.MissingRequiredHeadersException;
import dev.hireben.spring.web.demo.common.exception.TokenExpiredException;
import dev.hireben.spring.web.demo.common.exception.TokenInactiveException;
import dev.hireben.spring.web.demo.common.exception.TokenInvalidException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
final class AuthorizationHeaderResolver implements HandlerMethodArgumentResolver {

  private final JwtParser parser;

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(AuthorizationHeader.class)
        && parameter.getParameterType().equals(Client.class);
  }

  @Override
  public @Nullable Object resolveArgument(
      MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) throws Exception {

    String header = webRequest.getHeader(HttpHeaders.AUTHORIZATION);

    if (header == null || header.isBlank()) {
      throw new MissingRequiredHeadersException(List.of(HttpHeaders.AUTHORIZATION));
    }

    Claims claims;
    try {
      claims = parser.parseSignedClaims(header.substring("Bearer ".length())).getPayload();
    } catch (Exception e) {
      throw new TokenInvalidException();
    }

    if (claims == null || claims.getSubject() == null || claims.getSubject().isBlank()) {
      throw new TokenInvalidException();
    }

    Date now = Date.from(Instant.now());
    if (claims.getExpiration() != null && claims.getExpiration().before(now)) {
      throw new TokenExpiredException();
    }
    if (claims.getNotBefore() != null && claims.getNotBefore().after(now)) {
      throw new TokenInactiveException();
    }

    return Client.builder()
        .id(claims.getSubject())
        .build();
  }

}
