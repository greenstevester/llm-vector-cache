#!/bin/bash

echo "🚀 Starting LLM Vector Cache Application"

# Check if OPENAI_API_KEY is set
if [ -z "$OPENAI_API_KEY" ]; then
    echo "❌ Error: OPENAI_API_KEY environment variable is not set"
    echo "   Please set it with: export OPENAI_API_KEY=your_api_key_here"
    exit 1
fi

# Start Redis if not running
echo "🔧 Starting Redis..."
cd docker
docker-compose up -d redis
cd ..

# Wait for Redis to be ready
echo "⏳ Waiting for Redis to be ready..."
sleep 5

# Start the application
echo "🏃 Starting Spring Boot application..."
mvn spring-boot:run
