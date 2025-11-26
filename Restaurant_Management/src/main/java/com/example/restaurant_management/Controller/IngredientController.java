package com.example.restaurant_management.Controller;

import com.example.restaurant_management.entity.Ingredient;
import com.example.restaurant_management.entityRepo.IngredientRepo;
import com.example.restaurant_management.mapper.IngredientMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IngredientController {

    private final IngredientRepo ingredientRepo = new IngredientRepo(new IngredientMapper());
    
    @FXML private VBox ingredientListContainer;
    @FXML private ComboBox<String> ingredientFilterCombo;
    
    // Modal sẽ được lấy từ ManagerController
    private AddIngredientController addIngredientModalController;
    private javafx.scene.layout.StackPane addIngredientModal;
    
    // Method để set modal từ ManagerController
    public void setModal(javafx.scene.layout.StackPane modal, AddIngredientController controller) {
        this.addIngredientModal = modal;
        this.addIngredientModalController = controller;
    }

    private static final String FILTER_ALL = "Tất cả";
    private static final String FILTER_TODAY = "Hôm nay";
    private static final String FILTER_WEEK = "Tuần này";

    @FXML
    private VBox ingredientViewRoot; // Root của view này
    
    @FXML
    public void initialize() {
        // Lưu controller vào userData để có thể truy cập từ bên ngoài
        if (ingredientViewRoot != null) {
            ingredientViewRoot.setUserData(this);
        }
        setupFilterCombo();
        loadIngredientData();
    }

    // Method public để refresh dữ liệu từ bên ngoài
    public void refresh() {
        loadIngredientData();
    }

    public void loadIngredientData() {
        if (ingredientListContainer == null) return;
        
        ingredientListContainer.getChildren().clear();
        
        try {
            List<Ingredient> ingredients = ingredientRepo.findAllIngredients();
            ingredients = applyFilter(ingredients);
            
            if (ingredients.isEmpty()) {
                Label emptyLabel = new Label("Chưa có nguyên liệu nào");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                ingredientListContainer.getChildren().add(emptyLabel);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            for (Ingredient ingredient : ingredients) {
                HBox ingredientCard = createIngredientCard(ingredient, formatter);
                ingredientListContainer.getChildren().add(ingredientCard);
            }
        } catch (Exception e) {
            Label errorLabel = new Label("Lỗi khi tải dữ liệu nguyên liệu: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            ingredientListContainer.getChildren().add(errorLabel);
        }
    }

    private void setupFilterCombo() {
        if (ingredientFilterCombo == null) {
            return;
        }
        ingredientFilterCombo.getItems().setAll(FILTER_ALL, FILTER_TODAY, FILTER_WEEK);
        if (ingredientFilterCombo.getValue() == null) {
            ingredientFilterCombo.getSelectionModel().select(FILTER_ALL);
        }
        ingredientFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadIngredientData());
    }

    private List<Ingredient> applyFilter(List<Ingredient> ingredients) {
        if (ingredientFilterCombo == null || ingredientFilterCombo.getValue() == null) {
            return ingredients;
        }

        String selected = ingredientFilterCombo.getValue();
        if (FILTER_ALL.equals(selected)) {
            return ingredients;
        }

        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end;

        if (FILTER_TODAY.equals(selected)) {
            start = today;
            end = today.plusDays(1);
        } else if (FILTER_WEEK.equals(selected)) {
            LocalDate weekStart = today.with(DayOfWeek.MONDAY);
            start = weekStart;
            end = weekStart.plusDays(7);
        } else {
            return ingredients;
        }

        LocalDate finalStart = start;
        LocalDate finalEnd = end;
        return ingredients.stream()
                .filter(ingredient -> {
                    LocalDate importDate = ingredient.getImportDate();
                    return importDate != null && !importDate.isBefore(finalStart) && importDate.isBefore(finalEnd);
                })
                .collect(Collectors.toList());
    }

    private HBox createIngredientCard(Ingredient ingredient, DateTimeFormatter formatter) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 15; -fx-background-radius: 5;");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(Region.USE_COMPUTED_SIZE);
        
        VBox infoBox = new VBox(5);
        
        Label nameLabel = new Label(ingredient.getIngredientName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label quantityLabel = new Label("Số lượng: " + String.format("%.2f", ingredient.getQuantity()) + " " + ingredient.getUnit());
        quantityLabel.setStyle("-fx-font-size: 12px;");
        
        Label supplierLabel = new Label("Nhà cung cấp: " + (ingredient.getSupplier() != null ? ingredient.getSupplier() : "N/A"));
        supplierLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        String importDateText = ingredient.getImportDate() != null ? 
            ingredient.getImportDate().format(formatter) : "N/A";
        Label importDateLabel = new Label("Ngày nhập: " + importDateText);
        importDateLabel.setStyle("-fx-font-size: 12px;");
        
        String expiryDateText = ingredient.getExpiryDate() != null ? 
            ingredient.getExpiryDate().format(formatter) : "N/A";
        Label expiryDateLabel = new Label("Ngày hết hạn: " + expiryDateText);
        expiryDateLabel.setStyle("-fx-font-size: 12px;");
        
        // Kiểm tra nếu sắp hết hạn (trong vòng 7 ngày)
        if (ingredient.getExpiryDate() != null) {
            LocalDate today = LocalDate.now();
            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(today, ingredient.getExpiryDate());
            if (daysUntilExpiry >= 0 && daysUntilExpiry <= 7) {
                expiryDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #f57c00; -fx-font-weight: bold;");
            } else if (daysUntilExpiry < 0) {
                expiryDateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #c62828; -fx-font-weight: bold;");
            }
        }
        
        Label statusLabel = new Label("Trạng thái: " + getStatusText(ingredient.getStatus()));
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getStatusColor(ingredient.getStatus()) + ";");
        
        infoBox.getChildren().addAll(nameLabel, quantityLabel, supplierLabel, importDateLabel, expiryDateLabel, statusLabel);
        
        // Nút sửa và xóa
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button editBtn = new Button("Sửa");
        editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15;");
        editBtn.setOnAction(e -> showEditIngredientModal(ingredient));
        
        Button deleteBtn = new Button("Xóa");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15;");
        deleteBtn.setOnAction(e -> deleteIngredient(ingredient));
        
        actionBox.getChildren().addAll(editBtn, deleteBtn);
        
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        card.getChildren().addAll(infoBox, actionBox);
        
        return card;
    }

    private String getStatusText(String status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case "CON_HANG": return "Còn hàng";
            case "HET_HANG": return "Hết hàng";
            case "HET_HAN": return "Hết hạn";
            default: return status;
        }
    }

    private String getStatusColor(String status) {
        if (status == null) return "#666";
        switch (status) {
            case "CON_HANG": return "#2e7d32";
            case "HET_HANG": return "#f57c00";
            case "HET_HAN": return "#c62828";
            default: return "#666";
        }
    }

    @FXML
    public void displayAddIngredientForm() {
        if (addIngredientModal != null) {
            addIngredientModal.setVisible(true);
            addIngredientModal.setManaged(true);
            addIngredientModal.setMouseTransparent(false);
            addIngredientModal.toFront();
        }
        if (addIngredientModalController != null) {
            addIngredientModalController.setIngredient(null); // null = thêm mới
            addIngredientModalController.setOnIngredientAdded(this::loadIngredientData);
            addIngredientModalController.show();
        }
    }

    private void showEditIngredientModal(Ingredient ingredient) {
        if (addIngredientModal != null) {
            addIngredientModal.setVisible(true);
            addIngredientModal.setManaged(true);
            addIngredientModal.setMouseTransparent(false);
            addIngredientModal.toFront();
        }
        if (addIngredientModalController != null) {
            addIngredientModalController.setIngredient(ingredient); // có giá trị = sửa
            addIngredientModalController.setOnIngredientAdded(this::loadIngredientData);
            addIngredientModalController.show();
        }
    }

    private void deleteIngredient(Ingredient ingredient) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa nguyên liệu \"" + ingredient.getIngredientName() + "\"?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int rowsAffected = ingredientRepo.delete(ingredient.getIngredientId());
                Alert alert = new Alert(rowsAffected > 0 ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                alert.setTitle(rowsAffected > 0 ? "Thành công" : "Lỗi");
                alert.setHeaderText(null);
                alert.setContentText(rowsAffected > 0 ? "Đã xóa nguyên liệu thành công!" : "Không thể xóa nguyên liệu.");
                alert.showAndWait();
                
                if (rowsAffected > 0) {
                    loadIngredientData();
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Lỗi");
                alert.setHeaderText(null);
                alert.setContentText("Lỗi khi xóa nguyên liệu: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleIngredientRefresh() {
        loadIngredientData();
    }
}

