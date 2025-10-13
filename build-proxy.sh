#!/bin/bash

# Piped Backend Build Script with Proxy Support

set -e

echo "Building Piped Backend with Proxy Support..."

# Clean previous builds
./gradlew clean

# Build the project
./gradlew shadowJar

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful!"
    
    # Copy jar to root directory with proxy indicator
    cp build/libs/Piped-Backend-all.jar ./piped-backend-proxy.jar
    
    echo "Enhanced JAR copied to piped-backend-proxy.jar"
    echo ""
    echo "To run with proxy support:"
    echo "  java -jar piped-backend-proxy.jar"
    echo ""
    echo "Make sure to configure ENABLE_DYNAMIC_PROXY=true in config.properties"
    echo "or set the environment variable ENABLE_DYNAMIC_PROXY=true"
else
    echo "Build failed!"
    exit 1
fi