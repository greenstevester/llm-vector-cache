package com.example.llmcache.service.llmprovider;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/** OpenAI LLM provider implementation */
@Component
@Slf4j
@ConditionalOnProperty(name = "llmprovider.openai.api-key", matchIfMissing = false)
public class OpenAILlmProvider implements LlmProvider {

  private WebClient webClient;

  @Value("${llmprovider.openai.api-key:${OPENAI_API_KEY:}}")
  private String apiKey;

  @Value("${llmprovider.openai.model:text-embedding-ada-002}")
  private String model;

  @Value("${llmprovider.openai.base-url:https://api.openai.com/v1}")
  private String baseUrl;

  @PostConstruct
  private void initializeWebClient() {
    this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    log.info("OpenAI provider initialized with base URL: {}", baseUrl);
  }

  @Override
  public CompletableFuture<float[]> getVector(String text) {
    if (text == null || text.trim().isEmpty()) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("Text cannot be null or empty"));
    }

    if (!isAvailable()) {
      return CompletableFuture.failedFuture(
          new IllegalStateException("OpenAI provider not available - missing API key"));
    }

    EmbeddingRequest request = new EmbeddingRequest(text, model);

    return webClient
        .post()
        .uri("/embeddings")
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(EmbeddingResponse.class)
        .map(response -> response.getData().get(0).getEmbedding())
        .toFuture()
        .thenApply(embedding -> (float[]) embedding)
        .exceptionally(
            ex -> {
              log.error(
                  "Error getting OpenAI vector for text: '{}'",
                  text.substring(0, Math.min(50, text.length())),
                  ex);
              throw new RuntimeException("OpenAI vector generation failed", ex);
            });
  }

  @Override
  public CompletableFuture<String> generateResponse(String prompt, Map<String, Object> options) {
    String modelToUse =
        options != null && options.containsKey("model")
            ? options.get("model").toString()
            : "gpt-3.5-turbo";

    ChatRequest request = new ChatRequest(modelToUse, List.of(new ChatMessage("user", prompt)));

    return webClient
        .post()
        .uri("/chat/completions")
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(ChatResponse.class)
        .map(response -> response.getChoices().get(0).getMessage().getContent())
        .toFuture()
        .exceptionally(
            ex -> {
              log.error("Error generating response with OpenAI", ex);
              return "Error: " + ex.getMessage();
            });
  }

  @Override
  public int getVectorDimension() {
    // text-embedding-ada-002 produces 1536-dimensional embeddings
    // text-embedding-3-small produces 1536-dimensional embeddings
    // text-embedding-3-large produces 3072-dimensional embeddings
    return "text-embedding-3-large".equals(model) ? 3072 : 1536;
  }

  @Override
  public String getProviderName() {
    return "openai";
  }

  @Override
  public boolean isAvailable() {
    return apiKey != null && !apiKey.trim().isEmpty();
  }

  @Data
  @AllArgsConstructor
  public static class EmbeddingRequest {
    private String input;
    private String model;
  }

  @Data
  public static class EmbeddingResponse {
    private List<EmbeddingData> data;

    @Data
    public static class EmbeddingData {
      private float[] embedding;
    }
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
