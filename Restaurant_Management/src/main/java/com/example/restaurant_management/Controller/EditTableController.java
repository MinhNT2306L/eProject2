package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.TableMapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class EditTableController {

    @FXML private StackPane modalRoot;
    @FXML private VBox modalContainer;

    @FXML private TextField txtSoBan;
    @FXML private TextField txtSucChua;
    @FXML private ComboBox<String> cbTrangThai;

    private Table currentTable;
    private Runnable onSavedCallback;

    private final TableRepo tableRepo = new TableRepo(new TableMapper());

    public void setTable(Table table) {
        this.currentTable = table;
        txtSoBan.setText(String.valueOf(table.getTableNumber()));
        cbTrangThai.setValue(table.getStatus());
    }

    public void setOnSaved(Runnable callback) {
        this.onSavedCallback = callback;
    }

    @FXML
    private void handleSave() {
        try {
            int soBan = Integer.parseInt(txtSoBan.getText().trim());
            int sucChua = Integer.parseInt(txtSucChua.getText().trim());
            String status = cbTrangThai.getValue();

            if (soBan <= 0 || sucChua <= 0 || status == null) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ và hợp lệ!").show();
                return;
            }

            currentTable.setTableNumber(soBan);
            currentTable.setStatus(status);

            boolean success = updateTableInDB(currentTable);
            if (success && onSavedCallback != null) {
                onSavedCallback.run();
            }
            hide();
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Số bàn và sức chứa phải là số!").show();
        }
    }

    @FXML
    private void handleCancel() {
        hide();
    }

    private boolean updateTableInDB(Table table) {
        String sql = "UPDATE ban SET so_ban = ?, trang_thai = ? WHERE ban_id = ?";
        try (var conn = tableRepo.getConn();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, table.getTableNumber());
            stmt.setString(2, table.getStatus());
            stmt.setInt(3, table.getTableId());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void hide() {
        modalRoot.setVisible(false);
        modalRoot.setManaged(false);
    }

    public void show() {
        modalRoot.setVisible(true);
        modalRoot.setManaged(true);
        modalRoot.toFront();
    }
}