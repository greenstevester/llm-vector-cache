package com.example.llmcache.mocks;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.example.llmcache.service.llmprovider.LlmProvider;

/** Mock LLM provider for testing */
public class MockLlmProvider implements LlmProvider {

  private final String name;
  private final int dimension;
  private final boolean available;
  private final float[] mockVector;

  public MockLlmProvider(String name, int dimension, boolean available) {
    this.name = name;
    this.dimension = dimension;
    this.available = available;
    this.mockVector = createMockVector(dimension);
  }

  private float[] createMockVector(int dimension) {
    float[] vector = new float[dimension];
    for (int i = 0; i < dimension; i++) {
      vector[i] = (float) (Math.sin(i) * 0.1); // Deterministic test data
    }
    return vector;
  }

  @Override
  public CompletableFuture<float[]> getVector(String text) {
    if (!available) {
      return CompletableFuture.failedFuture(
          new IllegalStateException("Mock provider not available"));
    }

    // Create vector based on text hash for consistency
    float[] vector = new float[dimension];
    int hash = text.hashCode();
    for (int i = 0; i < dimension; i++) {
      hash = hash * 31 + i;
      vector[i] = (float) (Math.sin(hash) * 0.1);
    }

    return CompletableFuture.completedFuture(vector);
  }

  @Override
  public int getVectorDimension() {
    return dimension;
  }

  @Override
  public String getProviderName() {
    return name;
  }

  @Override
  public boolean isAvailable() {
    return available;
  }

  @Override
  public CompletableFuture<String> generateResponse(String prompt, Map<String, Object> options) {
    if (!available) {
      return CompletableFuture.failedFuture(
          new IllegalStateException("Mock provider not available"));
    }
    return CompletableFuture.completedFuture("Mock response for: " + prompt);
  }
}
