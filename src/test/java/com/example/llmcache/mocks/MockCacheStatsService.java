package com.example.llmcache.mocks;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import com.example.llmcache.service.CacheStatsService;

public class MockCacheStatsService extends CacheStatsService {
  private MockFramework.CallTracker callTracker = new MockFramework.CallTracker();
  private CacheStatistics mockStatistics;

  public MockCacheStatsService() {
    super(null);
    // Set default mock statistics
    this.mockStatistics =
        new CacheStatistics(
            100L, // totalCacheEntries
            80L, // cacheHits
            20L, // cacheMisses
            100L, // totalRequests
            80.0, // hitRatePercentage
            new CostSavings(
                BigDecimal.valueOf(0.003), // costPerRequest
                BigDecimal.valueOf(0.24), // totalSaved
                BigDecimal.valueOf(7.20), // estimatedMonthlySavings
                BigDecimal.valueOf(87.60) // estimatedYearlySavings
                ));
  }

  @Override
  public void recordCacheHit() {
    callTracker.recordCall("recordCacheHit");
  }

  @Override
  public void recordCacheMiss() {
    callTracker.recordCall("recordCacheMiss");
  }

  @Override
  public CompletableFuture<CacheStatistics> getCacheStatistics() {
    callTracker.recordCall("getCacheStatistics");
    return CompletableFuture.completedFuture(mockStatistics);
  }

  public void setMockStatistics(CacheStatistics statistics) {
    this.mockStatistics = statistics;
  }

  public MockFramework.CallTracker getCallTracker() {
    return callTracker;
  }
}
