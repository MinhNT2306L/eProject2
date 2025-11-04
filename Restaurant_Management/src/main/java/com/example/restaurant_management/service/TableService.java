package com.example.restaurant_management.service;

import com.example.restaurant_management.entity.Table;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import com.example.restaurant_management.Controller.OrderSummaryController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class TableService {
    public static void updateTableList(FlowPane tableList, List<Table> tables) {
        // Xóa nội dung cũ trước khi cập nhật
        tableList.getChildren().clear();

        for (Table table : tables) {
            // Tạo VBox làm card cho mỗi bàn
            VBox card = new VBox();
            card.setAlignment(Pos.CENTER);
            card.setSpacing(5);
            card.setPrefHeight(100);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                    + "-fx-padding: 10; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);");

            // Thêm label hiển thị thông tin
            Label lblSoBan = new Label("Bàn " + table.getTableNumber());
            lblSoBan.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");


            Label lblTrangThai = new Label(table.getStatus());
            lblTrangThai.setStyle("-fx-font-weight: bold; -fx-text-fill: white; "
                    + "-fx-background-radius: 8; -fx-padding: 3 8 3 8;");

            // Đặt màu cho trạng thái
            switch (table.getStatus()) {
                case "TRONG" -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #27AE60;");
                case "PHUC_VU" -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #E67E22;");
                case "DAT_TRUOC" -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #C0392B;");
                default -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #7F8C8D;");
            }

            // Gắn các label vào VBox
            card.getChildren().addAll(lblSoBan, lblTrangThai);

            // Hiệu ứng hover
            card.setOnMouseEntered(e ->
                    card.setStyle(card.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05; -fx-effect: dropshadow(gaussian, rgba(255,145,77,0.4), 12, 0, 0, 3);")
            );
            card.setOnMouseExited(e ->
                    card.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                            + "-fx-padding: 10; -fx-cursor: hand; "
                            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);")
            );

            Platform.runLater(()->{
                card.prefWidthProperty().bind(
                        tableList.widthProperty().subtract(80).divide(3)
                );
            });

            // Gắn sự kiện click chọn bàn
           // TODO: xử lý mở giao diện order cho bàn này
            card.setOnMouseClicked(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(
                            TableService.class.getResource("/com/example/restaurant_management/View/OrderSummaryView.fxml")
                    );
                    Parent root = loader.load();

                    // Lấy controller
                    OrderSummaryController controller = loader.getController();

                    // Truyền dữ liệu bàn vào controller
                    controller.setTableInfo(table);

                    // Tạo stage mới để hiển thị giao diện Order
                    Stage stage = new Stage();
                    stage.setTitle("Order - Bàn " + table.getTableNumber());
                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });


            // Thêm card vào FlowPane
            tableList.getChildren().add(card);
        }
    }
}
