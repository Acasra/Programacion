package Models;

public class Butaca {
    private int idButaca;
    private String fila;
    private String columna;
    private String tipo;

    // Constructores
    public Butaca(int idButaca, String fila, String columna, String tipo) {
        this.idButaca = idButaca;
        this.fila = fila;
        this.columna = columna;
        this.tipo = tipo;
    }

    public Butaca(String fila, String columna, String tipo) {
        this.fila = fila;
        this.columna = columna;
        this.tipo = tipo;
    }

    // Getters y Setters
    public int getIdButaca() {
        return idButaca;
    }

    public void setIdButaca(int idButaca) {
        this.idButaca = idButaca;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public String getColumna() {
        return columna;
    }

    public void setColumna(String columna) {
        this.columna = columna;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNumeroButaca() {
        return fila + columna;
    }

    @Override
    public String toString() {
        return "Butaca " + fila + columna + " (" + tipo + ")";
    }
}