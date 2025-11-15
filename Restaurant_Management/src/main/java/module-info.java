module com.example.restaurant_management {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.sql;

    opens com.example.restaurant_management to javafx.fxml;
    opens com.example.restaurant_management.Controller to javafx.fxml;

    exports com.example.restaurant_management;
    exports com.example.restaurant_management.Controller;
    exports com.example.restaurant_management.Controller.Manager;
    opens com.example.restaurant_management.Controller.Manager to javafx.fxml;
    exports com.example.restaurant_management.Controller.Manager.Food;
    opens com.example.restaurant_management.Controller.Manager.Food to javafx.fxml;
    exports com.example.restaurant_management.Controller.Manager.Table;
    opens com.example.restaurant_management.Controller.Manager.Table to javafx.fxml;
}