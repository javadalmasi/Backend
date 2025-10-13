package me.kavin.piped.proxy;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class ProxyHealthChecker {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int TIMEOUT_SECONDS = 10;
    private static final int MAX_CONCURRENT_CHECKS = 50;
    
    /**
     * Checks the health of a list of proxy configurations
     * @param proxies List of proxy configurations to check
     * @return List of working proxy configurations
     */
    public static List<String> checkProxyHealth(List<String> proxies) {
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Checking health of " + proxies.size() + " proxies...");
        
        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_CHECKS);
        List<Future<ProxyCheckResult>> futures = new ArrayList<>();
        
        for (String proxy : proxies) {
            Future<ProxyCheckResult> future = executor.submit(() -> checkSingleProxy(proxy));
            futures.add(future);
        }
        
        List<String> workingProxies = new ArrayList<>();
        
        for (Future<ProxyCheckResult> future : futures) {
            try {
                ProxyCheckResult result = future.get(15, TimeUnit.SECONDS); // Slightly longer than timeout
                if (result.isWorking()) {
                    workingProxies.add(result.getProxy());
                    System.out.println("Working proxy: " + result.getProxyIdentifier());
                }
            } catch (TimeoutException e) {
                System.err.println("Proxy check timed out");
            } catch (Exception e) {
                System.err.println("Error checking proxy: " + e.getMessage());
            }
        }
        
        executor.shutdown();
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Health check complete. Working proxies: " + workingProxies.size());
        return workingProxies;
    }
    
    /**
     * Checks the health of a single proxy configuration
     * @param proxy Proxy configuration to check
     * @return ProxyCheckResult with the result
     */
    private static ProxyCheckResult checkSingleProxy(String proxy) {
        try {
            URI proxyUri = URI.create(proxy);
            
            // Extract proxy details
            String scheme = proxyUri.getScheme();
            String host = proxyUri.getHost();
            int port = proxyUri.getPort();
            
            if (port == -1) {
                // Set default ports based on scheme
                switch (scheme.toLowerCase()) {
                    case "http":
                    case "ws":
                        port = 80;
                        break;
                    case "https":
                    case "wss":
                        port = 443;
                        break;
                    case "socks5":
                        port = 1080;
                        break;
                    default:
                        port = 80; // Default fallback
                }
            }
            
            // For demonstration, we'll do a simple connectivity test
            // In a real implementation, you would test actual proxy functionality
            boolean isWorking = testConnectivity(host, port);
            
            return new ProxyCheckResult(proxy, isWorking, host + ":" + port);
        } catch (Exception e) {
            return new ProxyCheckResult(proxy, false, "unknown");
        }
    }
    
    /**
     * Tests basic connectivity to a host and port
     * @param host Host to connect to
     * @param port Port to connect to
     * @return true if connection is successful, false otherwise
     */
    private static boolean testConnectivity(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_SECONDS * 1000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Inner class to hold proxy check results
     */
    private static class ProxyCheckResult {
        private final String proxy;
        private final boolean isWorking;
        private final String identifier;
        
        public ProxyCheckResult(String proxy, boolean isWorking, String identifier) {
            this.proxy = proxy;
            this.isWorking = isWorking;
            this.identifier = identifier;
        }
        
        public String getProxy() {
            return proxy;
        }
        
        public boolean isWorking() {
            return isWorking;
        }
        
        public String getProxyIdentifier() {
            return identifier;
        }
    }
}