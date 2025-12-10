package com.example.gateway;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/internal/hello")
    public Map<String, Object> internalHello() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", "Hello from the gateway itself");
        payload.put("timestamp", Instant.now().toString());
        return payload;
    }

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "Zuul gateway is running");
        payload.put("proxyExample", "/proxy/hello");
        return payload;
    }
}
