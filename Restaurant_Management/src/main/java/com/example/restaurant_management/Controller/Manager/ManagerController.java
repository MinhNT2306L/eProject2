package com.example.restaurant_management.Controller.Manager;

import com.example.restaurant_management.Controller.Manager.Table.AddTableController;
import com.example.restaurant_management.Controller.Manager.Table.EditTableController;
import com.example.restaurant_management.entity.Employee;
import com.example.restaurant_management.entity.Order;
import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.EmployeeRepo;
import com.example.restaurant_management.entityRepo.OrderRepo;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.TableMapper;
import com.example.restaurant_management.service.LoginService;
import com.example.restaurant_management.service.TableService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @FXML private VBox orderListContainer;
    @FXML private StackPane addTableModal;
    @FXML private AddTableController addTableModalController;
    @FXML private StackPane editTableModal;
    @FXML private EditTableController editTableModalController;

    // === REPOSITORIES ===
    private final OrderRepo orderRepo = new OrderRepo();
    private final EmployeeRepo employeeRepo = new EmployeeRepo();
    private final Map<Integer, Employee> employeeCache = new HashMap<>();
    private final Map<Integer, Table> tableCache = new HashMap<>();

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

    // === HIỂN THỊ ORDER MANAGER ===
    @FXML
    public void showOrderManager(ActionEvent event) {
        hideAllViews();
        if (orderView != null) {
            refreshOrderList();
            orderView.setVisible(true);
            orderView.setManaged(true);
            orderView.toFront();
        }
    }

    // === REFRESH DANH SÁCH ĐƠN HÀNG ===
    @FXML
    public void refreshOrderList(ActionEvent event) {
        refreshOrderList();
    }

    private void refreshOrderList() {
        if (orderListContainer == null) return;

        orderListContainer.getChildren().clear();

        try {
            List<Order> orders = orderRepo.getAllOrders();

            if (orders.isEmpty()) {
                Label noOrdersLabel = new Label("Chưa có đơn hàng nào");
                noOrdersLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7F8C8D;");
                orderListContainer.getChildren().add(noOrdersLabel);
                return;
            }

            // Create header row
            HBox headerRow = createOrderHeaderRow();
            orderListContainer.getChildren().add(headerRow);

            // Create order rows
            for (Order order : orders) {
                HBox orderRow = createOrderRow(order);
                orderListContainer.getChildren().add(orderRow);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
        }
    }

    // === TẠO HEADER ROW ===
    private HBox createOrderHeaderRow() {
        HBox headerRow = new HBox(10);
        headerRow.setPadding(new Insets(10));
        headerRow.setStyle("-fx-background-color: #34495E; -fx-background-radius: 5 5 0 0;");

        Label orderIdHeader = createHeaderLabel("Mã Đơn");
        Label tableHeader = createHeaderLabel("Bàn");
        Label staffHeader = createHeaderLabel("Nhân Viên");
        Label timeHeader = createHeaderLabel("Thời Gian");
        Label totalHeader = createHeaderLabel("Tổng Tiền");
        Label statusHeader = createHeaderLabel("Trạng Thái");
        Label actionHeader = createHeaderLabel("Thao Tác");

        headerRow.getChildren().addAll(orderIdHeader, tableHeader, staffHeader, timeHeader, totalHeader, statusHeader, actionHeader);
        return headerRow;
    }

    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        label.setPrefWidth(120);
        return label;
    }

    // === TẠO ORDER ROW ===
    private HBox createOrderRow(Order order) {
        HBox orderRow = new HBox(10);
        orderRow.setPadding(new Insets(12));
        orderRow.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");
        orderRow.setAlignment(Pos.CENTER_LEFT);

        // Order ID
        Label orderIdLabel = new Label("#" + order.getOrderId());
        orderIdLabel.setPrefWidth(120);
        orderIdLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        // Table Number
        String tableText = "N/A";
        if (order.getBanId() != null) {
            Table table = getTableById(order.getBanId());
            if (table != null) {
                tableText = "Bàn " + table.getTableNumber();
            }
        }
        Label tableLabel = new Label(tableText);
        tableLabel.setPrefWidth(120);

        // Staff Name
        String staffText = "N/A";
        if (order.getNvId() != null) {
            Employee employee = getEmployeeById(order.getNvId());
            if (employee != null) {
                staffText = employee.getFullName() != null ? employee.getFullName() : employee.getUsername();
            }
        }
        Label staffLabel = new Label(staffText);
        staffLabel.setPrefWidth(120);

        // Time
        String timeText = "N/A";
        if (order.getThoiGian() != null) {
            timeText = order.getThoiGian().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
        Label timeLabel = new Label(timeText);
        timeLabel.setPrefWidth(120);

        // Total
        Label totalLabel = new Label(String.format("%,.0f VND", order.getTongTien()));
        totalLabel.setPrefWidth(120);
        totalLabel.setStyle("-fx-font-weight: bold;");

        // Status with Vietnamese display
        Label statusLabel = createStatusLabel(order.getTrangThai());
        statusLabel.setPrefWidth(120);

        // Action buttons
        HBox actionBox = new HBox(5);
        actionBox.setPrefWidth(120);
        actionBox.setAlignment(Pos.CENTER);

        // Update status button (only for non-paid orders)
        if (!"DA_THANH_TOAN".equals(order.getTrangThai()) && !"DA_HUY".equals(order.getTrangThai())) {
            Button updateStatusBtn = new Button("Cập nhật");
            updateStatusBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 3;");
            updateStatusBtn.setOnAction(e -> showStatusUpdateDialog(order));
            actionBox.getChildren().add(updateStatusBtn);
        } else {
            Label completedLabel = new Label("Hoàn thành");
            completedLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 11px;");
            actionBox.getChildren().add(completedLabel);
        }

        orderRow.getChildren().addAll(orderIdLabel, tableLabel, staffLabel, timeLabel, totalLabel, statusLabel, actionBox);
        return orderRow;
    }

    // === TẠO STATUS LABEL VỚI MÀU SẮC ===
    private Label createStatusLabel(String status) {
        Label statusLabel = new Label();
        statusLabel.setPrefWidth(120);
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        statusLabel.setStyle("-fx-background-radius: 5; -fx-font-weight: bold; -fx-font-size: 12px;");

        switch (status) {
            case "DA_THANH_TOAN":
                statusLabel.setText("ĐÃ THANH TOÁN");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #27AE60; -fx-text-fill: white;");
                break;
            case "DANG_PHUC_VU":
                statusLabel.setText("ĐANG PHỤC VỤ");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #E67E22; -fx-text-fill: white;");
                break;
            case "MOI":
                statusLabel.setText("MỚI");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #3498DB; -fx-text-fill: white;");
                break;
            case "DA_HUY":
                statusLabel.setText("ĐÃ HỦY");
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #E74C3C; -fx-text-fill: white;");
                break;
            default:
                statusLabel.setText(status);
                statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #95A5A6; -fx-text-fill: white;");
        }

        return statusLabel;
    }

    // === HIỂN THỊ DIALOG CẬP NHẬT TRẠNG THÁI ===
    private void showStatusUpdateDialog(Order order) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật trạng thái đơn hàng");
        dialog.setHeaderText("Đơn hàng #" + order.getOrderId());

        ButtonType updateButtonType = new ButtonType("Cập nhật", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label currentStatusLabel = new Label("Trạng thái hiện tại: " + getStatusDisplayName(order.getTrangThai()));
        currentStatusLabel.setStyle("-fx-font-weight: bold;");

        Label newStatusLabel = new Label("Trạng thái mới:");
        ComboBox<String> statusComboBox = new ComboBox<>();
        // Add items with display names, but store database codes
        statusComboBox.getItems().addAll("DANG_PHUC_VU", "DA_THANH_TOAN");
        statusComboBox.setValue("DANG_PHUC_VU");
        statusComboBox.setPrefWidth(200);
        
        // Create a cell factory to display Vietnamese names
        statusComboBox.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(getStatusDisplayName(item));
                }
            }
        });
        
        statusComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(getStatusDisplayName(item));
                }
            }
        });

        vbox.getChildren().addAll(currentStatusLabel, newStatusLabel, statusComboBox);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                return statusComboBox.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newStatus -> {
            try {
                orderRepo.updateOrderStatus(order.getOrderId(), newStatus);
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Thành công");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Đã cập nhật trạng thái đơn hàng thành công!");
                successAlert.showAndWait();
                refreshOrderList();
            } catch (SQLException e) {
                e.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Lỗi");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("Không thể cập nhật trạng thái: " + e.getMessage());
                errorAlert.showAndWait();
            }
        });
    }

    // === LẤY TÊN HIỂN THỊ CỦA TRẠNG THÁI ===
    private String getStatusDisplayName(String status) {
        switch (status) {
            case "DA_THANH_TOAN":
                return "ĐÃ THANH TOÁN";
            case "DANG_PHUC_VU":
                return "ĐANG PHỤC VỤ";
            case "MOI":
                return "MỚI";
            case "DA_HUY":
                return "ĐÃ HỦY";
            default:
                return status;
        }
    }

    // === LẤY EMPLOYEE THEO ID (VỚI CACHE) ===
    private Employee getEmployeeById(int nvId) {
        if (employeeCache.containsKey(nvId)) {
            return employeeCache.get(nvId);
        }
        Employee employee = employeeRepo.findById(nvId);
        if (employee != null) {
            employeeCache.put(nvId, employee);
        }
        return employee;
    }

    // === LẤY TABLE THEO ID (VỚI CACHE) ===
    private Table getTableById(int banId) {
        if (tableCache.containsKey(banId)) {
            return tableCache.get(banId);
        }
        Table table = tableRepo.findById(banId);
        if (table != null) {
            tableCache.put(banId, table);
        }
        return table;
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