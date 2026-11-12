package dev.hireben.spring.web.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import io.jsonwebtoken.JwtParser;

@SpringBootTest
@ActiveProfiles("test")
class DemoApplicationTests {

  @MockitoBean
  JwtParser jwtParser;

  @Test
  void contextLoads() {
  }

}
