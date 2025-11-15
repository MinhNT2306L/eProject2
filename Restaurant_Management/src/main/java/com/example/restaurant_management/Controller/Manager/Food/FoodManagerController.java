// src/main/java/com/example/restaurant_management/Controller/FoodManagerController.java
package com.example.restaurant_management.Controller.Manager.Food;

import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.entityRepo.FoodRepo;
import com.example.restaurant_management.mapper.FoodMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.List;

public class FoodManagerController {

    @FXML private TableView<Food> tblFoods;
    @FXML private TableColumn<Food, Integer> colId;
    @FXML private TableColumn<Food, String> colName;
    @FXML private TableColumn<Food, String> colType;
    @FXML private TableColumn<Food, Double> colPrice;
    @FXML private TableColumn<Food, String> colStatus;
    @FXML private TableColumn<Food, Void> colActions;
    @FXML private Pagination pagination;
    @FXML private TextField txtSearch;
    @FXML private Button btnAdd;
    @FXML private Button btnSearch;

    @FXML private StackPane addFoodModal;
    @FXML private AddFoodController addFoodModalController;
    @FXML private StackPane editFoodModal;
    @FXML private EditFoodController editFoodModalController;

    private final int ROWS_PER_PAGE = 15;
    private List<Food> allFoods;
    private final FoodRepo foodRepo = new FoodRepo(new FoodMapper());

    @FXML
    public void initialize() {
        setupTableColumns();
        setupEventHandlers();
        setupPagination();
        loadFoodsFromDatabase();
        hideModals();
    }
    
    private void setupPagination() {
        // Lắng nghe sự kiện thay đổi trang và cập nhật TableView
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            updateTableForPage(newIndex.intValue());
        });
        
        // Không sử dụng page factory vì TableView đã có trong FXML
        pagination.setPageFactory(null);
    }
    
    private void updateTableForPage(int pageIndex) {
        if (allFoods == null || allFoods.isEmpty()) {
            tblFoods.setItems(FXCollections.observableArrayList());
            refreshTableView();
            return;
        }
        
        int from = pageIndex * ROWS_PER_PAGE;
        int to = Math.min(from + ROWS_PER_PAGE, allFoods.size());
        
        if (from < allFoods.size()) {
            ObservableList<Food> pageData = FXCollections.observableArrayList(
                allFoods.subList(from, to)
            );
            tblFoods.setItems(pageData);
        } else {
            tblFoods.setItems(FXCollections.observableArrayList());
        }
        
        // Refresh TableView để đảm bảo các cell được render lại đúng cách
        refreshTableView();
    }
    
    private void refreshTableView() {
        // Force refresh TableView để các cell được render lại
        javafx.application.Platform.runLater(() -> {
            // Refresh toàn bộ TableView - điều này sẽ trigger updateItem cho tất cả các cell
            tblFoods.refresh();
        });
    }

    private void hideModals() {
        addFoodModal.setVisible(false);
        addFoodModal.setMouseTransparent(true);
        editFoodModal.setVisible(false);
        editFoodModal.setMouseTransparent(true);
    }

    private void setupEventHandlers() {
        btnSearch.setOnAction(e -> searchFood());
        btnAdd.setOnAction(e -> openAddFoodModal());
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(d -> d.getValue().foodIdProperty().asObject());
        colName.setCellValueFactory(d -> d.getValue().foodNameProperty());
        colType.setCellValueFactory(d -> d.getValue().foodCategoryProperty());
        colPrice.setCellValueFactory(d -> d.getValue().priceProperty().asObject());
        colStatus.setCellValueFactory(d -> d.getValue().statusProperty());

        colActions.setCellFactory(param -> new TableCell<Food, Void>() {
            private final Button editBtn = new Button("Sửa");
            private final Button deleteBtn = new Button("Xóa");
            private final HBox graphic = new HBox(8, editBtn, deleteBtn);
            private Food currentFood = null;

            {
                editBtn.getStyleClass().add("edit-btn");
                deleteBtn.getStyleClass().add("delete-btn");
                graphic.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                // Luôn clear graphic trước để tránh hiển thị sai
                setGraphic(null);
                
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    currentFood = null;
                    return;
                }
                
                Food food = getTableRow().getItem();
                
                // Chỉ update nếu food thay đổi hoặc chưa được set
                if (food != currentFood) {
                    currentFood = food;
                    // Clear các event handler cũ trước khi set mới
                    editBtn.setOnAction(null);
                    deleteBtn.setOnAction(null);
                    
                    // Set event handlers mới
                    editBtn.setOnAction(e -> openEditFoodModal(food));
                    deleteBtn.setOnAction(e -> confirmDelete(food));
                }
                
                // Luôn set graphic để đảm bảo buttons được hiển thị
                setGraphic(graphic);
            }
        });
    }

    private void loadFoodsFromDatabase() {
        allFoods = foodRepo.findAllFoods();
        updatePagination();
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) allFoods.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        
        // Reset về trang đầu nếu trang hiện tại vượt quá số trang mới
        int currentPage = pagination.getCurrentPageIndex();
        if (currentPage >= pageCount && pageCount > 0) {
            pagination.setCurrentPageIndex(0);
            currentPage = 0;
        }
        
        // Cập nhật TableView cho trang hiện tại
        updateTableForPage(currentPage);
    }

    @FXML
    private void searchFood() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            loadFoodsFromDatabase();
            return;
        }
        List<Food> filtered = allFoods.stream()
                .filter(f -> f.getFoodName().toLowerCase().contains(keyword) ||
                        (f.getFoodCategory() != null && f.getFoodCategory().toLowerCase().contains(keyword)))
                .toList();
        allFoods = filtered;
        updatePagination();
    }

    private void openAddFoodModal() {
        addFoodModalController.setOnSaved(this::refreshFoodList);
        addFoodModal.setVisible(true);
        addFoodModal.toFront();
        addFoodModal.setMouseTransparent(false);
    }

    private void openEditFoodModal(Food food) {
        editFoodModalController.setFood(food);
        editFoodModalController.setOnSaved(this::refreshFoodList);
        editFoodModal.setVisible(true);
        editFoodModal.toFront();
        editFoodModal.setMouseTransparent(false);
    }

    private void confirmDelete(Food food) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa");
        alert.setContentText("Xóa món: \"" + food.getFoodName() + "\"?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (foodRepo.deleteFood(food.getFoodId())) {
                showInfo("Đã xóa thành công!");
                refreshFoodList();
            } else {
                showError("Xóa thất bại!");
            }
        }
    }

    public void refreshFoodList() {
        loadFoodsFromDatabase();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }
}