# Enhanced Piped Backend with Dynamic Proxy Support

This version of Piped Backend includes enhanced proxy capabilities that automatically fetch, rotate, and load balance SOCKS5 proxies from subscription URLs.

## Features

- Automatic fetching of proxy configurations from subscription URLs
- Support for VLESS, VMess, Trojan, Shadowsocks, and XHTTP protocols
- Health checking of proxies to ensure only working proxies are used
- Round-robin load balancing across active proxies
- Automatic rotation of proxy list every 10 minutes
- SOCKS5 proxy server for applications to connect through

## Configuration

### New Configuration Options

Add these options to your `config.properties` file:

```properties
# Dynamic Proxy Settings
# Enable dynamic proxy rotation
ENABLE_DYNAMIC_PROXY:true
# Port for the SOCKS5 proxy server
DYNAMIC_PROXY_PORT:1080
```

### Environment Variables

You can also configure using environment variables:

- `ENABLE_DYNAMIC_PROXY` - Enable/disable dynamic proxy (default: false)
- `DYNAMIC_PROXY_PORT` - Port for SOCKS5 proxy server (default: 1080)

## How It Works

1. The system fetches proxy configurations from these subscription URLs every 10 minutes:
   - VLESS: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/vless
   - VMess: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/vmess
   - Trojan: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/trojan
   - Shadowsocks: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/ss
   - XHTTP: https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/xhttp

2. Each proxy is health-checked to verify connectivity

3. Working proxies are added to a rotation pool

4. A SOCKS5 proxy server listens on port 1080 (configurable) and distributes requests among working proxies using round-robin algorithm

5. Applications can connect through this SOCKS5 proxy to benefit from rotating IPs

## Docker Deployment

Use the included `docker-compose.proxy.yml`:

```bash
docker-compose -f docker-compose.proxy.yml up -d
```

Or build the Docker image with proxy support:

```bash
docker build -t piped-backend-proxy -f Dockerfile.proxy .
```

## Usage

Once running, configure your applications to use SOCKS5 proxy at `localhost:1080` (or your configured port).

The system will automatically:
- Fetch new proxy configurations every 10 minutes
- Test each proxy for connectivity
- Maintain a pool of working proxies
- Distribute requests evenly among working proxies

## Troubleshooting

If you encounter issues:

1. Check logs for error messages
2. Verify the `ENABLE_DYNAMIC_PROXY` setting is true
3. Ensure the configured port is available
4. Confirm network connectivity to subscription URLs

## Limitations

- Current implementation includes placeholder functionality for proxy forwarding
- Real-world proxy forwarding would require protocol-specific implementations
- Health checks are basic connectivity tests (not full protocol validation)