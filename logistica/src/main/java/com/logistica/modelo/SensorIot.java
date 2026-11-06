package com.logistica.modelo;

import java.time.LocalDate;

public class SensorIot {

    private Integer id;
    private String tipo;
    private String fabricante;
    private LocalDate fechaInstalacion;
    private String estado; // ACTIVO | INACTIVO | FALLA | BAJA
    private Integer contenedorId; // null si no está instalado

    public SensorIot() {

    }

    public SensorIot(String tipo, String fabricante, LocalDate fechaInstalacion, Integer contenedorId) {
        this.tipo = tipo;
        this.fabricante = fabricante;
        this.fechaInstalacion = fechaInstalacion;
        this.contenedorId = contenedorId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public LocalDate getFechaInstalacion() {
        return fechaInstalacion;
    }

    public void setFechaInstalacion(LocalDate fechaInstalacion) {
        this.fechaInstalacion = fechaInstalacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getContenedorId() {
        return contenedorId;
    }

    public void setContenedorId(Integer contenedorId) {
        this.contenedorId = contenedorId;
    }

    @Override
    public String toString() {
        String ubicacion = (contenedorId == null) ? "sin contenedor" : "contenedor " + contenedorId;
        return id + " - " + tipo + " " + fabricante + " - " + estado + " (" + ubicacion + ")";
    }

}
