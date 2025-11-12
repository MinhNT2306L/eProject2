package com.example.restaurant_management.Controller;

import java.sql.Connection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import com.example.restaurant_management.ConnectDB.ConnectDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;

public class TableController {

    @FXML
    private TableView<TableModel> tableList;
    @FXML
    private TableColumn<TableModel, Integer> colId;
    @FXML
    private TableColumn<TableModel, String> colName;
    @FXML
    private TableColumn<TableModel, String> colStatus;

    private ObservableList<TableModel> data;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadTableData();

        // CSS
        tableList.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(
                        getClass().getResource("/com/example/restaurant_management/table.css").toExternalForm()
                );
            }
        });

        // ✅ DOUBLE CLICK MỞ HÓA ĐƠN
        tableList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Meaning double-click
                TableModel selected = tableList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openBillWindow(selected.getId());
                }
            }
        });
    }

    private void loadTableData() {
        data = FXCollections.observableArrayList();
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT ban_id, so_ban, trang_thai FROM ban");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                data.add(new TableModel(
                        rs.getInt("ban_id"),
                        "Bàn " + rs.getInt("so_ban"),
                        rs.getString("trang_thai")
                ));
            }

            tableList.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshTables(ActionEvent event) {
        loadTableData();
        System.out.println("Đã làm mới danh sách bàn từ database!");
    }

    // ✅ Hàm mở BillView và gọi loadBill()
    private void openBillWindow(int idBan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurant_management/View/BillView.fxml"));
            Parent root = loader.load();

            BillController controller = loader.getController();
            controller.loadBill(idBan); // GỌI LOAD HÓA ĐƠN Ở ĐÂY

            Stage stage = new Stage();
            stage.setTitle("Hóa đơn bàn " + idBan);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void backToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/restaurant_management/View/login-view.fxml"));
        Stage stage = (Stage) tableList.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("LOCAL FOOD - Đăng nhập");
        stage.show();
    }

}
