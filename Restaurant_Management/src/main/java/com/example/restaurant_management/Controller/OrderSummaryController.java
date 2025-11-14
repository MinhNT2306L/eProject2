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
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

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
    private double totalPrice = 0.0;

    private Map<Food, Integer> orderedItems = new HashMap<>();

    // khi bàn được truyền vào
    public void setTableInfo(Table table) {
        this.currentTable = table;
        lblTableInfo.setText("Bàn " + table.getTableNumber() + " - " + table.getStatus());
        topVBox.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        loadMenu();
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
                // Create Order
                Order order = new Order();
                order.setKhId(null); // Customer ID can be null for walk-in customers
                order.setNvId(UserSession.getCurrentEmployeeId());
                order.setBanId(currentTable.getTableId());
                order.setThoiGian(LocalDateTime.now());
                order.setTongTien(totalPrice);
                order.setTrangThai("DANG_PHUC_VU"); // Order is being served

                OrderRepo orderRepo = new OrderRepo();
                int orderId = orderRepo.createOrder(order);

                // Create Order Details
                OrderDetailRepo orderDetailRepo = new OrderDetailRepo();
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

                conn.commit(); // Commit transaction

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đã gửi đơn hàng đến bếp thành công! Trạng thái bàn đã chuyển sang 'Đang phục vụ'.");
                alert.showAndWait();

                // Clear ordered items and close window
                orderedItems.clear();
                updateOrderSummary();

                // Close the order window and return to dashboard
                Stage stage = (Stage) btnPay.getScene().getWindow();
                stage.close();
                
                // Refresh dashboard to update table status
                refreshDashboard();

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
            alert.setContentText("Có lỗi xảy ra khi tạo đơn hàng: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void refreshDashboard() {
        try {
            // Tìm dashboard window thông qua owner
            Stage currentStage = (Stage) btnPay.getScene().getWindow();
            Stage dashboardStage = null;
            
            // Kiểm tra owner
            javafx.stage.Window owner = currentStage.getOwner();
            if (owner instanceof Stage) {
                dashboardStage = (Stage) owner;
            }
            
            if (dashboardStage == null) {
                // Tìm dashboard window trong tất cả các stage đang mở
                for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                    if (window instanceof Stage stage) {
                        if (stage.getTitle() != null && stage.getTitle().contains("Dashboard")) {
                            dashboardStage = stage;
                            break;
                        }
                    }
                }
            }
            
            if (dashboardStage != null && dashboardStage.getScene() != null) {
                Scene scene = dashboardStage.getScene();
                Parent root = scene.getRoot();
                
                // Tìm DashBoardController và refresh
                Object controller = root.getUserData();
                if (controller instanceof DashBoardController) {
                    ((DashBoardController) controller).refreshTableList();
                } else {
                    // Reload dashboard view
                    reloadDashboardView(dashboardStage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void reloadDashboardView(Stage dashboardStage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/restaurant_management/View/dashboard-view.fxml")
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            dashboardStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}