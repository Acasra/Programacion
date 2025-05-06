package Controllers;

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

    @FXML
    public void initialize() {
        cargarEspectaculos();
    }

    private void cargarEspectaculos() {
        List<String> espectaculos = new ArrayList<>();
        espectaculos.add("Cars");
        espectaculos.add("Dragon Ball");
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

            // Pasar el espectáculo seleccionado al controlador de reservas
            ReservasController reservasController = loader.getController();
            reservasController.setEspectaculo(espectaculoSeleccionado);

            Stage stage = (Stage) espectaculosComboBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Reservas para " + espectaculoSeleccionado);
        }
    }
}
