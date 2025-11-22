package com.example.restaurant_management.api;

import com.example.restaurant_management.entity.Employee;
import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.entity.Order;
import com.example.restaurant_management.entity.OrderDetail;
import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.EmployeeRepo;
import com.example.restaurant_management.entityRepo.FoodRepo;
import com.example.restaurant_management.entityRepo.OrderDetailRepo;
import com.example.restaurant_management.entityRepo.OrderRepo;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.FoodMapper;
import com.example.restaurant_management.mapper.TableMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RestApiServer {
    private HttpServer server;
    private final FoodRepo foodRepo;
    private final OrderRepo orderRepo;
    private final OrderDetailRepo orderDetailRepo;
    private final TableRepo tableRepo;
    private final EmployeeRepo employeeRepo;
    private final Gson gson;
    private static final int DEFAULT_PORT = 8080;
    private int actualPort = DEFAULT_PORT;

    // Simple token storage (in production, use Redis or database)
    private final Map<String, TokenInfo> tokens = new HashMap<>();

    private static class TokenInfo {
        int employeeId;
        String username;
        long expiryTime;

        TokenInfo(int employeeId, String username) {
            this.employeeId = employeeId;
            this.username = username;
            this.expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 hours
        }

        boolean isValid() {
            return System.currentTimeMillis() < expiryTime;
        }
    }

    public RestApiServer() {
        this.foodRepo = new FoodRepo(new FoodMapper());
        this.orderRepo = new OrderRepo();
        this.orderDetailRepo = new OrderDetailRepo();
        this.tableRepo = new TableRepo(new TableMapper());
        this.employeeRepo = new EmployeeRepo();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void start() throws IOException {
        // Try to start on default port, or find an available port
        int port = DEFAULT_PORT;
        int maxAttempts = 10;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
                actualPort = port;
                break;
            } catch (IOException e) {
                if (attempt == maxAttempts - 1) {
                    throw new IOException("Failed to start server after " + maxAttempts + " attempts. " +
                            "Port " + DEFAULT_PORT + " and nearby ports are in use. " +
                            "Please close the application using port " + DEFAULT_PORT + " or restart your computer.",
                            e);
                }
                port++;
                System.out.println("Port " + (port - 1) + " is in use, trying port " + port + "...");
            }
        }

        // Handle OPTIONS for CORS preflight
        server.createContext("/api", this::handleOptions);

        // POST /api/auth/login - Authentication (public)
        server.createContext("/api/auth/login", this::handleLogin);

        // GET /api/menu - Get menu items (public for now, can be protected)
        server.createContext("/api/menu", this::handleGetMenu);

        // GET /api/tables - Get available tables (public)
        server.createContext("/api/tables", this::handleGetTables);

        // POST /api/orders - Create new order (protected)
        server.createContext("/api/orders", this::handlePostOrder);

        // GET /api/orders/active - Get active orders (protected)
        server.createContext("/api/orders/active", this::handleGetActiveOrders);

        // GET /api/kitchen/orders - Get active kitchen orders (protected)
        server.createContext("/api/kitchen/orders", this::handleGetKitchenOrders);

        // POST /api/kitchen/complete - Complete a batch order (protected)
        server.createContext("/api/kitchen/complete", this::handleCompleteBatchOrder);

        // Dynamic order routes - GET /api/orders/{id}, POST /api/orders/{id}/items,
        // POST /api/orders/{id}/pay
        server.createContext("/api/orders/", this::handleOrderRoutes);

        // Serve static web files
        server.createContext("/", this::handleStaticFiles);

        server.setExecutor(null);
        server.start();

        // Start WebSocket Server
        com.example.restaurant_management.websocket.WebSocketService.getInstance().startServer();

        System.out.println("REST API Server started on http://localhost:" + actualPort);
        if (actualPort != DEFAULT_PORT) {
            System.out.println("Note: Port " + DEFAULT_PORT + " was in use, using port " + actualPort + " instead.");
        }
    }

    public int getPort() {
        return actualPort;
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            try {
                com.example.restaurant_management.websocket.WebSocketService.getInstance().stopServer();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("REST API Server stopped");
        }
    }

    private void handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        }
    }

    private void handleGetMenu(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        try {
            List<Food> foods = foodRepo.findAllFoods();

            // Convert to JSON array
            List<JsonObject> foodList = new ArrayList<>();
            for (Food food : foods) {
                JsonObject foodJson = new JsonObject();
                foodJson.addProperty("id", food.getFoodId());
                foodJson.addProperty("name", food.getFoodName());
                foodJson.addProperty("category", food.getFoodCategory() != null ? food.getFoodCategory() : "");
                foodJson.addProperty("price", food.getPrice());
                foodJson.addProperty("status", food.getStatus() != null ? food.getStatus() : "");
                foodJson.addProperty("description", food.getDescription() != null ? food.getDescription() : "");
                foodList.add(foodJson);
            }

            String json = gson.toJson(foodList);
            sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendJsonResponse(exchange, 500, gson.toJson(error));
        }
    }

    private void handleGetTables(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        try {
            List<Table> tables = tableRepo.getAll();

            List<JsonObject> tableList = new ArrayList<>();
            for (Table table : tables) {
                JsonObject tableJson = new JsonObject();
                tableJson.addProperty("id", table.getTableId());
                tableJson.addProperty("number", table.getTableNumber());
                tableJson.addProperty("status", table.getStatus() != null ? table.getStatus() : "");
                tableList.add(tableJson);
            }

            String json = gson.toJson(tableList);
            sendJsonResponse(exchange, 200, json);
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendJsonResponse(exchange, 500, gson.toJson(error));
        }
    }

    private void handlePostOrder(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        Integer staffId = validateToken(exchange);
        if (staffId == null) {
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        try {
            // Read request body
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            // Parse JSON using Gson
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();

            if (!jsonRequest.has("items") || jsonRequest.get("items").getAsJsonArray().size() == 0) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Order must contain at least one item");
                sendJsonResponse(exchange, 400, gson.toJson(error));
                return;
            }

            // Extract order data
            Integer tableId = jsonRequest.has("tableId") && !jsonRequest.get("tableId").isJsonNull()
                    ? jsonRequest.get("tableId").getAsInt()
                    : null;
            // Use authenticated staff ID

            // Create order
            Order order = new Order();
            order.setBanId(tableId);
            order.setNvId(staffId); // Use authenticated staff ID
            order.setThoiGian(LocalDateTime.now());
            order.setTrangThai("DANG_PHUC_VU");

            // Calculate total and validate items
            double total = 0.0;
            var itemsArray = jsonRequest.get("items").getAsJsonArray();

            for (int i = 0; i < itemsArray.size(); i++) {
                JsonObject item = itemsArray.get(i).getAsJsonObject();
                int foodId = item.get("foodId").getAsInt();
                int quantity = item.get("quantity").getAsInt();

                if (quantity <= 0)
                    continue;

                Food food = foodRepo.findById(foodId);
                if (food != null && "CON_HANG".equals(food.getStatus())) {
                    total += food.getPrice() * quantity;
                }
            }

            if (total == 0) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "No valid items in order");
                sendJsonResponse(exchange, 400, gson.toJson(error));
                return;
            }

            order.setTongTien(total);

            // Save order
            int orderId;
            try {
                orderId = orderRepo.createOrder(order);
            } catch (java.sql.SQLException e) {
                throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
            }

            // Save order details
            for (int i = 0; i < itemsArray.size(); i++) {
                JsonObject item = itemsArray.get(i).getAsJsonObject();
                int foodId = item.get("foodId").getAsInt();
                int quantity = item.get("quantity").getAsInt();

                if (quantity <= 0)
                    continue;

                Food food = foodRepo.findById(foodId);
                if (food != null && "CON_HANG".equals(food.getStatus())) {
                    OrderDetail detail = new OrderDetail();
                    detail.setOrderId(orderId);
                    detail.setMonId(foodId);
                    detail.setSoLuong(quantity);
                    detail.setDonGia(food.getPrice());
                    detail.setThanhTien(food.getPrice() * quantity);
                    try {
                        orderDetailRepo.createOrderDetail(detail);
                    } catch (java.sql.SQLException e) {
                        throw new RuntimeException("Failed to create order detail: " + e.getMessage(), e);
                    }
                }
            }

            // Update table status if tableId is provided
            if (tableId != null) {
                try {
                    java.sql.Connection conn = com.example.restaurant_management.ConnectDB.ConnectDB.getConnection();
                    tableRepo.updateTableStatus(conn, tableId, "PHUC_VU");
                    // Broadcast update
                    com.example.restaurant_management.websocket.WebSocketService.getInstance().broadcastTableUpdate();
                } catch (Exception e) {
                    // Log but don't fail the order
                    System.err.println("Failed to update table status: " + e.getMessage());
                }
            }

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("orderId", orderId);
            response.addProperty("message", "Order created successfully");
            response.addProperty("total", total);

            sendJsonResponse(exchange, 201, gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendJsonResponse(exchange, 500, gson.toJson(error));
        }
    }

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Default to index.html
        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        // Try to load from resources
        try {
            String resourcePath = "/web" + path;
            java.io.InputStream is = getClass().getResourceAsStream(resourcePath);

            if (is == null) {
                sendResponse(exchange, 404, "File not found");
                return;
            }

            byte[] content = is.readAllBytes();
            String contentType = getContentType(path);

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, content.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
            is.close();
        } catch (Exception e) {
            sendResponse(exchange, 500, "Error serving file: " + e.getMessage());
        }
    }

    // Authentication methods
    private void handleLogin(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();

            String username = jsonRequest.has("username") ? jsonRequest.get("username").getAsString() : "";
            String password = jsonRequest.has("password") ? jsonRequest.get("password").getAsString() : "";

            if (username.isEmpty() || password.isEmpty()) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Username and password are required");
                sendJsonResponse(exchange, 400, gson.toJson(error));
                return;
            }

            Employee employee = employeeRepo.findByUsernameAndPassword(username, password);
            if (employee == null) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Invalid username or password");
                sendJsonResponse(exchange, 401, gson.toJson(error));
                return;
            }

            // Generate token
            String token = UUID.randomUUID().toString();
            tokens.put(token, new TokenInfo(employee.getNvId(), employee.getUsername()));

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("token", token);
            response.addProperty("employeeId", employee.getNvId());
            response.addProperty("username", employee.getUsername());
            response.addProperty("fullName", employee.getFullName() != null ? employee.getFullName() : "");

            sendJsonResponse(exchange, 200, gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendJsonResponse(exchange, 500, gson.toJson(error));
        }
    }

    private Integer validateToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        TokenInfo tokenInfo = tokens.get(token);

        if (tokenInfo == null || !tokenInfo.isValid()) {
            // Clean up expired tokens
            if (tokenInfo != null) {
                tokens.remove(token);
            }
            return null;
        }

        return tokenInfo.employeeId;
    }

    private void handleGetActiveOrders(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        Integer staffId = validateToken(exchange);
        if (staffId == null) {
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        try {
            List<Order> allOrders = orderRepo.getAllOrders();
            List<Order> activeOrders = new ArrayList<>();

            for (Order order : allOrders) {
                if ("DANG_PHUC_VU".equals(order.getTrangThai()) || "MOI".equals(order.getTrangThai())) {
                    activeOrders.add(order);
                }
            }

            JsonArray ordersJson = new JsonArray();
            for (Order order : activeOrders) {
                JsonObject orderJson = new JsonObject();
                orderJson.addProperty("id", order.getOrderId());
                orderJson.addProperty("tableId", order.getBanId() != null ? order.getBanId() : 0);
                orderJson.addProperty("total", order.getTongTien());
                orderJson.addProperty("status", order.getTrangThai());
                orderJson.addProperty("time", order.getThoiGian() != null ? order.getThoiGian().toString() : "");

                // Check if any item is READY
                boolean hasReadyItems = false;
                List<OrderDetail> details = orderDetailRepo.findByOrderId(order.getOrderId());
                for (OrderDetail detail : details) {
                    if ("READY".equals(detail.getTrangThai())) {
                        hasReadyItems = true;
                        break;
                    }
                }
                orderJson.addProperty("hasReadyItems", hasReadyItems);

                ordersJson.add(orderJson);
            }

            sendJsonResponse(exchange, 200, gson.toJson(ordersJson));
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendJsonResponse(exchange, 500, gson.toJson(error));
        }
    }

    private void handleServeItem(HttpExchange exchange, String[] parts) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        Integer staffId = validateToken(exchange);
        if (staffId == null) {
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        try {
            // Path: /api/orders/{orderId}/items/{foodId}/serve
            if (parts.length < 6) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request path\"}");
                return;
            }

            int orderId = Integer.parseInt(parts[parts.length - 4]);
            int foodId = Integer.parseInt(parts[parts.length - 2]);

            // Update item status to SERVED in database
            // Note: We need to update specific item. Since we don't have unique ID in URL,
            // we update all items of this food in this order that are READY.
            // Or better, if we have order_ct_id, we should use it.
            // But current API structure uses foodId.
            // Let's update all READY items of this food type in this order.

            try (java.sql.Connection conn = com.example.restaurant_management.ConnectDB.ConnectDB.getConnection()) {
                String sql = "UPDATE order_chitiet SET trang_thai = 'SERVED' WHERE order_id = ? AND mon_id = ? AND trang_thai = 'READY'";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, foodId);
                    int rowsUpdated = ps.executeUpdate();

                    if (rowsUpdated == 0) {
                        // Maybe it was already served or not ready?
                        // Let's check if it exists
                    }
                }
            }

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "Item served successfully");
            sendJsonResponse(exchange, 200, gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendJsonResponse(exchange, 500, gson.toJson(error));
        }
    }

    private void handleOrderRoutes(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        Integer staffId = validateToken(exchange);
        if (staffId == null) {
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");

        // Route: /api/orders/{id}/pay (POST) - Check this first
        if (parts.length >= 4 && "pay".equals(parts[parts.length - 1]) && "POST".equals(exchange.getRequestMethod())) {
            handlePayOrder(exchange, parts);
            return;
        }

        // Route: /api/orders/{id}/items (POST)
        if (parts.length >= 5 && "items".equals(parts[parts.length - 1])
                && "POST".equals(exchange.getRequestMethod())) {
            handleAddItemsToOrder(exchange, parts);
            return;
        }

        // Route: /api/orders/{id} (GET)
        if (parts.length >= 4 && "GET".equals(exchange.getRequestMethod())) {
            handleGetOrderDetail(exchange, parts);
            return;
        }

        // Route: /api/orders/{id}/items/{foodId}/serve (POST)
        if (parts.length >= 6 && "items".equals(parts[parts.length - 3]) && "serve".equals(parts[parts.length - 1])
                && "POST".equals(exchange.getRequestMethod())) {
            handleServeItem(exchange, parts);
            return;
        }

        // Route: /api/orders/{id}/serve-all (POST)
        if (parts.length >= 4 && "serve-all".equals(parts[parts.length - 1])
                && "POST".equals(exchange.getRequestMethod())) {
            handleServeAllItems(exchange, parts);
            return;
        }

        sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
    }

    private void handleServeAllItems(HttpExchange exchange, String[] parts) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        Integer staffId = validateToken(exchange);
        if (staffId == null) {
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        try {
            // Path: /api/orders/{orderId}/serve-all
            if (parts.length < 4) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request path\"}");
                return;
            }

            int orderId = Integer.parseInt(parts[parts.length - 2]);

            try (java.sql.Connection conn = com.example.restaurant_management.ConnectDB.ConnectDB.getConnection()) {
                String sql = "UPDATE order_chitiet SET trang_thai = 'SERVED' WHERE order_id = ? AND trang_thai = 'READY'";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, orderId);
                    int rowsUpdated = ps.executeUpdate();
                    System.out
                            .println("Served all items for order " + orderId + ". Updated " + rowsUpdated + " items.");
                }
            }

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("message", "All ready items served successfully");
            sendJsonResponse(exchange, 200, gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendJsonResponse(exchange, 500, gson.toJson(error));
        }
    }

    private void handleGetOrderDetail(HttpExchange exchange, String[] parts) throws IOException {
        try {
            if (parts.length < 4) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid order ID\"}");
                return;
            }

            int orderId = Integer.parseInt(parts[parts.length - 1]);
            Order order = orderRepo.findById(orderId);

            if (order == null) {
                sendJsonResponse(exchange, 404, "{\"error\":\"Order not found\"}");
                return;
            }

            // Get order details
            List<OrderDetail> details = orderDetailRepo.findByOrderId(orderId);

            JsonObject orderJson = new JsonObject();
            orderJson.addProperty("id", order.getOrderId());
            orderJson.addProperty("tableId", order.getBanId() != null ? order.getBanId() : 0);
            orderJson.addProperty("staffId", order.getNvId() != null ? order.getNvId() : 0);
            orderJson.addProperty("total", order.getTongTien());
            orderJson.addProperty("status", order.getTrangThai());
            orderJson.addProperty("time", order.getThoiGian() != null ? order.getThoiGian().toString() : "");

            List<JsonObject> itemsList = new ArrayList<>();
            for (OrderDetail detail : details) {
                // Check if monId is null before proceeding
                if (detail.getMonId() != null) {
                    Food food = foodRepo.findById(detail.getMonId());
                    if (food != null) {
                        JsonObject itemJson = new JsonObject();
                        itemJson.addProperty("foodId", detail.getMonId());
                        itemJson.addProperty("foodName", food.getFoodName());
                        itemJson.addProperty("quantity", detail.getSoLuong());
                        itemJson.addProperty("unitPrice", detail.getDonGia());
                        itemJson.addProperty("total", detail.getThanhTien());
                        itemJson.addProperty("status", detail.getTrangThai());
                        itemsList.add(itemJson);
                    }
                }
            }
            orderJson.add("items", gson.toJsonTree(itemsList));

            sendJsonResponse(exchange, 200, gson.toJson(orderJson));
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendJsonResponse(exchange, 500, gson.toJson(error));
        }
    }

    private void handleAddItemsToOrder(HttpExchange exchange, String[] parts) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            handleOptions(exchange);
            return;
        }

        Integer staffId = validateToken(exchange);
        if (staffId == null) {
            sendJsonResponse(exchange, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }

        try {
            if (parts.length < 5 || !"items".equals(parts[parts.length - 1])) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request path\"}");
                return;
            }

            int orderId = Integer.parseInt(parts[parts.length - 2]);
            Order order = orderRepo.findById(orderId);

            if (order == null) {
                sendJsonResponse(exchange, 404, "{\"error\":\"Order not found\"}");
                return;
            }

            // Read request body
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();

            if (!jsonRequest.has("items") || jsonRequest.get("items").getAsJsonArray().size() == 0) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Must add at least one item");
                sendJsonResponse(exchange, 400, gson.toJson(error));
                return;
            }

            var itemsArray = jsonRequest.get("items").getAsJsonArray();

            java.sql.Connection conn = null;
            try {
                conn = com.example.restaurant_management.ConnectDB.ConnectDB.getConnection();
                conn.setAutoCommit(false); // Start transaction

                // Calculate Batch ID
                int batchId = 1;
                // Check if order already has items to determine if it's a new batch
                String maxBatchSql = "SELECT MAX(batch_id) FROM order_chitiet WHERE order_id = ?";
                try (java.sql.PreparedStatement psBatch = conn.prepareStatement(maxBatchSql)) {
                    psBatch.setInt(1, orderId);
                    try (java.sql.ResultSet rsBatch = psBatch.executeQuery()) {
                        if (rsBatch.next() && rsBatch.getObject(1) != null) {
                            batchId = rsBatch.getInt(1) + 1;
                        }
                    }
                }

                // Insert items with batch_id
                String insertItemSql = "INSERT INTO order_chitiet (order_id, mon_id, so_luong, don_gia, batch_id) VALUES (?, ?, ?, ?, ?)";
                try (java.sql.PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                    for (com.google.gson.JsonElement itemElement : itemsArray) {
                        JsonObject item = itemElement.getAsJsonObject();
                        int foodId = item.get("foodId").getAsInt();
                        int quantity = item.get("quantity").getAsInt();

                        if (quantity <= 0)
                            continue;

                        // Get price
                        double price = 0;
                        String priceSql = "SELECT gia FROM monan WHERE mon_id = ?";
                        try (java.sql.PreparedStatement psPrice = conn.prepareStatement(priceSql)) {
                            psPrice.setInt(1, foodId);
                            try (java.sql.ResultSet rsPrice = psPrice.executeQuery()) {
                                if (rsPrice.next()) {
                                    price = rsPrice.getDouble("gia");
                                }
                            }
                        }

                        psItem.setInt(1, orderId);
                        psItem.setInt(2, foodId);
                        psItem.setInt(3, quantity);
                        psItem.setDouble(4, price);
                        psItem.setInt(5, batchId);
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }

                // Update total amount
                String updateTotalSql = "UPDATE orders SET tong_tien = (SELECT SUM(thanh_tien) FROM order_chitiet WHERE order_id = ?) WHERE order_id = ?";
                try (java.sql.PreparedStatement psUpdate = conn.prepareStatement(updateTotalSql)) {
                    psUpdate.setInt(1, orderId);
                    psUpdate.setInt(2, orderId);
                    psUpdate.executeUpdate();
                }

                conn.commit(); // Commit transaction

                // Broadcast updates
                com.example.restaurant_management.websocket.WebSocketService.getInstance().broadcastTableUpdate();

                // Broadcast New Batch Order for Kitchen
                com.google.gson.JsonArray broadcastItems = new com.google.gson.JsonArray();
                // Need to get tableId from the order object
                int tableId = order.getBanId() != null ? order.getBanId() : 0;
                for (com.google.gson.JsonElement itemElement : itemsArray) {
                    JsonObject item = itemElement.getAsJsonObject();
                    int foodId = item.get("foodId").getAsInt();
                    int quantity = item.get("quantity").getAsInt();

                    String itemName = "";
                    String nameSql = "SELECT ten_mon FROM monan WHERE mon_id = ?";
                    try (java.sql.PreparedStatement psName = conn.prepareStatement(nameSql)) {
                        psName.setInt(1, foodId);
                        try (java.sql.ResultSet rsName = psName.executeQuery()) {
                            if (rsName.next())
                                itemName = rsName.getString("ten_mon");
                        }
                    }

                    JsonObject broadcastItem = new JsonObject();
                    broadcastItem.addProperty("name", itemName);
                    broadcastItem.addProperty("quantity", quantity);
                    broadcastItems.add(broadcastItem);
                }

                com.example.restaurant_management.websocket.WebSocketService.getInstance()
                        .broadcastNewBatchOrder(tableId, batchId, broadcastItems.toString());

                JsonObject response = new JsonObject();
                response.addProperty("message", "Items added successfully");
                response.addProperty("orderId", orderId);
                response.addProperty("batchId", batchId);

                sendJsonResponse(exchange, 200, gson.toJson(response));

            } catch (java.sql.SQLException e) { // Catch for SQL exceptions during transaction
                e.printStackTrace();
                // Rollback transaction in case of error
                try {
                    if (conn != null)
                        conn.rollback();
                } catch (java.sql.SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
                JsonObject error = new JsonObject();
                error.addProperty("error", "Database error: " + e.getMessage());
                sendJsonResponse(exchange, 500, gson.toJson(error));
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (java.sql.SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) { // General catch for other exceptions in the method
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendJsonResponse(exchange, 500, gson.toJson(error));
        }
    }

    private void handlePayOrder(HttpExchange exchange, String[] parts) throws IOException {
        try {
            if (parts.length < 4 || !"pay".equals(parts[parts.length - 1])) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid request path\"}");
                return;
            }

            int orderId = Integer.parseInt(parts[parts.length - 2]);
            Order order = orderRepo.findById(orderId);

            if (order == null) {
                sendJsonResponse(exchange, 404, "{\"error\":\"Order not found\"}");
                return;
            }

            // Check if order is already paid
            if ("DA_THANH_TOAN".equals(order.getTrangThai())) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "ORDER_ALREADY_PAID");
                sendJsonResponse(exchange, 400, gson.toJson(error));
                return;
            }

            // Check if order is in a payable status
            if (!"DANG_PHUC_VU".equals(order.getTrangThai()) && !"MOI".equals(order.getTrangThai())) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Order is not in a payable status");
                sendJsonResponse(exchange, 400, gson.toJson(error));
                return;
            }

            // Read request body
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonRequest = JsonParser.parseString(requestBody).getAsJsonObject();

            String paymentMethod = jsonRequest.has("paymentMethod") && !jsonRequest.get("paymentMethod").isJsonNull()
                    ? jsonRequest.get("paymentMethod").getAsString()
                    : "";
            double amountPaid = jsonRequest.has("amountPaid") && !jsonRequest.get("amountPaid").isJsonNull()
                    ? jsonRequest.get("amountPaid").getAsDouble()
                    : 0.0;
            String note = null;
            if (jsonRequest.has("note") && !jsonRequest.get("note").isJsonNull()) {
                note = jsonRequest.get("note").getAsString();
            }

            // Validate payment method
            if (!"CASH".equals(paymentMethod) && !"BANK_TRANSFER".equals(paymentMethod)) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Invalid payment method. Must be CASH or BANK_TRANSFER");
                sendJsonResponse(exchange, 400, gson.toJson(error));
                return;
            }

            // Recalculate total from order details
            List<OrderDetail> details = orderDetailRepo.findByOrderId(orderId);
            double totalAmount = 0.0;
            for (OrderDetail detail : details) {
                totalAmount += detail.getThanhTien();
            }

            // Update order total if needed
            if (Math.abs(order.getTongTien() - totalAmount) > 0.01) {
                try {
                    orderRepo.updateOrderTotal(orderId, totalAmount);
                    order.setTongTien(totalAmount);
                } catch (java.sql.SQLException e) {
                    throw new RuntimeException("Failed to update order total: " + e.getMessage(), e);
                }
            }

            // Validate amount paid
            if (amountPaid < totalAmount) {
                JsonObject error = new JsonObject();
                error.addProperty("error", "Amount paid must be greater than or equal to total amount");
                sendJsonResponse(exchange, 400, gson.toJson(error));
                return;
            }

            // Save payment information to database
            // We'll create a payment record in a payments table or update the order
            // For now, we'll update the order status and save payment info to a payments
            // table
            // First, let's create the payment record
            try {
                java.sql.Connection conn = com.example.restaurant_management.ConnectDB.ConnectDB.getConnection();

                // Create payment record
                // Create payment record in 'hoadon' table
                String paymentSql = "INSERT INTO hoadon (ban_id, tong_tien, phuong_thuc, thoi_gian) VALUES (?, ?, ?, ?)";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(paymentSql)) {
                    if (order.getBanId() != null) {
                        ps.setInt(1, order.getBanId());
                    } else {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    }
                    ps.setDouble(2, amountPaid);
                    ps.setString(3, paymentMethod);
                    ps.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                    ps.executeUpdate();
                }

                // Update order status
                orderRepo.updateOrderStatus(orderId, "DA_THANH_TOAN");

                // Update table status if tableId is provided
                if (order.getBanId() != null) {
                    try {
                        tableRepo.updateTableStatus(conn, order.getBanId(), "TRONG");
                    } catch (Exception e) {
                        System.err.println("Failed to update table status: " + e.getMessage());
                    }
                }

            } catch (java.sql.SQLException e) {
                // If payments table doesn't exist, just update order status
                // This allows the feature to work even without the payments table
                try {
                    orderRepo.updateOrderStatus(orderId, "DA_THANH_TOAN");

                    // Update table status if tableId is provided (Fallback)
                    if (order.getBanId() != null) {
                        try {
                            // Use tableRepo's connection since the local 'conn' is out of scope
                            tableRepo.updateTableStatus(tableRepo.getConn(), order.getBanId(), "TRONG");
                        } catch (Exception ex) {
                            System.err.println("Failed to update table status in fallback: " + ex.getMessage());
                        }
                    }
                } catch (java.sql.SQLException ex) {
                    throw new RuntimeException("Failed to update order status: " + ex.getMessage(), ex);
                }
            }

            // Build response
            JsonObject response = new JsonObject();
            response.addProperty("orderId", orderId);
            response.addProperty("status", "DA_THANH_TOAN");
            response.addProperty("paymentMethod", paymentMethod);
            response.addProperty("totalAmount", totalAmount);
            response.addProperty("amountPaid", amountPaid);
            response.addProperty("paidAt", LocalDateTime.now().toString());
            if (note != null) {
                response.addProperty("note", note);
            }

            // Broadcast update
            com.example.restaurant_management.websocket.WebSocketService.getInstance().broadcastTableUpdate();

            sendJsonResponse(exchange, 200, gson.toJson(response));

        } catch (NumberFormatException e) {
            JsonObject error = new JsonObject();
            error.addProperty("error", "Invalid order ID");
            sendJsonResponse(exchange, 400, gson.toJson(error));
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("error", e.getMessage());
            sendJsonResponse(exchange, 500, gson.toJson(error));
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        sendResponse(exchange, statusCode, json);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void handleGetKitchenOrders(HttpExchange exchange) throws IOException {
        System.out.println("Received request for /api/kitchen/orders"); // Debug log
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method not allowed");
            return;
        }

        try (java.sql.Connection conn = com.example.restaurant_management.ConnectDB.ConnectDB.getConnection()) {
            // Query to get items grouped by table and batch, only for active orders
            String sql = "SELECT o.ban_id, od.batch_id, m.ten_mon, od.so_luong, o.thoi_gian, od.trang_thai " +
                    "FROM order_chitiet od " +
                    "JOIN orders o ON od.order_id = o.order_id " +
                    "JOIN monan m ON od.mon_id = m.mon_id " +
                    "WHERE o.trang_thai IN ('MOI', 'DANG_PHUC_VU') " +
                    "ORDER BY o.thoi_gian DESC, od.batch_id DESC";

            System.out.println("Executing SQL: " + sql); // Debug log

            java.util.Map<String, com.google.gson.JsonObject> batchMap = new java.util.LinkedHashMap<>();

            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
                    java.sql.ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int tableId = rs.getInt("ban_id");
                    int batchId = rs.getInt("batch_id");
                    String foodName = rs.getString("ten_mon");
                    int quantity = rs.getInt("so_luong");
                    java.sql.Timestamp timestamp = rs.getTimestamp("thoi_gian");
                    String status = rs.getString("trang_thai");

                    String key = tableId + "-" + batchId;

                    if (!batchMap.containsKey(key)) {
                        com.google.gson.JsonObject batch = new com.google.gson.JsonObject();
                        batch.addProperty("tableId", tableId);
                        batch.addProperty("batchId", batchId);
                        batch.addProperty("timestamp", timestamp.getTime());
                        // We can infer batch status from items, or just use the first item's status if
                        // they are uniform
                        // For now, let's just pass items and let frontend decide
                        batch.add("items", new com.google.gson.JsonArray());
                        batchMap.put(key, batch);
                    }

                    com.google.gson.JsonObject item = new com.google.gson.JsonObject();
                    item.addProperty("name", foodName);
                    item.addProperty("quantity", quantity);
                    item.addProperty("status", status);
                    batchMap.get(key).getAsJsonArray("items").add(item);
                }
            }

            System.out.println("Found batches: " + batchMap.size()); // Debug log

            com.google.gson.JsonArray result = new com.google.gson.JsonArray();
            for (com.google.gson.JsonObject batch : batchMap.values()) {
                result.add(batch);
            }

            String jsonResponse = gson.toJson(result);
            System.out.println("Sending response: " + jsonResponse); // Debug log
            sendJsonResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".html"))
            return "text/html; charset=UTF-8";
        if (path.endsWith(".css"))
            return "text/css";
        if (path.endsWith(".js"))
            return "application/javascript";
        if (path.endsWith(".json"))
            return "application/json";
        return "text/plain";
    }

    private void handleCompleteBatchOrder(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method not allowed");
            return;
        }

        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8);
            JsonObject jsonRequest = gson.fromJson(requestBody, JsonObject.class);

            int tableId = jsonRequest.get("tableId").getAsInt();
            int batchId = jsonRequest.get("batchId").getAsInt();

            // Idempotency Guard: Check if items are already processed
            boolean shouldUpdate = false;
            try (java.sql.Connection conn = com.example.restaurant_management.ConnectDB.ConnectDB.getConnection()) {
                // Check if there are any items that are NOT READY/SERVED (i.e. PENDING or NULL)
                String checkSql = "SELECT COUNT(*) FROM order_chitiet od " +
                        "JOIN orders o ON od.order_id = o.order_id " +
                        "WHERE o.ban_id = ? AND od.batch_id = ? AND (od.trang_thai IS NULL OR od.trang_thai = 'PENDING')";

                try (java.sql.PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setInt(1, tableId);
                    psCheck.setInt(2, batchId);
                    try (java.sql.ResultSet rsCheck = psCheck.executeQuery()) {
                        if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                            shouldUpdate = true;
                        }
                    }
                }

                if (shouldUpdate) {
                    String updateSql = "UPDATE order_chitiet od " +
                            "JOIN orders o ON od.order_id = o.order_id " +
                            "SET od.trang_thai = 'READY' " +
                            "WHERE o.ban_id = ? AND od.batch_id = ? AND (od.trang_thai IS NULL OR od.trang_thai = 'PENDING')";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        ps.setInt(1, tableId);
                        ps.setInt(2, batchId);
                        int rowsUpdated = ps.executeUpdate();
                        System.out.println(
                                "Updated " + rowsUpdated + " items to READY for table " + tableId + " batch "
                                        + batchId);
                    }

                    // Broadcast completion event ONLY if update happened
                    com.example.restaurant_management.websocket.WebSocketService.getInstance().broadcastBatchCompleted(
                            tableId,
                            batchId);
                } else {
                    System.out.println("Batch " + batchId + " for table " + tableId
                            + " already processed. Skipping update and broadcast.");
                }
            }

            JsonObject response = new JsonObject();
            response.addProperty("message", "Batch completed successfully");
            sendJsonResponse(exchange, 200, gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
