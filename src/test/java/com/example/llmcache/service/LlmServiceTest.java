package com.example.llmcache.service;

import com.example.llmcache.mocks.MockVectorCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LLMServiceTest {

    private MockVectorCacheService mockCacheService;
    private LLMService service;

    @BeforeEach
    void setUp() {
        mockCacheService = new MockVectorCacheService();
        service = new LLMService(mockCacheService);
    }

    @Test
    void shouldReturnCachedResponseWhenAvailable() throws Exception {
        // Given
        String prompt = "What is Java?";
        String cachedResponse = "Java is a programming language";
        Map<String, Object> options = Map.of("model", "gpt-3.5-turbo");
        
        mockCacheService.setGetResult(cachedResponse);

        // When
        String result = service.generateResponse(prompt, options).get();

        // Then
        assertEquals(cachedResponse, result);
        assertTrue(mockCacheService.getCallTracker().wasCalled("get"));
        assertTrue(mockCacheService.getCallTracker().wasNeverCalled("set"));
    }

    @Test
    void shouldTestServiceConstruction() {
        // Test that service can be constructed with a cache service
        assertNotNull(service);
        assertNotNull(mockCacheService);
    }

    @Test
    void shouldTestChatMessageConstruction() {
        // Test the inner classes can be constructed
        LLMService.ChatMessage message = new LLMService.ChatMessage("user", "test content");
        assertEquals("user", message.getRole());
        assertEquals("test content", message.getContent());
    }
    
    @Test
    void shouldTestChatRequestConstruction() {
        // Test the request object construction
        LLMService.ChatMessage message = new LLMService.ChatMessage("user", "test");
        LLMService.ChatRequest request = new LLMService.ChatRequest("gpt-3.5-turbo", java.util.List.of(message));
        assertEquals("gpt-3.5-turbo", request.getModel());
        assertEquals(1, request.getMessages().size());
    }

    @Test
    void shouldTestChatResponseConstruction() {
        // Test the response object construction
        LLMService.ChatResponse response = new LLMService.ChatResponse();
        LLMService.ChatResponse.Choice choice = new LLMService.ChatResponse.Choice();
        LLMService.ChatMessage message = new LLMService.ChatMessage("assistant", "response");
        choice.setMessage(message);
        response.setChoices(java.util.List.of(choice));
        
        assertEquals(1, response.getChoices().size());
        assertEquals("assistant", response.getChoices().get(0).getMessage().getRole());
        assertEquals("response", response.getChoices().get(0).getMessage().getContent());
    }
}