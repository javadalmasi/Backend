package me.kavin.piped.proxy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProxyFetcher {
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Subscription URLs
    private static final String[] SUBSCRIPTION_URLS = {
        "https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/vless",
        "https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/vmess",
        "https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/trojan",
        "https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/ss",
        "https://raw.githubusercontent.com/itsyebekhe/PSG/main/subscriptions/xray/base64/xhttp"
    };
    
    /**
     * Fetches and decodes proxy configurations from subscription URLs
     * @return List of decoded proxy configurations
     */
    public static List<String> fetchProxies() {
        List<String> allProxies = new ArrayList<>();
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Fetching proxy configurations...");
        
        for (String url : SUBSCRIPTION_URLS) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    // Decode base64 content
                    String decodedContent = new String(Base64.getDecoder().decode(response.body()));
                    
                    // Split by newline to get individual proxy configs
                    String[] proxies = decodedContent.split("\n");
                    for (String proxy : proxies) {
                        if (!proxy.trim().isEmpty() && !proxy.startsWith("#")) {
                            allProxies.add(proxy.trim());
                        }
                    }
                    
                    System.out.println("Successfully fetched " + proxies.length + " proxies from " + url);
                } else {
                    System.err.println("Failed to fetch proxies from " + url + ". Status code: " + response.statusCode());
                }
            } catch (Exception e) {
                System.err.println("Error fetching proxies from " + url + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Total proxies fetched: " + allProxies.size());
        return allProxies;
    }
    
    /**
     * Starts the scheduled proxy fetching task (every 10 minutes)
     */
    public static void startScheduledFetching(Runnable callback) {
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Starting scheduled proxy fetching (every 10 minutes)...");
        
        // Initial fetch
        callback.run();
        
        // Schedule recurring fetches
        scheduler.scheduleAtFixedRate(callback, 10, 10, TimeUnit.MINUTES);
    }
    
    /**
     * Stops the scheduled proxy fetching
     */
    public static void stopScheduledFetching() {
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Stopping scheduled proxy fetching...");
        scheduler.shutdown();
    }
}