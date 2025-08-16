#!/bin/bash

echo "🚀 Starting LLM Vector Cache Application (OpenAI Mode)"

# Check if OPENAI_API_KEY is set
if [ -z "$OPENAI_API_KEY" ]; then
    echo "❌ Error: OPENAI_API_KEY environment variable is not set"
    echo "   Please set it with: export OPENAI_API_KEY=your_api_key_here"
    exit 1
fi

# Check if Redis is already running
echo "🔍 Checking Redis status..."
if docker ps --format "table {{.Names}}" | grep -q "redis-stack"; then
    echo "✅ Redis is already running"
else
    echo "🔧 Starting Redis..."
    cd docker
    docker-compose up -d redis
    cd ..
    echo "✅ Redis started successfully"
fi

# Wait for Redis to be ready
echo "⏳ Waiting for Redis to be ready..."
sleep 5

# Start the application with OpenAI profile
echo "🏃 Starting Spring Boot application with OpenAI provider..."
echo "🤖 LLM Provider: OpenAI (text-embedding-ada-002)"
echo "📊 Dashboard will be available at: http://localhost:8080"
echo "🔌 API endpoint: http://localhost:8080/api/llm/generate"
echo "📈 Cache stats: http://localhost:8080/api/llm/cache/stats"
echo ""
mvn spring-boot:run -Dspring-boot.run.profiles=openai