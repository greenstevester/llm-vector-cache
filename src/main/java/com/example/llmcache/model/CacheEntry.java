package com.example.llmcache.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheEntry {
  private String prompt;
  private String response;
  private float[] vector;
  private LocalDateTime timestamp;
  private Map<String, Object> metadata;
  private String id;

  public CacheEntry(String prompt, String response, float[] vector) {
    this.prompt = prompt;
    this.response = response;
    this.vector = vector;
    this.timestamp = LocalDateTime.now();
    this.metadata = new HashMap<>();
    this.id = DigestUtils.md5Hex(prompt);
  }
}
