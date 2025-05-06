package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
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

    private int idUsuario;
    private int idEspectaculo;
    private int idButaca;
    private List<Integer> butacasSeleccionadas = new ArrayList<>(); // Ahora almacenamos IDs de butacas

    // Método para inicializar los datos
    public void ReservasController(int idUsuario, int idEspectaculo, int idButaca) {
        this.idUsuario = idUsuario;
        this.idEspectaculo = idEspectaculo;
        this.idButaca = idButaca;


    }
    @FXML
    public void initialize() {
        // Cargar las butacas al iniciar
        cargarButacas();
        actualizarContador();
    }
    private void cargarButacas() {
        butacasGrid.getChildren().clear();

        // Consulta para obtener todas las butacas
        String query = """
                SELECT b.id_butaca, b.fila, b.columna, b.tipo
                FROM Programacion.BUTACAS b
                """;
        String queryocupadas = """
                SELECT b.id_butaca, b.fila, b.columna, b.tipo, r.estado
                FROM Programacion.BUTACAS b, Programacion.RESERVAS r
                WHERE b.id_butaca = r.id_butaca
                """;

        try (Connection conn = ConexionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int idButaca = rs.getInt("id_butaca");
                int fila = rs.getInt("fila");
                int columna = rs.getInt("columna");
                String tipo = rs.getString("tipo");

                Button butaca = new Button(fila + "-" + columna + "\n" + tipo);
                butaca.setPrefSize(60, 60);

                if (tipo.equals("VIP")) {
                    butaca.setStyle("-fx-background-color: yellow; -fx-text-fill: white;");
                } else if (tipo.equals("ESTANDAR")) {
                    butaca.setStyle("-fx-background-color: green ; -fx-text-fill: white;");
                }
                butaca.setOnAction(event -> seleccionarButaca(butaca, idButaca));
                butacasGrid.add(butaca, columna-1, fila-1);
            }


        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las butacas: " + e.getMessage());
            e.printStackTrace();
        }


        try (Connection conn = ConexionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryocupadas)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int idButaca = rs.getInt("id_butaca");
                int fila = rs.getInt("fila");
                int columna = rs.getInt("columna");
                String tipo = rs.getString("tipo");
                String estado = rs.getString("estado");

                if (estado.equals("Ocupado")) {
                    //Obtener id_butaca y hacer setstyle red
                }

            }


        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar las butacas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seleccionarButaca(Button butaca, int idButaca) {
        if (butacasSeleccionadas.contains(idButaca)) {
            // Deseleccionar
            butaca.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            butacasSeleccionadas.remove(Integer.valueOf(idButaca));
        } else {
            if (butacasSeleccionadas.size() < 4) {
                // Seleccionar
                butaca.setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                butacasSeleccionadas.add(idButaca);
            } else {
                mostrarAlerta("Límite", "Máximo 4 butacas por reserva");
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

            // Insertar cada reserva
            for (int idButaca : butacasSeleccionadas) {
                String sql = "INSERT INTO Programacion.RESERVAS (ID_ESPECTACULO, ID_BUTACA, ID_USUARIO, ESTADO) " +
                        "VALUES (?, ?, ?, 'RESERVADA')";

                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setInt(1, idEspectaculo);
                    stmt.setInt(2, idButaca);
                    stmt.setInt(3, idUsuario);
                    stmt.executeUpdate();

                    // Opcional: Obtener el ID generado
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int idReserva = generatedKeys.getInt(1);
                            System.out.println("Reserva creada ID: " + idReserva);
                        }
                    }
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

    private void actualizarContador() {
        butacasSeleccionadasLabel.setText("Seleccionadas: " + butacasSeleccionadas.size() + "/4");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Establece el espectáculo seleccionado y actualiza la interfaz
     * @param espectaculoSeleccionado Nombre del espectáculo seleccionado
     */
    public void setEspectaculo(String espectaculoSeleccionado) {
        // 1. Asignar el nombre del espectáculo
        this.tituloEspectaculo.setText("Reservas para: " + espectaculoSeleccionado);

        // 2. Obtener el ID del espectáculo desde la base de datos
        try (Connection conn = ConexionBBDD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id_espectaculo FROM Programacion.ESPECTACULOS WHERE nombre = ?")) {

            stmt.setString(1, espectaculoSeleccionado);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                this.idEspectaculo = rs.getInt("id_espectaculo");
                // 3. Cargar las butacas para este espectáculo
                cargarButacas();
            } else {
                mostrarAlerta("Error", "No se encontró el espectáculo en la base de datos");
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo obtener información del espectáculo: " + e.getMessage());
            e.printStackTrace();
        }

        // 4. Reiniciar las selecciones
        butacasSeleccionadas.clear();
        actualizarContador();
    }
}