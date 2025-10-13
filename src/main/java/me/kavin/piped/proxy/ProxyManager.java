package me.kavin.piped.proxy;

import me.kavin.piped.consts.Constants;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProxyManager {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private Socks5ProxyServer socks5Server;
    private volatile boolean running = false;
    
    /**
     * Starts the proxy manager
     * @param port Port for the SOCKS5 proxy server
     */
    public void start(int port) {
        if (running) {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy manager is already running");
            return;
        }
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Starting proxy manager on port " + port + "...");
        
        try {
            // Initialize SOCKS5 server
            socks5Server = new Socks5ProxyServer();
            
            // Start SOCKS5 server in a separate thread
            new Thread(() -> {
                try {
                    socks5Server.start(port);
                } catch (IOException e) {
                    System.err.println("Failed to start SOCKS5 proxy server: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
            
            // Wait a moment for server to start
            Thread.sleep(1000);
            
            // Start the proxy update cycle
            startProxyUpdateCycle();
            
            running = true;
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy manager started successfully");
            
        } catch (Exception e) {
            System.err.println("Error starting proxy manager: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Stops the proxy manager
     */
    public void stop() {
        if (!running) {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy manager is not running");
            return;
        }
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Stopping proxy manager...");
        
        // Stop SOCKS5 server
        if (socks5Server != null) {
            socks5Server.stop();
        }
        
        // Shutdown scheduler
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        running = false;
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy manager stopped");
    }
    
    /**
     * Starts the periodic proxy update cycle
     */
    private void startProxyUpdateCycle() {
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Starting proxy update cycle...");
        
        Runnable updateTask = () -> {
            try {
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] Updating proxy list...");
                
                // Fetch new proxies
                List<String> allProxies = ProxyFetcher.fetchProxies();
                
                // Check proxy health
                List<String> workingProxies = ProxyHealthChecker.checkProxyHealth(allProxies);
                
                // Update SOCKS5 server with working proxies
                if (socks5Server != null) {
                    socks5Server.updateActiveProxies(workingProxies);
                }
                
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy update completed. Active proxies: " + workingProxies.size());
            } catch (Exception e) {
                System.err.println("Error during proxy update: " + e.getMessage());
                e.printStackTrace();
            }
        };
        
        // Initial update
        updateTask.run();
        
        // Schedule recurring updates every 10 minutes
        scheduler.scheduleAtFixedRate(updateTask, 10, 10, TimeUnit.MINUTES);
    }
    
    /**
     * Checks if the proxy manager is running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
}