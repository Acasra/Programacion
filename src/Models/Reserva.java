package Models;

public class Reserva {
    private int idReserva;
    private int id_espectaculo;
    private int id_butaca;
    private int id_usuario;
    private String estado;
    private String nombreEspectaculo;
    private String butaca;
    private String tipoButaca;
    private double precio;

    public Reserva(int idReserva, int id_espectaculo, int id_butaca, int id_usuario,
                   String estado, String nombreEspectaculo, String butaca,
                   String tipoButaca, double precio) {
        this.idReserva = idReserva;
        this.id_espectaculo = id_espectaculo;
        this.id_butaca = id_butaca;
        this.id_usuario = id_usuario;
        this.estado = estado;
        this.nombreEspectaculo = nombreEspectaculo;
        this.butaca = butaca;
        this.tipoButaca = tipoButaca;
        this.precio = precio;
    }

    // Getters y setters para cada campo

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public int getId_espectaculo() {
        return id_espectaculo;
    }

    public void setId_espectaculo(int id_espectaculo) {
        this.id_espectaculo = id_espectaculo;
    }

    public int getId_butaca() {
        return id_butaca;
    }

    public void setId_butaca(int id_butaca) {
        this.id_butaca = id_butaca;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombreEspectaculo() {
        return nombreEspectaculo;
    }

    public void setNombreEspectaculo(String nombreEspectaculo) {
        this.nombreEspectaculo = nombreEspectaculo;
    }

    public String getButaca() {
        return butaca;
    }

    public void setButaca(String butaca) {
        this.butaca = butaca;
    }

    public String getTipoButaca() {
        return tipoButaca;
    }

    public void setTipoButaca(String tipoButaca) {
        this.tipoButaca = tipoButaca;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
}
