package com.example.restaurant_management.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class RestaurantWebSocketClient extends WebSocketClient {

    private Runnable onTableUpdate;

    public RestaurantWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    public void setOnTableUpdate(Runnable onTableUpdate) {
        this.onTableUpdate = onTableUpdate;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("JavaFX Client connected to WebSocket Server");
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.has("type") && "TABLE_UPDATE".equals(json.get("type").getAsString())) {
                System.out.println("WebSocket Client: Received TABLE_UPDATE event");
                if (onTableUpdate != null) {
                    onTableUpdate.run();
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing WebSocket message: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("JavaFX Client disconnected from WebSocket Server: " + reason);
        // Optional: Implement reconnect logic here if needed
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("WebSocket Client Error: " + ex.getMessage());
    }
}
