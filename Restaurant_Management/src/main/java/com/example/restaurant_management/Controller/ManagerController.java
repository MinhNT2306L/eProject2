package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.TableMapper;
import com.example.restaurant_management.service.LoginService;
import com.example.restaurant_management.service.TableService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ManagerController {


    private final TableRepo tableRepo = new TableRepo(new TableMapper());
    @FXML private BorderPane rootPane;
    @FXML private AddTableController addTableModalController;
    @FXML private StackPane contentArea;
    @FXML private FlowPane tableContainer;
	@FXML private VBox tableView;
    @FXML private StackPane addTableModal;
    @FXML private StackPane editTableModal;
    @FXML private EditTableController editTableModalController;
    @FXML
    public void initialize() {
        rootPane.setUserData(this);
        if (addTableModal != null) addTableModal.setVisible(false);
        if (editTableModal != null) editTableModal.setVisible(false);

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
		// Ẩn tất cả các view, nhưng không ẩn modal (addTableModal)
		contentArea.getChildren().forEach(node -> {
			if (node != addTableModal) {
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

}
