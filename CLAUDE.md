# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Common Development Commands

### Build and Run

```bash
# Build the project
mvn clean install

# Run the application (requires Redis and OPENAI_API_KEY)
mvn spring-boot:run

# Quick start with Redis and application
./start.sh

# Run API tests
./test.sh
```

### Docker Operations

```bash
# Start Redis only
cd docker && docker-compose up -d redis

# Start full stack (Redis + Application)
docker-compose up -d

# Build Docker image for application
mvn clean package
docker build -f docker/Dockerfile -t llm-cache-app .
```

### Testing

```bash
# Run unit tests
mvn test

# Test individual endpoints
curl http://localhost:8080/api/llm/health
curl -X POST http://localhost:8080/api/llm/generate -H "Content-Type: application/json" -d '{"prompt": "test", "options": {"model": "gpt-3.5-turbo"}}'
```

## Architecture Overview

This is a Spring Boot application implementing a dual-layer LLM cache with vector similarity
matching:

### Core Components

1. **Dual-Layer Caching Strategy**

   - **Exact Match Cache**: Direct string hash lookup via Redis (MD5 hash keys)
   - **Semantic Cache**: Vector similarity search using OpenAI embeddings with configurable cosine
     similarity threshold

2. **Service Layer Architecture**

   - `LLMService`: Orchestrates cache lookups and OpenAI API calls
   - `VectorCacheService`: Manages Redis operations, vector indexing, and similarity searches
   - `EmbeddingService`: Handles OpenAI embedding generation and cosine similarity calculations

3. **Key Technologies**
   - Spring Boot 3.2.0 with WebFlux for async operations
   - Redis Stack with RediSearch for vector operations
   - OpenAI API integration (embeddings + chat completions)
   - CompletableFuture for non-blocking cache operations

### Configuration

Key settings in `application.yml`:

- `cache.similarity.threshold`: Cosine similarity threshold (default: 0.95)
- `cache.ttl.hours`: Cache entry TTL (default: 24 hours)
- `llmprovider.openai.model`: OpenAI embedding model (default: text-embedding-ada-002)

Environment requirements:

- `OPENAI_API_KEY`: Required for OpenAI API access
- `REDIS_HOST`: Redis hostname (default: localhost)
- `REDIS_PORT`: Redis port (default: 6379)

### Request Flow

1. Request enters via `LLMController` at `/api/llm/generate`
2. `VectorCacheService.get()` attempts exact match lookup
3. If no exact match, generates embedding and performs semantic search
4. On cache miss, calls OpenAI API and caches result with embedding
5. Returns response asynchronously via CompletableFuture

### API Endpoints

- `POST /api/llm/generate`: Generate LLM response with caching
- `GET /api/llm/cache/stats`: Get cache statistics
- `GET /api/llm/health`: Health check
- `POST /api/llm/cache/evict`: Evict expired entries
- Actuator endpoints at `/actuator/*` for monitoring
