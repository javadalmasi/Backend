package me.kavin.piped.proxy;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Socks5ProxyServer {
    
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final ProxyService proxyService;
    
    public Socks5ProxyServer(ProxyService proxyService) {
        this.proxyService = proxyService;
    }
    
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
                    System.err.println("[" + LocalDateTime.now().format(formatter) + "] Error accepting client connection: " + e.getMessage());
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
                System.err.println("[" + LocalDateTime.now().format(formatter) + "] Error closing server socket: " + e.getMessage());
            }
        }
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] SOCKS5 proxy server stopped");
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
            
            // Get current proxy
            ProxyInfo currentProxy = proxyService.getCurrentProxy();
            if (currentProxy == null) {
                System.err.println("[" + LocalDateTime.now().format(formatter) + "] No active proxies available");
                clientSocket.close();
                return;
            }
            
            // Forward the connection through the current proxy
            forwardConnection(clientSocket, target, currentProxy);
            
        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now().format(formatter) + "] Error handling client: " + e.getMessage());
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
            System.err.println("[" + LocalDateTime.now().format(formatter) + "] Unsupported SOCKS version: " + version);
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
            System.err.println("[" + LocalDateTime.now().format(formatter) + "] Invalid SOCKS version in request: " + version);
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
            System.err.println("[" + LocalDateTime.now().format(formatter) + "] Unsupported address type: " + addrType);
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
     * Forwards the connection through the selected proxy
     * @param clientSocket Client socket
     * @param target Target address
     * @param proxy Proxy to use
     */
    private void forwardConnection(Socket clientSocket, SocksTarget target, ProxyInfo proxy) {
        // Log connection info
        System.out.println("[" + LocalDateTime.now().format(formatter) + "] Forwarding connection to " + 
                          target.getAddress() + ":" + target.getPort() + 
                          " through proxy " + proxy.getDisplayInfo());
        
        // In a real implementation, you would:
        // 1. Parse the proxy URL to extract connection details
        // 2. Connect to the proxy server
        // 3. Send the target address through the proxy
        // 4. Relay data between client and proxy
        
        // For this implementation, we'll just acknowledge the connection
        try {
            // Simple relay implementation would go here
            // This is a placeholder for actual proxy forwarding logic
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println("HTTP/1.1 200 Connection Established\r\n\r\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println("[" + LocalDateTime.now().format(formatter) + "] Error forwarding connection: " + e.getMessage());
        }
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