# Piped Proxy Service - User Guide

This document explains how to use the Piped Proxy Service, which provides dynamic IP rotation for enhanced privacy and access to geo-restricted content.

## Overview

The Piped Proxy Service is a standalone SOCKS5 proxy server that automatically fetches, validates, and rotates proxy configurations from multiple subscription sources. It provides:

1. **Dynamic Proxy Rotation** - Fetches fresh proxy lists every 10 minutes
2. **Multi-Protocol Support** - Works with VLESS, VMess, Trojan, Shadowsocks, and XHTTP
3. **Health Checking** - Ensures only working proxies are used
4. **IP Rotation** - Automatically rotates IP addresses every 5 minutes
5. **Logging & Monitoring** - Shows current IP and active proxy information

## Architecture

The proxy service runs as a separate Docker container alongside the main Piped application and PostgreSQL database. It exposes a SOCKS5 proxy interface on port 1080 that other applications can use.

## Quick Start

1. **Start the services**:
   ```bash
   docker-compose up -d
   ```

2. **The proxy service will be available at**:
   - Host: `localhost`
   - Port: `1080`
   - Protocol: SOCKS5

3. **Configure applications to use the SOCKS5 proxy at `localhost:1080`**

## Configuration

### Environment Variables

- `DYNAMIC_PROXY_PORT` - Port for SOCKS5 proxy server (default: 1080)

### Subscription Sources

The proxy service fetches proxies from these sources every 10 minutes:
- VLESS: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/vless
- VMess: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/vmess
- Trojan: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/trojan
- Shadowsocks: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/ss
- XHTTP: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/xhttp

## Usage

### For Applications

Configure applications to use SOCKS5 proxy:
- Host: `localhost`
- Port: `1080`

### For Piped Backend

The main Piped application is automatically configured to use the proxy service through the `PROXY_HOST` and `PROXY_PORT` environment variables.

## Logging Information

The proxy service provides real-time logging information:

1. **Startup Messages**:
   ```
   [YYYY-MM-DD HH:MM:SS] Starting Piped Proxy Service...
   [YYYY-MM-DD HH:MM:SS] Proxy service started successfully
   ```

2. **Proxy Update Messages**:
   ```
   [YYYY-MM-DD HH:MM:SS] Updating proxy list...
   [YYYY-MM-DD HH:MM:SS] Fetched X proxies from [URL]
   [YYYY-MM-DD HH:MM:SS] Health check complete. Working proxies: Y
   [YYYY-MM-DD HH:MM:SS] Current proxy: [proxy_info]
   ```

3. **IP Rotation Messages**:
   ```
   [YYYY-MM-DD HH:MM:SS] Rotated to proxy: [proxy_info]
   [YYYY-MM-DD HH:MM:SS] Current IP: Using proxy: [host]:[port] | Active proxies: X
   ```

4. **Shutdown Messages**:
   ```
   [YYYY-MM-DD HH:MM:SS] Shutting down Piped Proxy Service...
   [YYYY-MM-DD HH:MM:SS] Proxy service stopped
   ```

## IP Rotation

The proxy service automatically rotates IP addresses:

1. **Automatic Updates** - Fetches new proxy lists every 10 minutes
2. **Health Validation** - Tests each proxy for connectivity before use
3. **Round-Robin Selection** - Distributes requests evenly across working proxies
4. **Periodic Rotation** - Changes proxy every 5 minutes for IP diversity

## Troubleshooting

### Common Issues

1. **Proxy Service Not Starting**:
   - Check Docker logs: `docker-compose logs proxy`
   - Verify internet connectivity
   - Check subscription URLs are accessible

2. **No Working Proxies**:
   - Wait for next update cycle (every 10 minutes)
   - Check if subscription URLs are working
   - Verify firewall settings allow outbound connections

3. **Applications Cannot Connect**:
   - Verify SOCKS5 proxy settings (localhost:1080)
   - Check if proxy service is running: `docker-compose ps`
   - Review proxy service logs: `docker-compose logs proxy`

### Log Analysis

Monitor logs for diagnostic information:
```bash
# View live logs
docker-compose logs -f proxy

# View last 100 lines of logs
docker-compose logs --tail=100 proxy
```

## Performance Considerations

1. **Resource Usage**:
   - CPU: Minimal (background updates every 10 minutes)
   - Memory: ~50-100MB depending on proxy count
   - Network: Periodic subscription updates and health checks

2. **Latency Impact**:
   - Adds minimal latency for proxy selection
   - Actual connection speed depends on selected proxy quality

3. **Scalability**:
   - Can handle hundreds of concurrent connections
   - Supports horizontal scaling with load balancing

## Security Notes

1. **Proxy Validation**:
   - Only uses proxies that pass connectivity tests
   - Regularly validates proxy health

2. **Data Privacy**:
   - Does not log or store user traffic
   - Only logs proxy-related operational information

3. **Network Security**:
   - Connections are limited to SOCKS5 protocol
   - No direct access to internal network services

## Integration with Piped Backend

The Piped Backend automatically connects to the proxy service:

1. **Service Discovery**: Uses Docker's internal networking (`proxy` hostname)
2. **Configuration**: Controlled through environment variables
3. **Failover**: Gracefully handles proxy service unavailability

## Customization

### Adding New Subscription Sources

Modify the `fetchProxiesFromSubscriptions()` method in `ProxyService.java` to add new subscription URLs.

### Adjusting Update Intervals

Change the scheduling parameters in `startProxyUpdateCycle()` and `startIPRotationDisplay()` methods.

### Modifying Health Checks

Update the `testProxyConnectivity()` method to implement custom health check logic.

## Monitoring

### Health Endpoints

The proxy service includes a health check endpoint for monitoring systems.

### Metrics

Key metrics available through logs:
- Active proxy count
- Current IP address
- Rotation events
- Error rates

## Best Practices

1. **Regular Updates**: Keep subscription URLs current
2. **Monitoring**: Watch for error messages in logs
3. **Backup**: Have alternative access methods for critical applications
4. **Performance Testing**: Test with your specific use cases

## Support

For issues with the proxy service:
1. Check the logs for error messages
2. Verify subscription URLs are accessible
3. Ensure Docker has proper network access
4. Report persistent issues to the development team