package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.entity.HoaDon;
import com.example.restaurant_management.entity.OrderDetail;
import com.example.restaurant_management.entityRepo.FoodRepo;
import com.example.restaurant_management.entityRepo.HoaDonRepo;
import com.example.restaurant_management.entityRepo.OrderDetailRepo;
import com.example.restaurant_management.mapper.FoodMapper;
import com.example.restaurant_management.mapper.HoaDonMapper;
import com.example.restaurant_management.mapper.OrderDetailMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class BillController {

    @FXML private Label lblTable;
    @FXML private Label lblTotal;
    @FXML private VBox vboxItems;

    private final HoaDonRepo hoaDonRepo = new HoaDonRepo(new HoaDonMapper());
    private final OrderDetailRepo orderDetailRepo = new OrderDetailRepo(new OrderDetailMapper());
    private final FoodRepo foodRepo = new FoodRepo(new FoodMapper());

    public void loadBill(int banId) {
        HoaDon hoaDon = hoaDonRepo.findLatestByTable(banId).orElse(null);
        if (hoaDon == null) {
            if (lblTable != null) lblTable.setText("Bàn " + banId + " - Chưa có hóa đơn");
            if (lblTotal != null) lblTotal.setText("0 VND");
            new Alert(Alert.AlertType.INFORMATION, "Chưa có hóa đơn cho bàn này.").show();
            return;
        }

        Integer orderId = hoaDon.getOrderId();
        double total = hoaDon.getTongTien();
        if (orderId == null) {
            // Không có order gắn với hoá đơn (trường hợp hiếm)
            if (lblTotal != null) lblTotal.setText(String.format("%,.0f VND", total));
            if (vboxItems != null) vboxItems.getChildren().clear();
            return;
        }

        List<OrderDetail> items = orderDetailRepo.findByOrder(orderId);

        if (lblTable != null) lblTable.setText("Bàn " + banId);
        if (lblTotal != null) lblTotal.setText(String.format("%,.0f VND", total));

        if (vboxItems != null) {
            vboxItems.getChildren().clear();
            for (OrderDetail d : items) {
                String tenMon = foodRepo.findById(d.getMonId())
                        .map(Food::getFoodName)
                        .orElse("Món #" + d.getMonId());
                javafx.scene.control.Label row = new javafx.scene.control.Label(
                        tenMon + " x" + d.getSoLuong() + " — " + String.format("%,.0f VND", d.getSoLuong() * d.getDonGia())
                );
                vboxItems.getChildren().add(row);
            }
        }
    }
}
