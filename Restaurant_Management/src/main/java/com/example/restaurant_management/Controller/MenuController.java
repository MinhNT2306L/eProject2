package com.example.restaurant_management.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;

public class MenuController {
    @FXML
    private VBox menuContainer;

    private final ObservableList<String> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        String[][] dishes = {
                {"Phở Bò", "Món truyền thống Việt Nam", "50000"},
                {"Cơm Tấm", "Cơm sườn, bì, chả", "45000"},
                {"Bún Chả", "Thịt nướng Hà Nội", "40000"},
                {"Trà Sữa", "Uống là ghiền", "35000"}
        };

        for (String[] d : dishes) {
            Label name = new Label(d[0]);
            Label desc = new Label(d[1]);
            Label price = new Label(d[2] + " đ");

            Spinner<Integer> quantity = new Spinner<>(1, 10, 1);
            Button addBtn = new Button("Chọn mua");
            addBtn.setOnAction(e -> {
                String item = d[0] + " x" + quantity.getValue() + " - " + d[2] + " đ";
                orderList.add(item);
            });

            VBox box = new VBox(5, name, desc, price, quantity, addBtn);
            box.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-radius: 8;");
            menuContainer.getChildren().add(box);
        }
    }

    @FXML
    private void openOrderSummary(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurant_management/view/OrderSummaryView.fxml"));
        Parent root = loader.load();

        OrderSummaryController controller = loader.getController();
        controller.setOrderList(orderList);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Hóa đơn tạm");
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }
}
