package com.example.llmcache.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.llmcache.service.llmprovider.LlmProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * UnderlyingLlmProviderService - Unified LLM Provider Facade
 *
 * <p>This service acts as a centralized facade for managing multiple LLM providers (OpenAI, Ollama,
 * local models, etc.) and provides a unified API for vector generation and vector operations.
 *
 * <p>Responsibilities: 1. Provider Management: Discovers, configures, and manages available LLM
 * providers 2. Provider Selection: Automatically selects the best available provider based on
 * configuration 3. Fallback Logic: Gracefully handles provider failures and switches to available
 * alternatives 4. Vector Generation: Delegates vector creation to the active provider 5. Vector
 * Mathematics: Provides utility functions for vector similarity calculations
 *
 * <p>Architecture: - Uses Strategy Pattern with LlmProvider interface - Auto-discovery of providers
 * via Spring's dependency injection - Configuration-driven provider selection with intelligent
 * fallbacks - Stateless design for thread safety and scalability
 *
 * <p>Usage: - VectorCacheService uses this for semantic similarity matching - LlmService integrates
 * this for vector-based caching - Can be extended to support new providers without code changes
 *
 * <p>Configuration: Set 'llmprovider.active' to choose provider (openai, ollama, etc.) Each
 * provider has its own configuration section under 'llmprovider.*'
 *
 * @see LlmProvider for provider implementation interface
 * @see OpenAILlmProvider for OpenAI integration
 * @see OllamaLlmProvider for local Ollama integration
 * @author LLM Vector Cache System
 * @since 1.0.0
 */
@Service
@Slf4j
public class UnderlyingLlmProviderService {

  private final List<LlmProvider> providers;
  private final LlmProvider activeProvider;

  @Value("${llmprovider.active:openai}")
  private String activeProviderName;

  /**
   * Constructor that initializes the provider service with dependency injection.
   *
   * @param providers List of all available LLM providers discovered by Spring
   */
  public UnderlyingLlmProviderService(List<LlmProvider> providers) {
    this.providers = providers;
    this.activeProvider = findActiveProvider();

    log.info(
        "Available LLM providers: {}",
        providers.stream().map(LlmProvider::getProviderName).toList());
    log.info(
        "Active LLM provider: {}",
        activeProvider != null ? activeProvider.getProviderName() : "none");
  }

  /**
   * Finds and selects the active LLM provider based on configuration and availability.
   *
   * <p>Selection Logic: 1. First, tries to find the specifically configured provider
   * (llmprovider.active) 2. If configured provider is not available, falls back to first available
   * provider 3. Returns null if no providers are available
   *
   * @return The selected LlmProvider or null if none available
   */
  private LlmProvider findActiveProvider() {
    // First, try to find the specifically configured provider
    LlmProvider configured =
        providers.stream()
            .filter(p -> p.getProviderName().equals(activeProviderName))
            .filter(LlmProvider::isAvailable)
            .findFirst()
            .orElse(null);

    if (configured != null) {
      return configured;
    }

    // Fall back to first available provider
    LlmProvider fallback =
        providers.stream().filter(LlmProvider::isAvailable).findFirst().orElse(null);

    if (fallback != null) {
      log.warn(
          "Configured provider '{}' not available, using fallback: {}",
          activeProviderName,
          fallback.getProviderName());
    }

    return fallback;
  }

  /**
   * Generates a vector representation for the given text using the active provider.
   *
   * <p>This method delegates to the currently active LLM provider to generate a vector
   * representation of the input text. The vector can be used for semantic similarity matching,
   * clustering, and other vector operations.
   *
   * @param text The input text to generate a vector for
   * @return CompletableFuture containing the vector as float array
   * @throws IllegalStateException if no LLM provider is available
   */
  public CompletableFuture<float[]> getVector(String text) {
    if (activeProvider == null) {
      return CompletableFuture.failedFuture(new IllegalStateException("No LLM provider available"));
    }

    return activeProvider.getVector(text);
  }

  /**
   * Generates a chat/completion response using the active provider.
   *
   * @param prompt The input prompt
   * @param options Additional options (model, temperature, etc.)
   * @return CompletableFuture containing the response text
   * @throws IllegalStateException if no LLM provider is available
   */
  public CompletableFuture<String> generateResponse(String prompt, Map<String, Object> options) {
    if (activeProvider == null) {
      return CompletableFuture.failedFuture(new IllegalStateException("No LLM provider available"));
    }

    return activeProvider.generateResponse(prompt, options);
  }

  /**
   * Gets the currently active LLM provider.
   *
   * <p>Useful for: - Checking provider availability - Getting provider-specific information (name,
   * dimensions, etc.) - Debugging provider selection issues
   *
   * @return The currently active provider, or null if none available
   */
  public LlmProvider getActiveProvider() {
    return activeProvider;
  }

  /**
   * Gets all available (configured and ready) LLM providers.
   *
   * <p>This returns only providers that are properly configured and ready to use, filtering out any
   * providers that are disabled or missing required configuration.
   *
   * @return List of all available LLM providers
   */
  public List<LlmProvider> getAvailableProviders() {
    return providers.stream().filter(LlmProvider::isAvailable).toList();
  }

  /**
   * Calculates cosine similarity between two vectors.
   *
   * <p>Cosine similarity measures the cosine of the angle between two vectors, providing a measure
   * of similarity that is independent of vector magnitude.
   *
   * <p>Results interpretation: - 1.0: Vectors are identical (0 degree angle) - 0.0: Vectors are
   * orthogonal (90 degree angle) or either vector is zero - -1.0: Vectors are opposite (180 degree
   * angle)
   *
   * <p>This is the primary similarity metric used for semantic matching in the cache.
   *
   * @param vectorA First vector
   * @param vectorB Second vector
   * @return Cosine similarity score between -1.0 and 1.0
   * @throws IllegalArgumentException if vectors have different lengths or are null
   */
  public double cosineSimilarity(float[] vectorA, float[] vectorB) {
    if (vectorA == null || vectorB == null) {
      throw new IllegalArgumentException("Vectors cannot be null");
    }
    if (vectorA.length != vectorB.length) {
      throw new IllegalArgumentException("Vectors must have the same length");
    }
    if (vectorA.length == 0) {
      throw new IllegalArgumentException("Vectors cannot be empty");
    }

    double dotProduct = 0.0;
    double normASquared = 0.0;
    double normBSquared = 0.0;

    for (int i = 0; i < vectorA.length; i++) {
      dotProduct += vectorA[i] * vectorB[i];
      normASquared += vectorA[i] * vectorA[i]; // More efficient than Math.pow
      normBSquared += vectorB[i] * vectorB[i];
    }

    double normA = Math.sqrt(normASquared);
    double normB = Math.sqrt(normBSquared);

    // Handle zero vectors gracefully
    if (normA == 0.0 || normB == 0.0) {
      log.warn("Encountered zero vector in cosine similarity calculation");
      return 0.0; // Orthogonal to everything
    }

    return dotProduct / (normA * normB);
  }
}
