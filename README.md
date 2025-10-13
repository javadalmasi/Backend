# Piped-Backend

An advanced open-source privacy friendly alternative to YouTube, crafted with the help of [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor).

## Official Frontend

-   VueJS frontend - [Piped](https://github.com/TeamPiped/Piped)

## Enhanced Proxy Support

This version includes enhanced proxy capabilities that automatically fetch, rotate, and load balance SOCKS5 proxies from subscription URLs.

The proxy functionality is **enabled by default** and runs on port 1080 alongside the main application.

See [README-PROXY.md](README-PROXY.md) for details on how the dynamic proxy functionality works.

## Quick Start

```bash
docker-compose up -d
```

The application will be available at:
- Main API: http://localhost:8080
- SOCKS5 Proxy: localhost:1080 (for applications to connect through)

## Community Projects

-   See https://github.com/TeamPiped/Piped#made-with-piped