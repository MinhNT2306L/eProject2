package com.example.restaurant_management;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Hiển thị màn hình login đầu tiên
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurant_management/View/login-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("LOCAL FOOD - Đăng nhập");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Không cho phép resize màn hình login
        primaryStage.centerOnScreen(); // Căn giữa màn hình
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
