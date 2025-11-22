package com.example.restaurant_management.websocket;

import com.google.gson.JsonObject;

public class WebSocketService {
    private static WebSocketService instance;
    private RestaurantWebSocketServer server;

    private WebSocketService() {
        server = new RestaurantWebSocketServer();
    }

    public static synchronized WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }

    public void startServer() {
        server.start();
    }

    public void stopServer() throws InterruptedException {
        server.stop();
    }

    public void broadcastTableUpdate() {
        long timestamp = System.currentTimeMillis();
        String message = String.format("{\"type\":\"TABLE_UPDATE\",\"timestamp\":%d}", timestamp);
        System.out.println("Broadcasting: " + message);
        server.broadcastMessage(message);
    }

    public void broadcastNewBatchOrder(int tableId, int batchId, String itemsJson) {
        long timestamp = System.currentTimeMillis();
        String message = String.format(
                "{\"type\":\"NEW_BATCH_ORDER\",\"tableId\":%d,\"batchId\":%d,\"items\":%s,\"timestamp\":%d}",
                tableId, batchId, itemsJson, timestamp);
        System.out.println("Broadcasting Batch Order: " + message);
        server.broadcastMessage(message);
    }

    public void broadcastBatchCompleted(int tableId, int batchId) {
        long timestamp = System.currentTimeMillis();
        String message = String.format("{\"type\":\"BATCH_COMPLETED\",\"tableId\":%d,\"batchId\":%d,\"timestamp\":%d}",
                tableId, batchId, timestamp);
        System.out.println("Broadcasting Batch Completed: " + message);
        server.broadcastMessage(message);
    }
}
