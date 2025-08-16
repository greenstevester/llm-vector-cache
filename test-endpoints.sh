#!/bin/bash

echo "🧪 Testing LLM Vector Cache API Endpoints"
echo "=========================================="

BASE_URL="http://localhost:8080"

# Color codes for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to test endpoint
test_endpoint() {
    local method=$1
    local path=$2
    local data=$3
    local description=$4
    
    echo ""
    echo -e "${YELLOW}Testing: $description${NC}"
    echo "Method: $method"
    echo "Path: $path"
    
    if [ "$method" == "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$BASE_URL$path")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE_URL$path" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi
    
    http_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✅ Success (HTTP $http_code)${NC}"
        echo "Response: $body" | head -c 200
        if [ ${#body} -gt 200 ]; then
            echo "... (truncated)"
        fi
    else
        echo -e "${RED}❌ Failed (HTTP $http_code)${NC}"
        echo "Response: $body"
    fi
}

# Health & Monitoring Endpoints
echo ""
echo "=== Health & Monitoring ==="
test_endpoint "GET" "/api/llm/health" "" "Health Check"
test_endpoint "GET" "/api/llm/cache/stats" "" "Cache Statistics"
test_endpoint "GET" "/actuator/health" "" "Actuator Health"

# LLM Generation Endpoints
echo ""
echo ""
echo "=== LLM Generation ==="
test_endpoint "POST" "/api/llm/generate" \
    '{"prompt": "What is 2+2?", "options": {"model": "qwen2.5-coder:3b"}}' \
    "Simple Question"

# Wait a bit to avoid rate limiting
sleep 2

test_endpoint "POST" "/api/llm/generate" \
    '{"prompt": "Explain the concept of caching", "options": {"model": "qwen2.5-coder:3b"}}' \
    "Semantic Question 1"

# Cache Management
echo ""
echo ""
echo "=== Cache Management ==="
test_endpoint "POST" "/api/llm/cache/evict" "" "Evict Expired Entries"

# Test Cache Hit (should be faster since we asked similar question before)
echo ""
echo ""
echo "=== Test Cache Hit ==="
test_endpoint "POST" "/api/llm/generate" \
    '{"prompt": "What is 2+2?", "options": {"model": "qwen2.5-coder:3b"}}' \
    "Exact Cache Hit Test"

# Error Scenarios
echo ""
echo ""
echo "=== Error Scenarios ==="
test_endpoint "POST" "/api/llm/generate" \
    '{"prompt": "", "options": {"model": "qwen2.5-coder:3b"}}' \
    "Empty Prompt Error"

test_endpoint "POST" "/api/llm/generate" \
    '{"prompt": "Test"}' \
    "Missing Model"

echo ""
echo "=========================================="
echo "✅ Testing complete!"