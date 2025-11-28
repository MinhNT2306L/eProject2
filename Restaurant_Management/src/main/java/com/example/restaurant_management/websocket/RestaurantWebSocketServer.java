package com.example.restaurant_management.websocket;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RestaurantWebSocketServer extends WebSocketServer {

    private int port;
    private final Set<WebSocket> conns = Collections.synchronizedSet(new HashSet<>());

    public RestaurantWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        this.port = port;
        setReuseAddr(true);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conns.add(conn);
        System.out.println("🟢 [CONNECT] Client mới: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        conns.remove(conn);
        System.out.println("🔴 [CLOSE] Client ngắt kết nối: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // --- CLEAN LOGGING ---
        System.out.println("\n=================== 📨 TIN NHẮN MỚI ===================");
        System.out.println("👤 Từ: " + conn.getRemoteSocketAddress());
        System.out.println("📦 Nội dung JSON: " + message);

        if (message.contains("ORDER")) {
            System.out.println("👉 Hành động: GỌI MÓN / ORDER MỚI");
        } else if (message.contains("PAYMENT")) {
            System.out.println("👉 Hành động: THANH TOÁN");
        } else if (message.contains("TABLE_UPDATE")) {
            System.out.println("👉 Hành động: CẬP NHẬT TRẠNG THÁI BÀN");
        }
        System.out.println("========================================================\n");

        broadcastMessage(message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("⚠️ [ERROR] Lỗi WebSocket: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("🚀 Server WebSocket đã khởi động tại cổng: " + port);
    }

    public void broadcastMessage(String message) {
        // Log ONCE outside the loop
        System.out.println("📡 [BROADCAST] Đang đồng bộ tới " + conns.size() + " clients...");
        for (WebSocket sock : conns) {
            if (sock.isOpen()) {
                sock.send(message);
            }
        }
        System.out.println("✅ [DONE] Đã gửi xong!\n");
    }
}
