package com.example.llmcache.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CacheEntryTest {

  @Test
  void shouldCreateCacheEntryWithDefaultConstructor() {
    // When
    CacheEntry entry = new CacheEntry();

    // Then
    assertNotNull(entry);
    assertNull(entry.getPrompt());
    assertNull(entry.getResponse());
    assertNull(entry.getVector());
    assertNull(entry.getTimestamp());
    assertNull(entry.getMetadata());
    assertNull(entry.getId());
  }

  @Test
  void shouldCreateCacheEntryWithAllArgsConstructor() {
    // Given
    String prompt = "Test prompt";
    String response = "Test response";
    float[] vector = new float[1536];
    LocalDateTime timestamp = LocalDateTime.now();
    Map<String, Object> metadata = Map.of("key", "value");
    String id = "test-id";

    // When
    CacheEntry entry = new CacheEntry(prompt, response, vector, timestamp, metadata, id);

    // Then
    assertEquals(prompt, entry.getPrompt());
    assertEquals(response, entry.getResponse());
    assertEquals(vector, entry.getVector());
    assertEquals(timestamp, entry.getTimestamp());
    assertEquals(metadata, entry.getMetadata());
    assertEquals(id, entry.getId());
  }

  @Test
  void shouldCreateCacheEntryWithConvenienceConstructor() {
    // Given
    String prompt = "What is Java?";
    String response = "Java is a programming language";
    float[] vector = new float[1536];

    // When
    CacheEntry entry = new CacheEntry(prompt, response, vector);

    // Then
    assertEquals(prompt, entry.getPrompt());
    assertEquals(response, entry.getResponse());
    assertEquals(vector, entry.getVector());
    assertNotNull(entry.getTimestamp());
    assertTrue(entry.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    assertTrue(entry.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    assertNotNull(entry.getMetadata());
    assertTrue(entry.getMetadata().isEmpty());
    assertNotNull(entry.getId());
    assertEquals(org.apache.commons.codec.digest.DigestUtils.md5Hex(prompt), entry.getId());
  }

  @Test
  void shouldGenerateConsistentIdFromPrompt() {
    // Given
    String prompt = "Consistent prompt";
    String response = "Response";
    float[] vector = new float[10];

    // When
    CacheEntry entry1 = new CacheEntry(prompt, response, vector);
    CacheEntry entry2 = new CacheEntry(prompt, response, vector);

    // Then
    assertEquals(entry1.getId(), entry2.getId());
    assertEquals(org.apache.commons.codec.digest.DigestUtils.md5Hex(prompt), entry1.getId());
  }

  @Test
  void shouldHaveDifferentIdsForDifferentPrompts() {
    // Given
    float[] vector = new float[10];

    // When
    CacheEntry entry1 = new CacheEntry("Prompt 1", "Response 1", vector);
    CacheEntry entry2 = new CacheEntry("Prompt 2", "Response 2", vector);

    // Then
    assertNotEquals(entry1.getId(), entry2.getId());
  }

  @Test
  void shouldHandleNullVectorInConstructor() {
    // When
    CacheEntry entry = new CacheEntry("prompt", "response", null);

    // Then
    assertNull(entry.getVector());
    assertEquals("prompt", entry.getPrompt());
    assertEquals("response", entry.getResponse());
  }

  @Test
  void shouldTestSettersAndGetters() {
    // Given
    CacheEntry entry = new CacheEntry();
    String prompt = "New prompt";
    String response = "New response";
    float[] vector = new float[100];
    LocalDateTime timestamp = LocalDateTime.now();
    Map<String, Object> metadata = Map.of("test", "data");
    String id = "new-id";

    // When
    entry.setPrompt(prompt);
    entry.setResponse(response);
    entry.setVector(vector);
    entry.setTimestamp(timestamp);
    entry.setMetadata(metadata);
    entry.setId(id);

    // Then
    assertEquals(prompt, entry.getPrompt());
    assertEquals(response, entry.getResponse());
    assertEquals(vector, entry.getVector());
    assertEquals(timestamp, entry.getTimestamp());
    assertEquals(metadata, entry.getMetadata());
    assertEquals(id, entry.getId());
  }

  @Test
  void shouldTestToString() {
    // Given
    CacheEntry entry = new CacheEntry("test prompt", "test response", new float[5]);

    // When
    String stringRepresentation = entry.toString();

    // Then
    assertNotNull(stringRepresentation);
    assertTrue(stringRepresentation.contains("prompt=test prompt"));
    assertTrue(stringRepresentation.contains("response=test response"));
  }

  @Test
  void shouldHandleVeryLongPromptsForIdGeneration() {
    // Given
    String longPrompt = "a".repeat(10000);
    String response = "response";
    float[] vector = new float[10];

    // When
    CacheEntry entry = new CacheEntry(longPrompt, response, vector);

    // Then
    assertNotNull(entry.getId());
    assertEquals(32, entry.getId().length()); // MD5 hex is always 32 characters
  }

  @Test
  void shouldInitializeEmptyMetadataMap() {
    // When
    CacheEntry entry = new CacheEntry("prompt", "response", new float[10]);

    // Then
    assertNotNull(entry.getMetadata());
    assertTrue(entry.getMetadata() instanceof Map);
    assertTrue(entry.getMetadata().isEmpty());
  }

  @Test
  void shouldPreserveMetadataWhenSet() {
    // Given
    CacheEntry entry = new CacheEntry("prompt", "response", new float[10]);
    Map<String, Object> metadata = new HashMap<>();
    metadata.put("key1", "value1");
    metadata.put("key2", 123);
    metadata.put("key3", true);

    // When
    entry.setMetadata(metadata);

    // Then
    assertEquals(metadata, entry.getMetadata());
    assertEquals("value1", entry.getMetadata().get("key1"));
    assertEquals(123, entry.getMetadata().get("key2"));
    assertEquals(true, entry.getMetadata().get("key3"));
  }
}
