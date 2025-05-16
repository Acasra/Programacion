package Controllers;

import DataBase.ConexionBBDD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import Models.Reserva;

import java.sql.*;

public class GestionReservasController {

    @FXML
    private TableView<Reserva> tablaReservas;

    private int idUsuario;
    private final ObservableList<Reserva> listaReservas = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar listener para actualizaciones
        tablaReservas.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> actualizarInterfaz());
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
        cargarReservasUsuario();
    }

    private void cargarReservasUsuario() {
        listaReservas.clear();

        String sql = """
            SELECT r.id_reserva, r.id_espectaculo, r.id_butaca, r.id_usuario, r.estado,
                   e.nombre AS nombreEspectaculo,
                   b.fila || b.columna AS butaca,
                   b.tipo AS tipoButaca,
                   CASE WHEN b.tipo = 'VIP' THEN e.precio_vip ELSE e.precio_base END AS precio
            FROM Programacion.RESERVAS r
            JOIN Programacion.ESPECTACULOS e ON r.id_espectaculo = e.id_espectaculo
            JOIN Programacion.BUTACAS b ON r.id_butaca = b.id_butaca
            WHERE r.id_usuario = ? AND r.estado = 'RESERVADA'
            ORDER BY r.id_reserva DESC
            """;

        try (Connection conn = ConexionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reserva reserva = new Reserva(
                        rs.getInt("id_reserva"),
                        rs.getInt("id_espectaculo"),
                        rs.getInt("id_butaca"),
                        rs.getInt("id_usuario"),
                        rs.getString("estado"),
                        rs.getString("nombreEspectaculo"),
                        rs.getString("butaca"),
                        rs.getString("tipoButaca"),
                        rs.getDouble("precio")
                );
                listaReservas.add(reserva);
            }

            tablaReservas.setItems(listaReservas);

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las reservas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelarReserva() {
        Reserva reservaSeleccionada = tablaReservas.getSelectionModel().getSelectedItem();

        if (reservaSeleccionada == null) {
            mostrarAlerta("Error", "Seleccione una reserva para cancelar");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cancelación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Está seguro que desea cancelar la reserva de " +
                reservaSeleccionada.getNombreEspectaculo() + "?");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ejecutarCancelacion(reservaSeleccionada);
            }
        });
    }

    private void ejecutarCancelacion(Reserva reserva) {
        try (Connection conn = ConexionBBDD.getConnection()) {
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Verificar que la reserva existe y está activa
            if (!validarReservaActiva(conn, reserva.getIdReserva())) {
                mostrarAlerta("Error", "La reserva ya está cancelada o no existe");
                return;
            }

            // 2. Actualizar estado en la base de datos
            String sql = "UPDATE PROGRAMACION.RESERVAS SET ESTADO = 'CANCELADA' WHERE ID_RESERVA = ? AND ESTADO = 'RESERVADA'";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, reserva.getIdReserva());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows == 0) {
                    conn.rollback();
                    mostrarAlerta("Error", "No se pudo cancelar la reserva");
                    return;
                }
            }

            conn.commit(); // Confirmar cambios

            // 3. Actualizar la interfaz
            reserva.setEstado("CANCELADA");
            listaReservas.remove(reserva); // Eliminar de la lista visible
            tablaReservas.refresh();

            mostrarAlerta("Éxito", "Reserva cancelada correctamente. Butaca liberada.");

            // 🔁 NUEVO: Notificar que se ha liberado una butaca
            notificarButacasActualizadas(); // <<--- implementa esto si quieres refrescar la interfaz de butacas

        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cancelar reserva: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🔁 MÉTODO OPCIONAL A IMPLEMENTAR EN TU CONTROLADOR PRINCIPAL
    private void notificarButacasActualizadas() {
        // Aquí podrías llamar a un método de otro controlador que recargue las butacas.
        // Por ejemplo:
        // PrincipalController.getInstance().actualizarButacas(idEspectaculo);
    }

    private boolean validarReservaActiva(Connection conn, int idReserva) throws SQLException {
        String sql = "SELECT 1 FROM PROGRAMACION.RESERVAS WHERE ID_RESERVA = ? AND ESTADO = 'RESERVADA'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idReserva);
            return stmt.executeQuery().next();
        }
    }

    private void actualizarInterfaz() {
        tablaReservas.refresh();
    }

    @FXML
    private void volverACartelera() {
        Stage stage = (Stage) tablaReservas.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
