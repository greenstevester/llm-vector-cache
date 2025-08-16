# LLM Vector Cache with Spring Boot

[![Java](https://img.shields.io/badge/Java-17%2B-blue)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-C71A36)](https://maven.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-Stack-DC382D)](https://redis.io/docs/stack/)
[![OpenAI](https://img.shields.io/badge/OpenAI-API-412991)](https://openai.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A high-performance LLM cache implementation using vector similarity matching with Redis and OpenAI embeddings.

## Features

- **Dual-Layer Caching**: Exact string matching + semantic similarity
- **Vector Search**: Redis with RediSearch for efficient similarity searches
- **Async Operations**: Non-blocking cache operations using CompletableFuture
- **Configurable**: Similarity thresholds, TTL settings, and more
- **Monitoring**: Built-in cache statistics and health endpoints

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- OpenAI API Key

### 1. Start Redis
```bash
cd docker
docker-compose up -d redis
```

### 2. Set Environment Variables
```bash
export OPENAI_API_KEY=your_openai_api_key_here
```

### 3. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### 4. Test the Application
```bash
# Health check
curl http://localhost:8080/api/llm/health

# Generate response (will be cached)
curl -X POST http://localhost:8080/api/llm/generate \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "What is the capital of France?",
    "options": {"model": "gpt-3.5-turbo"}
  }'

# Check cache stats
curl http://localhost:8080/api/llm/cache/stats
```

## API Endpoints

### Generate Response
```bash
POST /api/llm/generate
{
  "prompt": "Your question here",
  "options": {
    "model": "gpt-3.5-turbo"
  }
}
```

### Get Cache Statistics
```bash
GET /api/llm/cache/stats
```

### Health Check
```bash
GET /api/llm/health
```

### Evict Expired Entries
```bash
POST /api/llm/cache/evict
```

## Configuration

Key configuration options in `application.yml`:

- `cache.similarity.threshold`: Similarity threshold for semantic matching (0.0-1.0)
- `cache.ttl.hours`: Cache entry TTL in hours
- `openai.embedding.model`: OpenAI embedding model to use
- `spring.redis.*`: Redis connection settings

## Architecture

The system uses a dual-layer caching approach:

1. **Exact Match Cache**: Fast string-based lookups using Redis
2. **Semantic Cache**: Vector similarity search for semantically similar queries

### How it Works

1. **Cache Lookup**: First tries exact string match, then semantic similarity
2. **Embedding Generation**: Uses OpenAI embeddings for semantic matching
3. **Similarity Calculation**: Cosine similarity between query and cached embeddings
4. **Threshold Matching**: Returns cached response if similarity > threshold
5. **LLM Fallback**: Calls OpenAI API if no cache hit, then caches the result

## Production Deployment

### Using Docker
```bash
# Build the application
mvn clean package

# Start with Docker Compose
docker-compose up -d
```

### Environment Variables
- `OPENAI_API_KEY`: Your OpenAI API key
- `REDIS_HOST`: Redis hostname (default: localhost)
- `REDIS_PORT`: Redis port (default: 6379)
- `CACHE_SIMILARITY_THRESHOLD`: Similarity threshold (default: 0.95)
- `CACHE_TTL_HOURS`: Cache TTL in hours (default: 24)

## Performance Tuning

1. **Similarity Threshold**: Lower values increase cache hits but may reduce accuracy
2. **TTL Settings**: Balance between cache freshness and performance
3. **Redis Configuration**: Tune Redis memory and persistence settings
4. **Connection Pooling**: Adjust Jedis pool settings for your load

## Monitoring

The application provides several monitoring endpoints:

- `/actuator/health`: Application health status
- `/actuator/metrics`: Application metrics
- `/api/llm/cache/stats`: Cache-specific statistics

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

MIT License - see [LICENSE](LICENSE) file for details.
