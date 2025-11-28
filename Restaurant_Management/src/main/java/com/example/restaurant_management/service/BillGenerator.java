package com.example.restaurant_management.service;

import com.example.restaurant_management.entity.*;
import com.example.restaurant_management.entityRepo.FoodRepo;
import com.example.restaurant_management.mapper.FoodMapper;
import javafx.print.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillGenerator {

    public static void generateBill(Invoice invoice, Order order, List<OrderDetail> orderDetails, Table table) {
        try {
            // Generate printable bill content
            String billContent = createBillContent(invoice, order, orderDetails, table);

            // Save to text file (simple format)
            saveBillToFile(billContent, invoice.getHoadonId());

            // Show print dialog
            showPrintDialog(billContent);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating bill: " + e.getMessage(), e);
        }
    }

    public static String createBillContent(Invoice invoice, Order order, List<OrderDetail> orderDetails, Table table) {
        // Get food names
        FoodRepo foodRepo = new FoodRepo(new FoodMapper());
        List<Food> allFoods = foodRepo.findAllFoods();
        Map<Integer, Food> foodMap = new HashMap<>();
        for (Food food : allFoods) {
            foodMap.put(food.getFoodId(), food);
        }

        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        sb.append("========================================\n");
        sb.append("        HÓA ĐƠN THANH TOÁN\n");
        sb.append("========================================\n\n");

        sb.append("Số hóa đơn: #").append(invoice.getHoadonId()).append("\n");
        sb.append("Bàn: ").append(table.getTableNumber()).append("\n");
        sb.append("Thời gian: ").append(invoice.getThoiGian().format(formatter)).append("\n");
        sb.append("Phương thức: ").append(invoice.getPhuongThuc()).append("\n");
        sb.append("----------------------------------------\n\n");

        sb.append(String.format("%-25s %6s %12s %12s\n", "Tên món", "SL", "Đơn giá", "Thành tiền"));
        sb.append("----------------------------------------\n");

        double total = 0.0;
        for (OrderDetail detail : orderDetails) {
            Food food = foodMap.get(detail.getMonId());
            String foodName = (food != null) ? food.getFoodName() : "Món #" + detail.getMonId();
            int quantity = detail.getSoLuong();
            double unitPrice = detail.getDonGia();
            double lineTotal = detail.getThanhTien();
            total += lineTotal;

            sb.append(String.format("%-25s %6d %12.0f %12.0f\n",
                    foodName.length() > 25 ? foodName.substring(0, 22) + "..." : foodName,
                    quantity, unitPrice, lineTotal));
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("%-43s %12.0f VND\n", "TỔNG CỘNG:", total));
        sb.append("========================================\n");
        sb.append("        Cảm ơn quý khách!\n");
        sb.append("========================================\n");

        return sb.toString();
    }

    public static void saveBillToFile(String content, int invoiceId) {
        try {
            String fileName = "bill_" + invoiceId + "_" + System.currentTimeMillis() + ".txt";
            FileWriter writer = new FileWriter(fileName);
            writer.write(content);
            writer.close();
            System.out.println("Bill saved to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving bill to file: " + e.getMessage());
        }
    }

    private static void showPrintDialog(String billContent) {
        try {
            // Create a simple printable node
            VBox printNode = new VBox(5);
            printNode.setStyle("-fx-padding: 20; -fx-font-family: 'Courier New'; -fx-font-size: 12;");

            String[] lines = billContent.split("\n");
            for (String line : lines) {
                Label label = new Label(line);
                printNode.getChildren().add(label);
            }

            // Create printer job
            PrinterJob printerJob = PrinterJob.createPrinterJob();
            if (printerJob != null) {
                boolean showDialog = printerJob.showPrintDialog(new Stage());
                if (showDialog) {
                    PageLayout pageLayout = printerJob.getPrinter().getDefaultPageLayout();
                    printNode.setPrefWidth(pageLayout.getPrintableWidth());

                    boolean success = printerJob.printPage(pageLayout, printNode);
                    if (success) {
                        printerJob.endJob();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error showing print dialog: " + e.getMessage());
        }
    }
}
