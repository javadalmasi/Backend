#!/bin/bash

# Build script for Piped Proxy Service

set -e

echo "Building Piped Proxy Service..."

# Clean previous builds
./gradlew clean

# Build the project
./gradlew shadowJar

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    # Copy jar to root directory with proxy indicator
    cp build/libs/Piped-Backend-all.jar ./piped-proxy-service.jar
    
    echo "Proxy service JAR copied to piped-proxy-service.jar"
    echo ""
    echo "To run the proxy service:"
    echo "  java -jar piped-proxy-service.jar"
    echo ""
else
    echo "Build failed!"
    exit 1
fi