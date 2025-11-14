package com.example.restaurant_management.service;

import com.example.restaurant_management.Controller.ManagerController;
import com.example.restaurant_management.Controller.OrderSummaryController;
import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.TableMapper;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TableService {

    // ======== Dành cho nhân viên phục vụ ========
    public static void updateTableList(FlowPane tableList, List<Table> tables) {
        updateTableList(tableList, tables, t -> {});
    }

    public static void updateTableList(FlowPane tableList, List<Table> tables, Consumer<Table> onTableClicked) {
        // Xóa nội dung cũ
        tableList.getChildren().clear();

        for (Table table : tables) {
            VBox card = new VBox();
            card.setAlignment(Pos.CENTER);
            card.setSpacing(5);
            card.setPrefHeight(100);
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                    + "-fx-padding: 10; -fx-cursor: hand; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);");

            Label lblSoBan = new Label("Bàn " + table.getTableNumber());
            lblSoBan.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Label lblTrangThai = new Label(table.getStatus());
            lblTrangThai.setStyle("-fx-font-weight: bold; -fx-text-fill: white; "
                    + "-fx-background-radius: 8; -fx-padding: 3 8 3 8;");

            switch (table.getStatus()) {
                case "TRONG" -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #27AE60;");
                case "PHUC_VU" -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #E67E22;");
                case "DAT_TRUOC" -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #C0392B;");
                default -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #7F8C8D;");
            }

            card.getChildren().addAll(lblSoBan, lblTrangThai);

            // Hiệu ứng hover
            card.setOnMouseEntered(e ->
                    card.setStyle(card.getStyle() + "-fx-scale-x: 1.05; -fx-scale-y: 1.05; "
                            + "-fx-effect: dropshadow(gaussian, rgba(255,145,77,0.4), 12, 0, 0, 3);"));
            card.setOnMouseExited(e ->
                    card.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                            + "-fx-padding: 10; -fx-cursor: hand; "
                            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);"));

            Platform.runLater(() ->
                    card.prefWidthProperty().bind(tableList.widthProperty().subtract(80).divide(3))
            );

            // Sự kiện click
            card.setOnMouseClicked(e -> {
                try {
                    // Check if table has an active order
                    com.example.restaurant_management.entityRepo.OrderRepo orderRepo = 
                        new com.example.restaurant_management.entityRepo.OrderRepo();
                    com.example.restaurant_management.entity.Order activeOrder = 
                        orderRepo.findActiveOrderByTableId(table.getTableId());

                    if (activeOrder != null && (activeOrder.getTrangThai().equals("MOI") || 
                        activeOrder.getTrangThai().equals("DANG_PHUC_VU"))) {
                        // Table has active order - open payment screen
                        FXMLLoader loader = new FXMLLoader(
                                TableService.class.getResource("/com/example/restaurant_management/View/PaymentView.fxml")
                        );
                        Parent root = loader.load();

                        com.example.restaurant_management.Controller.PaymentController controller = loader.getController();
                        controller.setTableInfo(table);

                        Stage stage = new Stage();
                        stage.setTitle("Thanh toán - Bàn " + table.getTableNumber());
                        stage.setScene(new Scene(root));
                        
                        // Set owner để có thể refresh dashboard sau khi đóng
                        Stage dashboardStage = findDashboardStage();
                        if (dashboardStage != null) {
                            stage.initOwner(dashboardStage);
                        }
                        
                        stage.show();
                    } else {
                        // No active order - open order screen
                        FXMLLoader loader = new FXMLLoader(
                                TableService.class.getResource("/com/example/restaurant_management/View/OrderSummaryView.fxml")
                        );
                        Parent root = loader.load();

                        OrderSummaryController controller = loader.getController();
                        controller.setTableInfo(table);

                        Stage stage = new Stage();
                        stage.setTitle("Order - Bàn " + table.getTableNumber());
                        stage.setScene(new Scene(root));
                        
                        // Set owner để có thể refresh dashboard sau khi đóng
                        Stage dashboardStage = findDashboardStage();
                        if (dashboardStage != null) {
                            stage.initOwner(dashboardStage);
                        }
                        
                        stage.show();
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            });

            tableList.getChildren().add(card);
        }
    }

    // ======== Dành cho phần quản lý ========
    public static void updateTableManager(FlowPane tableList, List<Table> tables, Runnable onRefresh) {
        tableList.getChildren().clear();

        // Gắn stylesheet nếu chưa có
        URL stylesheetUrl = TableService.class.getResource("/styles/tables.css");
        if (stylesheetUrl != null && tableList.getStylesheets().stream().noneMatch(s -> s.equals(stylesheetUrl.toExternalForm()))) {
            tableList.getStylesheets().add(stylesheetUrl.toExternalForm());
        }

        for (Table table : tables) {
            VBox card = new VBox();
            card.setAlignment(Pos.CENTER);
            card.setSpacing(5);
            card.setPrefHeight(120);
            card.getStyleClass().addAll("table-card", "table-card--manager");

            Label lblSoBan = new Label("Bàn " + table.getTableNumber());
            Label lblTrangThai = new Label("Trạng thái: " + table.getStatus());
            lblTrangThai.setStyle("-fx-font-weight: bold; -fx-text-fill: white; "
                    + "-fx-background-radius: 8; -fx-padding: 3 8 3 8;");

            switch (table.getStatus()) {
                case "TRONG" -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #27AE60;");
                case "PHUC_VU" -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #E67E22;");
                case "DANG_SUA" -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #E74C3C;");
                default -> lblTrangThai.setStyle(lblTrangThai.getStyle() + "-fx-background-color: #7F8C8D;");
            }

            Button btnEdit = new Button("Sửa");
            Button btnDelete = new Button("Xóa");
            HBox actions = new HBox(10, btnEdit, btnDelete);
            actions.setAlignment(Pos.CENTER);

            btnEdit.setOnAction(e -> openEditModal(table, tableList, onRefresh));
            btnDelete.setOnAction(e -> deleteTable(table.getTableId(), tableList, onRefresh));

            card.getChildren().addAll(lblSoBan, lblTrangThai, actions);

            Platform.runLater(() ->
                    card.prefWidthProperty().bind(tableList.widthProperty().subtract(80).divide(3))
            );

            tableList.getChildren().add(card);
        }
    }

    // ======== Hàm xóa bàn ========
    public static void deleteTable(int tableId, FlowPane tableList, Runnable onRefresh) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn xóa bàn này?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            TableRepo tableRepo = new TableRepo(new TableMapper());
            boolean success = tableRepo.deleteByID(tableId);

            Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle(success ? "Thành công" : "Lỗi");
            alert.setHeaderText(null);
            alert.setContentText(success ? "Đã xóa bàn thành công!" : "Không thể xóa bàn.");
            alert.showAndWait();

            if (onRefresh != null) onRefresh.run();
        }
    }

    // ======== Hàm mở modal sửa ========
    private static void openEditModal(Table table, FlowPane tableList, Runnable onRefresh) {
        Node root = tableList.getScene().getRoot();

        if (root instanceof BorderPane borderPane) {
            Object controller = borderPane.getUserData();

            if (controller instanceof ManagerController managerCtrl) {
                managerCtrl.showTableEditModal(table, onRefresh);
            } else {
                System.err.println("Không tìm thấy ManagerController trong userData của BorderPane!");
            }
        } else {
            System.err.println(" Root không phải là BorderPane, không thể mở modal edit!");
        }
    }
    
    private static Stage findDashboardStage() {
        for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
            if (window instanceof Stage) {
                Stage stage = (Stage) window;
                if (stage.getTitle() != null && stage.getTitle().contains("Dashboard")) {
                    return stage;
                }
            }
        }
        return null;
    }
}
