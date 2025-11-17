package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.TableMapper;
import com.example.restaurant_management.service.LoginService;
import com.example.restaurant_management.service.TableService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.util.List;

public class DashBoardController {
    private TableRepo tableRepo = new TableRepo(new TableMapper());

    @FXML
    private FlowPane tableContainer;

    @FXML
    public void initialize(){
        refreshTableList();
        // Lưu controller vào userData sau khi scene đã được set
        javafx.application.Platform.runLater(() -> {
            if (tableContainer != null && tableContainer.getScene() != null) {
                Parent root = tableContainer.getScene().getRoot();
                root.setUserData(this);
            }
        });
    }
    
    public void refreshTableList() {
        List<Table> tables = tableRepo.getAll();
        TableService.updateTableList(tableContainer, tables);
    }


    @FXML
    public void logout(ActionEvent event){
        Stage currentStage =(Stage) (((Node) event.getSource()).getScene().getWindow());
        LoginService.logout(currentStage);
    }
}
