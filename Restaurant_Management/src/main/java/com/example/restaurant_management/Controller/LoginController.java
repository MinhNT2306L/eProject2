package com.example.restaurant_management.Controller;

import com.example.restaurant_management.service.LoginService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private Label errorLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    public void login(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = LoginService.verifyLogin(username,password);
        // fix
        if (role.equals("Manager")) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/restaurant_management/View/manager-view.fxml")
                );

                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.setTitle("LOCAL FOOD - Quản lý");
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setVisible(true);
                errorLabel.setText("Không thể mở giao diện đặt bàn.");
            }
        } else if (role.equals("Nhân viên")){
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/restaurant_management/View/dashboard-view.fxml")
                );

                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.setTitle("LOCAL FOOD - Dashboard");
                stage.show();
            } catch (IOException e) {
                errorLabel.setVisible(true);
                errorLabel.setText("Không thể mở giao diện đặt bàn.");
                throw new RuntimeException(e);
            }
        } else {
            errorLabel.setVisible(true);
            errorLabel.setText("Sai thông tin đăng nhập, vui lòng thử lại.");
        }
    }
}
