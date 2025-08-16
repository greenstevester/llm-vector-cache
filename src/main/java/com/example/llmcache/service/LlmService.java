package com.example.llmcache.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LlmService {

  private final VectorCacheService cacheService;
  private final CacheStatsService cacheStatsService;
  private final UnderlyingLlmProviderService llmProviderService;

  public LlmService(
      VectorCacheService cacheService,
      CacheStatsService cacheStatsService,
      UnderlyingLlmProviderService llmProviderService) {
    this.cacheService = cacheService;
    this.cacheStatsService = cacheStatsService;
    this.llmProviderService = llmProviderService;
  }

  public CompletableFuture<String> generateResponse(String prompt, Map<String, Object> options) {
    return cacheService
        .get(prompt)
        .thenCompose(
            cachedResponse -> {
              if (cachedResponse.isPresent()) {
                log.info("Cache hit for prompt");
                cacheStatsService.recordCacheHit();
                return CompletableFuture.completedFuture(cachedResponse.get());
              }

              log.info("Cache miss, calling LLM");
              cacheStatsService.recordCacheMiss();
              return callLLM(prompt, options)
                  .thenCompose(
                      response -> {
                        // Cache the response asynchronously
                        cacheService.set(prompt, response, options);
                        return CompletableFuture.completedFuture(response);
                      });
            });
  }

  private CompletableFuture<String> callLLM(String prompt, Map<String, Object> options) {
    // Delegate to the active LLM provider
    return llmProviderService
        .generateResponse(prompt, options)
        .exceptionally(
            ex -> {
              log.error("Error calling LLM", ex);
              return "Error generating response: " + ex.getMessage();
            });
  }

  @Data
  @AllArgsConstructor
  public static class ChatRequest {
    private String model;
    private List<ChatMessage> messages;
  }

  @Data
  @AllArgsConstructor
  public static class ChatMessage {
    private String role;
    private String content;
  }

  @Data
  public static class ChatResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
      private ChatMessage message;
    }
  }
}
