package Models;

import java.time.LocalDate;

public class Espectaculo {
    private int idEspectaculo;
    private String nombre;
    private LocalDate fecha;
    private double precioBase;
    private double precioVip;

    // Constructor completo
    public Espectaculo(int idEspectaculo, String nombre, LocalDate fecha, double precioBase, double precioVip) {
        this.idEspectaculo = idEspectaculo;
        this.nombre = nombre;
        this.fecha = fecha;
        this.precioBase = precioBase;
        this.precioVip = precioVip;
    }

    // Constructor sin ID (para inserciones)
    public Espectaculo(String nombre, LocalDate fecha, double precioBase, double precioVip) {
        this.nombre = nombre;
        this.fecha = fecha;
        this.precioBase = precioBase;
        this.precioVip = precioVip;
    }

    // Getters y Setters
    public int getIdEspectaculo() {
        return idEspectaculo;
    }

    public void setIdEspectaculo(int idEspectaculo) {
        this.idEspectaculo = idEspectaculo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(double precioBase) {
        this.precioBase = precioBase;
    }

    public double getPrecioVip() {
        return precioVip;
    }

    public void setPrecioVip(double precioVip) {
        this.precioVip = precioVip;
    }

    @Override
    public String toString() {
        return nombre + " (" + fecha + ")";
    }
}