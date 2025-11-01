package com.example.restaurant_management.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    public void login(ActionEvent event){
        String userName = usernameField.getText();
        String pw = passwordField.getText();
        if (true){ //TODO: If login is true
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/example/restaurant_management/menu-view.fxml"));
                Scene newScene = new Scene(root);
                Stage currentStage = (Stage) loginButton.getScene().getWindow();
                currentStage.setScene(newScene);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            errorLabel.setVisible(true);
            errorLabel.setText("Kiểm tra lại thông tin đăng nhập");
        }
    }

}
