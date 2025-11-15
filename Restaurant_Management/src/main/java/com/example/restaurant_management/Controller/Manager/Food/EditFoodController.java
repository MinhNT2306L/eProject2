// src/main/java/com/example/restaurant_management/Controller/EditFoodController.java
package com.example.restaurant_management.Controller.Manager.Food;

import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.entityRepo.FoodRepo;
import com.example.restaurant_management.mapper.FoodMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

public class EditFoodController {

    @FXML private TextField txtName;
    @FXML private TextField txtCategory;
    @FXML private TextField txtPrice;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private StackPane modal;

    private Food currentFood;
    private final FoodRepo foodRepo = new FoodRepo(new FoodMapper());
    private Runnable onSavedCallback;

    @FXML
    public void initialize() {
        cbStatus.getItems().addAll("CON_HANG", "HET_HANG");
        btnCancel.setOnAction(e -> closeModal());
    }

    public void setFood(Food food) {
        this.currentFood = food;
        loadData();
    }

    public void setOnSaved(Runnable callback) {
        this.onSavedCallback = callback;
    }

    private void loadData() {
        if (currentFood == null) return;
        txtName.setText(currentFood.getFoodName());
        txtCategory.setText(currentFood.getFoodCategory());
        txtPrice.setText(String.valueOf(currentFood.getPrice()));
        txtDescription.setText(currentFood.getDescription());
        cbStatus.setValue(currentFood.getStatus());
    }

    @FXML
    private void saveFood() {
        if (!validate()) return;

        try {
            currentFood.setFoodName(txtName.getText().trim());
            currentFood.setFoodCategory(txtCategory.getText().trim());
            currentFood.setPrice(Double.parseDouble(txtPrice.getText().trim()));
            currentFood.setDescription(txtDescription.getText());
            currentFood.setStatus(cbStatus.getValue());

            if (foodRepo.updateFood(currentFood)) {
                showAlert(Alert.AlertType.INFORMATION, "Cập nhật thành công!");
                if (onSavedCallback != null) onSavedCallback.run();
                closeModal();
            } else {
                showAlert(Alert.AlertType.ERROR, "Cập nhật thất bại!");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Giá phải là số!");
        }
    }

    private boolean validate() {
        if (txtName.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Tên món không được trống!");
            return false;
        }
        try {
            double price = Double.parseDouble(txtPrice.getText().trim());
            if (price < 0) {
                showAlert(Alert.AlertType.WARNING, "Giá không được âm!");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Giá phải là số!");
            return false;
        }
        return true;
    }

    private void closeModal() {
        modal.setVisible(false);
        modal.setMouseTransparent(true);
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).show();
    }
}