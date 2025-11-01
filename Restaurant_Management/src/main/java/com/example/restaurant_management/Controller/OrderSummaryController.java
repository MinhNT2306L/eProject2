package com.example.restaurant_management.Controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class OrderSummaryController {
    @FXML
    private ListView<String> orderListView;

    @FXML
    private Label totalLabel;

    public void setOrderList(ObservableList<String> orders) {
        orderListView.setItems(orders);
        int total = orders.stream()
                .mapToInt(o -> Integer.parseInt(o.replaceAll("\\D+", "")))
                .sum();
        totalLabel.setText("Tổng cộng: " + total + " đ");
    }
}
