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
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        ConexionBBDD conexion = new ConexionBBDD();

        try (Connection conn = conexion.getConnection()) {
            String sql = "SELECT * FROM Programacion.USUARIOS WHERE EMAIL = ? AND CONTRASEÑA = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int idUsuario = rs.getInt("ID_USUARIO");
                System.out.println("Login exitoso");
                goToCartelera(idUsuario);
            } else {
                System.out.println("Credenciales incorrectas");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error de conexión a la base de datos");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToRegister() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/views/registro.fxml"));
        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Registro de Usuario");
    }

    private void goToCartelera(int idUsuario) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cartelera.fxml"));
        Parent root = loader.load();

        SeleccionEspectaculosController controller = loader.getController();
        controller.setIdUsuario(idUsuario);

        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Cartelera de Espectáculos");
    }
}
