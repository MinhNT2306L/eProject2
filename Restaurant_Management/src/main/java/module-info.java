module com.example.restaurant_management {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.sql;

    opens com.example.restaurant_management to javafx.fxml;
    opens com.example.restaurant_management.Controller to javafx.fxml;
    opens com.example.restaurant_management.service;

    exports com.example.restaurant_management;
    exports com.example.restaurant_management.Controller;
}