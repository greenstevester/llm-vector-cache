package com.example.llmcache.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingServiceTest {

    private EmbeddingService service;

    @BeforeEach
    void setUp() {
        service = new EmbeddingService();
    }

    // Note: Full API integration tests would require WebClient mocking
    // For now, we focus on testing the utility methods that don't require external APIs

    @Test
    void shouldCalculateCosineSimilarityCorrectly() {
        // Test identical vectors
        float[] vectorA = {1, 0, 0};
        float[] vectorB = {1, 0, 0};
        double similarity = service.cosineSimilarity(vectorA, vectorB);
        assertEquals(1.0, similarity, 0.001);

        // Test orthogonal vectors
        vectorA = new float[]{1, 0, 0};
        vectorB = new float[]{0, 1, 0};
        similarity = service.cosineSimilarity(vectorA, vectorB);
        assertEquals(0.0, similarity, 0.001);

        // Test opposite vectors
        vectorA = new float[]{1, 0, 0};
        vectorB = new float[]{-1, 0, 0};
        similarity = service.cosineSimilarity(vectorA, vectorB);
        assertEquals(-1.0, similarity, 0.001);
    }

    @Test
    void shouldThrowExceptionForDifferentVectorLengths() {
        float[] vectorA = {1, 2, 3};
        float[] vectorB = {1, 2};

        assertThrows(IllegalArgumentException.class, 
            () -> service.cosineSimilarity(vectorA, vectorB));
    }

    @Test
    void shouldHandleZeroVectors() {
        float[] vectorA = {0, 0, 0};
        float[] vectorB = {1, 2, 3};

        double similarity = service.cosineSimilarity(vectorA, vectorB);
        assertTrue(Double.isNaN(similarity));
    }
}