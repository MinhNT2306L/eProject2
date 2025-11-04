package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.FoodRepo;
import com.example.restaurant_management.mapper.FoodMapper;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
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

    // Biến FXML mới từ file FXML
    @FXML
    private VBox orderDetailsContainer;

    // Biến FXML cho VBox ở <top>
    @FXML
    private VBox topVBox;

    private Table currentTable;
    private double totalPrice = 0.0;

    // Map để lưu các món đã order: Key = Food, Value = Số lượng
    private Map<Food, Integer> orderedItems = new HashMap<>();

    // khi bàn được truyền vào
    public void setTableInfo(Table table) {
        this.currentTable = table;
        lblTableInfo.setText("Bàn " + table.getTableNumber() + " - " + table.getStatus());
        // Style cho VBox top
        topVBox.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        loadMenu();
    }

    // tải danh sách món ăn từ DB (Giữ nguyên)
    private void loadMenu() {
        menuContainer.getChildren().clear();
        FoodRepo repo = new FoodRepo(new FoodMapper());
        List<Food> foods = repo.findAllFoods();

        for (Food food : foods) {
            VBox card = createFoodCard(food);
            menuContainer.getChildren().add(card);
        }
    }

    // tạo thẻ hiển thị món ăn (SỬ DỤNG PHIÊN BẢN Ở BƯỚC 1)
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

        // Cập nhật: Khi nhấn nút, lấy cả số lượng
        addBtn.setOnAction(e -> addFoodToOrder(food, quantitySpinner.getValue()));

        box.getChildren().addAll(name, price, quantitySpinner, addBtn);
        return box;
    }

    // XỬ LÝ KHI THÊM MÓN (CẬP NHẬT)
    private void addFoodToOrder(Food food, int quantity) {
        // Cập nhật số lượng trong map, nếu đã có thì cộng dồn
        orderedItems.put(food, orderedItems.getOrDefault(food, 0) + quantity);

        // Cập nhật lại toàn bộ UI hóa đơn và tổng tiền
        updateOrderSummary();
    }

    // HÀM MỚI: XÓA MỘT MÓN KHỎI ORDER
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
        btnPay.setOnAction(e -> handlePayment());
        // Khởi tạo hóa đơn trống
        updateOrderSummary();
    }

    private void handlePayment() {
        System.out.println("Thanh toán tổng: " + totalPrice + " VND cho bàn " + currentTable.getTableNumber());
        System.out.println("Chi tiết order:");
        for (Map.Entry<Food, Integer> entry : orderedItems.entrySet()) {
            System.out.println(entry.getKey().getFoodName() + " - Số lượng: " + entry.getValue());
        }

        // TODO: Ghi `orderedItems` (Map) và `totalPrice` vào CSDL
        // Bạn sẽ cần tạo 2 bảng: `orders` (table_id, total_price, status)
        // và `order_details` (order_id, food_id, quantity, price)
    }
}