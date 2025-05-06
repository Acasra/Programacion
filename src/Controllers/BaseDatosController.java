package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import DataBase.ConexionBBDD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseDatosController {
    @FXML
    private GridPane butacasGrid;
    @FXML
    private Label espectaculoLabel;
    @FXML
    private Button confirmarButton;

    private String espectaculo;
    private int butacasSeleccionadas = 0;

    ConexionBBDD conexion = new ConexionBBDD();

    public void setEspectaculo(String espectaculo) {
        this.espectaculo = espectaculo;
        espectaculoLabel.setText("Espectáculo: " + espectaculo);
        cargarButacas();
    }

    private void cargarButacas() {
        // Configurar el grid de butacas (ejemplo: 10x10)
        butacasGrid.getChildren().clear();

        for (int fila = 0; fila < 10; fila++) {
            for (int columna = 0; columna < 10; columna++) {
                Button butaca = new Button((fila+1) + "-" + (columna+1));
                butaca.setPrefSize(50, 50);

                // Verificar estado de la butaca en la BD
                if (butacaDisponible(fila+1, columna+1)) {
                    butaca.setStyle("-fx-background-color: green;");
                    butaca.setOnAction(e -> seleccionarButaca(butaca));
                } else {
                    butaca.setStyle("-fx-background-color: red;");
                    butaca.setDisable(true);
                }

                butacasGrid.add(butaca, columna, fila);
            }
        }
    }

    private boolean butacaDisponible(int fila, int columna) {
        try (Connection conn = conexion.getConnection()) {
            String sql = "SELECT COUNT(*) FROM RESERVAS WHERE id_espectaculo = " +
                    "(SELECT id_espectaculo FROM ESPECTACULOS WHERE nombre = ?) " +
                    "AND id_butaca = (SELECT id_butaca FROM BUTACAS WHERE fila = ? AND columna = ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, espectaculo);
            stmt.setInt(2, fila);
            stmt.setInt(3, columna);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void seleccionarButaca(Button butaca) {
        if (butacasSeleccionadas < 4) {
            butaca.setStyle("-fx-background-color: yellow;");
            butacasSeleccionadas++;
        } else {
            System.out.println("Máximo 4 butacas por reserva");
        }
    }

    @FXML
    private void confirmarReserva() {
        // Implementar lógica para guardar reservas en BD
        System.out.println("Reserva confirmada para " + espectaculo);
    }
}