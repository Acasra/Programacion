package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import DataBase.ConexionBBDD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservasController {
    @FXML private GridPane butacasGrid;
    @FXML private Label tituloEspectaculo;
    @FXML private Label butacasSeleccionadasLabel;

    private int idUsuario = -1;
    private int idEspectaculo = -1;
    private List<Integer> butacasSeleccionadas = new ArrayList<>();

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

    @FXML
    public void initialize() {
        // No hace nada hasta que se llamen los datos desde otro controlador
    }

    private void cargarButacas() {
        butacasGrid.getChildren().clear();

        String query = """
        SELECT b.id_butaca, b.fila, b.columna, b.tipo,
               CASE 
                   WHEN r.id_reserva IS NOT NULL AND r.id_usuario = ? THEN 'TU_RESERVA'
                   WHEN r.id_reserva IS NOT NULL THEN 'OCUPADA'
                   ELSE 'DISPONIBLE' 
               END as estado
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

                Button butaca = new Button(fila + "-" + columna + "\n" + tipo);
                butaca.setPrefSize(60, 60);

                switch (estado) {
                    case "TU_RESERVA":
                        butaca.setStyle("-fx-background-color: purple; -fx-text-fill: white;");
                        butaca.setDisable(true);
                        butaca.setTooltip(new Tooltip("Ya reservada por ti"));
                        break;
                    case "OCUPADA":
                        butaca.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                        butaca.setDisable(true);
                        butaca.setTooltip(new Tooltip("Ocupada por otro usuario"));
                        break;
                    default:
                        if (tipo.equals("VIP")) {
                            butaca.setStyle("-fx-background-color: gold; -fx-text-fill: black;");
                        } else {
                            butaca.setStyle("-fx-background-color: green; -fx-text-fill: white;");
                        }
                        butaca.setOnAction(event -> seleccionarButaca(butaca, idButaca));
                }
                butacasGrid.add(butaca, columna-1, fila-1);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las butacas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seleccionarButaca(Button butaca, int idButaca) {
        if (butacasSeleccionadas.contains(idButaca)) {
            // Deseleccionar
            String tipo = butaca.getText().split("\n")[1];
            if (tipo.equals("VIP")) {
                butaca.setStyle("-fx-background-color: gold; -fx-text-fill: black;");
            } else {
                butaca.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            }
            butacasSeleccionadas.remove(Integer.valueOf(idButaca));
        } else {
            if (butacasSeleccionadas.size() < 4) {
                // Verificar que no exceda el límite total de 4
                try (Connection conn = ConexionBBDD.getConnection()) {
                    if (puedeReservarMas(conn)) {
                        butaca.setStyle("-fx-background-color: blue; -fx-text-fill: white;");
                        butacasSeleccionadas.add(idButaca);
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
    }

    @FXML
    private void confirmarReserva() {
        if (butacasSeleccionadas.isEmpty()) {
            mostrarAlerta("Error", "Seleccione al menos una butaca");
            return;
        }

        try (Connection conn = ConexionBBDD.getConnection()) {
            conn.setAutoCommit(false);

            // Verificar límite de 4 butacas por usuario y espectáculo
            if (!puedeReservarMas(conn)) {
                mostrarAlerta("Límite alcanzado",
                        "Ya has reservado el máximo de 4 butacas para este espectáculo.\n" +
                                "No puedes reservar más butacas.");
                return;
            }

            // Verificar disponibilidad de butacas
            for (int idButaca : butacasSeleccionadas) {
                if (!validarButacaDisponible(conn, idButaca)) {
                    conn.rollback();
                    mostrarAlerta("Error", "La butaca seleccionada ya no está disponible");
                    return;
                }
            }

            // Procesar reservas
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
            cargarButacas();
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al reservar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validarUsuarioYEspectaculo(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Programacion.USUARIOS WHERE ID_USUARIO = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
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

    private void actualizarContador() {
        butacasSeleccionadasLabel.setText("Seleccionadas: " + butacasSeleccionadas.size() + "/4");
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
                return (reservasActuales + butacasSeleccionadas.size()) <= 4;
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

    public void setEspectaculo(String nombreEspectaculo) {
        try (Connection conn = ConexionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id_espectaculo FROM Programacion.ESPECTACULOS WHERE nombre = ?")) {

            stmt.setString(1, nombreEspectaculo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.idEspectaculo = rs.getInt("id_espectaculo");
                tituloEspectaculo.setText("Reservas para: " + nombreEspectaculo);

                // Verificar límite de reservas
                if (!puedeReservarMas(conn)) {
                    mostrarAlerta("Límite alcanzado",
                            "Ya has reservado el máximo de 4 butacas para este espectáculo");
                }

                cargarButacas();
            } else {
                mostrarAlerta("Error", "Espectáculo no encontrado");
            }
        } catch (SQLException e) {
            mostrarAlerta("Error BD", "Error al buscar espectáculo: " + e.getMessage());
            e.printStackTrace();
        }
        butacasSeleccionadas.clear();
        actualizarContador();
    }
}
