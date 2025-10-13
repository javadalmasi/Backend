package me.kavin.piped.proxy;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Socks5ProxyServer {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_SOCKS5_PORT = 1080;
    private static final AtomicInteger proxyIndex = new AtomicInteger(0);
    
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private List<String> activeProxies = new CopyOnWriteArrayList<>();
    private final Object proxyLock = new Object();
    
    /**
     * Starts the SOCKS5 proxy server
     * @param port Port to listen on
     * @throws IOException If server fails to start
     */
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] SOCKS5 proxy server started on port " + port);
        
        // Start accepting connections
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                // Handle each client in a separate thread
                new Thread(() -> handleClient(clientSocket)).start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
                break;
            }
        }
    }
    
    /**
     * Stops the SOCKS5 proxy server
     */
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] SOCKS5 proxy server stopped");
    }
    
    /**
     * Updates the list of active proxies
     * @param proxies List of working proxies
     */
    public void updateActiveProxies(List<String> proxies) {
        synchronized (proxyLock) {
            this.activeProxies.clear();
            this.activeProxies.addAll(proxies);
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] Updated active proxies. Count: " + activeProxies.size());
        }
    }
    
    /**
     * Handles a client connection
     * @param clientSocket Client socket
     */
    private void handleClient(Socket clientSocket) {
        try {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            
            // SOCKS5 handshake
            if (!performSocks5Handshake(in, out)) {
                clientSocket.close();
                return;
            }
            
            // Get the target address and port
            SocksTarget target = parseTargetAddress(in, out);
            if (target == null) {
                clientSocket.close();
                return;
            }
            
            // Select a proxy using round-robin
            String selectedProxy = selectProxyRoundRobin();
            if (selectedProxy == null) {
                System.err.println("No active proxies available");
                clientSocket.close();
                return;
            }
            
            // Forward the connection through the selected proxy
            forwardConnection(clientSocket, target, selectedProxy);
            
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Performs the SOCKS5 handshake
     * @param in Input stream
     * @param out Output stream
     * @return true if handshake successful, false otherwise
     * @throws IOException If IO error occurs
     */
    private boolean performSocks5Handshake(DataInputStream in, DataOutputStream out) throws IOException {
        // Read version and number of methods
        int version = in.read();
        int nMethods = in.read();
        
        // Check version
        if (version != 5) {
            System.err.println("Unsupported SOCKS version: " + version);
            return false;
        }
        
        // Read methods
        byte[] methods = new byte[nMethods];
        in.readFully(methods);
        
        // Reply with method selection (no authentication)
        out.write(new byte[]{5, 0}); // SOCKS5, no auth
        out.flush();
        
        return true;
    }
    
    /**
     * Parses the target address from the SOCKS5 request
     * @param in Input stream
     * @param out Output stream
     * @return Target address or null if parsing failed
     * @throws IOException If IO error occurs
     */
    private SocksTarget parseTargetAddress(DataInputStream in, DataOutputStream out) throws IOException {
        // Read request header
        int version = in.read();
        int cmd = in.read();
        in.read(); // Reserved
        int addrType = in.read();
        
        // Check version
        if (version != 5) {
            System.err.println("Invalid SOCKS version in request: " + version);
            return null;
        }
        
        // Parse address based on type
        String address;
        if (addrType == 1) { // IPv4
            byte[] ipBytes = new byte[4];
            in.readFully(ipBytes);
            address = InetAddress.getByAddress(ipBytes).getHostAddress();
        } else if (addrType == 3) { // Domain name
            int domainLength = in.read();
            byte[] domainBytes = new byte[domainLength];
            in.readFully(domainBytes);
            address = new String(domainBytes);
        } else if (addrType == 4) { // IPv6
            byte[] ipv6Bytes = new byte[16];
            in.readFully(ipv6Bytes);
            address = InetAddress.getByAddress(ipv6Bytes).getHostAddress();
        } else {
            System.err.println("Unsupported address type: " + addrType);
            return null;
        }
        
        // Read port
        int port = in.readUnsignedShort();
        
        // Reply with success
        out.write(new byte[]{5, 0, 0, 1, 0, 0, 0, 0, 0, 0});
        out.flush();
        
        return new SocksTarget(address, port);
    }
    
    /**
     * Selects a proxy using round-robin algorithm
     * @return Selected proxy or null if none available
     */
    private String selectProxyRoundRobin() {
        synchronized (proxyLock) {
            if (activeProxies.isEmpty()) {
                return null;
            }
            
            int index = proxyIndex.getAndIncrement() % activeProxies.size();
            if (index < 0) {
                index = -index;
                proxyIndex.set(index);
            }
            
            return activeProxies.get(index);
        }
    }
    
    /**
     * Forwards the connection through the selected proxy
     * @param clientSocket Client socket
     * @param target Target address
     * @param proxy Proxy to use
     */
    private void forwardConnection(Socket clientSocket, SocksTarget target, String proxy) {
        // In a real implementation, you would:
        // 1. Parse the proxy URL to extract connection details
        // 2. Connect to the proxy server
        // 3. Send the target address through the proxy
        // 4. Relay data between client and proxy
        
        // For this implementation, we'll just acknowledge the connection
        System.out.println("Forwarding connection to " + target.getAddress() + ":" + target.getPort() + 
                          " through proxy " + proxy);
        
        // In a real implementation, you would establish a connection to the proxy
        // and relay data between the client and the proxy
    }
    
    /**
     * Inner class to hold SOCKS target information
     */
    private static class SocksTarget {
        private final String address;
        private final int port;
        
        public SocksTarget(String address, int port) {
            this.address = address;
            this.port = port;
        }
        
        public String getAddress() {
            return address;
        }
        
        public int getPort() {
            return port;
        }
    }
}