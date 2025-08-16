package com.example.llmcache.mocks;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.example.llmcache.service.VectorCacheService;

public class MockVectorCacheService extends VectorCacheService {
  private MockFramework.CallTracker callTracker = new MockFramework.CallTracker();
  private String getResult;
  private RuntimeException getException;
  private RuntimeException setException;
  private VectorCacheService.CacheStats statsResult;

  public MockVectorCacheService() {
    super(null, null, null);
  }

  public void setGetResult(String result) {
    this.getResult = result;
    this.getException = null;
  }

  public void setGetException(RuntimeException exception) {
    this.getException = exception;
    this.getResult = null;
  }

  public void setSetException(RuntimeException exception) {
    this.setException = exception;
  }

  public void setStatsResult(VectorCacheService.CacheStats stats) {
    this.statsResult = stats;
  }

  @Override
  public CompletableFuture<Optional<String>> get(String prompt) {
    callTracker.recordCall("get", prompt);
    if (getException != null) {
      return CompletableFuture.failedFuture(getException);
    }
    Optional<String> result = getResult != null ? Optional.of(getResult) : Optional.empty();
    return CompletableFuture.completedFuture(result);
  }

  @Override
  public CompletableFuture<Void> set(String prompt, String response, Map<String, Object> metadata) {
    callTracker.recordCall("set", prompt, response, metadata);
    if (setException != null) {
      return CompletableFuture.failedFuture(setException);
    }
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CacheStats getStats() {
    callTracker.recordCall("getStats");
    return statsResult != null ? statsResult : new CacheStats(0, 0, 0);
  }

  @Override
  public void evictExpired() {
    callTracker.recordCall("evictExpired");
  }

  public MockFramework.CallTracker getCallTracker() {
    return callTracker;
  }
}
