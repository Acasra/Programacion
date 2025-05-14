package Controllers;

import DataBase.ConexionBBDD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeleccionEspectaculosController {
    @FXML
    private ComboBox<String> espectaculosComboBox;

    @FXML
    private ImageView imagenEspectaculo;

    private int idUsuario;

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    @FXML
    public void initialize() {
        cargarEspectaculos();
        cargarImagenPorDefecto();

        // Evento: cuando el cursor se mueve dentro del ComboBox
        espectaculosComboBox.setOnMouseMoved(event -> {
            String seleccionado = espectaculosComboBox.getValue();
            mostrarImagen(seleccionado);
        });

        // Evento: cuando se selecciona una opción (por si quieres usarlo también)
        espectaculosComboBox.setOnAction(event -> {
            String seleccionado = espectaculosComboBox.getValue();
            mostrarImagen(seleccionado);
        });
    }

    private void cargarEspectaculos() {
        List<String> espectaculos = new ArrayList<>();
        espectaculos.add("Cars");
        espectaculos.add("Dragon Ball Super");
        espectaculos.add("Spiderman");
        espectaculos.add("Frozen");

        espectaculosComboBox.getItems().addAll(espectaculos);
    }

    private void cargarImagenPorDefecto() {
        // Ruta relativa a src/Resources
        Image imagenNegra = cargarImagen("negro.png");
        if (imagenNegra != null) {
            imagenEspectaculo.setImage(imagenNegra);
        }
    }

    private void mostrarImagen(String espectaculo) {
        if (espectaculo == null) return;

        String nombreArchivo = switch (espectaculo) {
            case "Cars" -> "Cars.jpg";
            case "Dragon Ball Super" -> "Dragon_Ball_Super.jpg";
            case "Frozen" -> "Frozen.jpeg";
            case "Spiderman" -> "Spiderman.jpg";
            default -> null;
        };

        if (nombreArchivo != null) {
            Image img = cargarImagen(nombreArchivo);
            if (img != null) {
                imagenEspectaculo.setImage(img);
            }
        }
    }

    private Image cargarImagen(String nombreArchivo) {
        try {
            URL imageUrl = getClass().getResource("/Resources/" + nombreArchivo);
            if (imageUrl != null) {
                return new Image(imageUrl.toString());
            } else {
                System.err.println("No se encontró la imagen: " + nombreArchivo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
