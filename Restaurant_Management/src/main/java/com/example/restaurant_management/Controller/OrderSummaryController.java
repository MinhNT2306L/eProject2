package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.*;
import com.example.restaurant_management.mapper.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class OrderSummaryController {

    @FXML private Label lblTableInfo;
    @FXML private Label lblTotalPrice;
    @FXML private Button btnPay;
    @FXML private FlowPane menuContainer;
    @FXML private VBox orderDetailsContainer;
    @FXML private VBox topVBox;

    private Table currentTable;

    // Repos
    private final OrderRepo orderRepo = new OrderRepo(new OrderMapper());
    private final OrderDetailRepo orderDetailRepo = new OrderDetailRepo(new OrderDetailMapper());
    private final HoaDonRepo hoaDonRepo = new HoaDonRepo(new HoaDonMapper());
    private final TableRepo tableRepo = new TableRepo(new TableMapper());
    private final FoodRepo foodRepo = new FoodRepo(new FoodMapper());

    private Integer currentOrderId = null;

    @FXML
    public void initialize() {
        btnPay.setText("Thanh toán");
        btnPay.setOnAction(e -> handlePayment());
    }

    public void setTableInfo(Table table) {
        this.currentTable = table;
        lblTableInfo.setText("Bàn " + table.getTableNumber() + " (" + table.getStatus() + ")");
        topVBox.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        ensureOpenOrder();       // tạo/lấy order tạm
        loadMenu();              // hiển thị menu
        refreshSummary();        // vẽ hóa đơn tạm (từ DB)
    }

    private void loadMenu() {
        menuContainer.getChildren().clear();
        List<Food> foods = foodRepo.findAllFoods();

        for (Food food : foods) {
            menuContainer.getChildren().add(createFoodCard(food));
        }
    }

    private VBox createFoodCard(Food food) {
        VBox box = new VBox();
        box.setSpacing(8);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-color: #e0e0e0; -fx-border-radius: 10;");
        box.setPrefSize(160, 150);

        Label name = new Label(food.getFoodName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label price = new Label(String.format("%,.0f VND", food.getPrice()));

        Spinner<Integer> quantitySpinner = new Spinner<>(1, 20, 1);

        Button addBtn = new Button("Thêm");
        addBtn.setOnAction(e -> onAddFood(food, quantitySpinner.getValue()));

        box.getChildren().addAll(name, price, quantitySpinner, addBtn);
        return box;
    }

    private static final String TABLE_TRONG = "TRONG";
    private static final String TABLE_DANG_PHUC_VU = "DANG_PHUC_VU";
    private static final String ORDER_DANG_PHUC_VU = "DANG_PHUC_VU";

    private void ensureOpenOrder() {
        var open = orderRepo.findOpenOrderByTable(currentTable.getTableId());
        if (open.isPresent()) {
            currentOrderId = open.get().getOrderId();
            try {
                if ("MOI".equals(open.get().getTrangThai())) {
                    orderRepo.updateStatus(currentOrderId, ORDER_DANG_PHUC_VU);
                    tableRepo.updateStatusSimple(currentTable.getTableId(), TABLE_DANG_PHUC_VU);
                }
            } catch (Exception e) { showError(e); }
        } else {
            try {
                currentOrderId = orderRepo.createOrder(currentTable.getTableId(), null);
                orderRepo.updateStatus(currentOrderId, ORDER_DANG_PHUC_VU);
                tableRepo.updateStatusSimple(currentTable.getTableId(), TABLE_DANG_PHUC_VU);
            } catch (Exception e) { showError(e); }
        }
    }

    private void onAddFood(Food f, int qty) {
        try {
            ensureOpenOrder();
            orderDetailRepo.addOrUpdateItem(currentOrderId, f.getFoodId(), qty, f.getPrice());
            orderRepo.updateTotal(currentOrderId);
            refreshSummary();
        } catch (Exception e) { showError(e); }
    }

    private void refreshSummary() {
        orderDetailsContainer.getChildren().clear();
        if (currentOrderId == null) { lblTotalPrice.setText("0 VND"); return; }

        var details = orderDetailRepo.findByOrder(currentOrderId);
        double total = 0;

        for (var d : details) {
            double sub = d.getThanhTien();
            total += sub;

            Food f = foodRepo.findById(d.getMonId()).orElse(null);
            String tenMon = (f != null ? f.getFoodName() : ("Món #" + d.getMonId()));

            HBox row = new HBox(10);
            row.setStyle("-fx-padding: 6; -fx-background-color: #ffffff; -fx-background-radius: 6;");

            Label lbName = new Label(tenMon + " x" + d.getSoLuong());
            Label lbPrice = new Label(String.format("%,.0f", sub));

            Button btnRemove = new Button("X");
            btnRemove.setOnAction(e -> {
                try {
                    orderDetailRepo.removeItem(currentOrderId, d.getMonId());
                    orderRepo.updateTotal(currentOrderId);
                    refreshSummary();
                } catch (Exception ex) { showError(ex); }
            });

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row.getChildren().addAll(lbName, spacer, lbPrice, btnRemove);
            orderDetailsContainer.getChildren().add(row);
        }

        lblTotalPrice.setText(String.format("%,.0f VND", total));
    }

    private void handlePayment() {
        if (currentOrderId == null) return;
        var details = orderDetailRepo.findByOrder(currentOrderId);
        double total = details.stream().mapToDouble(d -> d.getThanhTien()).sum();

        ChoiceDialog<String> pm = new ChoiceDialog<>("TIEN_MAT", "TIEN_MAT", "CHUYEN_KHOAN");
        pm.setTitle("Thanh toán"); pm.setHeaderText("Chọn phương thức");
        var method = pm.showAndWait().orElse(null);
        if (method == null) return;

        Double khachTra = null, tienThoi = null;
        if ("TIEN_MAT".equals(method)) {
            TextInputDialog cash = new TextInputDialog();
            cash.setTitle("Tiền mặt"); cash.setHeaderText("Nhập số tiền khách đưa");
            var s = cash.showAndWait().orElse(null);
            if (s == null) return;
            khachTra = Double.parseDouble(s);
            tienThoi = Math.max(0, khachTra - total);
        }

        try {
            orderRepo.updateTotal(currentOrderId);
            orderRepo.updateStatus(currentOrderId, "DA_THANH_TOAN");
            int hoaDonId = hoaDonRepo.createFromOrder(currentOrderId, currentTable.getTableId(), total, method, khachTra, tienThoi);
            tableRepo.updateStatusSimple(currentTable.getTableId(), "TRONG");

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Thanh toán thành công");
            ok.setHeaderText("Hóa đơn #" + hoaDonId);
            ok.setContentText(
                    "Tổng: " + String.format("%,.0f", total) + " VND\nPhương thức: " + method +
                            (khachTra != null
                                    ? "\nKhách trả: " + String.format("%,.0f", khachTra) +
                                    " VND\nTiền thối: " + String.format("%,.0f", tienThoi) + " VND"
                                    : "")
            );
            ok.showAndWait();
            // Reset UI
            currentOrderId = null;
            lblTotalPrice.setText("0 VND");
            orderDetailsContainer.getChildren().clear();

        } catch (Exception e) { showError(e); }
    }

    private void showError(Exception e) {
        e.printStackTrace();
        new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
    }
}
