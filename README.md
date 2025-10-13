# Piped-Backend

An advanced open-source privacy friendly alternative to YouTube, crafted with the help of [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor).

## Official Frontend

-   VueJS frontend - [Piped](https://github.com/TeamPiped/Piped)

## Enhanced Proxy Support

This version includes enhanced proxy capabilities that automatically fetch, rotate, and load balance SOCKS5 proxies from subscription URLs.

The proxy functionality is provided as a separate service that runs alongside the main application, similar to how PostgreSQL is set up.

See [README-PROXY.md](README-PROXY.md) for details on how the dynamic proxy functionality works.

## Quick Start

```bash
docker-compose up -d
```

The application will be available at:
- Main API: http://localhost:8080
- SOCKS5 Proxy: localhost:1080 (for applications to connect through)

## Services Included

1. **Main Application** - The core Piped-Backend service
2. **Proxy Service** - Dynamic proxy rotation and SOCKS5 server
3. **PostgreSQL** - Database for storing user data and subscriptions

## Community Projects

-   See https://github.com/TeamPiped/Piped#made-with-piped