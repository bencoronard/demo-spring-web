package dev.hireben.spring.web.demo.common.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.hireben.spring.web.demo.common.property.AuditProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessLogFilter extends OncePerRequestFilter {

  private final AuditProperties properties;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    return excluded(pathWithinApplication(request));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    long start = System.currentTimeMillis();

    log.info("HTTP {} {}", request.getMethod(), pathWithinApplication(request));

    try {
      filterChain.doFilter(request, response);
    } finally {
      log.info("HTTP {} {}ms", response.getStatus(), System.currentTimeMillis() - start);
    }
  }

  private String pathWithinApplication(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String contextPath = request.getContextPath();
    return (contextPath != null && !contextPath.isEmpty() &&
        uri.startsWith(contextPath))
            ? uri.substring(contextPath.length())
            : uri;
  }

  private boolean excluded(String path) {
    for (String prefix : properties.accessLog().exclude()) {
      if (path.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

}
