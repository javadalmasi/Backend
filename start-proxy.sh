#!/bin/bash

# Piped Backend Startup Script with Proxy Support

set -e

echo "Starting Piped Backend with Proxy Support..."

# Check if config.properties exists
if [ ! -f "config.properties" ]; then
    echo "config.properties not found!"
    echo "Copying from config.properties.example..."
    cp config.properties.example config.properties
fi

# Check if VERSION file exists
if [ ! -f "VERSION" ]; then
    echo "VERSION file not found, creating..."
    echo "1.0.0-proxy-enhanced" > VERSION
fi

# Set default environment variables if not set
export ENABLE_DYNAMIC_PROXY=${ENABLE_DYNAMIC_PROXY:-true}
export DYNAMIC_PROXY_PORT=${DYNAMIC_PROXY_PORT:-1080}

echo "Configuration:"
echo "  Dynamic Proxy Enabled: $ENABLE_DYNAMIC_PROXY"
echo "  Proxy Port: $DYNAMIC_PROXY_PORT"

# Start the application
java -jar piped-backend-proxy.jar