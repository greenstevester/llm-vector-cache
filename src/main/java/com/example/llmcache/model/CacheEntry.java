package com.example.llmcache.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheEntry {
    private String prompt;
    private String response;
    private float[] embedding;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;
    private String id;
    
    public CacheEntry(String prompt, String response, float[] embedding) {
        this.prompt = prompt;
        this.response = response;
        this.embedding = embedding;
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
        this.id = DigestUtils.md5Hex(prompt);
    }
}
