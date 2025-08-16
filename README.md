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

## Testing with Postman

A comprehensive Postman collection is included for testing all API endpoints.

### Importing the Collection

1. Open Postman
2. Click **Import** button (or File → Import)
3. Select the file: `LLM-Vector-Cache.postman_collection.json`
4. Click **Import**

### Running the Collection

1. Set your environment variables (optional):
   - Click the **Environment** quick look (eye icon)
   - Add `baseUrl` if not using default `http://localhost:8080`

2. Run individual requests:
   - Navigate to the imported collection
   - Choose from 16+ pre-configured requests organized in folders:
     - **Health & Monitoring**: System health checks
     - **LLM Generation**: Various prompt examples
     - **Cache Management**: Cache control operations
     - **Test Scenarios**: Cache hit/miss testing

3. Run the entire collection:
   - Right-click the collection → **Run collection**
   - Configure iterations and delays as needed
   - Click **Run LLM Vector Cache API**

### Collection Features

- **Pre-configured Examples**: 16+ ready-to-use API requests
- **Semantic Testing**: Examples to test cache similarity matching
- **Error Scenarios**: Test cases for error handling
- **Response Validation**: Automatic response time checks
- **Multiple Models**: Examples for GPT-3.5 and GPT-4

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

## Redis Vector Capabilities

This project leverages [Redis Stack](https://redis.io/docs/stack/) with the [RediSearch module](https://redis.io/docs/stack/search/) for advanced vector similarity search capabilities:

### Key Technologies

- **Jedis Client**: We use [Jedis 5.0+](https://github.com/redis/jedis) with built-in RediSearch support for vector operations
- **Redis Stack**: Includes [RediSearch 2.0+](https://redis.io/docs/stack/search/reference/vectors/) with native vector similarity search
- **Vector Indexing**: Supports FLAT and HNSW indexing algorithms for efficient k-NN searches

### Vector Search Features

- **Embedding Storage**: Store OpenAI embeddings (1536 dimensions for text-embedding-ada-002) as FLOAT32 vectors
- **Cosine Similarity**: Built-in distance metrics including COSINE, L2, and IP (Inner Product)
- **Hybrid Queries**: Combine vector similarity with filters on metadata fields
- **Real-time Indexing**: Automatic indexing of new vectors without rebuilding
- **Scalability**: Handles millions of vectors with sub-millisecond query times

### Implementation Details

```java
// Vector index creation in Redis
FT.CREATE idx:cache 
  ON HASH 
  PREFIX 1 cache: 
  SCHEMA 
    embedding VECTOR FLAT 6 
      TYPE FLOAT32 
      DIM 1536 
      DISTANCE_METRIC COSINE
    prompt TEXT 
    response TEXT
    created_at NUMERIC
```

### Performance Optimizations

- **Pre-filtering**: Reduces search space using metadata filters before vector search
- **Batch Operations**: Pipeline multiple Redis commands for improved throughput
- **Connection Pooling**: Jedis pool configuration for optimal resource utilization
- **Lazy Loading**: Embeddings generated only when needed for semantic search

### Learn More

- [Redis Vector Similarity Docs](https://redis.io/docs/stack/search/reference/vectors/)
- [RediSearch Query Syntax](https://redis.io/docs/stack/search/reference/query_syntax/)
- [Jedis RediSearch Integration](https://github.com/redis/jedis/tree/master/src/main/java/redis/clients/jedis/search)
- [Vector Database Benchmarks](https://redis.io/docs/stack/search/reference/vectors/#vector-similarity-examples)

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

## Inspired By

This project was inspired by [Raul Junco's post on X](https://x.com/RaulJuncoV/status/1954876732261253578) about implementing LLM caching with vector similarity search. Thanks for sharing the concept and sparking the idea for this implementation!

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

MIT License - see [LICENSE](LICENSE) file for details.
