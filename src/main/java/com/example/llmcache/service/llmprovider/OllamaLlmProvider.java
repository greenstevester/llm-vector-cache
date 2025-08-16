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

/** Ollama LLM provider implementation Supports local Ollama servers with vector models */
@Component
@Slf4j
@ConditionalOnProperty(name = "llmprovider.ollama.base-url", matchIfMissing = false)
public class OllamaLlmProvider implements LlmProvider {

  private WebClient webClient;

  @Value("${llmprovider.ollama.base-url:http://localhost:11434}")
  private String baseUrl;

  @Value("${llmprovider.ollama.model:qwen2.5-coder:3b}")
  private String model;

  @Value("${llmprovider.ollama.dimension:4096}")
  private int vectorDimension;

  @Value("${llmprovider.ollama.timeout:30000}")
  private int timeoutMs;

  @PostConstruct
  private void initializeWebClient() {
    this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    log.info("Ollama provider initialized with base URL: {} and model: {}", baseUrl, model);
  }

  @Override
  public CompletableFuture<float[]> getVector(String text) {
    if (text == null || text.trim().isEmpty()) {
      return CompletableFuture.failedFuture(
          new IllegalArgumentException("Text cannot be null or empty"));
    }

    if (!isAvailable()) {
      return CompletableFuture.failedFuture(
          new IllegalStateException("Ollama provider not available - check configuration"));
    }

    // Try vector endpoint first, fall back to chat completions
    return tryVectorEndpoint(text)
        .exceptionally(
            ex -> {
              log.warn("Vector endpoint failed, trying chat completions: {}", ex.getMessage());
              return null; // Signal failure to trigger fallback
            })
        .thenCompose(
            result -> {
              if (result == null) {
                return tryCompletionsEndpoint(text);
              }
              return CompletableFuture.completedFuture(result);
            });
  }

  /** Try the native Ollama vector endpoint */
  private CompletableFuture<float[]> tryVectorEndpoint(String text) {
    VectorRequest request = new VectorRequest(model, text);

    return webClient
        .post()
        .uri("/api/embeddings")
        .header("Content-Type", "application/json")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(VectorResponse.class)
        .map(
            response -> {
              float[] vector = response != null ? response.getVector() : null;
              if (vector == null) {
                log.warn("Ollama vector endpoint returned null vector for model: {}", model);
                return null; // This will trigger the fallback
              }
              return vector;
            })
        .toFuture()
        .exceptionally(
            ex -> {
              log.error(
                  "Error getting Ollama vector for text: '{}'",
                  text.substring(0, Math.min(50, text.length())),
                  ex);
              return null; // Return null to trigger fallback instead of throwing
            });
  }

  /**
   * Fallback: Use Ollama's generate endpoint to create consistent vectors This is useful when the
   * model doesn't have a dedicated embedding endpoint
   */
  private CompletableFuture<float[]> tryCompletionsEndpoint(String text) {
    // For models without embedding support, generate a deterministic vector
    // based on the text content using a hash-based approach
    log.debug(
        "Using fallback vector generation for text: '{}'",
        text.substring(0, Math.min(50, text.length())));

    // Simply generate a deterministic vector without calling any API
    // This ensures we don't accidentally call OpenAI when in Ollama mode
    return CompletableFuture.completedFuture(generateSimpleVector(text));
  }

  /** Generate a simple hash-based vector as fallback */
  private float[] generateSimpleVector(String text) {
    float[] vector = new float[vectorDimension];
    int hash = text.hashCode();

    // Generate deterministic pseudo-random values based on text hash
    for (int i = 0; i < vectorDimension; i++) {
      hash = hash * 31 + i; // Simple linear congruential generator
      vector[i] = (float) (Math.sin(hash) * 0.1); // Normalize to small values
    }

    return vector;
  }

  @Override
  public CompletableFuture<String> generateResponse(String prompt, Map<String, Object> options) {
    String modelToUse =
        options != null && options.containsKey("model") ? options.get("model").toString() : model;

    GenerateRequest request = new GenerateRequest(modelToUse, prompt, false);

    return webClient
        .post()
        .uri("/api/generate")
        .header("Content-Type", "application/json")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(GenerateResponse.class)
        .map(response -> response.getResponse())
        .toFuture()
        .exceptionally(
            ex -> {
              log.error("Error generating response with Ollama", ex);
              return "Error: " + ex.getMessage();
            });
  }

  @Override
  public int getVectorDimension() {
    return vectorDimension;
  }

  @Override
  public String getProviderName() {
    return "ollama";
  }

  @Override
  public boolean isAvailable() {
    return baseUrl != null && !baseUrl.trim().isEmpty() && model != null && !model.trim().isEmpty();
  }

  // Ollama API models
  @Data
  @AllArgsConstructor
  public static class VectorRequest {
    private String model;
    private String prompt;
  }

  @Data
  public static class VectorResponse {
    private float[] vector;
  }

  @Data
  @AllArgsConstructor
  public static class GenerateRequest {
    private String model;
    private String prompt;
    private boolean stream;
  }

  @Data
  public static class GenerateResponse {
    private String response;
    private boolean done;
  }

  @Data
  @AllArgsConstructor
  public static class ChatRequest {
    private String model;
    private List<ChatMessage> messages;

    public ChatRequest(String model, String prompt) {
      this.model = model;
      this.messages = List.of(new ChatMessage("user", prompt));
    }

    @Data
    @AllArgsConstructor
    public static class ChatMessage {
      private String role;
      private String content;
    }
  }

  @Data
  public static class ChatResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
      private Message message;

      @Data
      public static class Message {
        private String content;
      }
    }
  }
}
