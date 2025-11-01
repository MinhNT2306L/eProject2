package com.example.restaurant_management.Controller;

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

        // fix
        if (true) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/restaurant_management/View/TableView.fxml")
                );

                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("LOCAL FOOD - Đặt bàn");
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                errorLabel.setVisible(true);
                errorLabel.setText("Không thể mở giao diện đặt bàn.");
            }
        } else {
            errorLabel.setVisible(true);
            errorLabel.setText("Sai thông tin đăng nhập, vui lòng thử lại.");
        }
    }
}
