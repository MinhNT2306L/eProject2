package com.example.restaurant_management.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RestaurantWebSocketServer extends WebSocketServer {

    private static final int PORT = 8887;
    private final Set<WebSocket> conns = Collections.synchronizedSet(new HashSet<>());

    public RestaurantWebSocketServer() {
        super(new InetSocketAddress(PORT));
        setReuseAddr(true);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conns.add(conn);
        System.out.println("New WebSocket connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        conns.remove(conn);
        System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message from " + conn.getRemoteSocketAddress() + ": " + message);
        // Handle incoming messages if needed (e.g., join room)
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            conns.remove(conn);
        }
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket Server started on port: " + PORT);
    }

    /**
     * Broadcasts a message to all connected clients.
     * 
     * @param message The message to broadcast
     */
    public void broadcastMessage(String message) {
        System.out.println("Broadcasting: " + message);
        synchronized (conns) {
            for (WebSocket conn : conns) {
                conn.send(message);
            }
        }
    }
}
