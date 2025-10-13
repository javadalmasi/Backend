package me.kavin.piped.proxy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProxyMain {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static ProxyService proxyService;
    
    public static void main(String[] args) {
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Starting Piped Proxy Service...");
        
        // Parse command line arguments
        int port = 1080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[" + LocalDateTime.now().format(formatter) + "] Invalid port number: " + args[0]);
                System.exit(1);
            }
        }
        
        // Create and start proxy service
        proxyService = new ProxyService();
        proxyService.setProxyPort(port);
        proxyService.start();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Shutting down Piped Proxy Service...");
            if (proxyService != null) {
                proxyService.stop();
            }
        }));
        
        // Keep the service running
        try {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Piped Proxy Service is running on port " + port + ". Press Ctrl+C to stop.");
            
            // Display current IP periodically
            while (true) {
                Thread.sleep(30000); // 30 seconds
                
                if (proxyService != null && proxyService.isRunning()) {
                    String currentIP = proxyService.getCurrentIPAddress();
                    int activeCount = proxyService.getActiveProxyCount();
                    System.out.println("[" + LocalDateTime.now().format(formatter) + "] Current IP: " + currentIP + 
                                     " | Active proxies: " + activeCount);
                }
            }
        } catch (InterruptedException e) {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy service interrupted");
            Thread.currentThread().interrupt();
        }
    }
}