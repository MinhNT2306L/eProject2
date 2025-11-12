package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.*;
import com.example.restaurant_management.entityRepo.FoodRepo;
import com.example.restaurant_management.mapper.FoodMapper;
import com.example.restaurant_management.service.BillGenerator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BillPreviewController implements Initializable {

    @FXML
    private Label lblInvoiceNumber;
    @FXML
    private Label lblTableNumber;
    @FXML
    private Label lblDateTime;
    @FXML
    private Label lblPaymentMethod;
    @FXML
    private Label lblTotalAmount;
    @FXML
    private VBox itemsContainer;
    @FXML
    private Button btnPrintPDF;
    @FXML
    private Button btnClose;

    private Invoice invoice;
    private Order order;
    private List<OrderDetail> orderDetails;
    private Table table;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization
    }

    public void setBillData(Invoice invoice, Order order, List<OrderDetail> orderDetails, Table table) {
        this.invoice = invoice;
        this.order = order;
        this.orderDetails = orderDetails;
        this.table = table;

        displayBill();
    }

    private void displayBill() {
        // Set invoice info
        lblInvoiceNumber.setText("#" + invoice.getHoadonId());
        lblTableNumber.setText("Bàn " + table.getTableNumber());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        lblDateTime.setText(invoice.getThoiGian().format(formatter));
        lblPaymentMethod.setText(invoice.getPhuongThuc());

        // Get food names
        FoodRepo foodRepo = new FoodRepo(new FoodMapper());
        List<Food> allFoods = foodRepo.findAllFoods();
        Map<Integer, Food> foodMap = new HashMap<>();
        for (Food food : allFoods) {
            foodMap.put(food.getFoodId(), food);
        }

        // Clear items container
        itemsContainer.getChildren().clear();

        double total = 0.0;
        for (OrderDetail detail : orderDetails) {
            Food food = foodMap.get(detail.getMonId());
            if (food == null) continue;

            String foodName = food.getFoodName();
            int quantity = detail.getSoLuong();
            double unitPrice = detail.getDonGia();
            double lineTotal = detail.getThanhTien();
            total += lineTotal;

            // Create item row
            HBox itemRow = new HBox(10);
            itemRow.setPadding(new Insets(10, 5, 10, 5));
            itemRow.setStyle("-fx-background-color: #ffffff; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

            Label lblFoodName = new Label(foodName);
            lblFoodName.setPrefWidth(200);
            lblFoodName.setWrapText(true);
            lblFoodName.setStyle("-fx-text-fill: #2C3E50;");

            Label lblQuantity = new Label(String.valueOf(quantity));
            lblQuantity.setPrefWidth(50);
            lblQuantity.setStyle("-fx-alignment: center; -fx-text-fill: #34495E;");

            Label lblUnitPrice = new Label(String.format("%,.0f VND", unitPrice));
            lblUnitPrice.setPrefWidth(120);
            lblUnitPrice.setStyle("-fx-alignment: center-right; -fx-text-fill: #34495E;");

            Label lblLineTotal = new Label(String.format("%,.0f VND", lineTotal));
            lblLineTotal.setPrefWidth(120);
            lblLineTotal.setStyle("-fx-alignment: center-right; -fx-font-weight: bold; -fx-text-fill: #E67E22;");

            itemRow.getChildren().addAll(lblFoodName, lblQuantity, lblUnitPrice, lblLineTotal);
            itemsContainer.getChildren().add(itemRow);
        }

        // Set total
        lblTotalAmount.setText(String.format("%,.0f VND", total));
    }

    @FXML
    private void handlePrintPDF() {
        try {
            // PDF is already generated, just show success message
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Hóa đơn PDF đã được lưu trong thư mục 'bill'.\nBạn có thể mở file để in.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Có lỗi xảy ra: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}

