package com.example.restaurant_management.Controller;

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
    }

    private void loadTableData() {
        data = FXCollections.observableArrayList(
                new TableModel(1, "Bàn 1", "Trống"),
                new TableModel(2, "Bàn 2", "Đã đặt"),
                new TableModel(3, "Bàn 3", "Trống"),
                new TableModel(4, "Bàn 4", "Trống")
        );
        tableList.setItems(data);
    }

    @FXML
    private void refreshTables(ActionEvent event) {
        // Làm mới danh sách bàn — ở đây tạm dùng lại dữ liệu mẫu
        ObservableList<TableModel> data = FXCollections.observableArrayList(
                new TableModel(1, "Bàn 1", "Trống"),
                new TableModel(2, "Bàn 2", "Đã đặt"),
                new TableModel(3, "Bàn 3", "Trống"),
                new TableModel(4, "Bàn 4", "Đang phục vụ")
        );
        tableList.setItems(data);

        System.out.println("Đã làm mới danh sách bàn!");
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
