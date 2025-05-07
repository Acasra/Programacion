package Controllers;

import DataBase.ConexionBBDD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeleccionEspectaculosController {
    @FXML
    private ComboBox<String> espectaculosComboBox;

    private int idUsuario;

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    @FXML
    public void initialize() {
        cargarEspectaculos();
    }

    private void cargarEspectaculos() {
        List<String> espectaculos = new ArrayList<>();
        espectaculos.add("Cars");
        espectaculos.add("Dragon Ball Super");
        espectaculos.add("Spiderman");
        espectaculos.add("Frozen");

        espectaculosComboBox.getItems().addAll(espectaculos);
    }

    @FXML
    private void irAReservas() throws IOException {
        String espectaculoSeleccionado = espectaculosComboBox.getValue();
        if (espectaculoSeleccionado != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reservas.fxml"));
            Parent root = loader.load();

            ReservasController reservasController = loader.getController();

            int idEspectaculo = obtenerIdEspectaculo(espectaculoSeleccionado);
            reservasController.inicializarDatos(idUsuario, idEspectaculo);

            Stage stage = (Stage) espectaculosComboBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Reservas para " + espectaculoSeleccionado);
        }
    }

    private int obtenerIdEspectaculo(String nombre) {
        int id = -1;
        try (Connection conn = ConexionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id_espectaculo FROM Programacion.ESPECTACULOS WHERE nombre = ?")) {

            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt("id_espectaculo");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
}
