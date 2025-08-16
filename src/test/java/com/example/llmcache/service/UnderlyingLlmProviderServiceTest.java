package com.example.llmcache.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.llmcache.mocks.MockLlmProvider;
import com.example.llmcache.service.llmprovider.LlmProvider;

class UnderlyingLlmProviderServiceTest {

  private UnderlyingLlmProviderService service;
  private MockLlmProvider mockProvider1;
  private MockLlmProvider mockProvider2;
  private MockLlmProvider unavailableProvider;

  @BeforeEach
  void setUp() {
    mockProvider1 = new MockLlmProvider("mock1", 1536, true);
    mockProvider2 = new MockLlmProvider("mock2", 4096, true);
    unavailableProvider = new MockLlmProvider("unavailable", 1536, false);

    List<LlmProvider> providers = Arrays.asList(mockProvider1, mockProvider2, unavailableProvider);
    service = new UnderlyingLlmProviderService(providers);
  }

  @Test
  void shouldSelectFirstAvailableProvider() {
    assertEquals("mock1", service.getActiveProvider().getProviderName());
  }

  @Test
  void shouldGenerateVectors() throws ExecutionException, InterruptedException {
    CompletableFuture<float[]> future = service.getVector("test text");
    float[] vector = future.get();

    assertNotNull(vector);
    assertEquals(1536, vector.length); // mock1 dimension
  }

  @Test
  void shouldReturnConsistentVectorsForSameText() throws ExecutionException, InterruptedException {
    String text = "consistent test text";

    CompletableFuture<float[]> future1 = service.getVector(text);
    CompletableFuture<float[]> future2 = service.getVector(text);

    float[] vector1 = future1.get();
    float[] vector2 = future2.get();

    assertArrayEquals(vector1, vector2);
  }

  @Test
  void shouldListAvailableProviders() {
    List<LlmProvider> available = service.getAvailableProviders();

    assertEquals(2, available.size());
    assertTrue(available.stream().anyMatch(p -> "mock1".equals(p.getProviderName())));
    assertTrue(available.stream().anyMatch(p -> "mock2".equals(p.getProviderName())));
    assertFalse(available.stream().anyMatch(p -> "unavailable".equals(p.getProviderName())));
  }

  @Test
  void shouldFailWhenNoProvidersAvailable() {
    // Create service with only unavailable providers
    List<LlmProvider> unavailableProviders = Arrays.asList(unavailableProvider);
    UnderlyingLlmProviderService failingService =
        new UnderlyingLlmProviderService(unavailableProviders);

    assertNull(failingService.getActiveProvider());

    CompletableFuture<float[]> future = failingService.getVector("test");
    assertThrows(ExecutionException.class, future::get);
  }

  @Test
  void shouldCalculateCosineSimilarityCorrectly() {
    // Test identical vectors
    float[] vectorA = {1, 0, 0};
    float[] vectorB = {1, 0, 0};
    double similarity = service.cosineSimilarity(vectorA, vectorB);
    assertEquals(1.0, similarity, 0.001);

    // Test orthogonal vectors
    vectorA = new float[] {1, 0, 0};
    vectorB = new float[] {0, 1, 0};
    similarity = service.cosineSimilarity(vectorA, vectorB);
    assertEquals(0.0, similarity, 0.001);

    // Test opposite vectors
    vectorA = new float[] {1, 0, 0};
    vectorB = new float[] {-1, 0, 0};
    similarity = service.cosineSimilarity(vectorA, vectorB);
    assertEquals(-1.0, similarity, 0.001);
  }

  @Test
  void shouldThrowExceptionForDifferentVectorLengths() {
    float[] vectorA = {1, 2, 3};
    float[] vectorB = {1, 2};

    assertThrows(IllegalArgumentException.class, () -> service.cosineSimilarity(vectorA, vectorB));
  }

  @Test
  void shouldHandleZeroVectors() {
    float[] vectorA = {0, 0, 0};
    float[] vectorB = {1, 2, 3};

    double similarity = service.cosineSimilarity(vectorA, vectorB);
    assertEquals(0.0, similarity, 0.001);
  }

  @Test
  void shouldThrowExceptionForNullVectors() {
    float[] vectorA = {1, 2, 3};
    float[] vectorB = null;

    assertThrows(IllegalArgumentException.class, () -> service.cosineSimilarity(vectorA, vectorB));

    assertThrows(IllegalArgumentException.class, () -> service.cosineSimilarity(null, vectorA));
  }

  @Test
  void shouldThrowExceptionForEmptyVectors() {
    float[] vectorA = {};
    float[] vectorB = {};

    assertThrows(IllegalArgumentException.class, () -> service.cosineSimilarity(vectorA, vectorB));
  }
}
