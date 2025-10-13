package me.kavin.piped.proxy;

import me.kavin.piped.consts.Constants;

/**
 * Main class for running only the proxy service
 */
public class ProxyMain {
    
    public static void main(String[] args) {
        System.out.println("Starting Piped Proxy Service...");
        
        // Create and start proxy manager
        ProxyManager proxyManager = new ProxyManager();
        proxyManager.start(Constants.DYNAMIC_PROXY_PORT);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Piped Proxy Service...");
            proxyManager.stop();
        }));
        
        // Keep the service running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println("Proxy service interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}