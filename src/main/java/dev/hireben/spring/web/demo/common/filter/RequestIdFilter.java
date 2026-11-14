package dev.hireben.spring.web.demo.common.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.hireben.spring.web.demo.common.utility.RequestUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RequestIdFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String requestId = RequestUtil.generateRequestId();

    log.info(RequestUtil.getLogPrefix(requestId) + "request");

    request.setAttribute(RequestUtil.ATTRIBUTE_REQUEST_ID, requestId);

    filterChain.doFilter(request, response);
  }

}
