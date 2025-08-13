#!/bin/bash

echo "🧪 Testing LLM Vector Cache API"

BASE_URL="http://localhost:8080"

# Test health endpoint
echo "1. Testing health endpoint..."
curl -s "$BASE_URL/api/llm/health" | jq .

echo -e "\n2. Testing LLM generation..."
curl -s -X POST "$BASE_URL/api/llm/generate" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "What is the capital of France?",
    "options": {"model": "gpt-3.5-turbo"}
  }' | jq .

echo -e "\n3. Testing cache stats..."
curl -s "$BASE_URL/api/llm/cache/stats" | jq .

echo -e "\n4. Testing semantic similarity (ask similar question)..."
curl -s -X POST "$BASE_URL/api/llm/generate" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "What is the capital city of France?",
    "options": {"model": "gpt-3.5-turbo"}
  }' | jq .

echo -e "\n✅ Tests completed!"
