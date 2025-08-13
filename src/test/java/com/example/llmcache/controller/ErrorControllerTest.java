package com.example.llmcache.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorControllerTest {

    private CustomErrorController controller;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        controller = new CustomErrorController();
        request = new MockHttpServletRequest();
    }

    @Test
    void shouldHandleNotFoundError() {
        // Given
        request.setAttribute("jakarta.servlet.error.status_code", 404);
        request.setAttribute("jakarta.servlet.error.request_uri", "/nonexistent");

        // When
        ResponseEntity<Map<String, Object>> response = controller.handleError(request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("/nonexistent", body.get("path"));
        assertEquals("LLM Vector Cache", body.get("service"));
        assertTrue(body.get("message").toString().contains("Available endpoints"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void shouldHandleInternalServerError() {
        // Given
        request.setAttribute("jakarta.servlet.error.status_code", 500);
        request.setAttribute("jakarta.servlet.error.message", "Database connection failed");
        request.setAttribute("jakarta.servlet.error.request_uri", "/api/llm/generate");

        // When
        ResponseEntity<Map<String, Object>> response = controller.handleError(request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("/api/llm/generate", body.get("path"));
        assertTrue(body.get("message").toString().contains("Internal server error"));
    }

    @Test
    void shouldHandleDefaultStatusWhenNull() {
        // Given - no status code set
        request.setAttribute("jakarta.servlet.error.request_uri", "/test");

        // When
        ResponseEntity<Map<String, Object>> response = controller.handleError(request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
    }

    @Test
    void shouldReturnHomePageInfo() {
        // When
        ResponseEntity<Map<String, Object>> response = controller.home();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("LLM Vector Cache", body.get("service"));
        assertEquals("1.0.0", body.get("version"));
        assertEquals("running", body.get("status"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> endpoints = (Map<String, Object>) body.get("endpoints");
        assertNotNull(endpoints);
        assertTrue(endpoints.containsKey("health"));
        assertTrue(endpoints.containsKey("generate"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> documentation = (Map<String, String>) body.get("documentation");
        assertNotNull(documentation);
        assertTrue(documentation.containsKey("readme"));
        assertTrue(documentation.containsKey("api_docs"));
    }

    @Test
    void shouldHandleBadRequestError() {
        // Given
        request.setAttribute("jakarta.servlet.error.status_code", 400);
        request.setAttribute("jakarta.servlet.error.request_uri", "/api/llm/generate");

        // When
        ResponseEntity<Map<String, Object>> response = controller.handleError(request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertTrue(body.get("message").toString().contains("Bad request"));
    }
}