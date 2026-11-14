package dev.hireben.spring.web.demo.common.utility;

import java.util.UUID;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestUtil {

  public final String ATTRIBUTE_REQUEST_ID = "request-id";

  public String generateRequestId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }

  public String getLogPrefix(String id) {
    return "[%s] ".formatted(id);
  }

}
