package me.kavin.piped.proxy;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProxyManager {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_SOCKS5_PORT = 1080;
    
    private final ProxyFetcher fetcher;
    private final ProxyHealthChecker healthChecker;
    private final Socks5ProxyServer socks5Server;
    private final ScheduledExecutorService scheduler;
    
    public ProxyManager() {
        this.fetcher = new ProxyFetcher();
        this.healthChecker = new ProxyHealthChecker();
        this.socks5Server = new Socks5ProxyServer();
        this.scheduler = Executors.newScheduledThreadPool(2);
    }
    
    /**
     * Starts the proxy manager
     * @param socks5Port Port for the SOCKS5 proxy server
     */
    public void start(int socks5Port) {
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Starting proxy manager...");
        
        // Start the SOCKS5 proxy server
        try {
            new Thread(() -> {
                try {
                    socks5Server.start(socks5Port);
                } catch (IOException e) {
                    System.err.println("Failed to start SOCKS5 proxy server: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            System.err.println("Error starting SOCKS5 proxy server: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Start the proxy update cycle
        startProxyUpdateCycle();
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
                List<String> workingProxies = healthChecker.checkProxyHealth(allProxies);
                
                // Update SOCKS5 server with working proxies
                socks5Server.updateActiveProxies(workingProxies);
                
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
     * Stops the proxy manager
     */
    public void stop() {
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Stopping proxy manager...");
        
        // Stop the SOCKS5 server
        socks5Server.stop();
        
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
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy manager stopped");
    }
}