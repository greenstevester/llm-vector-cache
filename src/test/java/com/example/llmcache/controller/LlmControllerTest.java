package com.example.llmcache.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.llmcache.mocks.MockLlmService;
import com.example.llmcache.mocks.MockVectorCacheService;
import com.example.llmcache.service.VectorCacheService;

class LlmControllerTest {

  private MockLlmService mockLLMService;
  private MockVectorCacheService mockCacheService;
  private LLMController controller;

  @BeforeEach
  void setUp() {
    mockLLMService = new MockLlmService();
    mockCacheService = new MockVectorCacheService();
    controller = new LLMController(mockLLMService, mockCacheService);
  }

  @Test
  void shouldGenerateResponseSuccessfully() throws Exception {
    // Given
    LLMController.LLMRequest request = new LLMController.LLMRequest();
    request.setPrompt("What is Spring Boot?");
    request.setOptions(Map.of("model", "gpt-3.5-turbo"));

    String expectedResponse = "Spring Boot is a framework...";
    mockLLMService.setGenerateResponseResult(expectedResponse);

    // When
    ResponseEntity<LLMController.LLMResponse> result = controller.generate(request).get();

    // Then
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(expectedResponse, result.getBody().getContent());
    assertTrue(result.getBody().isSuccess());
    assertTrue(mockLLMService.getCallTracker().wasCalled("generateResponse"));
  }

  @Test
  void shouldHandleServiceErrorsGracefully() throws Exception {
    // Given
    LLMController.LLMRequest request = new LLMController.LLMRequest();
    request.setPrompt("Test");
    request.setOptions(Map.of());

    mockLLMService.setGenerateResponseException(new RuntimeException("Service error"));

    // When
    ResponseEntity<LLMController.LLMResponse> result = controller.generate(request).get();

    // Then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    assertEquals("Error generating response", result.getBody().getContent());
    assertFalse(result.getBody().isSuccess());
  }

  @Test
  void shouldGetCacheStatistics() {
    // Given
    VectorCacheService.CacheStats stats = new VectorCacheService.CacheStats(10, 5, 3);
    mockCacheService.setStatsResult(stats);

    // When
    ResponseEntity<VectorCacheService.CacheStats> result = controller.getCacheStats();

    // Then
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(10, result.getBody().getTotalEntries());
    assertEquals(5, result.getBody().getHitCount());
    assertEquals(3, result.getBody().getMissCount());
    assertTrue(mockCacheService.getCallTracker().wasCalled("getStats"));
  }

  @Test
  void shouldEvictExpiredCacheEntries() {
    // When
    ResponseEntity<Void> result = controller.evictExpired();

    // Then
    assertTrue(mockCacheService.getCallTracker().wasCalled("evictExpired"));
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNull(result.getBody());
  }

  @Test
  void shouldReturnHealthStatus() {
    // When
    ResponseEntity<Map<String, String>> result = controller.health();

    // Then
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals("UP", result.getBody().get("status"));
    assertEquals("LLM Vector Cache", result.getBody().get("service"));
  }

  @Test
  void shouldHandleEmptyOptions() throws Exception {
    // Given
    LLMController.LLMRequest request = new LLMController.LLMRequest();
    request.setPrompt("Test prompt");
    request.setOptions(Map.of());

    String expectedResponse = "Test response";
    mockLLMService.setGenerateResponseResult(expectedResponse);

    // When
    ResponseEntity<LLMController.LLMResponse> result = controller.generate(request).get();

    // Then
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(expectedResponse, result.getBody().getContent());
  }

  @Test
  void shouldTestLLMResponseCreation() {
    // When
    LLMController.LLMResponse response = new LLMController.LLMResponse("Test content", true);

    // Then
    assertEquals("Test content", response.getContent());
    assertTrue(response.isSuccess());
  }

  @Test
  void shouldInitializeRequestOptionsIfNull() {
    // Given
    LLMController.LLMRequest request = new LLMController.LLMRequest();

    // When
    Map<String, Object> options = request.getOptions();

    // Then
    assertNotNull(options);
    assertTrue(options instanceof Map);
    assertTrue(options.isEmpty());
  }
}
