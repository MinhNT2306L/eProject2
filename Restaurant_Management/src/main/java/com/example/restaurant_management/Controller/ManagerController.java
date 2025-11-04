package com.example.restaurant_management.Controller;

import com.example.restaurant_management.service.LoginService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class ManagerController {
    @FXML
    public void logout(ActionEvent event){
        Stage currentStage =(Stage) (((Node) event.getSource()).getScene().getWindow());
        LoginService.logout(currentStage);
    }
}
