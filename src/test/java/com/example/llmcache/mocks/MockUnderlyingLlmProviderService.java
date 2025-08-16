package com.example.llmcache.mocks;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.example.llmcache.service.UnderlyingLlmProviderService;

public class MockUnderlyingLlmProviderService extends UnderlyingLlmProviderService {
  private MockFramework.CallTracker callTracker = new MockFramework.CallTracker();
  private String generateResponseResult = "Default mock response";
  private RuntimeException generateResponseException;

  public MockUnderlyingLlmProviderService() {
    super(java.util.List.of(new MockLlmProvider("mock", 1536, true)));
  }

  public void setGenerateResponseResult(String result) {
    this.generateResponseResult = result;
    this.generateResponseException = null;
  }

  public void setGenerateResponseException(RuntimeException exception) {
    this.generateResponseException = exception;
    this.generateResponseResult = null;
  }

  @Override
  public CompletableFuture<String> generateResponse(String prompt, Map<String, Object> options) {
    callTracker.recordCall("generateResponse", prompt, options);
    if (generateResponseException != null) {
      return CompletableFuture.failedFuture(generateResponseException);
    }
    return CompletableFuture.completedFuture(generateResponseResult);
  }

  public MockFramework.CallTracker getCallTracker() {
    return callTracker;
  }
}
