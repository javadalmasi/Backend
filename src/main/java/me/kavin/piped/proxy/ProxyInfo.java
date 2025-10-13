package me.kavin.piped.proxy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProxyInfo {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final String scheme;
    private final String host;
    private final int port;
    private final String fullConfig;
    private final LocalDateTime createdAt;
    private volatile boolean isWorking = true;
    
    public ProxyInfo(String scheme, String host, int port, String fullConfig) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.fullConfig = fullConfig;
        this.createdAt = LocalDateTime.now();
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getFullConfig() {
        return fullConfig;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public boolean isWorking() {
        return isWorking;
    }
    
    public void setWorking(boolean working) {
        isWorking = working;
    }
    
    /**
     * Gets a display-friendly representation of the proxy info
     * @return Display string
     */
    public String getDisplayInfo() {
        return String.format("%s://%s:%d (%s)", scheme, host, port, createdAt.format(formatter));
    }
    
    @Override
    public String toString() {
        return "ProxyInfo{" +
                "scheme='" + scheme + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", isWorking=" + isWorking +
                '}';
    }
}