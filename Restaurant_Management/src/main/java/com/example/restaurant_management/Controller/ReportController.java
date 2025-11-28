package com.example.restaurant_management.Controller;

import com.example.restaurant_management.service.RevenueReportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportController {

    @FXML
    private VBox reportContainer;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private Button filterBtn;

    @FXML
    private Button todayBtn;

    @FXML
    private Button weekBtn;

    @FXML
    private Button monthBtn;

    @FXML
    private Button yearBtn;

    @FXML
    private Label totalRevenueLabel;

    @FXML
    private Label totalInvoicesLabel;

    @FXML
    private Label averageOrderLabel;

    @FXML
    private VBox chartContainer;

    @FXML
    private VBox tableContainer;

    private RevenueReportService reportService;

    @FXML
    public void initialize() {
        try {
            // Set default dates (last 30 days)
            if (endDatePicker != null) {
                endDatePicker.setValue(LocalDate.now());
            }
            if (startDatePicker != null) {
                startDatePicker.setValue(LocalDate.now().minusDays(30));
            }

            // Setup button actions
            if (filterBtn != null) {
                filterBtn.setOnAction(e -> loadReport());
            }
            if (todayBtn != null) {
                todayBtn.setOnAction(e -> setDateRangeToday());
            }
            if (weekBtn != null) {
                weekBtn.setOnAction(e -> setDateRangeWeek());
            }
            if (monthBtn != null) {
                monthBtn.setOnAction(e -> setDateRangeMonth());
            }
            if (yearBtn != null) {
                yearBtn.setOnAction(e -> setDateRangeYear());
            }

            // Initialize service
            try {
                reportService = new RevenueReportService();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error creating RevenueReportService: " + e.getMessage());
            }

            // Load initial report after a short delay to ensure all components are ready
            javafx.application.Platform.runLater(() -> {
                try {
                    if (reportService != null && startDatePicker != null && endDatePicker != null) {
                        loadReport();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Error loading initial report: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error initializing ReportController: " + e.getMessage());
        }
    }

    @FXML
    private void setDateRangeToday() {
        LocalDate today = LocalDate.now();
        if (startDatePicker != null) {
            startDatePicker.setValue(today);
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(today);
        }
        loadReport();
    }

    @FXML
    private void setDateRangeWeek() {
        LocalDate today = LocalDate.now();
        if (startDatePicker != null) {
            startDatePicker.setValue(today.minusDays(7));
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(today);
        }
        loadReport();
    }

    @FXML
    private void setDateRangeMonth() {
        LocalDate today = LocalDate.now();
        if (startDatePicker != null) {
            startDatePicker.setValue(today.minusDays(30));
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(today);
        }
        loadReport();
    }

    @FXML
    private void setDateRangeYear() {
        LocalDate today = LocalDate.now();
        if (startDatePicker != null) {
            startDatePicker.setValue(today.minusYears(1));
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(today);
        }
        loadReport();
    }

    @FXML
    private void loadReport() {
        try {
            if (startDatePicker == null || endDatePicker == null) {
                return;
            }

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showAlert("Vui lòng chọn khoảng thời gian!");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showAlert("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc!");
                return;
            }

            if (reportService == null) {
                reportService = new RevenueReportService();
            }

            // Load statistics
            double totalRevenue = reportService.getTotalRevenue(startDate, endDate);
            int totalInvoices = reportService.getTotalInvoices(startDate, endDate);
            double avgOrder = reportService.getAverageOrderValue(startDate, endDate);

            // Update summary labels
            if (totalRevenueLabel != null) {
                totalRevenueLabel.setText(formatCurrency(totalRevenue));
            }
            if (totalInvoicesLabel != null) {
                totalInvoicesLabel.setText(String.valueOf(totalInvoices));
            }
            if (averageOrderLabel != null) {
                averageOrderLabel.setText(formatCurrency(avgOrder));
            }

            // Load charts
            if (chartContainer != null) {
                loadCharts(startDate, endDate);
            }

            // Load tables
            if (tableContainer != null) {
                loadTables(startDate, endDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khi tải báo cáo: " + e.getMessage());
        }
    }

    private void loadCharts(LocalDate startDate, LocalDate endDate) {
        chartContainer.getChildren().clear();

        // Daily Revenue Chart
        LineChart<String, Number> dailyChart = createDailyRevenueChart(startDate, endDate);
        chartContainer.getChildren().add(dailyChart);

        // Payment Method Chart
        PieChart paymentChart = createPaymentMethodChart(startDate, endDate);
        chartContainer.getChildren().add(paymentChart);
    }

    private LineChart<String, Number> createDailyRevenueChart(LocalDate startDate, LocalDate endDate) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Doanh thu (VND)");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Doanh Thu Theo Ngày");
        lineChart.setPrefHeight(400);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        List<Map<String, Object>> dailyData = reportService.getDailyRevenue(startDate, endDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (Map<String, Object> data : dailyData) {
            LocalDate date = (LocalDate) data.get("date");
            double revenue = (Double) data.get("revenue");
            series.getData().add(new XYChart.Data<>(date.format(formatter), revenue));
        }

        lineChart.getData().add(series);
        return lineChart;
    }

    private PieChart createPaymentMethodChart(LocalDate startDate, LocalDate endDate) {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Doanh Thu Theo Phương Thức Thanh Toán");
        pieChart.setPrefHeight(400);

        List<Map<String, Object>> paymentData = reportService.getRevenueByPaymentMethod(startDate, endDate);
        for (Map<String, Object> data : paymentData) {
            String method = (String) data.get("method");
            double revenue = (Double) data.get("revenue");
            String displayName = method != null ? method : "Khác";
            PieChart.Data slice = new PieChart.Data(displayName + ": " + formatCurrency(revenue), revenue);
            pieChart.getData().add(slice);
        }

        return pieChart;
    }

    private void loadTables(LocalDate startDate, LocalDate endDate) {
        tableContainer.getChildren().clear();

        // Top Selling Foods Table
        createTopFoodsTable(startDate, endDate);
    }

    private void createTopFoodsTable(LocalDate startDate, LocalDate endDate) {
        TableView<Map<String, Object>> table = new TableView<>();
        table.setPrefHeight(300);

        // Food Name Column
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Tên Món");
        nameCol.setCellValueFactory(data -> {
            String name = (String) data.getValue().get("foodName");
            return new javafx.beans.property.SimpleStringProperty(name != null ? name : "");
        });
        nameCol.setPrefWidth(200);

        // Category Column
        TableColumn<Map<String, Object>, String> categoryCol = new TableColumn<>("Loại");
        categoryCol.setCellValueFactory(data -> {
            String category = (String) data.getValue().get("category");
            return new javafx.beans.property.SimpleStringProperty(category != null ? category : "");
        });
        categoryCol.setPrefWidth(150);

        // Quantity Column
        TableColumn<Map<String, Object>, Integer> quantityCol = new TableColumn<>("Số Lượng");
        quantityCol.setCellValueFactory(data -> {
            Integer qty = (Integer) data.getValue().get("quantity");
            return new javafx.beans.property.SimpleIntegerProperty(qty != null ? qty : 0).asObject();
        });
        quantityCol.setPrefWidth(100);

        // Revenue Column
        TableColumn<Map<String, Object>, String> revenueCol = new TableColumn<>("Doanh Thu");
        revenueCol.setCellValueFactory(data -> {
            Double revenue = (Double) data.getValue().get("revenue");
            return new javafx.beans.property.SimpleStringProperty(formatCurrency(revenue != null ? revenue : 0.0));
        });
        revenueCol.setPrefWidth(150);

        table.getColumns().addAll(nameCol, categoryCol, quantityCol, revenueCol);

        List<Map<String, Object>> topFoods = reportService.getTopSellingFoods(startDate, endDate, 10);
        ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(topFoods);
        table.setItems(data);

        // Add title
        Label title = new Label("Top 10 Món Ăn Bán Chạy");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setPadding(new Insets(10, 0, 5, 0));

        VBox tableBox = new VBox(5);
        tableBox.getChildren().addAll(title, table);
        tableContainer.getChildren().add(tableBox);
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0f VND", amount);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
