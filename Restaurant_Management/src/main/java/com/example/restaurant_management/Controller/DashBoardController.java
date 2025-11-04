package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.TableMapper;
import com.example.restaurant_management.service.LoginService;
import com.example.restaurant_management.service.TableSevice;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.util.List;

public class DashBoardController {
    private TableRepo tableRepo = new TableRepo(new TableMapper());

    @FXML
    private FlowPane tableContainer;

    @FXML
    public void initialize(){
        List<Table> tables = tableRepo.getAll();
        TableSevice.updateTableList(tableContainer,tables);
    }


    @FXML
    public void logout(ActionEvent event){
        Stage currentStage =(Stage) (((Node) event.getSource()).getScene().getWindow());
        LoginService.logout(currentStage);
    }
}
