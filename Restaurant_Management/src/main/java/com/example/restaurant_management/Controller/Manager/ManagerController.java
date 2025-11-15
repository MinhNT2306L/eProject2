package com.example.restaurant_management.Controller.Manager;

import com.example.restaurant_management.Controller.Manager.Table.AddTableController;
import com.example.restaurant_management.Controller.Manager.Table.EditTableController;
import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.TableMapper;
import com.example.restaurant_management.service.LoginService;
import com.example.restaurant_management.service.TableService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ManagerController {

    private final TableRepo tableRepo = new TableRepo(new TableMapper());

    // === CÁC VIEW CHÍNH ===
    @FXML private VBox tableView;
    @FXML private VBox orderView;
    @FXML private VBox customerView;
    @FXML private VBox reportView;
    @FXML private VBox menuManagementView;

    // === CÁC MODAL & COMPONENT ===
    @FXML private BorderPane rootPane;
    @FXML private StackPane contentArea;
    @FXML private FlowPane tableContainer;
    @FXML private StackPane addTableModal;
    @FXML private AddTableController addTableModalController;
    @FXML private StackPane editTableModal;
    @FXML private EditTableController editTableModalController;

    // === KHỞI TẠO ===
    @FXML
    public void initialize() {
        rootPane.setUserData(this);
        hideAllModals();
        showTableManager(null); // Mở mặc định Table Manager
    }

    // === ẨN TẤT CẢ MODAL ===
    private void hideAllModals() {
        if (addTableModal != null) {
            addTableModal.setVisible(false);
            addTableModal.setManaged(false);
        }
        if (editTableModal != null) {
            editTableModal.setVisible(false);
            editTableModal.setManaged(false);
        }
    }

    // === REFRESH DANH SÁCH BÀN ===
    private void refreshTableList() {
        if (tableContainer != null && tableRepo != null) {
            TableService.updateTableManager(tableContainer, tableRepo.getAll(), this::refreshTableList);
        }
    }

    // === ĐĂNG XUẤT ===
    @FXML
    public void logout(ActionEvent event) {
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        LoginService.logout(currentStage);
    }

    // === HIỂN THỊ TABLE MANAGER ===
    @FXML
    public void showTableManager(ActionEvent event) {
        hideAllViews();
        if (tableView != null) {
            refreshTableList();
            tableView.setVisible(true);
            tableView.setManaged(true);
            tableView.toFront();
        }
    }

    // === HIỂN THỊ FOOD MANAGER ===
    @FXML
    public void showFoodManager() {
        hideAllViews();
        if (menuManagementView == null) return;

        try {
            menuManagementView.getChildren().clear();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/restaurant_management/View/food-manager-view.fxml")
            );
            AnchorPane foodView = loader.load();

            // Đảm bảo foodView chiếm hết không gian
            VBox.setVgrow(foodView, Priority.ALWAYS);
            HBox.setHgrow(foodView, Priority.ALWAYS);
            foodView.setMaxWidth(Double.MAX_VALUE);
            foodView.setMaxHeight(Double.MAX_VALUE);

            menuManagementView.getChildren().add(foodView);
            menuManagementView.setVisible(true);
            menuManagementView.setManaged(true);
            menuManagementView.toFront();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Không thể tải giao diện quản lý món ăn!");
        }
    }

    // === MỞ MODAL THÊM BÀN ===
    @FXML
    public void displayAddTableForm() {
        if (addTableModal == null || addTableModalController == null) return;

        addTableModalController.setOnTableAdded(this::refreshTableList);
        addTableModalController.show();

        addTableModal.setVisible(true);
        addTableModal.setManaged(true);
        addTableModal.toFront();
    }

    // === MỞ MODAL SỬA BÀN ===
    public void showTableEditModal(Table table, Runnable onRefresh) {
        if (editTableModal == null || editTableModalController == null) return;

        editTableModalController.setTable(table);
        editTableModalController.setOnSaved(onRefresh);

        editTableModal.setVisible(true);
        editTableModal.setManaged(true);
        editTableModal.toFront();
    }

    // === ẨN TẤT CẢ CÁC VIEW CHÍNH ===
    private void hideAllViews() {
        setVisibleAndManaged(tableView, false);
        setVisibleAndManaged(orderView, false);
        setVisibleAndManaged(customerView, false);
        setVisibleAndManaged(reportView, false);
        setVisibleAndManaged(menuManagementView, false);
    }

    // === HÀM HỖ TRỢ: ẨN/HIỆN NODE AN TOÀN ===
    private void setVisibleAndManaged(Pane pane, boolean visible) {
        if (pane != null) {
            pane.setVisible(visible);
            pane.setManaged(visible);
        }
    }

    // === HIỂN THỊ LỖI ===
    private void showError(String message) {
        new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR,
                message
        ).show();
    }
}