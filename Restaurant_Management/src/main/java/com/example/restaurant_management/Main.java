package com.example.restaurant_management;

import com.example.restaurant_management.api.RestApiServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static RestApiServer apiServer;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start REST API server in background thread
        startApiServer();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/restaurant_management/View/login-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("LOCAL FOOD - Đăng nhập");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Stop API server when application closes
        if (apiServer != null) {
            apiServer.stop();
        }
        super.stop();
    }

    private void startApiServer() {
        try {
            apiServer = new RestApiServer();
            apiServer.start();
        } catch (Exception e) {
            System.err.println("Failed to start REST API server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
