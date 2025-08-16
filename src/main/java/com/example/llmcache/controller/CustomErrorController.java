package com.example.llmcache.controller;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class CustomErrorController implements ErrorController {

  private static final String ERROR_PATH = "/error";

  @RequestMapping(ERROR_PATH)
  public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
    Map<String, Object> errorDetails = new HashMap<>();

    // Get error status
    Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    String errorMessage = (String) request.getAttribute("jakarta.servlet.error.message");
    String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");

    if (statusCode == null) {
      statusCode = 500;
    }

    HttpStatus status = HttpStatus.valueOf(statusCode);

    errorDetails.put("status", statusCode);
    errorDetails.put("error", status.getReasonPhrase());
    errorDetails.put("message", getErrorMessage(statusCode, errorMessage));
    errorDetails.put("path", requestUri != null ? requestUri : "unknown");
    errorDetails.put("timestamp", java.time.Instant.now().toString());
    errorDetails.put("service", "LLM Vector Cache");

    // Log the error
    log.warn("Error {} {} at path: {}", statusCode, status.getReasonPhrase(), requestUri);

    return new ResponseEntity<>(errorDetails, status);
  }

  private String getErrorMessage(int statusCode, String originalMessage) {
    return switch (statusCode) {
      case 404 -> "The requested resource was not found. Available endpoints: /api/llm/generate,"
          + " /api/llm/health, /api/llm/cache/stats";
      case 400 -> "Bad request. Please check your request parameters.";
      case 401 -> "Authentication required to access this resource.";
      case 403 -> "Access forbidden. You don't have permission to access this resource.";
      case 405 -> "Method not allowed for this endpoint.";
      case 500 -> "Internal server error. Please try again later.";
      case 503 -> "Service temporarily unavailable. Please try again later.";
      default -> originalMessage != null
          ? originalMessage
          : "An error occurred while processing your request.";
    };
  }

  @RequestMapping("/")
  public ResponseEntity<Map<String, Object>> home() {
    Map<String, Object> response = new HashMap<>();
    response.put("service", "LLM Vector Cache");
    response.put("version", "1.0.0");
    response.put("status", "running");
    response.put("description", "LLM Cache with Vector Similarity using Redis and Spring Boot");

    Map<String, Object> endpoints = new HashMap<>();
    endpoints.put("health", "GET /api/llm/health");
    endpoints.put("generate", "POST /api/llm/generate");
    endpoints.put("cache_stats", "GET /api/llm/cache/stats");
    endpoints.put("cache_evict", "POST /api/llm/cache/evict");
    response.put("endpoints", endpoints);

    Map<String, String> documentation = new HashMap<>();
    documentation.put("readme", "See README.md for setup and usage instructions");
    documentation.put(
        "api_docs",
        "POST to /api/llm/generate with {'prompt': 'your question', 'options': {'model':"
            + " 'gpt-3.5-turbo'}}");
    response.put("documentation", documentation);

    return ResponseEntity.ok(response);
  }
}
