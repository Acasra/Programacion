package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import DataBase.ConexionBBDD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ReservasController {
    @FXML private GridPane butacasGrid;
    @FXML private Label tituloEspectaculo;
    @FXML private Label butacasSeleccionadasLabel;
    @FXML private Label precioTotalLabel;

    private int idUsuario = -1;
    private int idEspectaculo = -1;
    private List<Integer> butacasSeleccionadas = new ArrayList<>();
    private double precioTotal = 0;

    // Imágenes para las butacas
    private final Image imgLibre = new Image(getClass().getResourceAsStream("/Resources/Butaca_Libre.png"));
    private final Image imgOcupada = new Image(getClass().getResourceAsStream("/Resources/Butaca_ocupada.png"));
    private final Image imgSeleccionada = new Image(getClass().getResourceAsStream("/Resources/Butaca_seleccionada.png"));
    private final Image imgVIP = new Image(getClass().getResourceAsStream("/Resources/Butaca_vip.png"));

    public void inicializarDatos(int idUsuario, int idEspectaculo) {
        this.idUsuario = idUsuario;
        this.idEspectaculo = idEspectaculo;

        if (idUsuario == -1 || idEspectaculo == -1) {
            mostrarAlerta("Error", "Usuario o espectáculo no válido");
            return;
        }

        cargarButacas();
        actualizarContador();
    }

    private void cargarButacas() {
        butacasGrid.getChildren().clear();

        String query = """
        SELECT b.id_butaca, b.fila, b.columna, b.tipo,
               CASE 
                   WHEN r.id_reserva IS NOT NULL AND r.id_usuario = ? THEN 'TU_RESERVA'
                   WHEN r.id_reserva IS NOT NULL THEN 'OCUPADA'
                   ELSE 'DISPONIBLE' 
               END as estado, 
               CASE 
                   WHEN b.tipo = 'VIP' THEN 15.00 
                   ELSE 10.00 
               END as precio
        FROM Programacion.BUTACAS b
        LEFT JOIN Programacion.RESERVAS r ON b.id_butaca = r.id_butaca AND r.id_espectaculo = ? 
        """;

        try (Connection conn = ConexionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idEspectaculo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int idButaca = rs.getInt("id_butaca");
                int fila = rs.getInt("fila");
                int columna = rs.getInt("columna");
                String tipo = rs.getString("tipo");
                String estado = rs.getString("estado");
                double precio = rs.getDouble("precio");

                ImageView imageView = new ImageView();
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);

                Button butaca = new Button();
                butaca.setGraphic(imageView);
                butaca.setPrefSize(50, 50);
                butaca.setStyle("-fx-background-color: transparent;");

                switch (estado) {
                    case "TU_RESERVA":
                        imageView.setImage(imgOcupada);
                        butaca.setDisable(true);
                        butaca.setTooltip(new Tooltip("Ya reservada por ti"));
                        break;
                    case "OCUPADA":
                        imageView.setImage(imgOcupada);
                        butaca.setDisable(true);
                        butaca.setTooltip(new Tooltip("Ocupada por otro usuario"));
                        break;
                    default:
                        if (tipo.equals("VIP")) {
                            imageView.setImage(imgVIP);
                            butaca.setTooltip(new Tooltip("Butaca VIP - €15.00"));
                        } else {
                            imageView.setImage(imgLibre);
                            butaca.setTooltip(new Tooltip("Butaca Normal - €10.00"));
                        }
                        butaca.setOnAction(event -> seleccionarButaca(butaca, imageView, idButaca, precio, tipo));
                }
                butacasGrid.add(butaca, columna - 1, fila - 1);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las butacas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seleccionarButaca(Button butaca, ImageView imageView, int idButaca, double precio, String tipo) {
        if (butacasSeleccionadas.contains(idButaca)) {
            // Deseleccionar
            butacasSeleccionadas.remove(Integer.valueOf(idButaca));
            precioTotal -= precio;
            if (tipo.equals("VIP")) {
                imageView.setImage(imgVIP);
            } else {
                imageView.setImage(imgLibre);
            }
        } else {
            if (butacasSeleccionadas.size() < 4) {
                try (Connection conn = ConexionBBDD.getConnection()) {
                    if (puedeReservarMas(conn)) {
                        butacasSeleccionadas.add(idButaca);
                        precioTotal += precio;
                        imageView.setImage(imgSeleccionada);
                    } else {
                        mostrarAlerta("Límite alcanzado",
                                "Ya tienes 4 butacas reservadas en este espectáculo.\n" +
                                        "No puedes seleccionar más.");
                    }
                } catch (SQLException e) {
                    mostrarAlerta("Error", "No se pudo verificar tus reservas");
                    e.printStackTrace();
                }
            } else {
                mostrarAlerta("Límite", "Solo puedes seleccionar 4 butacas a la vez");
            }
        }
        actualizarContador();
        actualizarPrecioTotal();
    }

    private void actualizarContador() {
        butacasSeleccionadasLabel.setText("Seleccionadas: " + butacasSeleccionadas.size() + "/4");
    }

    private void actualizarPrecioTotal() {
        precioTotalLabel.setText("Precio Total: €" + String.format("%.2f", precioTotal));
    }

    private boolean puedeReservarMas(Connection conn) throws SQLException {
        String sql = """
        SELECT COUNT(*) FROM Programacion.RESERVAS 
        WHERE ID_USUARIO = ? AND ID_ESPECTACULO = ? 
        AND ESTADO = 'RESERVADA'
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idEspectaculo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int reservasActuales = rs.getInt(1);
                return (reservasActuales + butacasSeleccionadas.size()) < 4;
            }
        }
        return false;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void confirmarReserva() {
        if (butacasSeleccionadas.isEmpty()) {
            mostrarAlerta("Error", "Seleccione al menos una butaca");
            return;
        }

        try (Connection conn = ConexionBBDD.getConnection()) {
            conn.setAutoCommit(false);

            if (!puedeReservarMas(conn)) {
                mostrarAlerta("Límite alcanzado",
                        "Ya has reservado el máximo de 4 butacas para este espectáculo.\n" +
                                "No puedes reservar más butacas.");
                return;
            }

            for (int idButaca : butacasSeleccionadas) {
                if (!validarButacaDisponible(conn, idButaca)) {
                    conn.rollback();
                    mostrarAlerta("Error", "La butaca seleccionada ya no está disponible");
                    return;
                }
            }

            for (int idButaca : butacasSeleccionadas) {
                String sql = "INSERT INTO Programacion.RESERVAS (ID_RESERVA, ID_ESPECTACULO, ID_BUTACA, ID_USUARIO, ESTADO) " +
                        "VALUES (Programacion.SEQ_RESERVAS.NEXTVAL, ?, ?, ?, 'RESERVADA')";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, idEspectaculo);
                    stmt.setInt(2, idButaca);
                    stmt.setInt(3, idUsuario);
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            mostrarAlerta("Éxito", "Reserva confirmada para " + butacasSeleccionadas.size() + " butacas");
            butacasSeleccionadas.clear();
            precioTotal = 0;
            cargarButacas();
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al reservar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validarButacaDisponible(Connection conn, int idButaca) throws SQLException {
        String sql = """
        SELECT COUNT(*) FROM Programacion.BUTACAS b
        LEFT JOIN Programacion.RESERVAS r ON b.ID_BUTACA = r.ID_BUTACA AND r.ID_ESPECTACULO = ? 
        WHERE b.ID_BUTACA = ? AND (r.ID_RESERVA IS NULL OR r.ESTADO = 'CANCELADA')
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEspectaculo);
            stmt.setInt(2, idButaca);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    @FXML
    private void volverACartelera() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/cartelera.fxml"));
        Parent root = loader.load();

        SeleccionEspectaculosController controller = loader.getController();
        controller.setIdUsuario(idUsuario);

        Stage stage = (Stage) butacasGrid.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Cartelera de Espectáculos");
    }
}