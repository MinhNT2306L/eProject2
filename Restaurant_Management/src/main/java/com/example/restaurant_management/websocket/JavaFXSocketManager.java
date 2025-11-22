package com.example.restaurant_management.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class JavaFXSocketManager {
    private static JavaFXSocketManager instance;
    private RestaurantWebSocketClient client;
    private List<Runnable> subscribers = new ArrayList<>();

    private JavaFXSocketManager() {
        try {
            client = new RestaurantWebSocketClient(new URI("ws://localhost:8887"));
            client.setOnTableUpdate(this::notifySubscribers);
            client.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static synchronized JavaFXSocketManager getInstance() {
        if (instance == null) {
            instance = new JavaFXSocketManager();
        }
        return instance;
    }

    public void subscribe(Runnable callback) {
        subscribers.add(callback);
    }

    private void notifySubscribers() {
        System.out.println("JavaFXSocketManager: Notifying " + subscribers.size() + " subscribers");
        for (Runnable subscriber : subscribers) {
            try {
                subscriber.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void reconnect() {
        if (client != null && client.isClosed()) {
            client.reconnect();
        }
    }
}
