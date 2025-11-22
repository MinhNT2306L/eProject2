package com.example.restaurant_management.utils;

import javafx.stage.Stage;

public class StageUtils {

    /**
     * Automatically closes the stage when it loses focus.
     * 
     * @param stage The stage to apply the behavior to.
     */
    public static void autoCloseOnBlur(Stage stage) {
        if (stage == null)
            return;

        stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                stage.close();
            }
        });
    }
}
