package com.example.llmcache.mocks;

import java.util.HashMap;
import java.util.Map;

public class MockFramework {

  public static class CallTracker {
    private Map<String, Integer> callCounts = new HashMap<>();
    private Map<String, Object[]> lastCallArgs = new HashMap<>();

    public void recordCall(String methodName, Object... args) {
      callCounts.put(methodName, callCounts.getOrDefault(methodName, 0) + 1);
      lastCallArgs.put(methodName, args);
    }

    public int getCallCount(String methodName) {
      return callCounts.getOrDefault(methodName, 0);
    }

    public boolean wasCalled(String methodName) {
      return callCounts.getOrDefault(methodName, 0) > 0;
    }

    public boolean wasNeverCalled(String methodName) {
      return !wasCalled(methodName);
    }

    public Object[] getLastCallArgs(String methodName) {
      return lastCallArgs.get(methodName);
    }
  }
}
