package com.example.restaurant_management.websocket;

import com.google.gson.JsonObject;

public class WebSocketService {
    private static WebSocketService instance;
    private RestaurantWebSocketServer server;

    private int port = 8887;

    private WebSocketService() {
    }

    public static synchronized WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }

    public void startServer() {
        int startPort = 8887;
        int maxAttempts = 10;

        for (int i = 0; i < maxAttempts; i++) {
            int currentPort = startPort + i;
            if (isPortAvailable(currentPort)) {
                try {
                    server = new RestaurantWebSocketServer(currentPort);
                    server.start();
                    this.port = currentPort;
                    System.out.println("WebSocket Server initialized on port " + currentPort);
                    return;
                } catch (Exception e) {
                    System.err.println("Failed to start WebSocket server on port " + currentPort);
                }
            }
        }
        System.err.println("Could not find a free port for WebSocket Server after " + maxAttempts + " attempts.");
    }

    private boolean isPortAvailable(int port) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    public int getPort() {
        return port;
    }

    public void stopServer() throws InterruptedException {
        if (server != null) {
            server.stop();
        }
    }

    public void broadcastTableUpdate() {
        long timestamp = System.currentTimeMillis();
        String message = String.format("{\"type\":\"TABLE_UPDATE\",\"timestamp\":%d}", timestamp);
        System.out.println("Broadcasting: " + message);
        if (server != null) {
            server.broadcastMessage(message);
        }
    }

    public void broadcastNewBatchOrder(int tableId, String tableName, int batchId, String itemsJson) {
        long timestamp = System.currentTimeMillis();
        String message = String.format(
                "{\"type\":\"NEW_BATCH_ORDER\",\"tableId\":%d,\"tableName\":\"%s\",\"batchId\":%d,\"items\":%s,\"timestamp\":%d}",
                tableId, tableName, batchId, itemsJson, timestamp);
        System.out.println("Broadcasting Batch Order: " + message);
        if (server != null) {
            server.broadcastMessage(message);
        }
    }

    public void broadcastBatchCompleted(int tableId, int batchId) {
        long timestamp = System.currentTimeMillis();
        String message = String.format("{\"type\":\"BATCH_COMPLETED\",\"tableId\":%d,\"batchId\":%d,\"timestamp\":%d}",
                tableId, batchId, timestamp);
        System.out.println("Broadcasting Batch Completed: " + message);
        if (server != null) {
            server.broadcastMessage(message);
        }
    }
}
