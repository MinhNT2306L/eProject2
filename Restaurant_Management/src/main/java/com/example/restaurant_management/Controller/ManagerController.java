package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Employee;
import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.entity.Order;
import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.EmployeeRepo;
import com.example.restaurant_management.entityRepo.FoodRepo;
import com.example.restaurant_management.entityRepo.OrderRepo;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.FoodMapper;
import com.example.restaurant_management.mapper.TableMapper;
import com.example.restaurant_management.service.LoginService;
import com.example.restaurant_management.service.TableService;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ManagerController {


    private final TableRepo tableRepo = new TableRepo(new TableMapper());
    private final FoodRepo foodRepo = new FoodRepo(new FoodMapper());
    private final OrderRepo orderRepo = new OrderRepo();
    private final EmployeeRepo employeeRepo = new EmployeeRepo();
    
    @FXML private BorderPane rootPane;
    @FXML private AddTableController addTableModalController;
    @FXML private StackPane contentArea;
    @FXML private FlowPane tableContainer;
	@FXML private VBox tableView;
    @FXML private StackPane addTableModal;
    @FXML private StackPane editTableModal;
    @FXML private EditTableController editTableModalController;
    @FXML private StackPane addEmployeeModal;
    @FXML private AddEmployeeController addEmployeeModalController;
    @FXML private StackPane addIngredientModal;
    @FXML private AddIngredientController addIngredientModalController;
    
    // Các view khác
    @FXML private VBox orderView;
    @FXML private VBox menuManagementView;
    @FXML private VBox customerView;
    @FXML private VBox ingredientView;
    @FXML private VBox reportView;
    
    // Containers cho dữ liệu
    @FXML private VBox orderListContainer;
    @FXML private VBox dishListContainer;
    @FXML private VBox customerListContainer;
    @FXML
    public void initialize() {
        rootPane.setUserData(this);
        if (addTableModal != null) addTableModal.setVisible(false);
        if (editTableModal != null) editTableModal.setVisible(false);
        if (addEmployeeModal != null) addEmployeeModal.setVisible(false);
        if (addIngredientModal != null) addIngredientModal.setVisible(false);

        if (tableView != null) {
            refreshTableList();
            tableView.setVisible(true);
            tableView.setManaged(true);
        }
    }

    private void refreshTableList() {
        TableService.updateTableManager(tableContainer, tableRepo.getAll(), this::refreshTableList);
    }



    @FXML
    public void logout(ActionEvent event){
        Stage currentStage =(Stage) (((Node) event.getSource()).getScene().getWindow());
        LoginService.logout(currentStage);
    }

    @FXML
    public void showTableManager(ActionEvent event){
		// Ẩn tất cả các view, nhưng không ẩn modal (addTableModal, addEmployeeModal, addIngredientModal)
		contentArea.getChildren().forEach(node -> {
			if (node != addTableModal && node != addEmployeeModal && node != addIngredientModal) {
				node.setVisible(false);
				node.setManaged(false);
			}
		});
        refreshTableList();
		tableView.setVisible(true);
		tableView.setManaged(true);
		tableView.toFront();
		// Đảm bảo modal không chặn sự kiện khi bị ẩn
		if (addTableModal != null && !addTableModal.isVisible()) {
			addTableModal.setMouseTransparent(true);
		}
		if (addEmployeeModal != null && !addEmployeeModal.isVisible()) {
			addEmployeeModal.setMouseTransparent(true);
		}
		if (addIngredientModal != null && !addIngredientModal.isVisible()) {
			addIngredientModal.setMouseTransparent(true);
		}
	}

    @FXML
    public void displayAddTableForm() {
        if (addTableModal != null) {
            addTableModal.setMouseTransparent(false);
            addTableModal.toFront();
        }
        if (addTableModalController != null) {
            // Setup callback để refresh danh sách bàn sau khi thêm thành công
            addTableModalController.setOnTableAdded(this::refreshTableList);
            addTableModalController.show();
        }
    }

    public void showTableEditModal(Table table, Runnable onRefresh) {
        editTableModalController.setTable(table);
        editTableModalController.setOnSaved(onRefresh);

        editTableModal.setVisible(true);
        editTableModal.setManaged(true);
        editTableModal.toFront();
    }

    // ============ Các method handler cho các menu ============
    
    @FXML
    public void showOrderManager(ActionEvent event) {
        hideAllViews();
        loadOrderData();
        orderView.setVisible(true);
        orderView.setManaged(true);
        orderView.toFront();
    }

    @FXML
    public void showMenuManager(ActionEvent event) {
        hideAllViews();
        loadMenuData();
        menuManagementView.setVisible(true);
        menuManagementView.setManaged(true);
        menuManagementView.toFront();
    }

    @FXML
    public void showCustomerManager(ActionEvent event) {
        hideAllViews();
        loadCustomerData();
        customerView.setVisible(true);
        customerView.setManaged(true);
        customerView.toFront();
    }

    @FXML
    public void showIngredientManager(ActionEvent event) {
        hideAllViews();
        ingredientView.setVisible(true);
        ingredientView.setManaged(true);
        ingredientView.toFront();
        // Refresh dữ liệu nguyên liệu và truyền modal
        try {
            // Lấy controller từ userData của ingredientView (được set trong initialize)
            Object controllerObj = ingredientView.getUserData();
            IngredientController controller = null;
            if (controllerObj instanceof IngredientController) {
                controller = (IngredientController) controllerObj;
            } else {
                // Nếu chưa có, tìm trong scene graph
                controller = findControllerInScene(ingredientView, IngredientController.class);
            }
            if (controller != null) {
                // Truyền modal và controller vào IngredientController
                controller.setModal(addIngredientModal, addIngredientModalController);
                controller.refresh();
            }
        } catch (Exception e) {
            // Controller sẽ tự load trong initialize()
        }
    }

    // Helper method để tìm controller trong scene graph
    private <T> T findControllerInScene(javafx.scene.Node node, Class<T> controllerClass) {
        if (node == null) return null;
        Object userData = node.getUserData();
        if (userData != null && controllerClass.isInstance(userData)) {
            return controllerClass.cast(userData);
        }
        if (node instanceof javafx.scene.Parent) {
            for (javafx.scene.Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                T result = findControllerInScene(child, controllerClass);
                if (result != null) return result;
            }
        }
        return null;
    }

    @FXML
    public void showReportView(ActionEvent event) {
        hideAllViews();
        reportView.setVisible(true);
        reportView.setManaged(true);
        reportView.toFront();
    }

    // ============ Helper methods ============
    
    private void hideAllViews() {
        if (tableView != null) {
            tableView.setVisible(false);
            tableView.setManaged(false);
        }
        if (orderView != null) {
            orderView.setVisible(false);
            orderView.setManaged(false);
        }
        if (menuManagementView != null) {
            menuManagementView.setVisible(false);
            menuManagementView.setManaged(false);
        }
        if (customerView != null) {
            customerView.setVisible(false);
            customerView.setManaged(false);
        }
        if (ingredientView != null) {
            ingredientView.setVisible(false);
            ingredientView.setManaged(false);
        }
        if (reportView != null) {
            reportView.setVisible(false);
            reportView.setManaged(false);
        }
    }

    private void loadOrderData() {
        if (orderListContainer == null) return;
        
        orderListContainer.getChildren().clear();
        
        try {
            List<Order> orders = orderRepo.getAllOrders();
            
            if (orders.isEmpty()) {
                Label emptyLabel = new Label("Chưa có đơn hàng nào");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                orderListContainer.getChildren().add(emptyLabel);
                return;
            }

            // Lấy danh sách bàn một lần và tạo map để tra cứu nhanh
            List<Table> allTables = tableRepo.getAll();
            java.util.Map<Integer, Integer> tableIdToNumberMap = new java.util.HashMap<>();
            for (Table table : allTables) {
                tableIdToNumberMap.put(table.getTableId(), table.getTableNumber());
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (Order order : orders) {
                HBox orderCard = createOrderCard(order, formatter, tableIdToNumberMap);
                orderListContainer.getChildren().add(orderCard);
            }
        } catch (Exception e) {
            Label errorLabel = new Label("Lỗi khi tải dữ liệu đơn hàng: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            orderListContainer.getChildren().add(errorLabel);
        }
    }

    private HBox createOrderCard(Order order, DateTimeFormatter formatter, java.util.Map<Integer, Integer> tableIdToNumberMap) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-radius: 5;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        VBox infoBox = new VBox(5);
        
        Label orderIdLabel = new Label("Đơn #" + order.getOrderId());
        orderIdLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label timeLabel = new Label("Thời gian: " + (order.getThoiGian() != null ? order.getThoiGian().format(formatter) : "N/A"));
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        // Lấy số bàn từ map
        String tableNumberText = "N/A";
        if (order.getBanId() != null) {
            Integer tableNumber = tableIdToNumberMap.get(order.getBanId());
            if (tableNumber != null) {
                tableNumberText = "Bàn #" + tableNumber;
            } else {
                tableNumberText = "Bàn ID: " + order.getBanId();
            }
        }
        
        Label tableLabel = new Label("Bàn: " + tableNumberText);
        tableLabel.setStyle("-fx-font-size: 12px;");
        
        Label totalLabel = new Label("Tổng tiền: " + String.format("%,.0f", order.getTongTien()) + " đ");
        totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        Label statusLabel = new Label("Trạng thái: " + getStatusText(order.getTrangThai()));
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getStatusColor(order.getTrangThai()) + ";");
        
        infoBox.getChildren().addAll(orderIdLabel, timeLabel, tableLabel, totalLabel, statusLabel);
        
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        card.getChildren().add(infoBox);
        
        return card;
    }

    private String getStatusText(String status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case "MOI": return "Mới";
            case "DANG_PHUC_VU": return "Đang phục vụ";
            case "DA_THANH_TOAN": return "Đã thanh toán";
            case "DA_HUY": return "Đã hủy";
            default: return status;
        }
    }

    private String getStatusColor(String status) {
        if (status == null) return "#666";
        switch (status) {
            case "MOI": return "#1976d2";
            case "DANG_PHUC_VU": return "#f57c00";
            case "DA_THANH_TOAN": return "#2e7d32";
            case "DA_HUY": return "#c62828";
            default: return "#666";
        }
    }

    private void loadMenuData() {
        if (dishListContainer == null) return;
        
        dishListContainer.getChildren().clear();
        
        try {
            List<Food> foods = foodRepo.getAll();
            
            if (foods.isEmpty()) {
                Label emptyLabel = new Label("Chưa có món ăn nào");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                dishListContainer.getChildren().add(emptyLabel);
                return;
            }

            for (Food food : foods) {
                HBox foodCard = createFoodCard(food);
                dishListContainer.getChildren().add(foodCard);
            }
        } catch (Exception e) {
            Label errorLabel = new Label("Lỗi khi tải dữ liệu món ăn: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            dishListContainer.getChildren().add(errorLabel);
        }
    }

    private HBox createFoodCard(Food food) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-radius: 5;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        VBox infoBox = new VBox(5);
        
        Label nameLabel = new Label(food.getFoodName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label categoryLabel = new Label("Loại: " + (food.getFoodCategory() != null ? food.getFoodCategory() : "N/A"));
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        if (food.getDescription() != null && !food.getDescription().isEmpty()) {
            Label descLabel = new Label(food.getDescription());
            descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-wrap-text: true;");
            descLabel.setMaxWidth(400);
            infoBox.getChildren().add(descLabel);
        }
        
        Label priceLabel = new Label("Giá: " + String.format("%,.0f", food.getPrice()) + " đ");
        priceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
        
        infoBox.getChildren().addAll(nameLabel, categoryLabel, priceLabel);
        
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        card.getChildren().add(infoBox);
        
        return card;
    }

    private void loadCustomerData() {
        if (customerListContainer == null) return;
        
        customerListContainer.getChildren().clear();
        
        try {
            List<Employee> employees = employeeRepo.getAll();
            
            if (employees.isEmpty()) {
                Label emptyLabel = new Label("Chưa có nhân viên nào");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                customerListContainer.getChildren().add(emptyLabel);
                return;
            }

            // Lấy danh sách roles để hiển thị tên role
            List<Map<String, Object>> roles = employeeRepo.getAllRoles();
            Map<Integer, String> roleIdToNameMap = new java.util.HashMap<>();
            for (Map<String, Object> role : roles) {
                roleIdToNameMap.put((Integer) role.get("roleId"), (String) role.get("roleName"));
            }

            for (Employee employee : employees) {
                HBox employeeCard = createEmployeeCard(employee, roleIdToNameMap);
                customerListContainer.getChildren().add(employeeCard);
            }
        } catch (Exception e) {
            Label errorLabel = new Label("Lỗi khi tải dữ liệu nhân viên: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            customerListContainer.getChildren().add(errorLabel);
        }
    }

    private HBox createEmployeeCard(Employee employee, Map<Integer, String> roleIdToNameMap) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-radius: 5;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        VBox infoBox = new VBox(5);
        
        Label nameLabel = new Label(employee.getFullName() != null ? employee.getFullName() : "N/A");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label usernameLabel = new Label("Username: " + employee.getUsername());
        usernameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label phoneLabel = new Label("SĐT: " + (employee.getPhone() != null ? employee.getPhone() : "N/A"));
        phoneLabel.setStyle("-fx-font-size: 12px;");
        
        Label emailLabel = new Label("Email: " + (employee.getEmail() != null ? employee.getEmail() : "N/A"));
        emailLabel.setStyle("-fx-font-size: 12px;");
        
        String roleName = "N/A";
        if (employee.getRoleId() != null) {
            roleName = roleIdToNameMap.getOrDefault(employee.getRoleId(), "N/A");
        }
        Label roleLabel = new Label("Vai trò: " + roleName);
        roleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1976d2;");
        
        infoBox.getChildren().addAll(nameLabel, usernameLabel, phoneLabel, emailLabel, roleLabel);
        
        // Nút sửa và xóa
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Sửa");
        editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15;");
        editBtn.setOnAction(e -> showEditEmployeeModal(employee));
        
        Button deleteBtn = new Button("Xóa");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15;");
        deleteBtn.setOnAction(e -> deleteEmployee(employee));
        
        actionBox.getChildren().addAll(editBtn, deleteBtn);
        
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        card.getChildren().addAll(infoBox, actionBox);
        
        return card;
    }

    @FXML
    public void displayAddEmployeeForm() {
        if (addEmployeeModal != null) {
            addEmployeeModal.setVisible(true);
            addEmployeeModal.setManaged(true);
            addEmployeeModal.setMouseTransparent(false);
            addEmployeeModal.toFront();
        }
        if (addEmployeeModalController != null) {
            addEmployeeModalController.setEmployee(null); // null = thêm mới
            addEmployeeModalController.setOnEmployeeAdded(this::refreshEmployeeList);
            addEmployeeModalController.show();
        }
    }

    private void showEditEmployeeModal(Employee employee) {
        if (addEmployeeModal != null) {
            addEmployeeModal.setVisible(true);
            addEmployeeModal.setManaged(true);
            addEmployeeModal.setMouseTransparent(false);
            addEmployeeModal.toFront();
        }
        if (addEmployeeModalController != null) {
            addEmployeeModalController.setEmployee(employee); // có giá trị = sửa
            addEmployeeModalController.setOnEmployeeAdded(this::refreshEmployeeList);
            addEmployeeModalController.show();
        }
    }

    private void deleteEmployee(Employee employee) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa nhân viên \"" + employee.getFullName() + "\"?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = employeeRepo.delete(employee.getNvId());
            Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
            alert.setTitle(success ? "Thành công" : "Lỗi");
            alert.setHeaderText(null);
            alert.setContentText(success ? "Đã xóa nhân viên thành công!" : "Không thể xóa nhân viên.");
            alert.showAndWait();
            
            if (success) {
                refreshEmployeeList();
            }
        }
    }

    private void refreshEmployeeList() {
        loadCustomerData();
    }

}
