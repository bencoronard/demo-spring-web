package dev.hireben.spring.web.demo.common.dto;

import lombok.Builder;

@Builder
public record Client(
    String id) {
}
