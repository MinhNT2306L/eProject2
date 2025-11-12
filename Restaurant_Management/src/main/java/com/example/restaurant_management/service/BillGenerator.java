package com.example.restaurant_management.service;

import com.example.restaurant_management.entity.*;
import com.example.restaurant_management.entityRepo.FoodRepo;
import com.example.restaurant_management.mapper.FoodMapper;
import javafx.fxml.FXMLLoader;
import javafx.print.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillGenerator {

    public static void generateBill(Invoice invoice, Order order, List<OrderDetail> orderDetails, Table table) {
        try {
            // Generate and save PDF
            String pdfPath = generatePDFBill(invoice, order, orderDetails, table);
            
            // Show bill preview window
            showBillPreview(invoice, order, orderDetails, table);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating bill: " + e.getMessage(), e);
        }
    }

    public static String generatePDFBill(Invoice invoice, Order order, List<OrderDetail> orderDetails, Table table) throws IOException {
        // Create bill folder if it doesn't exist
        File billFolder = new File("bill");
        if (!billFolder.exists()) {
            billFolder.mkdirs();
        }

        // Get food names
        FoodRepo foodRepo = new FoodRepo(new FoodMapper());
        List<Food> allFoods = foodRepo.findAllFoods();
        Map<Integer, Food> foodMap = new HashMap<>();
        for (Food food : allFoods) {
            foodMap.put(food.getFoodId(), food);
        }

        // Create PDF document
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Set up fonts
        PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        float margin = 50;
        float yPosition = page.getMediaBox().getHeight() - margin;
        float lineHeight = 20;
        float currentY = yPosition;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Title
        contentStream.beginText();
        contentStream.setFont(titleFont, 24);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("LOCAL FOOD RESTAURANT");
        contentStream.endText();
        currentY -= lineHeight * 1.5f;

        contentStream.beginText();
        contentStream.setFont(headerFont, 18);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("HÓA ĐƠN THANH TOÁN");
        contentStream.endText();
        currentY -= lineHeight * 2;

        // Invoice Info
        contentStream.beginText();
        contentStream.setFont(normalFont, 12);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("Số hóa đơn: #" + invoice.getHoadonId());
        contentStream.endText();
        currentY -= lineHeight;

        contentStream.beginText();
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("Bàn: " + table.getTableNumber());
        contentStream.endText();
        currentY -= lineHeight;

        contentStream.beginText();
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("Thời gian: " + invoice.getThoiGian().format(formatter));
        contentStream.endText();
        currentY -= lineHeight;

        contentStream.beginText();
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("Phương thức: " + invoice.getPhuongThuc());
        contentStream.endText();
        currentY -= lineHeight * 1.5f;

        // Draw line
        contentStream.moveTo(margin, currentY);
        contentStream.lineTo(page.getMediaBox().getWidth() - margin, currentY);
        contentStream.stroke();
        currentY -= lineHeight;

        // Table Header
        contentStream.beginText();
        contentStream.setFont(headerFont, 12);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("Tên món");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(margin + 200, currentY);
        contentStream.showText("SL");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(margin + 250, currentY);
        contentStream.showText("Đơn giá");
        contentStream.endText();

        contentStream.beginText();
        contentStream.newLineAtOffset(margin + 400, currentY);
        contentStream.showText("Thành tiền");
        contentStream.endText();
        currentY -= lineHeight;

        // Draw line
        contentStream.moveTo(margin, currentY);
        contentStream.lineTo(page.getMediaBox().getWidth() - margin, currentY);
        contentStream.stroke();
        currentY -= lineHeight;

        // Items
        double total = 0.0;
        for (OrderDetail detail : orderDetails) {
            Food food = foodMap.get(detail.getMonId());
            if (food == null) continue;

            String foodName = food.getFoodName();
            int quantity = detail.getSoLuong();
            double unitPrice = detail.getDonGia();
            double lineTotal = detail.getThanhTien();
            total += lineTotal;

            // Check if we need a new page
            if (currentY < 100) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                currentY = page.getMediaBox().getHeight() - margin;
            }

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(margin, currentY);
            contentStream.showText(foodName.length() > 30 ? foodName.substring(0, 27) + "..." : foodName);
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(margin + 200, currentY);
            contentStream.showText(String.valueOf(quantity));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(margin + 250, currentY);
            contentStream.showText(String.format("%,.0f VND", unitPrice));
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(margin + 400, currentY);
            contentStream.showText(String.format("%,.0f VND", lineTotal));
            contentStream.endText();
            currentY -= lineHeight;
        }

        currentY -= lineHeight;

        // Draw line
        contentStream.moveTo(margin, currentY);
        contentStream.lineTo(page.getMediaBox().getWidth() - margin, currentY);
        contentStream.stroke();
        currentY -= lineHeight;

        // Total
        contentStream.beginText();
        contentStream.setFont(headerFont, 16);
        contentStream.newLineAtOffset(margin + 300, currentY);
        contentStream.showText("TỔNG CỘNG:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(headerFont, 18);
        contentStream.newLineAtOffset(margin + 400, currentY);
        contentStream.showText(String.format("%,.0f VND", total));
        contentStream.endText();
        currentY -= lineHeight * 2;

        // Footer
        contentStream.beginText();
        contentStream.setFont(normalFont, 12);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("Cảm ơn quý khách đã sử dụng dịch vụ!");
        contentStream.endText();
        currentY -= lineHeight;

        contentStream.beginText();
        contentStream.setFont(normalFont, 10);
        contentStream.newLineAtOffset(margin, currentY);
        contentStream.showText("Hẹn gặp lại!");
        contentStream.endText();

        contentStream.close();

        // Save PDF
        String fileName = "bill_" + invoice.getHoadonId() + "_" + System.currentTimeMillis() + ".pdf";
        File pdfFile = new File(billFolder, fileName);
        document.save(pdfFile);
        document.close();

        System.out.println("PDF Bill saved to: " + pdfFile.getAbsolutePath());
        return pdfFile.getAbsolutePath();
    }

    private static void showBillPreview(Invoice invoice, Order order, List<OrderDetail> orderDetails, Table table) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    BillGenerator.class.getResource("/com/example/restaurant_management/View/BillPreviewView.fxml")
            );
            Parent root = loader.load();

            com.example.restaurant_management.Controller.BillPreviewController controller = loader.getController();
            controller.setBillData(invoice, order, orderDetails, table);

            Stage stage = new Stage();
            stage.setTitle("Hóa đơn #" + invoice.getHoadonId());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error showing bill preview: " + e.getMessage());
        }
    }

    private static String createBillContent(Invoice invoice, Order order, List<OrderDetail> orderDetails, Table table) {
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

    private static void saveBillToFile(String content, int invoiceId) {
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

