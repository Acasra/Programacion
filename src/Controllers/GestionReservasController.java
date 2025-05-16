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

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
        cargarReservasUsuario();
    }

    private void cargarReservasUsuario() {
        listaReservas.clear();

        String sql = """
        SELECT r.id_reserva, r.id_espectaculo, r.id_butaca, r.id_usuario, r.estado,
               e.nombre AS nombreEspectaculo,
               e.precio_base,
               e.precio_vip,
               b.fila || b.columna AS butaca,
               b.tipo AS tipoButaca
        FROM Programacion.RESERVAS r
        JOIN Programacion.ESPECTACULOS e ON r.id_espectaculo = e.id_espectaculo
        JOIN Programacion.BUTACAS b ON r.id_butaca = b.id_butaca
        WHERE r.id_usuario = ?
        """;

        try (Connection conn = ConexionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String tipo = rs.getString("tipoButaca");
                double precio;
                if ("VIP".equalsIgnoreCase(tipo)) {
                    precio = rs.getDouble("precio_vip");
                } else {
                    precio = rs.getDouble("precio_base");
                }

                Reserva reserva = new Reserva(
                        rs.getInt("id_reserva"),
                        rs.getInt("id_espectaculo"),
                        rs.getInt("id_butaca"),
                        rs.getInt("id_usuario"),
                        rs.getString("estado"),
                        rs.getString("nombreEspectaculo"),
                        rs.getString("butaca"),
                        tipo,
                        precio
                );
                listaReservas.add(reserva);
            }

            tablaReservas.setItems(listaReservas);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar las reservas.");
        }
    }




    @FXML
    private void cancelarReserva() {
        Reserva seleccionada = tablaReservas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Aviso", "Selecciona una reserva para cancelar.");
            return;
        }

        // Confirmar la acción con el usuario (opcional)
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cancelación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Estás seguro de que quieres cancelar esta reserva?");
        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;  // El usuario canceló la acción
        }

        String sql = "UPDATE Programacion.RESERVAS SET estado = ? WHERE id_reserva = ?";

        try (Connection conn = ConexionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "cancelada"); // o el estado que uses para canceladas
            stmt.setInt(2, seleccionada.getIdReserva());

            int filasActualizadas = stmt.executeUpdate();

            if (filasActualizadas > 0) {
                mostrarAlerta("Éxito", "Reserva cancelada correctamente.");
                cargarReservasUsuario(); // Recargar tabla para refrescar datos
            } else {
                mostrarAlerta("Error", "No se pudo cancelar la reserva.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error al cancelar la reserva.");
        }
    }


    @FXML
    private void volverACartelera() {
        Stage stage = (Stage) tablaReservas.getScene().getWindow();
        stage.close(); // Cierra solo esta ventana (asumimos que hay una principal abierta)
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
