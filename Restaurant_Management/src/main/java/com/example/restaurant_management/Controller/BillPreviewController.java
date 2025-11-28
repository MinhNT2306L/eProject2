package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Invoice;
import com.example.restaurant_management.entity.Order;
import com.example.restaurant_management.entity.OrderDetail;
import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.OrderDetailRepo;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.TableMapper;
import com.example.restaurant_management.service.BillGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.List;

public class BillPreviewController {

    @FXML
    private TextArea billTextArea;

    @FXML
    private Button printButton;

    @FXML
    private Button closeButton;

    private final OrderDetailRepo orderDetailRepo = new OrderDetailRepo();
    private final TableRepo tableRepo = new TableRepo(new TableMapper());

    @FXML
    public void initialize() {
        closeButton.setOnAction(event -> closeWindow());
    }

    public void initData(Order order) {
        try {
            // 1. Get Order Details
            List<OrderDetail> details = orderDetailRepo.findByOrderId(order.getOrderId());

            // 2. Get Table
            Table table = null;
            if (order.getBanId() != null) {
                table = tableRepo.findById(order.getBanId());
            }
            if (table == null) {
                table = new Table(0, 0, "Unknown");
            }

            // 3. Create Dummy Invoice for display
            Invoice invoice = new Invoice();
            invoice.setHoadonId(order.getOrderId()); // Use Order ID as proxy
            invoice.setThoiGian(order.getThoiGian() != null ? order.getThoiGian() : LocalDateTime.now());
            invoice.setPhuongThuc("Tiền mặt"); // Default or unknown
            invoice.setTongTien(order.getTongTien());
            invoice.setBanId(order.getBanId());

            // 4. Generate Content
            String content = BillGenerator.createBillContent(invoice, order, details, table);
            billTextArea.setText(content);

            // 5. Setup Print Action
            final int invoiceIdForFile = invoice.getHoadonId();
            printButton.setOnAction(event -> {
                BillGenerator.saveBillToFile(content, invoiceIdForFile);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thành công");
                alert.setHeaderText(null);
                alert.setContentText("Đã lưu hóa đơn vào thư mục bill!");
                alert.showAndWait();
            });

        } catch (Exception e) {
            e.printStackTrace();
            billTextArea.setText("Lỗi khi tải chi tiết đơn hàng: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
