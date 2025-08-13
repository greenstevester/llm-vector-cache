package com.example.llmcache.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmbeddingService {
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.api.key}")
    private String openAiApiKey;
    
    @Value("${openai.embedding.model:text-embedding-ada-002}")
    private String embeddingModel;
    
    public EmbeddingService() {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public CompletableFuture<float[]> getEmbedding(String text) {
        EmbeddingRequest request = new EmbeddingRequest(text, embeddingModel);
        
        return webClient.post()
            .uri("/embeddings")
            .header("Authorization", "Bearer " + openAiApiKey)
            .header("Content-Type", "application/json")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(EmbeddingResponse.class)
            .map(response -> response.getData().get(0).getEmbedding())
            .toFuture()
            .thenApply(embedding -> (float[]) embedding)
            .exceptionally(ex -> {
                log.error("Error getting embedding", ex);
                return new float[1536]; // Return empty embedding on error
            });
    }
    
    public double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    @Data
    @AllArgsConstructor
    public static class EmbeddingRequest {
        private String input;
        private String model;
    }
    
    @Data
    public static class EmbeddingResponse {
        private List<EmbeddingData> data;
        
        @Data
        public static class EmbeddingData {
            private float[] embedding;
        }
    }
}
