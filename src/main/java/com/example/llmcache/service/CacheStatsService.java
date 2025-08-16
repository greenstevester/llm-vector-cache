package com.example.llmcache.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.llmcache.service.VectorCacheService.CacheStats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for calculating comprehensive cache statistics and cost savings
 *
 * <p>Tracks cache performance metrics and estimates cost savings based on OpenAI API pricing for
 * avoided requests.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CacheStatsService {

  private final VectorCacheService vectorCacheService;

  // Runtime counters for cache hits/misses
  private final AtomicLong totalRequests = new AtomicLong(0);
  private final AtomicLong cacheHits = new AtomicLong(0);
  private final AtomicLong cacheMisses = new AtomicLong(0);

  // OpenAI pricing (as of 2024) - per 1K tokens
  @Value("${openai.pricing.input-tokens-per-1k:0.0015}")
  private double inputTokenPricePer1k;

  @Value("${openai.pricing.output-tokens-per-1k:0.002}")
  private double outputTokenPricePer1k;

  @Value("${openai.pricing.embedding-tokens-per-1k:0.0001}")
  private double embeddingTokenPricePer1k;

  // Estimated average token counts
  @Value("${cache.stats.avg-input-tokens:50}")
  private int avgInputTokens;

  @Value("${cache.stats.avg-output-tokens:150}")
  private int avgOutputTokens;

  @Value("${cache.stats.avg-embedding-tokens:50}")
  private int avgEmbeddingTokens;

  /** Records a cache hit */
  public void recordCacheHit() {
    totalRequests.incrementAndGet();
    cacheHits.incrementAndGet();
    log.debug("Cache hit recorded. Total: {}, Hits: {}", totalRequests.get(), cacheHits.get());
  }

  /** Records a cache miss */
  public void recordCacheMiss() {
    totalRequests.incrementAndGet();
    cacheMisses.incrementAndGet();
    log.debug("Cache miss recorded. Total: {}, Misses: {}", totalRequests.get(), cacheMisses.get());
  }

  /** Gets comprehensive cache statistics including cost savings */
  public CompletableFuture<CacheStatistics> getCacheStatistics() {
    return CompletableFuture.supplyAsync(
        () -> {
          CacheStats basicStats = vectorCacheService.getStats();

          long currentHits = cacheHits.get();
          long currentMisses = cacheMisses.get();
          long currentTotal = totalRequests.get();

          // Calculate hit rate
          double hitRate = currentTotal > 0 ? (double) currentHits / currentTotal * 100 : 0.0;

          // Calculate cost savings
          CostSavings costSavings = calculateCostSavings(currentHits);

          return new CacheStatistics(
              basicStats.getTotalEntries(),
              currentHits,
              currentMisses,
              currentTotal,
              hitRate,
              costSavings);
        });
  }

  /** Calculates cost savings based on cache hits */
  private CostSavings calculateCostSavings(long cacheHits) {
    if (cacheHits == 0) {
      return new CostSavings(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    // Calculate cost per request to OpenAI
    BigDecimal inputCostPerRequest =
        BigDecimal.valueOf(avgInputTokens)
            .multiply(BigDecimal.valueOf(inputTokenPricePer1k))
            .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

    BigDecimal outputCostPerRequest =
        BigDecimal.valueOf(avgOutputTokens)
            .multiply(BigDecimal.valueOf(outputTokenPricePer1k))
            .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

    BigDecimal embeddingCostPerRequest =
        BigDecimal.valueOf(avgEmbeddingTokens)
            .multiply(BigDecimal.valueOf(embeddingTokenPricePer1k))
            .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

    BigDecimal totalCostPerRequest =
        inputCostPerRequest.add(outputCostPerRequest).add(embeddingCostPerRequest);

    // Calculate total savings
    BigDecimal totalSavings =
        totalCostPerRequest
            .multiply(BigDecimal.valueOf(cacheHits))
            .setScale(4, RoundingMode.HALF_UP);

    // Calculate daily savings rate (total savings divided by days the service has been running)
    // For simplicity, assume 1 day of operation for now - this could be enhanced to track actual
    // uptime
    BigDecimal dailySavingsRate = totalSavings; // Simplified assumption

    // Estimate monthly savings (daily rate * 30)
    BigDecimal monthlySavings = dailySavingsRate.multiply(BigDecimal.valueOf(30));

    // Estimate yearly savings (daily rate * 365)
    BigDecimal yearlySavings = dailySavingsRate.multiply(BigDecimal.valueOf(365));

    return new CostSavings(totalCostPerRequest, totalSavings, monthlySavings, yearlySavings);
  }

  /** Comprehensive cache statistics with cost analysis */
  @Data
  @AllArgsConstructor
  public static class CacheStatistics {
    private long totalCacheEntries;
    private long cacheHits;
    private long cacheMisses;
    private long totalRequests;
    private double hitRatePercentage;
    private CostSavings costSavings;
  }

  /** Cost savings analysis */
  @Data
  @AllArgsConstructor
  public static class CostSavings {
    private BigDecimal costPerRequest;
    private BigDecimal totalSaved;
    private BigDecimal estimatedMonthlySavings;
    private BigDecimal estimatedYearlySavings;
  }
}
