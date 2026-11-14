package dev.hireben.spring.web.demo.callback;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.hireben.spring.web.demo.common.annotation.api.AuthorizationHeader;
import dev.hireben.spring.web.demo.common.dto.Client;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/callbacks")
final class CallbackController {

  @GetMapping
  ResponseEntity<Slice<String>> listCallbacks(
      @AuthorizationHeader Client client,
      @PageableDefault(page = 0, size = 20) Pageable pageable) {

    throw new RuntimeException();

    // Slice<String> callbacks = new SliceImpl<>(List.of("http://a.com/cb",
    // "http://b.com/cb"), pageable, false);

    // return ResponseEntity.ok(callbacks);
  }

}
