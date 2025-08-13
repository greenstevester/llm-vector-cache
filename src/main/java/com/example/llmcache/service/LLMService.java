package com.example.llmcache.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class LLMService {
    
    private final VectorCacheService cacheService;
    private final WebClient webClient;
    
    @Value("${openai.api.key}")
    private String openAiApiKey;
    
    public LLMService(VectorCacheService cacheService) {
        this.cacheService = cacheService;
        this.webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .build();
    }
    
    public CompletableFuture<String> generateResponse(String prompt, Map<String, Object> options) {
        return cacheService.get(prompt)
            .thenCompose(cachedResponse -> {
                if (cachedResponse.isPresent()) {
                    log.info("Cache hit for prompt");
                    return CompletableFuture.completedFuture(cachedResponse.get());
                }
                
                log.info("Cache miss, calling LLM");
                return callLLM(prompt, options)
                    .thenCompose(response -> {
                        // Cache the response asynchronously
                        cacheService.set(prompt, response, options);
                        return CompletableFuture.completedFuture(response);
                    });
            });
    }
    
    private CompletableFuture<String> callLLM(String prompt, Map<String, Object> options) {
        ChatRequest request = new ChatRequest(
            options.getOrDefault("model", "gpt-3.5-turbo").toString(),
            List.of(new ChatMessage("user", prompt))
        );
        
        return webClient.post()
            .uri("/chat/completions")
            .header("Authorization", "Bearer " + openAiApiKey)
            .header("Content-Type", "application/json")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(ChatResponse.class)
            .map(response -> response.getChoices().get(0).getMessage().getContent())
            .toFuture()
            .thenApply(content -> (String) content)
            .exceptionally(ex -> {
                log.error("Error calling LLM", ex);
                return "Error generating response";
            });
    }
    
    @Data
    @AllArgsConstructor
    public static class ChatRequest {
        private String model;
        private List<ChatMessage> messages;
    }
    
    @Data
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
    }
    
    @Data
    public static class ChatResponse {
        private List<Choice> choices;
        
        @Data
        public static class Choice {
            private ChatMessage message;
        }
    }
}
