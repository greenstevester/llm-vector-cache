#!/bin/bash

echo "🚀 LLM Vector Cache - Startup Script Selector"
echo ""
echo "Please choose your LLM provider:"
echo "1) OpenAI (requires OPENAI_API_KEY)"
echo "2) Ollama (requires local Ollama server)"
echo ""
read -p "Enter your choice (1 or 2): " choice

case $choice in
    1)
        echo "Starting with OpenAI provider..."
        ./start-openai.sh
        ;;
    2)
        echo "Starting with Ollama provider..."
        ./start-ollama.sh
        ;;
    *)
        echo "❌ Invalid choice. Please run:"
        echo "   ./start-openai.sh  (for OpenAI)"
        echo "   ./start-ollama.sh  (for Ollama)"
        exit 1
        ;;
esac
