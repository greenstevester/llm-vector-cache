package com.example.llmcache.service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import jakarta.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.llmcache.model.CacheEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;

@Service
@Slf4j
public class VectorCacheService {

  private static final String CACHE_PREFIX = "llm_cache:";
  private static final String VECTOR_INDEX = "llm_vector_idx";

  private final RedisTemplate<String, Object> redisTemplate;
  private final UnderlyingLlmProviderService vectorService;
  private final JedisPool jedisPool;
  private final ObjectMapper objectMapper;

  @Value("${cache.similarity.threshold:0.95}")
  private double similarityThreshold;

  @Value("${cache.ttl.hours:24}")
  private long cacheTtlHours;

  public VectorCacheService(
      RedisTemplate<String, Object> redisTemplate,
      UnderlyingLlmProviderService vectorService,
      JedisPool jedisPool) {
    this.redisTemplate = redisTemplate;
    this.vectorService = vectorService;
    this.jedisPool = jedisPool;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  @PostConstruct
  public void initializeVectorIndex() {
    // Vector index initialization simplified - using basic Redis operations
    log.info("Vector cache service initialized");
  }

  public CompletableFuture<Optional<String>> get(String prompt) {
    // First try exact match
    String exactMatch = getExactMatch(prompt);
    if (exactMatch != null) {
      log.debug("Exact cache hit for prompt");
      return CompletableFuture.completedFuture(Optional.of(exactMatch));
    }

    // Then try semantic similarity search
    return vectorService
        .getVector(prompt)
        .thenCompose(vector -> semanticSearch(prompt, vector))
        .exceptionally(
            ex -> {
              log.error("Error during cache lookup", ex);
              return Optional.empty();
            });
  }

  private String getExactMatch(String prompt) {
    String key = CACHE_PREFIX + DigestUtils.md5Hex(prompt);
    try {
      CacheEntry entry = (CacheEntry) redisTemplate.opsForValue().get(key);
      return entry != null ? entry.getResponse() : null;
    } catch (Exception e) {
      log.error("Error getting exact match", e);
      return null;
    }
  }

  private CompletableFuture<Optional<String>> semanticSearch(
      String queryPrompt, float[] queryVector) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
              return Optional.<String>empty();
            }

            double bestSimilarity = 0.0;
            String bestResponse = null;

            for (String key : keys) {
              try {
                CacheEntry entry = (CacheEntry) redisTemplate.opsForValue().get(key);

                if (entry != null && entry.getVector() != null) {
                  double similarity =
                      vectorService.cosineSimilarity(queryVector, entry.getVector());

                  if (similarity > bestSimilarity && similarity >= similarityThreshold) {
                    bestSimilarity = similarity;
                    bestResponse = entry.getResponse();
                  }
                }
              } catch (Exception e) {
                log.error("Error processing entry in semantic search", e);
              }
            }

            if (bestResponse != null) {
              log.debug("Semantic cache hit with similarity: {}", bestSimilarity);
              return Optional.of(bestResponse);
            }

            return Optional.<String>empty();
          } catch (Exception e) {
            log.error("Error during semantic search", e);
            return Optional.<String>empty();
          }
        });
  }

  public CompletableFuture<Void> set(String prompt, String response, Map<String, Object> metadata) {
    return vectorService
        .getVector(prompt)
        .thenAccept(
            vector -> {
              CacheEntry entry = new CacheEntry(prompt, response, vector);
              if (metadata != null) {
                entry.setMetadata(metadata);
              }

              String key = CACHE_PREFIX + entry.getId();

              try {
                // Store in Redis with TTL
                redisTemplate.opsForValue().set(key, entry, Duration.ofHours(cacheTtlHours));
                log.debug("Cached response for prompt with key: {}", key);
              } catch (Exception e) {
                log.error("Error storing cache entry", e);
              }
            })
        .exceptionally(
            ex -> {
              log.error("Error caching response", ex);
              return null;
            });
  }

  public void evictExpired() {
    log.info("Running cache cleanup...");
    // Implementation for cleanup would go here
    // For now, Redis TTL handles expiration automatically
  }

  public CacheStats getStats() {
    try {
      var connFactory = redisTemplate.getConnectionFactory();
      if (connFactory == null) {
        log.warn("Redis connection factory is not configured. Returning zero stats.");
        return new CacheStats(0, 0, 0);
      }
      try (var conn = connFactory.getConnection()) {
        if (conn == null) {
          log.warn("Could not obtain Redis connection. Returning zero stats.");
          return new CacheStats(0, 0, 0);
        }
        long totalKeys = conn.keyCommands().keys((CACHE_PREFIX + "*").getBytes()).size();
        return new CacheStats(totalKeys, 0, 0);
      } catch (Exception e) {
        log.error("Error getting cache stats from Redis: {}", e.getMessage(), e);
        // Defensive: If Redis unavailable, report zero stats but don't fail
        return new CacheStats(0, 0, 0);
      }
    } catch (Exception ex) {
      log.error("Severe error in getStats: {}", ex.getMessage(), ex);
      return new CacheStats(0, 0, 0);
    }
  }

  @Data
  @AllArgsConstructor
  public static class CacheStats {
    private long totalEntries;
    private long hitCount;
    private long missCount;
  }
}
