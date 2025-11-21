package com.example.restaurant_management.Controller;

import com.example.restaurant_management.ConnectDB.ConnectDB;
import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.entity.Order;
import com.example.restaurant_management.entity.OrderDetail;
import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.FoodRepo;
import com.example.restaurant_management.entityRepo.OrderDetailRepo;
import com.example.restaurant_management.entityRepo.OrderRepo;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.FoodMapper;
import com.example.restaurant_management.mapper.TableMapper;
import com.example.restaurant_management.service.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderSummaryController {

    @FXML
    private Label lblTableInfo;
    @FXML
    private Label lblTotalPrice;
    @FXML
    private Button btnPay;
    @FXML
    private FlowPane menuContainer;
    @FXML
    private VBox orderDetailsContainer;
    @FXML
    private VBox topVBox;
    

    private Table currentTable;
    private Order existingOrder; // Order to add items to (if adding to existing order)
    private double totalPrice = 0.0;

    private Map<Food, Integer> orderedItems = new HashMap<>();
    private OrderRepo orderRepo = new OrderRepo();
    private OrderDetailRepo orderDetailRepo = new OrderDetailRepo();

    // khi bàn được truyền vào
    public void setTableInfo(Table table) {
        this.currentTable = table;
        lblTableInfo.setText("Bàn " + table.getTableNumber() + " - " + table.getStatus());
        topVBox.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        loadMenu();
    }

    // Set existing order when adding items to existing order
    public void setExistingOrder(Order order) {
        this.existingOrder = order;
        // Don't load existing items - user will only add NEW items
        // The payment screen already shows existing items
        orderedItems.clear();
        updateOrderSummary();
        // Change button text to indicate adding to existing order
        if (order != null) {
            btnPay.setText("Thêm món");
        } else {
            btnPay.setText("Gửi bếp");
        }
    }

    private void loadMenu() {
        menuContainer.getChildren().clear();
        FoodRepo repo = new FoodRepo(new FoodMapper());
        List<Food> foods = repo.findAllFoods();

        for (Food food : foods) {
            VBox card = createFoodCard(food);
            menuContainer.getChildren().add(card);
        }
    }

    private VBox createFoodCard(Food food) {
        VBox box = new VBox();
        box.setSpacing(8);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; "
                + "-fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-alignment: center;");
        box.setPrefSize(160, 150);

        Label name = new Label(food.getFoodName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label price = new Label(String.format("%,.0f VND", food.getPrice()));
        price.setStyle("-fx-text-fill: #009688; -fx-font-size: 13px;");

        Spinner<Integer> quantitySpinner = new Spinner<>(1, 20, 1);
        quantitySpinner.setPrefWidth(80);
        quantitySpinner.setStyle("-fx-font-size: 13px;");

        Button addBtn = new Button("Thêm");
        addBtn.setStyle("-fx-background-color: #009688; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");

        addBtn.setOnAction(e -> addFoodToOrder(food, quantitySpinner.getValue()));

        box.getChildren().addAll(name, price, quantitySpinner, addBtn);
        return box;
    }

    private void addFoodToOrder(Food food, int quantity) {
        // Cập nhật số lượng trong map, nếu đã có thì cộng dồn
        orderedItems.put(food, orderedItems.getOrDefault(food, 0) + quantity);

        updateOrderSummary();
    }

    private void removeFoodFromOrder(Food food) {
        orderedItems.remove(food);
        updateOrderSummary(); // Cập nhật lại UI
    }

    // HÀM MỚI: CẬP NHẬT UI HÓA ĐƠN VÀ TỔNG TIỀN
    private void updateOrderSummary() {
        // 1. Xóa toàn bộ danh sách cũ
        orderDetailsContainer.getChildren().clear();

        // 2. Reset tổng tiền
        totalPrice = 0.0;

        // 3. Tạo lại danh sách từ Map
        for (Map.Entry<Food, Integer> entry : orderedItems.entrySet()) {
            Food food = entry.getKey();
            Integer quantity = entry.getValue();
            double itemTotal = food.getPrice() * quantity;
            totalPrice += itemTotal; // Cộng dồn vào tổng tiền

            // Tạo HBox để hiển thị "Tên x Số lượng" và nút "Xóa"
            HBox itemBox = new HBox();
            itemBox.setSpacing(10);
            itemBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            itemBox.setStyle("-fx-padding: 5; -fx-background-color: #ffffff; -fx-background-radius: 5;");

            Label itemName = new Label(food.getFoodName() + " x" + quantity);
            itemName.setStyle("-fx-font-weight: bold;");
            Label itemPrice = new Label(String.format("%,.0f", itemTotal));

            // Nút "X" để xóa
            Button removeBtn = new Button("X");
            removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-padding: 1 6;");
            removeBtn.setOnAction(e -> removeFoodFromOrder(food));

            // Dùng Pane rỗng để đẩy giá và nút xóa sang phải
            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            itemBox.getChildren().addAll(itemName, spacer, itemPrice, removeBtn);
            orderDetailsContainer.getChildren().add(itemBox);
        }

        // 4. Cập nhật label tổng tiền
        lblTotalPrice.setText(String.format("%,.0f VND", totalPrice));
    }


    @FXML
    public void initialize() {
        btnPay.setText("Gửi bếp"); // Change button text to "Send to Kitchen"
        btnPay.setOnAction(e -> handleSendToKitchen());
        // Khởi tạo hóa đơn trống
        updateOrderSummary();
    }

    private void handleSendToKitchen() {
        if (orderedItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng chọn ít nhất một món ăn!");
            alert.showAndWait();
            return;
        }

        if (currentTable == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không tìm thấy thông tin bàn!");
            alert.showAndWait();
            return;
        }

        try {
            Connection conn = ConnectDB.getConnection();
            conn.setAutoCommit(false); // Start transaction

            try {
                int orderId;

                if (existingOrder != null) {
                    // Adding items to existing order
                    orderId = existingOrder.getOrderId();
                    
                    // Get existing order details to check for duplicates
                    List<OrderDetail> existingDetails = orderDetailRepo.findByOrderId(orderId);
                    
                    // Add new items to existing order
                    for (Map.Entry<Food, Integer> entry : orderedItems.entrySet()) {
                        Food food = entry.getKey();
                        Integer quantity = entry.getValue();
                        
                        // Check if this food already exists in order details
                        boolean foodExists = false;
                        for (OrderDetail existingDetail : existingDetails) {
                            if (existingDetail.getMonId().equals(food.getFoodId())) {
                                // Update existing order detail - add to current quantity
                                int newQuantity = existingDetail.getSoLuong() + quantity;
                                existingDetail.setSoLuong(newQuantity);
                                // thanh_tien is auto-calculated by database
                                orderDetailRepo.updateOrderDetail(existingDetail);
                                foodExists = true;
                                break;
                            }
                        }
                        
                        if (!foodExists) {
                            // Create new order detail
                            OrderDetail orderDetail = new OrderDetail();
                            orderDetail.setOrderId(orderId);
                            orderDetail.setMonId(food.getFoodId());
                            orderDetail.setSoLuong(quantity);
                            orderDetail.setDonGia(food.getPrice());
                            orderDetail.setThanhTien(food.getPrice() * quantity);
                            orderDetailRepo.createOrderDetail(orderDetail);
                        }
                    }
                    
                    // Recalculate total from database (to get accurate thanh_tien for updated items)
                    List<OrderDetail> updatedDetails = orderDetailRepo.findByOrderId(orderId);
                    double finalTotal = 0.0;
                    for (OrderDetail detail : updatedDetails) {
                        finalTotal += detail.getThanhTien();
                    }
                    
                    // Update order total
                    orderRepo.updateOrderTotal(orderId, finalTotal);
                    
                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thành công");
                    alert.setHeaderText(null);
                    alert.setContentText("Đã thêm món vào đơn hàng thành công!");
                    alert.showAndWait();
                    
                } else {
                    // Create new Order
                    Order order = new Order();
                    order.setKhId(null); // Customer ID can be null for walk-in customers
                    order.setNvId(UserSession.getCurrentEmployeeId());
                    order.setBanId(currentTable.getTableId());
                    order.setThoiGian(LocalDateTime.now());
                    order.setTongTien(totalPrice);
                    order.setTrangThai("DANG_PHUC_VU"); // Order is being prepared/served

                    orderId = orderRepo.createOrder(order);

                    // Create Order Details
                    for (Map.Entry<Food, Integer> entry : orderedItems.entrySet()) {
                        Food food = entry.getKey();
                        Integer quantity = entry.getValue();

                        OrderDetail orderDetail = new OrderDetail();
                        orderDetail.setOrderId(orderId);
                        orderDetail.setMonId(food.getFoodId());
                        orderDetail.setSoLuong(quantity);
                        orderDetail.setDonGia(food.getPrice());
                        orderDetail.setThanhTien(food.getPrice() * quantity);

                        orderDetailRepo.createOrderDetail(orderDetail);
                    }

                    // Update table status to PHUC_VU (Serving)
                    TableRepo tableRepo = new TableRepo(new TableMapper());
                    tableRepo.updateTableStatus(conn, currentTable.getTableId(), "PHUC_VU");

                    // Show success message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Thành công");
                    alert.setHeaderText(null);
                    alert.setContentText("Đã gửi đơn hàng đến bếp thành công!");
                    alert.showAndWait();
                }

                conn.commit(); // Commit transaction

                // Clear ordered items and close window
                orderedItems.clear();
                updateOrderSummary();

                // Close the order window
                Stage stage = (Stage) btnPay.getScene().getWindow();
                stage.close();

            } catch (SQLException e) {
                conn.rollback(); // Rollback on error
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Có lỗi xảy ra khi tạo/cập nhật đơn hàng: " + e.getMessage());
            alert.showAndWait();
        }
    }
}