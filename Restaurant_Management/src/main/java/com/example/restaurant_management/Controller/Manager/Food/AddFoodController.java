// src/main/java/com/example/restaurant_management/Controller/AddFoodController.java
package com.example.restaurant_management.Controller.Manager.Food;

import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.entityRepo.FoodRepo;
import com.example.restaurant_management.mapper.FoodMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.regex.Pattern;

public class AddFoodController {

    @FXML private TextField txtName;
    @FXML private TextField txtCategory;
    @FXML private TextField txtPrice;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private StackPane modal;

    private final FoodRepo foodRepo = new FoodRepo(new FoodMapper());
    private Runnable onSavedCallback;
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} ]+$");

    @FXML
    public void initialize() {
        cbStatus.getItems().addAll("CON_HANG", "HET_HANG");
        cbStatus.setValue("CON_HANG");
        btnCancel.setOnAction(e -> closeModal());
    }

    public void setOnSaved(Runnable callback) {
        this.onSavedCallback = callback;
    }

    @FXML
    private void saveFood() {
        if (!validate()) return;

        try {
            Food food = new Food(
                    0,
                    txtName.getText().trim(),
                    txtCategory.getText().trim(),
                    Double.parseDouble(txtPrice.getText().trim()),
                    cbStatus.getValue(),
                    txtDescription.getText()
            );

            if (foodRepo.addFood(food)) {
                showAlert(Alert.AlertType.INFORMATION, "Thêm thành công!");
                if (onSavedCallback != null) onSavedCallback.run();
                closeModal();
            } else {
                showAlert(Alert.AlertType.ERROR, "Thêm thất bại!");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Giá phải là số!");
        }
    }

    private boolean validate() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập tên món!");
            return false;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            showAlert(Alert.AlertType.WARNING, "Tên món chỉ được chứa chữ và khoảng trắng!");
            return false;
        }

        String category = txtCategory.getText().trim();
        if (category.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập loại món!");
            return false;
        }
        if (!NAME_PATTERN.matcher(category).matches()) {
            showAlert(Alert.AlertType.WARNING, "Loại món chỉ được chứa chữ và khoảng trắng!");
            return false;
        }

        if (cbStatus.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn trạng thái!");
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
        clearForm();
    }

    private void clearForm() {
        txtName.clear(); txtCategory.clear(); txtPrice.clear(); txtDescription.clear();
        cbStatus.setValue("CON_HANG");
    }

    private void showAlert(Alert.AlertType type, String msg) {
        new Alert(type, msg).show();
    }
}