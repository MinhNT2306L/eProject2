package com.example.restaurant_management.Controller;

import com.example.restaurant_management.ConnectDB.ConnectDB;
import com.example.restaurant_management.entity.*;
import com.example.restaurant_management.entityRepo.*;
import com.example.restaurant_management.mapper.FoodMapper;
import com.example.restaurant_management.mapper.TableMapper;
import com.example.restaurant_management.service.BillGenerator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentController implements Initializable {

    @FXML
    private Label lblTableInfo;
    @FXML
    private Label lblInvoiceNumber;
    @FXML
    private Label lblTotalAmount;
    @FXML
    private VBox invoiceDetailsContainer;
    @FXML
    private Button btnPay;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnAddMoreItems;
    @FXML
    private Button btnCash;
    @FXML
    private Button btnBankTransfer;
    @FXML
    private Button btnConfirmPayment;
    @FXML
    private VBox paymentMethodContainer;
    @FXML
    private VBox qrCodeContainer;
    @FXML
    private ImageView qrCodeImage;

    private Table currentTable;
    private Order currentOrder;
    private List<OrderDetail> orderDetails;
    private String selectedPaymentMethod;
    private double totalAmount = 0.0;

    private OrderRepo orderRepo;
    private OrderDetailRepo orderDetailRepo;
    private FoodRepo foodRepo;
    private InvoiceRepo invoiceRepo;
    private TableRepo tableRepo;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderRepo = new OrderRepo();
        orderDetailRepo = new OrderDetailRepo();
        foodRepo = new FoodRepo(new FoodMapper());
        invoiceRepo = new InvoiceRepo();
        tableRepo = new TableRepo(new TableMapper());
    }

    public void setTableInfo(Table table) {
        this.currentTable = table;
        lblTableInfo.setText("HÓA ĐƠN THANH TOÁN - Bàn " + table.getTableNumber());

        // Find active order for this table
        currentOrder = orderRepo.findActiveOrderByTableId(table.getTableId());
        if (currentOrder == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Không tìm thấy đơn hàng đang phục vụ cho bàn này!");
            alert.showAndWait();
            closeWindow();
            return;
        }

        // Load order details
        orderDetails = orderDetailRepo.findByOrderId(currentOrder.getOrderId());
        if (orderDetails == null || orderDetails.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cảnh báo");
            alert.setHeaderText(null);
            alert.setContentText("Đơn hàng không có chi tiết!");
            alert.showAndWait();
            closeWindow();
            return;
        }

        // Display invoice details
        displayInvoiceDetails();
    }

    private void displayInvoiceDetails() {
        // Clear existing items (keep only header row and first separator)
        List<javafx.scene.Node> toRemove = new java.util.ArrayList<>();
        boolean foundHeader = false;
        boolean foundFirstSeparator = false;

        for (javafx.scene.Node node : invoiceDetailsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                if (hbox.getChildren().size() > 0 && hbox.getChildren().get(0) instanceof Label) {
                    Label firstLabel = (Label) hbox.getChildren().get(0);
                    if ("Tên món".equals(firstLabel.getText()) && !foundHeader) {
                        foundHeader = true;
                        continue; // Keep header
                    }
                }
            } else if (node instanceof Separator && !foundFirstSeparator) {
                foundFirstSeparator = true;
                continue; // Keep first separator
            }
            toRemove.add(node);
        }

        invoiceDetailsContainer.getChildren().removeAll(toRemove);

        totalAmount = 0.0;

        // Get all foods for mapping
        List<Food> allFoods = foodRepo.findAllFoods();
        java.util.Map<Integer, Food> foodMap = new java.util.HashMap<>();
        for (Food food : allFoods) {
            foodMap.put(food.getFoodId(), food);
        }

        // Display each order detail
        for (OrderDetail detail : orderDetails) {
            Food food = foodMap.get(detail.getMonId());
            if (food == null) {
                // If food not found, use placeholder
                continue;
            }

            String foodName = food.getFoodName();
            int quantity = detail.getSoLuong();
            double unitPrice = detail.getDonGia();
            double lineTotal = detail.getThanhTien();
            totalAmount += lineTotal;

            HBox itemRow = new HBox(10);
            itemRow.setPadding(new Insets(8));
            itemRow.setStyle("-fx-background-color: #ffffff;");

            Label lblFoodName = new Label(foodName);
            lblFoodName.setPrefWidth(200);
            lblFoodName.setWrapText(true);

            Label lblQuantity = new Label(String.valueOf(quantity));
            lblQuantity.setPrefWidth(80);
            lblQuantity.setStyle("-fx-alignment: center;");

            Label lblUnitPrice = new Label(String.format("%,.0f VND", unitPrice));
            lblUnitPrice.setPrefWidth(120);
            lblUnitPrice.setStyle("-fx-alignment: center-right;");

            Label lblLineTotal = new Label(String.format("%,.0f VND", lineTotal));
            lblLineTotal.setPrefWidth(120);
            lblLineTotal.setStyle("-fx-alignment: center-right; -fx-font-weight: bold;");

            itemRow.getChildren().addAll(lblFoodName, lblQuantity, lblUnitPrice, lblLineTotal);
            invoiceDetailsContainer.getChildren().add(itemRow);
        }

        // Update total amount
        lblTotalAmount.setText(String.format("%,.0f VND", totalAmount));

        // Update invoice number (using order ID as invoice number for now)
        lblInvoiceNumber.setText("Số hóa đơn: #" + currentOrder.getOrderId());
    }

    @FXML
    private void handlePayButton() {
        // Show payment method selection
        paymentMethodContainer.setVisible(true);
        paymentMethodContainer.setManaged(true);
        btnPay.setVisible(false);
        btnPay.setManaged(false);
    }

    @FXML
    private void handleCashPayment() {
        selectedPaymentMethod = "Tiền mặt";
        processPayment();
    }

    @FXML
    private void handleBankTransferPayment() {
        selectedPaymentMethod = "Chuyển khoản";
        // Show QR code
        showQRCode();
    }

    private void showQRCode() {
        // Try to load QR code image from classpath first (recommended for JavaFX)
        try {
            URL qrUrl = getClass().getResource("/com/example/restaurant_management/image/qr-code.png");
            if (qrUrl != null) {
                Image qrImage = new Image(qrUrl.toString());
                qrCodeImage.setImage(qrImage);
            } else {
                // Try file system paths as fallback
                File qrFile = new File("src/main/resources/com/example/restaurant_management/image/qr-code.png");
                if (!qrFile.exists()) {
                    qrFile = new File(
                            "Restaurant_Management/src/main/resources/com/example/restaurant_management/image/qr-code.png");
                }

                if (qrFile.exists()) {
                    Image qrImage = new Image(qrFile.toURI().toString());
                    qrCodeImage.setImage(qrImage);
                } else {
                    showQRCodeNotFoundMessage();
                }
            }
        } catch (Exception e) {
            showQRCodeNotFoundMessage();
        }

        paymentMethodContainer.setVisible(false);
        paymentMethodContainer.setManaged(false);
        qrCodeContainer.setVisible(true);
        qrCodeContainer.setManaged(true);
    }

    private void showQRCodeNotFoundMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông tin");
        alert.setHeaderText(null);
        alert.setContentText("Vui lòng thêm file QR code vào thư mục:\n" +
                "src/main/resources/com/example/restaurant_management/image/qr-code.png\n\n" +
                "Sau đó chọn 'Đã thanh toán' để hoàn tất.");
        alert.showAndWait();
    }

    @FXML
    private void handleConfirmPayment() {
        // This is called after showing QR code for bank transfer
        processPayment();
    }

    private void processPayment() {
        try {
            Connection conn = ConnectDB.getConnection();
            conn.setAutoCommit(false);

            try {
                // Create Invoice (HoaDon)
                Invoice invoice = new Invoice();
                invoice.setBanId(currentTable.getTableId());
                invoice.setTongTien(totalAmount);
                invoice.setPhuongThuc(selectedPaymentMethod);
                invoice.setThoiGian(LocalDateTime.now());

                invoiceRepo.createInvoice(invoice);

                // Update order status to DA_THANH_TOAN (Paid)
                orderRepo.updateOrderStatus(currentOrder.getOrderId(), "DA_THANH_TOAN");

                // Update table status to TRONG (Empty)
                tableRepo.updateTableStatus(conn, currentTable.getTableId(), "TRONG");

                conn.commit();

                // Generate and print bill
                BillGenerator.generateBill(invoice, currentOrder, orderDetails, currentTable);

                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Thanh toán thành công! Hóa đơn đã được in.");
                alert.showAndWait();

                // Close window
                closeWindow();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Có lỗi xảy ra khi thanh toán: " + e.getMessage());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Có lỗi xảy ra khi tạo hóa đơn: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleClose() {
        closeWindow();
    }

    @FXML
    private void handleAddMoreItems() {
        try {
            // Open OrderSummaryView for adding more items to existing order
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/restaurant_management/View/OrderSummaryView.fxml"));
            Parent root = loader.load();

            OrderSummaryController controller = loader.getController();
            // Pass the current order so it can add items to existing order
            controller.setTableInfo(currentTable);
            controller.setExistingOrder(currentOrder); // New method to set existing order

            Stage orderStage = new Stage();
            orderStage.setTitle("Thêm món - Bàn " + currentTable.getTableNumber());
            orderStage.setScene(new Scene(root));

            // When order window closes, refresh payment screen
            orderStage.setOnCloseRequest(e -> refreshOrderDetails());

            orderStage.showAndWait();

            // Refresh after window closes
            refreshOrderDetails();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText(null);
            alert.setContentText("Không thể mở màn hình thêm món: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void refreshOrderDetails() {
        // Reload order details from database
        if (currentOrder != null && currentTable != null) {
            // Refresh current order
            currentOrder = orderRepo.findActiveOrderByTableId(currentTable.getTableId());
            if (currentOrder != null) {
                orderDetails = orderDetailRepo.findByOrderId(currentOrder.getOrderId());
                displayInvoiceDetails();
            }
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
