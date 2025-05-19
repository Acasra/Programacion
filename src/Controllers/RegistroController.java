package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import DataBase.ConexionBBDD;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistroController {
    @FXML
    private TextField nombreField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button registrarButton;
    @FXML
    private Button cancelarButton;

    @FXML
    private void handleRegistro() {
        String nombre = nombreField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        ConexionBBDD conexion = new ConexionBBDD();

        try (Connection conn = conexion.getConnection()) {
            String sql = "INSERT INTO Programacion.USUARIOS (nombre, email, contraseña) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();

            // Volver al login después del registro
            volverALogin();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void volverALogin() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/login.fxml"));
        Stage stage = (Stage) cancelarButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Login");
    }
}