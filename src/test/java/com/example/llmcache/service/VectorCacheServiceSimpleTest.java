package com.example.llmcache.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class VectorCacheServiceSimpleTest {

  @Test
  void shouldTestCacheStatsConstruction() {
    // Test the inner CacheStats class construction
    VectorCacheService.CacheStats stats = new VectorCacheService.CacheStats(10, 5, 3);
    assertEquals(10, stats.getTotalEntries());
    assertEquals(5, stats.getHitCount());
    assertEquals(3, stats.getMissCount());
  }

  @Test
  void shouldCreateVectorCacheServiceWithNullDependencies() {
    // Test that service can be created (though it won't work without dependencies)
    VectorCacheService service = new VectorCacheService(null, null, null);
    assertNotNull(service);
  }
}
