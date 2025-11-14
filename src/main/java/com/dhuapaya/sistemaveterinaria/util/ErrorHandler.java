package com.dhuapaya.sistemaveterinaria.util;

import javafx.scene.control.Label;

public class ErrorHandler {

    public static void showError(Label label, Exception e, String msg) {
        label.setText(msg + ": " + e.getMessage());
        e.printStackTrace();
    }

    public static void showValidationError(Label label, IllegalArgumentException e) {
        label.setText("Validación fallida: " + e.getMessage());
    }
}
