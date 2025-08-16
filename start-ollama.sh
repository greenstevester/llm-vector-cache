#!/bin/bash

echo "🚀 Starting LLM Vector Cache Application (Ollama Mode)"

# Parse command line arguments
OLLAMA_URL="${1:-http://localhost:11434}"
OLLAMA_MODEL="${2:-qwen2.5-coder:3b}"

echo "🔧 Configuration:"
echo "   Ollama URL: $OLLAMA_URL"
echo "   Model: $OLLAMA_MODEL"
echo ""

# Check if Ollama is running
echo "🔍 Checking Ollama server availability at $OLLAMA_URL..."
if ! curl -s "$OLLAMA_URL/api/version" >/dev/null 2>&1; then
    echo "❌ Error: Ollama server is not running at $OLLAMA_URL"
    echo "   Please start Ollama server or check the URL"
    echo "   Usage: $0 [ollama_url] [model_name]"
    echo "   Example: $0 http://10.0.0.125:11434 qwen2.5-coder:3b"
    exit 1
fi

echo "✅ Ollama server is available at $OLLAMA_URL"

# Extract host and port for ollama command (if using localhost)
if [[ "$OLLAMA_URL" == "http://localhost:"* ]] || [[ "$OLLAMA_URL" == "http://127.0.0.1:"* ]]; then
    echo "🔍 Checking if model '$OLLAMA_MODEL' is available..."
    if ! ollama list | grep -q "$OLLAMA_MODEL" 2>/dev/null; then
        echo "⚠️  Warning: Model '$OLLAMA_MODEL' not found in local Ollama"
        echo "   You may need to pull it with: ollama pull $OLLAMA_MODEL"
        echo "   Continuing anyway - Ollama will auto-pull if needed"
    fi
else
    echo "🔍 Skipping local model check for remote Ollama server"
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

# Start the application with Ollama profile
echo "🏃 Starting Spring Boot application with Ollama provider..."
echo "🤖 LLM Provider: Ollama ($OLLAMA_MODEL)"
echo "🌐 Ollama Server: $OLLAMA_URL"
echo "📊 Dashboard will be available at: http://localhost:8080"
echo "🔌 API endpoint: http://localhost:8080/api/llm/generate"
echo "📈 Cache stats: http://localhost:8080/api/llm/cache/stats"
echo ""

# Extract base URL for configuration
OLLAMA_BASE_URL="$OLLAMA_URL"

mvn spring-boot:run \
    -Dspring-boot.run.profiles=ollama \
    -Dspring-boot.run.jvmArguments="-Dllmprovider.ollama.base-url=$OLLAMA_BASE_URL -Dllmprovider.ollama.model=$OLLAMA_MODEL"