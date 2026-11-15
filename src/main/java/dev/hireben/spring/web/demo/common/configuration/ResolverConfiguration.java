package dev.hireben.spring.web.demo.common.configuration;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.hireben.spring.web.demo.common.annotation.AuthorizationHeaderResolver;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class ResolverConfiguration implements WebMvcConfigurer {

  private final AuthorizationHeaderResolver authHeaderResolver;

  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(authHeaderResolver);
  }

}
