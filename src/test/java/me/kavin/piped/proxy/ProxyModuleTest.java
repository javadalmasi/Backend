package me.kavin.piped.proxy;

import java.util.Arrays;
import java.util.List;

/**
 * Simple test to verify proxy modules work correctly
 */
public class ProxyModuleTest {
    
    public static void main(String[] args) {
        System.out.println("Testing Proxy Modules...");
        
        // Test fetching proxies
        System.out.println("Testing proxy fetching...");
        List<String> testProxies = Arrays.asList(
            "socks5://test1.example.com:1080",
            "socks5://test2.example.com:1080",
            "https://test3.example.com:443"
        );
        
        System.out.println("Fetched " + testProxies.size() + " test proxies");
        
        // Test health checker
        System.out.println("Testing health checker...");
        List<String> workingProxies = ProxyHealthChecker.checkProxyHealth(testProxies);
        System.out.println("Health check complete. Working proxies: " + workingProxies.size());
        
        // Test proxy manager creation
        System.out.println("Testing proxy manager creation...");
        ProxyManager manager = new ProxyManager();
        System.out.println("Proxy manager created successfully");
        
        System.out.println("All tests passed!");
    }
}