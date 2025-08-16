package com.example.llmcache.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.llmcache.service.LlmService;
import com.example.llmcache.service.VectorCacheService;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/llm")
@Slf4j
public class LLMController {

  private final LlmService llmService;
  private final VectorCacheService cacheService;

  public LLMController(LlmService llmService, VectorCacheService cacheService) {
    this.llmService = llmService;
    this.cacheService = cacheService;
  }

  @PostMapping("/generate")
  public CompletableFuture<ResponseEntity<LLMResponse>> generate(@RequestBody LLMRequest request) {
    return llmService
        .generateResponse(request.getPrompt(), request.getOptions())
        .thenApply(response -> ResponseEntity.ok(new LLMResponse(response, true)))
        .exceptionally(
            ex -> {
              log.error("Error generating response", ex);
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(new LLMResponse("Error generating response", false));
            });
  }

  @GetMapping("/cache/stats")
  public ResponseEntity<VectorCacheService.CacheStats> getCacheStats() {
    return ResponseEntity.ok(cacheService.getStats());
  }

  @PostMapping("/cache/evict")
  public ResponseEntity<Void> evictExpired() {
    cacheService.evictExpired();
    return ResponseEntity.ok().build();
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> health() {
    Map<String, String> status = new HashMap<>();
    status.put("status", "UP");
    status.put("service", "LLM Vector Cache");
    return ResponseEntity.ok(status);
  }

  @Data
  public static class LLMRequest {
    private String prompt;
    private Map<String, Object> options = new HashMap<>();
  }

  @Data
  @AllArgsConstructor
  public static class LLMResponse {
    private String content;
    private boolean success;
  }
}
