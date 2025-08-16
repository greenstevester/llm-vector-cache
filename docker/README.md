# Docker Configuration for LLM Vector Cache

This directory contains Docker configurations for running the LLM Vector Cache application with different LLM providers.

## Files

- `docker-compose.yml` - Main configuration (supports both OpenAI and Ollama)
- `docker-compose.ollama.yml` - Ollama-specific configuration
- `Dockerfile` - Application container build configuration

## Quick Start Guide

### Step 1: Choose Your LLM Provider

The application supports two modes:
- **Local Ollama** (recommended for development/privacy)
- **OpenAI** (requires API key and costs per request)

---

## 🦙 Local Ollama Setup (Recommended Default)

### Step 1: Install and Start Ollama

**On macOS:**
```bash
# Install Ollama
brew install ollama

# Start Ollama service
ollama serve
```

**On Linux:**
```bash
# Install Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# Start Ollama service
ollama serve
```

### Step 2: Pull a Model

```bash
# Pull the default model (recommended)
ollama pull qwen2.5-coder:3b

# Or pull alternative models
ollama pull mistral:latest
ollama pull llama2:7b
```

### Step 3: Start the Application with Ollama

**Option A: Using dedicated Ollama configuration (simplest)**
```bash
# Navigate to project directory
cd your-project-directory

# Start with local Ollama (uses localhost:11434 by default)
docker-compose -f docker/docker-compose.ollama.yml up -d
```

**Option B: Using main configuration**
```bash
# Configure for Ollama
export SPRING_PROFILES_ACTIVE=ollama
export LLMPROVIDER_ACTIVE=ollama
export LLMPROVIDER_OLLAMA_ENABLED=true
export LLMPROVIDER_OPENAI_ENABLED=false

# Start the stack
docker-compose up -d
```

### Step 4: Verify Ollama Setup

```bash
# Check if Ollama is responding
curl http://localhost:11434/api/version

# Check available models
ollama list
```

---

## 🤖 OpenAI Setup (Alternative)

### Step 1: Get OpenAI API Key

1. Go to https://platform.openai.com/account/api-keys
2. Create a new API key
3. Copy the key (starts with `sk-`)

### Step 2: Set Environment Variable

```bash
# Set your OpenAI API key
export OPENAI_API_KEY=sk-your-actual-api-key-here
```

### Step 3: Start with OpenAI

```bash
# Default configuration uses OpenAI
docker-compose up -d

# Or explicitly configure for OpenAI
export SPRING_PROFILES_ACTIVE=prod
export LLMPROVIDER_ACTIVE=openai
docker-compose up -d
```

---

## 🔄 Switching Between Providers

### From Ollama to OpenAI

```bash
# Stop current containers
docker-compose down

# Set OpenAI configuration
export OPENAI_API_KEY=your_api_key_here
export SPRING_PROFILES_ACTIVE=prod
export LLMPROVIDER_ACTIVE=openai

# Start with OpenAI
docker-compose up -d
```

### From OpenAI to Ollama

```bash
# Stop current containers
docker-compose down

# Start with Ollama
docker-compose -f docker/docker-compose.ollama.yml up -d
```

## Environment Variables

### General Configuration
- `APP_PORT` - Application port (default: 8080)
- `REDIS_PORT` - Redis port (default: 6379)
- `REDIS_INSIGHT_PORT` - Redis Insight port (default: 8001)
- `SPRING_PROFILES_ACTIVE` - Spring profile (default: prod)

### OpenAI Configuration
- `OPENAI_API_KEY` - Your OpenAI API key (**required for OpenAI mode**)
- `LLMPROVIDER_OPENAI_ENABLED` - Enable OpenAI provider (default: true)

### Ollama Configuration
- `LLMPROVIDER_OLLAMA_BASE_URL` - Ollama server URL (default: http://host.docker.internal:11434)
- `LLMPROVIDER_OLLAMA_MODEL` - Model name (default: qwen2.5-coder:3b)
- `LLMPROVIDER_OLLAMA_ENABLED` - Enable Ollama provider (default: false)
- `LLMPROVIDER_ACTIVE` - Active provider (openai/ollama, default: openai)

## Networking Notes

### Ollama Server Access

**Local Ollama Server:**
- Use `http://host.docker.internal:11434` (works on Docker Desktop)
- Or use your host machine's IP address: `http://192.168.1.100:11434`

**Remote Ollama Server:**
- Use the full URL: `http://your-server:11434`

**Docker Network Ollama:**
- If Ollama runs in Docker, ensure both containers are on the same network

## Common Scenarios

### Scenario 1: First Time Setup (Recommended - Local Ollama)

```bash
# 1. Install Ollama
brew install ollama  # macOS
# or curl -fsSL https://ollama.ai/install.sh | sh  # Linux

# 2. Start Ollama in terminal 1
ollama serve

# 3. Pull model in terminal 2
ollama pull qwen2.5-coder:3b

# 4. Navigate to project and start
cd your-project-directory
docker-compose -f docker/docker-compose.ollama.yml up -d

# 5. Access dashboard
open http://localhost:8080
```

### Scenario 2: Using Remote Ollama Server

```bash
# If you have Ollama running on another machine
export LLMPROVIDER_OLLAMA_BASE_URL=http://10.0.0.125:11434
export LLMPROVIDER_OLLAMA_MODEL=llama2:7b
docker-compose -f docker/docker-compose.ollama.yml up -d
```

### Scenario 3: Switch to OpenAI Later

```bash
# Stop Ollama setup
docker-compose down

# Set OpenAI key
export OPENAI_API_KEY=sk-your-key-here

# Start with OpenAI
docker-compose up -d
```

### Scenario 4: Development with Different Models

```bash
# Stop current setup
docker-compose down

# Try different model
export LLMPROVIDER_OLLAMA_MODEL=mistral:latest
ollama pull mistral:latest
docker-compose -f docker/docker-compose.ollama.yml up -d
```

## Access Points

After starting the containers:
- **Dashboard**: http://localhost:8080
- **API**: http://localhost:8080/api/llm/generate
- **Cache Stats**: http://localhost:8080/api/llm/cache/stats
- **Redis Insight**: http://localhost:8001

## Troubleshooting

### Ollama Connection Issues
1. Verify Ollama server is running: `curl http://your-ollama-url/api/version`
2. Check firewall/network connectivity
3. Ensure correct URL format (include http:// prefix)
4. For local Ollama, try host IP instead of `host.docker.internal`

### Model Issues
1. Verify model exists on Ollama server: `ollama list`
2. Pull model if needed: `ollama pull model-name`
3. Check model name matches exactly (case-sensitive)