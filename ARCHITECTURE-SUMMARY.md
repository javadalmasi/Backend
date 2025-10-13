# Piped-Backend with Separate Proxy Service - Architecture Summary

This document describes the new architecture where the dynamic proxy functionality runs as a separate service alongside the main Piped-Backend application.

## Architecture Overview

The system now consists of three separate services:
1. **Main Application** - Core Piped-Backend functionality
2. **Proxy Service** - Dynamic proxy rotation and SOCKS5 server
3. **PostgreSQL** - Database for storing user data and subscriptions

All services are orchestrated using docker-compose and can communicate with each other through Docker's internal networking.

## Services Details

### 1. Main Application (piped)

- **Image**: 1337kavin/piped:latest
- **Ports**: 8080 (exposed to localhost only)
- **Dependencies**: postgres, proxy
- **Environment Variables**:
  - PROXY_HOST=proxy (Docker service name)
  - PROXY_PORT=1080

### 2. Proxy Service (proxy)

- **Image**: Built from Dockerfile.proxy
- **Ports**: 1080 (exposed to localhost only)
- **Dependencies**: None
- **Environment Variables**:
  - DYNAMIC_PROXY_PORT=1080

### 3. PostgreSQL (postgres)

- **Image**: postgres:17-alpine
- **Ports**: None (internal access only)
- **Volumes**: ./data/db:/var/lib/postgresql/data
- **Environment Variables**:
  - POSTGRES_DB=piped
  - POSTGRES_USER=piped
  - POSTGRES_PASSWORD=changeme

## Communication Flow

1. **Main Application** connects to **Proxy Service** through Docker's internal network
2. **Proxy Service** fetches proxy lists from subscription URLs
3. **Proxy Service** validates proxies and maintains a healthy pool
4. **Proxy Service** serves SOCKS5 proxy connections on port 1080
5. **External Applications** connect to the SOCKS5 proxy at localhost:1080
6. **Main Application** and **External Applications** can both utilize the rotating proxy pool

## Key Benefits

1. **Modularity** - Services can be scaled independently
2. **Isolation** - Proxy functionality doesn't affect main application performance
3. **Maintainability** - Easier to update or replace individual services
4. **Scalability** - Can run multiple proxy service instances behind a load balancer
5. **Fault Tolerance** - Failure of one service doesn't necessarily affect others

## Deployment

### Using Docker Compose (Recommended)

```bash
docker-compose up -d
```

This will start all three services:
- Main application at http://localhost:8080
- SOCKS5 proxy at localhost:1080

### Individual Service Management

```bash
# Start only the proxy service
docker-compose up -d proxy

# Start only the main application and database
docker-compose up -d piped postgres

# View logs for proxy service
docker-compose logs -f proxy
```

## Configuration

The system is configured through environment variables in docker-compose.yml and config.properties:

### Environment Variables

1. **Main Application**:
   - PROXY_HOST - Hostname of proxy service (default: proxy)
   - PROXY_PORT - Port of proxy service (default: 1080)

2. **Proxy Service**:
   - DYNAMIC_PROXY_PORT - Port for SOCKS5 proxy server (default: 1080)

### Configuration File (config.properties)

Additional configuration options can be set in config.properties:
- PROXY_HOST - Hostname of proxy service
- PROXY_PORT - Port of proxy service

## Proxy Functionality

The proxy service provides:

1. **Automatic Proxy Rotation** - Fetches fresh proxy lists every 10 minutes
2. **Multi-Protocol Support** - Works with VLESS, VMess, Trojan, Shadowsocks, XHTTP
3. **Health Checking** - Continuously validates proxies to ensure reliability
4. **Load Balancing** - Distributes requests evenly across working proxies
5. **SOCKS5 Interface** - Standard SOCKS5 proxy interface on port 1080

## Subscription Sources

The proxy service fetches proxies from these sources every 10 minutes:
- VLESS: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/vless
- VMess: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/vmess
- Trojan: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/trojan
- Shadowsocks: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/ss
- XHTTP: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/xhttp

## Usage

### For External Applications

Configure applications to use SOCKS5 proxy at `localhost:1080` to benefit from rotating IPs.

### For Main Application

The main application can route outgoing requests through the proxy service by configuring the REQWEST_PROXY settings:

```properties
REQWEST_PROXY=socks5://proxy:1080
```

## Health Monitoring

Each service includes health checks:
- **Main Application**: HTTP endpoint at /healthcheck
- **Proxy Service**: HTTP endpoint at /health
- **PostgreSQL**: Built-in PostgreSQL health checks

## Scaling

The architecture supports horizontal scaling:
1. Multiple proxy service instances can run behind a load balancer
2. Multiple main application instances can connect to the same proxy service
3. PostgreSQL can be replaced with a clustered database solution for high availability

This design provides a robust, scalable, and maintainable solution for providing dynamic proxy capabilities to the Piped-Backend application.