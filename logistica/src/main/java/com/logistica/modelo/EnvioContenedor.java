package com.logistica.modelo;

import java.time.LocalDateTime;

public class EnvioContenedor {

    private Integer envioId;
    private Integer contenedorId;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaLiberacion;

    public EnvioContenedor() {

    }

    public EnvioContenedor(Integer envioId, Integer contenedorId) {
        this.envioId = envioId;
        this.contenedorId = contenedorId;
    }

    public Integer getEnvioId() {
        return envioId;
    }

    public void setEnvioId(Integer envioId) {
        this.envioId = envioId;
    }

    public Integer getContenedorId() {
        return contenedorId;
    }

    public void setContenedorId(Integer contenedorId) {
        this.contenedorId = contenedorId;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public LocalDateTime getFechaLiberacion() {
        return fechaLiberacion;
    }

    public void setFechaLiberacion(LocalDateTime fechaLiberacion) {
        this.fechaLiberacion = fechaLiberacion;
    }

    @Override
    public String toString() {
        return "Contenedor " + contenedorId + " | asignado " + Formato.fecha(fechaAsignacion)
                + (fechaLiberacion == null ? " | en el envio" : " | liberado " + Formato.fecha(fechaLiberacion));
    }

}
