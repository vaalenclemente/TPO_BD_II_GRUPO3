package com.logistica.modelo;

//fila del ranking de rutas con más incidentes .
public class RutaIncidentes {

    private String origen;
    private String destino;
    private Integer cantidad;

    public RutaIncidentes(String origen, String destino, Integer cantidad) {
        this.origen = origen;
        this.destino = destino;
        this.cantidad = cantidad;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    @Override
    public String toString() {
        return origen + " → " + destino + ": " + cantidad + " incidentes";
    }

}
