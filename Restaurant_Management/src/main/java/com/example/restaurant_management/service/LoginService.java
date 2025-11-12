package com.example.restaurant_management.service;

import com.example.restaurant_management.ConnectDB.ConnectDB;
import com.example.restaurant_management.entity.Employee;
import com.example.restaurant_management.entityRepo.EmployeeRepo;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {
    public static String verifyLogin(String userName, String password){
        try {
            Connection conn = ConnectDB.getConnection();
            String sql = "SELECT * FROM `nhanvien` N LEFT JOIN `roles` R ON N.role_id = R.role_id WHERE username = \"" + userName + "\"";
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while(rs.next()){
                if (password.equals(rs.getString("password"))){
                    // Store current employee in session
                    EmployeeRepo employeeRepo = new EmployeeRepo();
                    Employee employee = employeeRepo.findByUsernameAndPassword(userName, password);
                    if (employee != null) {
                        UserSession.setCurrentEmployee(employee);
                    }
                    return rs.getString("role_name");
                }
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public static void logout(Stage primaryStage){
        UserSession.clear(); // Clear session on logout
        try {
            FXMLLoader loader = new FXMLLoader(LoginService.class.getResource("/com/example/restaurant_management/View/login-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
