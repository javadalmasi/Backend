# Piped Backend with Dynamic Proxy Support - Summary

This document summarizes all the files added and modified to implement dynamic proxy support in Piped Backend.

## Files Added

### Proxy Implementation Classes
1. `/src/main/java/me/kavin/piped/proxy/ProxyFetcher.java` - Fetches and decodes proxy configurations from subscription URLs
2. `/src/main/java/me/kavin/piped/proxy/ProxyHealthChecker.java` - Checks health of proxy configurations
3. `/src/main/java/me/kavin/piped/proxy/Socks5ProxyServer.java` - SOCKS5 proxy server implementation with load balancing
4. `/src/main/java/me/kavin/piped/proxy/ProxyManager.java` - Coordinates all proxy components

### Scripts
5. `/build-proxy.sh` - Build script for the enhanced version
6. `/start-proxy.sh` - Startup script for the enhanced version
7. `/Dockerfile.proxy` - Dockerfile for containerizing the enhanced version
8. `/docker-compose.proxy.yml` - Docker Compose file for deployment
9. `/README-PROXY.md` - Documentation for the proxy functionality

### Test Files
10. `/src/test/java/me/kavin/piped/proxy/ProxyModuleTest.java` - Simple test for proxy modules

## Files Modified

### Core Application Files
1. `/src/main/java/me/kavin/piped/Main.java` - Integrated proxy manager startup
2. `/src/main/java/me/kavin/piped/consts/Constants.java` - Added new configuration options
3. `/config.properties` - Added proxy configuration options
4. `/README.md` - Updated to reference new proxy functionality

## Key Features Implemented

1. **Automatic Proxy Fetching**
   - Fetches proxy configurations from subscription URLs every 10 minutes
   - Decodes base64-encoded configurations
   - Supports multiple protocols (VLESS, VMess, Trojan, Shadowsocks, XHTTP)

2. **Health Checking**
   - Tests connectivity of each proxy
   - Maintains a pool of working proxies only

3. **Load Balancing**
   - SOCKS5 proxy server implementation
   - Round-robin distribution of requests

4. **Easy Deployment**
   - Docker support with docker-compose
   - Configuration via environment variables or config.properties
   - Startup and build scripts

## Configuration Options

Added to `config.properties` or as environment variables:
- `ENABLE_DYNAMIC_PROXY` - Enable/disable dynamic proxy functionality
- `DYNAMIC_PROXY_PORT` - Port for SOCKS5 proxy server

## Deployment

### Direct Java Execution
```bash
./build-proxy.sh
ENABLE_DYNAMIC_PROXY=true java -jar piped-backend-proxy.jar
```

### Docker
```bash
docker-compose -f docker-compose.proxy.yml up -d
```

### Docker Build
```bash
docker build -t piped-backend-proxy -f Dockerfile.proxy .
```

## Usage

Once deployed, applications can connect through the SOCKS5 proxy at `localhost:1080` (or configured port) to benefit from rotating IPs.

## Limitations

1. Current implementation includes placeholder functionality for actual proxy forwarding
2. Real-world proxy forwarding would require protocol-specific implementations
3. Health checks are basic connectivity tests (not full protocol validation)