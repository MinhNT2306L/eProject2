package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Employee;
import com.example.restaurant_management.entityRepo.EmployeeRepo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EmployeeController {

    private final EmployeeRepo employeeRepo = new EmployeeRepo();
    
    @FXML private VBox employeeListContainer;
    
    // Modal sẽ được lấy từ ManagerController
    private AddEmployeeController addEmployeeModalController;
    private javafx.scene.layout.StackPane addEmployeeModal;
    
    // Method để set modal từ ManagerController
    public void setModal(javafx.scene.layout.StackPane modal, AddEmployeeController controller) {
        this.addEmployeeModal = modal;
        this.addEmployeeModalController = controller;
    }

    @FXML
    private VBox employeeViewRoot; // Root của view này
    
    @FXML
    public void initialize() {
        // Lưu controller vào userData để có thể truy cập từ bên ngoài
        if (employeeViewRoot != null) {
            employeeViewRoot.setUserData(this);
        }
        loadEmployeeData();
    }

    // Method public để refresh dữ liệu từ bên ngoài
    public void refresh() {
        loadEmployeeData();
    }

    public void loadEmployeeData() {
        if (employeeListContainer == null) return;
        
        employeeListContainer.getChildren().clear();
        
        try {
            List<Employee> employees = employeeRepo.getAll();
            
            if (employees.isEmpty()) {
                Label emptyLabel = new Label("Chưa có nhân viên nào");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                employeeListContainer.getChildren().add(emptyLabel);
                return;
            }

            // Lấy danh sách roles để map roleId sang roleName
            List<Map<String, Object>> roles = employeeRepo.getAllRoles();
            Map<Integer, String> roleMap = new java.util.HashMap<>();
            for (Map<String, Object> role : roles) {
                roleMap.put((Integer) role.get("roleId"), (String) role.get("roleName"));
            }
            
            for (Employee employee : employees) {
                HBox employeeCard = createEmployeeCard(employee, roleMap);
                employeeListContainer.getChildren().add(employeeCard);
            }
        } catch (Exception e) {
            Label errorLabel = new Label("Lỗi khi tải dữ liệu nhân viên: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            employeeListContainer.getChildren().add(errorLabel);
        }
    }

    private HBox createEmployeeCard(Employee employee, Map<Integer, String> roleMap) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-radius: 5;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        VBox infoBox = new VBox(5);
        
        Label nameLabel = new Label(employee.getFullName() != null ? employee.getFullName() : employee.getUsername());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label usernameLabel = new Label("Username: " + employee.getUsername());
        usernameLabel.setStyle("-fx-font-size: 12px;");
        
        Label phoneLabel = new Label("SĐT: " + (employee.getPhone() != null ? employee.getPhone() : "N/A"));
        phoneLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        Label emailLabel = new Label("Email: " + (employee.getEmail() != null ? employee.getEmail() : "N/A"));
        emailLabel.setStyle("-fx-font-size: 12px;");
        
        String roleName = "N/A";
        if (employee.getRoleId() != null && roleMap.containsKey(employee.getRoleId())) {
            roleName = roleMap.get(employee.getRoleId());
        }
        Label roleLabel = new Label("Vai trò: " + roleName);
        roleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        
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
            addEmployeeModalController.setOnEmployeeAdded(this::loadEmployeeData);
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
            addEmployeeModalController.setOnEmployeeAdded(this::loadEmployeeData);
            addEmployeeModalController.show();
        }
    }

    private void deleteEmployee(Employee employee) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa nhân viên \"" + 
            (employee.getFullName() != null ? employee.getFullName() : employee.getUsername()) + "\"?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = employeeRepo.delete(employee.getNvId());
                Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                alert.setTitle(success ? "Thành công" : "Lỗi");
                alert.setHeaderText(null);
                alert.setContentText(success ? "Đã xóa nhân viên thành công!" : "Không thể xóa nhân viên.");
                alert.showAndWait();
                
                if (success) {
                    loadEmployeeData();
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Lỗi khi xóa nhân viên: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}

