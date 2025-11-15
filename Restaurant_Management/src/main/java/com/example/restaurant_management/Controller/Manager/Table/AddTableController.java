package com.example.restaurant_management.Controller.Manager.Table;

import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.TableMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AddTableController {

    @FXML private StackPane modalRoot;
    @FXML private VBox modalContainer;
    @FXML private TextField soBanField;
    @FXML private TextField sucChuaField;
    @FXML private ComboBox<String> trangThaiBox;

    private Runnable onTableAdded; // callback sau khi thêm xong

    public void setOnTableAdded(Runnable callback) {
        this.onTableAdded = callback;
    }

    @FXML
    private void handleSave() {
        // Validation
        if (soBanField.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập số bàn").showAndWait();
            return;
        }
        
        if (sucChuaField.getText().trim().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập sức chứa").showAndWait();
            return;
        }
        
        if (trangThaiBox.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn trạng thái").showAndWait();
            return;
        }

        try {
            int soBan = Integer.parseInt(soBanField.getText().trim());
            int sucChua = Integer.parseInt(sucChuaField.getText().trim());
            String trangThai = trangThaiBox.getValue();

            if (soBan <= 0) {
                new Alert(Alert.AlertType.WARNING, "Số bàn phải lớn hơn 0").showAndWait();
                return;
            }
            
            if (sucChua <= 0) {
                new Alert(Alert.AlertType.WARNING, "Sức chứa phải lớn hơn 0").showAndWait();
                return;
            }

            TableRepo tableRepo = new TableRepo(new TableMapper());
            Table newTable = new Table(0, soBan, trangThai);
            boolean success = tableRepo.insert(newTable);
            
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Thêm bàn thành công!").showAndWait();
                // Clear form
                clearForm();
                // Callback để refresh danh sách
                if (onTableAdded != null) {
                    onTableAdded.run();
                }
                hide();
            } else {
                new Alert(Alert.AlertType.ERROR, "Thêm bàn thất bại! Có thể số bàn đã tồn tại.").showAndWait();
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Vui lòng nhập số hợp lệ cho số bàn và sức chứa").showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Lỗi: " + e.getMessage()).showAndWait();
        }
    }
    
    private void clearForm() {
        soBanField.clear();
        sucChuaField.clear();
        trangThaiBox.setValue(null);
    }

    @FXML
    private void handleCancel() {
        hide();
    }

    @FXML
    public void initialize() {
            StackPane.setAlignment(modalContainer, javafx.geometry.Pos.CENTER);
    }

    public void show() {
        // Clear form trước khi hiển thị
        clearForm();
        modalRoot.setVisible(true);
        modalRoot.setMouseTransparent(false);
    }

    private void hide() {
        modalRoot.setVisible(false);
        modalRoot.setMouseTransparent(true);
    }
}
