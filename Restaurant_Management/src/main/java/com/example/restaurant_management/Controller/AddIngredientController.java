package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Ingredient;
import com.example.restaurant_management.entityRepo.IngredientRepo;
import com.example.restaurant_management.mapper.IngredientMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class AddIngredientController {

    @FXML
    private StackPane modalRoot;
    @FXML
    private VBox modalContainer;
    @FXML
    private Label titleLabel;
    @FXML
    private TextField ingredientNameField;
    @FXML
    private TextField quantityField;
    @FXML
    private ComboBox<String> unitComboBox;
    @FXML
    private TextField supplierField;
    @FXML
    private DatePicker importDatePicker;
    @FXML
    private DatePicker expiryDatePicker;
    @FXML
    private ComboBox<String> statusComboBox;

    private Runnable onIngredientAdded; // callback sau khi thêm/sửa xong
    private Ingredient currentIngredient; // null nếu là thêm mới, có giá trị nếu là sửa
    private IngredientRepo ingredientRepo;

    @FXML
    public void initialize() {
        ingredientRepo = new IngredientRepo(new IngredientMapper());
        loadUnits();
        loadStatuses();
        // Đảm bảo modal container được căn giữa
        StackPane.setAlignment(modalContainer, javafx.geometry.Pos.CENTER);
        // Đảm bảo modal root được căn giữa
        modalRoot.setAlignment(javafx.geometry.Pos.CENTER);

        // Close on blur (click outside)
        modalRoot.setOnMouseClicked(event -> {
            if (event.getTarget() == modalRoot) {
                hide();
            }
        });
    }

    private void loadUnits() {
        ObservableList<String> units = FXCollections.observableArrayList("kg", "g", "cai");
        unitComboBox.setItems(units);
    }

    private void loadStatuses() {
        ObservableList<String> statuses = FXCollections.observableArrayList("CON_HANG", "HET_HANG", "HET_HAN");
        statusComboBox.setItems(statuses);
        statusComboBox.setValue("CON_HANG"); // Mặc định
    }

    public void setOnIngredientAdded(Runnable callback) {
        this.onIngredientAdded = callback;
    }

    public void setIngredient(Ingredient ingredient) {
        this.currentIngredient = ingredient;
        if (ingredient != null) {
            // Chế độ sửa
            titleLabel.setText("✏️ Sửa Nguyên Liệu");
            ingredientNameField.setText(ingredient.getIngredientName());
            quantityField.setText(String.valueOf(ingredient.getQuantity()));
            unitComboBox.setValue(ingredient.getUnit());
            supplierField.setText(ingredient.getSupplier());
            importDatePicker.setValue(ingredient.getImportDate());
            expiryDatePicker.setValue(ingredient.getExpiryDate());
            statusComboBox.setValue(ingredient.getStatus());
        } else {
            // Chế độ thêm mới
            titleLabel.setText("➕ Thêm Nguyên Liệu");
            clearForm();
        }
    }

    @FXML
    private void handleSave() {
        // Validation
        if (ingredientNameField.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập tên nguyên liệu").showAndWait();
            return;
        }

        try {
            double quantity = Double.parseDouble(quantityField.getText().trim());
            if (quantity < 0) {
                new Alert(Alert.AlertType.WARNING, "Số lượng phải >= 0").showAndWait();
                return;
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Số lượng không hợp lệ").showAndWait();
            return;
        }

        if (unitComboBox.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn đơn vị").showAndWait();
            return;
        }

        if (statusComboBox.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn trạng thái").showAndWait();
            return;
        }

        try {
            Ingredient ingredient;
            if (currentIngredient == null) {
                // Thêm mới
                ingredient = new Ingredient();
                ingredient.setIngredientName(ingredientNameField.getText().trim());
                ingredient.setQuantity(Double.parseDouble(quantityField.getText().trim()));
                ingredient.setUnit(unitComboBox.getValue());
                ingredient
                        .setSupplier(supplierField.getText().trim().isEmpty() ? null : supplierField.getText().trim());
                ingredient.setImportDate(
                        importDatePicker.getValue() != null ? importDatePicker.getValue() : LocalDate.now());
                ingredient.setExpiryDate(expiryDatePicker.getValue());
                ingredient.setStatus(statusComboBox.getValue());

                int rowsAffected = ingredientRepo.insert(ingredient);
                if (rowsAffected > 0) {
                    new Alert(Alert.AlertType.INFORMATION, "Thêm nguyên liệu thành công!").showAndWait();
                    clearForm();
                    if (onIngredientAdded != null) {
                        onIngredientAdded.run();
                    }
                    hide();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Thêm nguyên liệu thất bại!").showAndWait();
                }
            } else {
                // Sửa
                currentIngredient.setIngredientName(ingredientNameField.getText().trim());
                currentIngredient.setQuantity(Double.parseDouble(quantityField.getText().trim()));
                currentIngredient.setUnit(unitComboBox.getValue());
                currentIngredient
                        .setSupplier(supplierField.getText().trim().isEmpty() ? null : supplierField.getText().trim());
                currentIngredient.setImportDate(
                        importDatePicker.getValue() != null ? importDatePicker.getValue() : LocalDate.now());
                currentIngredient.setExpiryDate(expiryDatePicker.getValue());
                currentIngredient.setStatus(statusComboBox.getValue());

                int rowsAffected = ingredientRepo.update(currentIngredient);
                if (rowsAffected > 0) {
                    new Alert(Alert.AlertType.INFORMATION, "Sửa nguyên liệu thành công!").showAndWait();
                    if (onIngredientAdded != null) {
                        onIngredientAdded.run();
                    }
                    hide();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Sửa nguyên liệu thất bại!").showAndWait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage()).showAndWait();
        }
    }

    private void clearForm() {
        ingredientNameField.clear();
        quantityField.clear();
        unitComboBox.setValue(null);
        supplierField.clear();
        importDatePicker.setValue(LocalDate.now());
        expiryDatePicker.setValue(null);
        statusComboBox.setValue("CON_HANG");
    }

    @FXML
    private void handleCancel() {
        hide();
    }

    public void show() {
        if (currentIngredient == null) {
            clearForm();
        }
        // Đảm bảo modal được căn giữa
        modalRoot.setAlignment(javafx.geometry.Pos.CENTER);
        StackPane.setAlignment(modalContainer, javafx.geometry.Pos.CENTER);
        modalRoot.setVisible(true);
        modalRoot.setMouseTransparent(false);
        // Đưa modal lên front để đảm bảo hiển thị trên cùng
        modalRoot.toFront();
    }

    private void hide() {
        modalRoot.setVisible(false);
        modalRoot.setMouseTransparent(true);
    }
}
