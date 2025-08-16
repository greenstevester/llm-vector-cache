package com.example.llmcache.service.llmprovider;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Interface for different LLM providers (OpenAI, Ollama, etc.) */
public interface LlmProvider {

  /**
   * Generate vector representations for the given text
   *
   * @param text The input text to generate vectors for
   * @return A CompletableFuture containing the vector array
   */
  CompletableFuture<float[]> getVector(String text);

  /**
   * Generate a chat/completion response for the given prompt
   *
   * @param prompt The input prompt
   * @param options Additional options (model, temperature, etc.)
   * @return A CompletableFuture containing the response text
   */
  CompletableFuture<String> generateResponse(String prompt, Map<String, Object> options);

  /**
   * Get the dimension size of vectors produced by this provider
   *
   * @return The vector dimension
   */
  int getVectorDimension();

  /**
   * Get the name/identifier of this provider
   *
   * @return Provider name
   */
  String getProviderName();

  /**
   * Check if this provider is available/configured
   *
   * @return true if provider is ready to use
   */
  boolean isAvailable();
}
