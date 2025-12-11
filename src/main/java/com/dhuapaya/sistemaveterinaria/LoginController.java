package com.dhuapaya.sistemaveterinaria;

import com.dhuapaya.sistemaveterinaria.dao.UserDao;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label lblStatus;

    @FXML
    private final UserDao userDao = new UserDao();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            if (userDao.validateCredentials(username, password)) {
                // Redirigir a main.fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(loader.load()));
                stage.show();
            } else {
                lblStatus.setText("Usuario o contraseña incorrectos.");
            }
        } catch (Exception e) {
            lblStatus.setText("Error al iniciar sesión.");
            e.printStackTrace();
        }
    }
}