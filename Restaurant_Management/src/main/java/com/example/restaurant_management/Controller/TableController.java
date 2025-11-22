package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.TableMapper;
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
import java.util.List;

public class TableController {

    @FXML
    private TableView<TableModel> tableList;
    @FXML
    private TableColumn<TableModel, Integer> colId;
    @FXML
    private TableColumn<TableModel, String> colName;
    @FXML
    private TableColumn<TableModel, String> colStatus;

    private TableRepo tableRepo = new TableRepo(new TableMapper());

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
                        getClass().getResource("/com/example/restaurant_management/table.css").toExternalForm());
            }
        });
    }

    private void loadTableData() {
        List<com.example.restaurant_management.entity.Table> tables = tableRepo.getAll();
        ObservableList<TableModel> data = FXCollections.observableArrayList();
        for (com.example.restaurant_management.entity.Table table : tables) {
            data.add(new TableModel(table.getTableId(), "Bàn " + table.getTableNumber(), table.getStatus()));
        }
        tableList.setItems(data);
    }

    @FXML
    private void refreshTables(ActionEvent event) {
        loadTableData();
        System.out.println("Đã làm mới danh sách bàn từ database!");
    }

    @FXML
    private void backToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader
                .load(getClass().getResource("/com/example/restaurant_management/View/login-view.fxml"));
        Stage stage = (Stage) tableList.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("LOCAL FOOD - Đăng nhập");
        stage.show();
    }

}
