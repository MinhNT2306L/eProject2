package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Employee;
import com.example.restaurant_management.entityRepo.EmployeeRepo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AddEmployeeController {

    @FXML
    private StackPane modalRoot;
    @FXML
    private VBox modalContainer;
    @FXML
    private Label titleLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<String> roleComboBox;

    private Runnable onEmployeeAdded; // callback sau khi thêm/sửa xong
    private Employee currentEmployee; // null nếu là thêm mới, có giá trị nếu là sửa
    private EmployeeRepo employeeRepo;
    private List<Map<String, Object>> roles;
    private static final Pattern FULL_NAME_PATTERN = Pattern.compile("^[\\p{L} ]+$");
    private static final Pattern PHONE_PATTERN_11_DIGITS = Pattern.compile("^\\d{11}$");
    private static final Pattern PHONE_PATTERN_PLUS84 = Pattern.compile("^\\+84\\d+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@gmail\\.com$");

    @FXML
    public void initialize() {
        employeeRepo = new EmployeeRepo();
        loadRoles();
        // Đảm bảo modal container được căn giữa
        StackPane.setAlignment(modalContainer, javafx.geometry.Pos.CENTER);
        // Đảm bảo modal root được căn giữa
        modalRoot.setAlignment(javafx.geometry.Pos.CENTER);

        // Close on blur (click outside)
        modalRoot.setOnMouseClicked(event -> {
            if (event.getTarget() == modalRoot) {
                hide();
            }
        });
    }

    private void loadRoles() {
        roles = employeeRepo.getAllRoles();
        ObservableList<String> roleNames = FXCollections.observableArrayList();
        for (Map<String, Object> role : roles) {
            roleNames.add((String) role.get("roleName"));
        }
        roleComboBox.setItems(roleNames);
    }

    public void setOnEmployeeAdded(Runnable callback) {
        this.onEmployeeAdded = callback;
    }

    public void setEmployee(Employee employee) {
        this.currentEmployee = employee;
        if (employee != null) {
            // Chế độ sửa
            titleLabel.setText("✏️ Sửa Nhân Viên");
            usernameField.setText(employee.getUsername());
            passwordField.setText(employee.getPassword());
            fullNameField.setText(employee.getFullName());
            phoneField.setText(employee.getPhone());
            emailField.setText(employee.getEmail());

            // Set role
            if (employee.getRoleId() != null) {
                for (Map<String, Object> role : roles) {
                    if (role.get("roleId").equals(employee.getRoleId())) {
                        roleComboBox.setValue((String) role.get("roleName"));
                        break;
                    }
                }
            }
        } else {
            // Chế độ thêm mới
            titleLabel.setText("➕ Thêm Nhân Viên");
            clearForm();
        }
    }

    @FXML
    private void handleSave() {
        // Validation
        if (usernameField.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập username").showAndWait();
            return;
        }

        if (passwordField.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập password").showAndWait();
            return;
        }

        String fullName = fullNameField.getText().trim();
        if (fullName.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập họ tên").showAndWait();
            return;
        }
        if (!FULL_NAME_PATTERN.matcher(fullName).matches()) {
            new Alert(Alert.AlertType.WARNING, "Họ tên chỉ được chứa chữ cái và khoảng trắng").showAndWait();
            return;
        }

        String phone = phoneField.getText().trim();
        if (!phone.isEmpty()
                && !(PHONE_PATTERN_11_DIGITS.matcher(phone).matches()
                || PHONE_PATTERN_PLUS84.matcher(phone).matches())) {
            new Alert(Alert.AlertType.WARNING, "Số điện thoại phải có 11 chữ số hoặc bắt đầu bằng +84").showAndWait();
            return;
        }

        String email = emailField.getText().trim();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            new Alert(Alert.AlertType.WARNING, "Email phải kết thúc bằng @gmail.com").showAndWait();
            return;
        }

        if (roleComboBox.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn vai trò").showAndWait();
            return;
        }

        try {
            Employee employee;
            if (currentEmployee == null) {
                // Thêm mới
                employee = new Employee();
                employee.setUsername(usernameField.getText().trim());
                employee.setPassword(passwordField.getText().trim());
                employee.setFullName(fullNameField.getText().trim());
                employee.setPhone(phoneField.getText().trim());
                employee.setEmail(emailField.getText().trim());

                // Lấy roleId từ roleName
                Integer roleId = getRoleIdByName(roleComboBox.getValue());
                employee.setRoleId(roleId);

                boolean success = employeeRepo.insert(employee);
                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "Thêm nhân viên thành công!").showAndWait();
                    clearForm();
                    if (onEmployeeAdded != null) {
                        onEmployeeAdded.run();
                    }
                    hide();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Thêm nhân viên thất bại! Có thể username đã tồn tại.")
                            .showAndWait();
                }
            } else {
                // Sửa
                currentEmployee.setUsername(usernameField.getText().trim());
                currentEmployee.setPassword(passwordField.getText().trim());
                currentEmployee.setFullName(fullNameField.getText().trim());
                currentEmployee.setPhone(phoneField.getText().trim());
                currentEmployee.setEmail(emailField.getText().trim());

                // Lấy roleId từ roleName
                Integer roleId = getRoleIdByName(roleComboBox.getValue());
                currentEmployee.setRoleId(roleId);

                boolean success = employeeRepo.update(currentEmployee);
                if (success) {
                    new Alert(Alert.AlertType.INFORMATION, "Sửa nhân viên thành công!").showAndWait();
                    if (onEmployeeAdded != null) {
                        onEmployeeAdded.run();
                    }
                    hide();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Sửa nhân viên thất bại!").showAndWait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage()).showAndWait();
        }
    }

    private Integer getRoleIdByName(String roleName) {
        for (Map<String, Object> role : roles) {
            if (role.get("roleName").equals(roleName)) {
                return (Integer) role.get("roleId");
            }
        }
        return null;
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        fullNameField.clear();
        phoneField.clear();
        emailField.clear();
        roleComboBox.setValue(null);
    }

    @FXML
    private void handleCancel() {
        hide();
    }

    public void show() {
        if (currentEmployee == null) {
            clearForm();
        }
        // Đảm bảo modal được căn giữa
        modalRoot.setAlignment(javafx.geometry.Pos.CENTER);
        StackPane.setAlignment(modalContainer, javafx.geometry.Pos.CENTER);
        modalRoot.setVisible(true);
        modalRoot.setMouseTransparent(false);
        // Đưa modal lên front để đảm bảo hiển thị trên cùng
        modalRoot.toFront();
    }

    private void hide() {
        modalRoot.setVisible(false);
        modalRoot.setMouseTransparent(true);
    }
}
