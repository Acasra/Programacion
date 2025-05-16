package Models;

import javafx.beans.property.*;

public class Reserva {
    private final IntegerProperty idReserva = new SimpleIntegerProperty();
    private final IntegerProperty idEspectaculo = new SimpleIntegerProperty();
    private final IntegerProperty idButaca = new SimpleIntegerProperty();
    private final IntegerProperty idUsuario = new SimpleIntegerProperty();
    private final StringProperty estado = new SimpleStringProperty();
    private final StringProperty nombreEspectaculo = new SimpleStringProperty();
    private final StringProperty butaca = new SimpleStringProperty();
    private final StringProperty tipoButaca = new SimpleStringProperty();
    private final DoubleProperty precio = new SimpleDoubleProperty();

    public Reserva(int idReserva, int idEspectaculo, int idButaca, int idUsuario,
                   String estado, String nombreEspectaculo, String butaca,
                   String tipoButaca, double precio) {
        this.idReserva.set(idReserva);
        this.idEspectaculo.set(idEspectaculo);
        this.idButaca.set(idButaca);
        this.idUsuario.set(idUsuario);
        this.estado.set(estado);
        this.nombreEspectaculo.set(nombreEspectaculo);
        this.butaca.set(butaca);
        this.tipoButaca.set(tipoButaca);
        this.precio.set(precio);
    }

    // Getters para propiedades (necesarios para PropertyValueFactory)
    public int getIdReserva() { return idReserva.get(); }
    public int getIdEspectaculo() { return idEspectaculo.get(); }
    public int getIdButaca() { return idButaca.get(); }
    public int getIdUsuario() { return idUsuario.get(); }
    public String getEstado() { return estado.get(); }
    public String getNombreEspectaculo() { return nombreEspectaculo.get(); }
    public String getButaca() { return butaca.get(); }
    public String getTipoButaca() { return tipoButaca.get(); }
    public double getPrecio() { return precio.get(); }

    // Setters
    public void setEstado(String estado) { this.estado.set(estado); }

    // Property getters (para binding)
    public IntegerProperty idReservaProperty() { return idReserva; }
    public StringProperty estadoProperty() { return estado; }
    public StringProperty nombreEspectaculoProperty() { return nombreEspectaculo; }
    public StringProperty butacaProperty() { return butaca; }
    public StringProperty tipoButacaProperty() { return tipoButaca; }
    public DoubleProperty precioProperty() { return precio; }
}