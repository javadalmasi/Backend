package me.kavin.piped.proxy;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProxyService {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final AtomicInteger currentProxyIndex = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final List<ProxyInfo> activeProxies = new CopyOnWriteArrayList<>();
    private final Socks5ProxyServer socks5Server;
    private volatile boolean running = false;
    private int proxyPort = 1080;
    
    public ProxyService() {
        this.socks5Server = new Socks5ProxyServer(this);
    }
    
    /**
     * Starts the proxy service
     */
    public void start() {
        if (running) {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy service is already running");
            return;
        }
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Starting proxy service...");
        
        running = true;
        
        // Start the SOCKS5 server
        try {
            new Thread(() -> {
                try {
                    socks5Server.start(proxyPort);
                } catch (IOException e) {
                    System.err.println("[" + LocalDateTime.now().format(formatter) + "] Failed to start SOCKS5 server: " + e.getMessage());
                }
            }).start();
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now().format(formatter) + "] Error starting SOCKS5 server: " + e.getMessage());
        }
        
        // Start the proxy update cycle
        startProxyUpdateCycle();
        
        // Start IP rotation display
        startIPRotationDisplay();
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy service started successfully");
    }
    
    /**
     * Stops the proxy service
     */
    public void stop() {
        if (!running) {
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy service is not running");
            return;
        }
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Stopping proxy service...");
        
        // Stop SOCKS5 server
        socks5Server.stop();
        
        running = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy service stopped");
    }
    
    /**
     * Gets the current active proxy
     * @return Current proxy info or null if none available
     */
    public ProxyInfo getCurrentProxy() {
        if (activeProxies.isEmpty()) {
            return null;
        }
        
        int index = Math.abs(currentProxyIndex.get() % activeProxies.size());
        return activeProxies.get(index);
    }
    
    /**
     * Rotates to the next proxy
     */
    public void rotateProxy() {
        if (!activeProxies.isEmpty()) {
            currentProxyIndex.incrementAndGet();
            ProxyInfo current = getCurrentProxy();
            if (current != null) {
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] Rotated to proxy: " + current.getDisplayInfo());
            }
        }
    }
    
    /**
     * Sets the proxy port
     * @param port Port to use for SOCKS5 server
     */
    public void setProxyPort(int port) {
        this.proxyPort = port;
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
                List<ProxyInfo> allProxies = fetchProxiesFromSubscriptions();
                
                // Check proxy health
                List<ProxyInfo> workingProxies = checkProxyHealth(allProxies);
                
                // Update active proxies
                activeProxies.clear();
                activeProxies.addAll(workingProxies);
                
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] Proxy update completed. Active proxies: " + workingProxies.size());
                
                // Display current proxy info
                if (!workingProxies.isEmpty()) {
                    ProxyInfo current = getCurrentProxy();
                    if (current != null) {
                        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Current proxy: " + current.getDisplayInfo());
                    }
                }
                
            } catch (Exception e) {
                System.err.println("[" + LocalDateTime.now().format(formatter) + "] Error during proxy update: " + e.getMessage());
                e.printStackTrace();
            }
        };
        
        // Initial update
        updateTask.run();
        
        // Schedule recurring updates every 10 minutes
        scheduler.scheduleAtFixedRate(updateTask, 10, 10, TimeUnit.MINUTES);
    }
    
    /**
     * Starts the IP rotation display
     */
    private void startIPRotationDisplay() {
        // Schedule IP rotation every 5 minutes
        scheduler.scheduleAtFixedRate(() -> {
            if (running && !activeProxies.isEmpty()) {
                rotateProxy();
                
                // Display current IP
                String currentIP = getCurrentIPAddress();
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] Current IP: " + currentIP);
            }
        }, 1, 5, TimeUnit.MINUTES);
    }
    
    /**
     * Fetches proxies from subscription URLs
     * @return List of proxy info objects
     */
    private List<ProxyInfo> fetchProxiesFromSubscriptions() {
        List<ProxyInfo> allProxies = new ArrayList<>();
        
        // Subscription URLs
        String[] subscriptionUrls = {
            "https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/vless",
            "https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/vmess",
            "https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/trojan",
            "https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/ss",
            "https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/xhttp"
        };
        
        for (String url : subscriptionUrls) {
            try {
                List<ProxyInfo> proxies = fetchProxiesFromUrl(url);
                allProxies.addAll(proxies);
                System.out.println("[" + LocalDateTime.now().format(formatter) + "] Fetched " + proxies.size() + " proxies from " + url);
            } catch (Exception e) {
                System.err.println("[" + LocalDateTime.now().format(formatter) + "] Error fetching proxies from " + url + ": " + e.getMessage());
            }
        }
        
        return allProxies;
    }
    
    /**
     * Fetches proxies from a single URL
     * @param url Subscription URL
     * @return List of proxy info objects
     * @throws Exception If fetching fails
     */
    private List<ProxyInfo> fetchProxiesFromUrl(String url) throws Exception {
        List<ProxyInfo> proxies = new ArrayList<>();
        
        try {
            URL subscriptionUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) subscriptionUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    
                    // Decode base64 content
                    String decodedContent = new String(Base64.getDecoder().decode(content.toString()));
                    
                    // Split by newline to get individual proxy configs
                    String[] proxyLines = decodedContent.split("\n");
                    for (String proxyLine : proxyLines) {
                        String trimmedProxy = proxyLine.trim();
                        if (!trimmedProxy.isEmpty() && !trimmedProxy.startsWith("#")) {
                            try {
                                ProxyInfo proxyInfo = parseProxy(trimmedProxy);
                                if (proxyInfo != null) {
                                    proxies.add(proxyInfo);
                                }
                            } catch (Exception e) {
                                // Skip invalid proxies
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to fetch proxies from " + url, e);
        }
        
        return proxies;
    }
    
    /**
     * Parses a proxy string into a ProxyInfo object
     * @param proxyString Proxy configuration string
     * @return ProxyInfo object or null if invalid
     */
    private ProxyInfo parseProxy(String proxyString) {
        try {
            URI uri = URI.create(proxyString);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            
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
                    case "vless":
                    case "vmess":
                    case "trojan":
                        port = 443;
                        break;
                    default:
                        port = 1080; // Default fallback
                }
            }
            
            return new ProxyInfo(scheme, host, port, proxyString);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Checks the health of proxy configurations
     * @param proxies List of proxy info objects to check
     * @return List of working proxy info objects
     */
    private List<ProxyInfo> checkProxyHealth(List<ProxyInfo> proxies) {
        List<ProxyInfo> workingProxies = new ArrayList<>();
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Checking health of " + proxies.size() + " proxies...");
        
        for (ProxyInfo proxy : proxies) {
            if (testProxyConnectivity(proxy)) {
                workingProxies.add(proxy);
            }
        }
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Health check complete. Working proxies: " + workingProxies.size());
        
        return workingProxies;
    }
    
    /**
     * Tests the connectivity of a single proxy
     * @param proxy Proxy to test
     * @return true if proxy is working, false otherwise
     */
    private boolean testProxyConnectivity(ProxyInfo proxy) {
        try {
            // For demonstration, we'll do a simple connectivity test
            // In a real implementation, you would test actual proxy functionality
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(proxy.getHost(), proxy.getPort()), 5000); // 5 second timeout
            socket.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets the current IP address through the current proxy
     * @return Current IP address or "Unknown" if not available
     */
    public String getCurrentIPAddress() {
        ProxyInfo currentProxy = getCurrentProxy();
        if (currentProxy == null) {
            return "No active proxy";
        }
        
        // In a real implementation, you would make an HTTP request through the proxy
        // to determine the actual IP address being used
        return "Using proxy: " + currentProxy.getHost() + ":" + currentProxy.getPort();
    }
    
    /**
     * Checks if the service is running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Gets the number of active proxies
     * @return Number of active proxies
     */
    public int getActiveProxyCount() {
        return activeProxies.size();
    }
    
    /**
     * Gets information about all active proxies
     * @return List of active proxy information
     */
    public List<String> getActiveProxyInfo() {
        List<String> info = new ArrayList<>();
        for (ProxyInfo proxy : activeProxies) {
            info.add(proxy.getDisplayInfo());
        }
        return info;
    }
}