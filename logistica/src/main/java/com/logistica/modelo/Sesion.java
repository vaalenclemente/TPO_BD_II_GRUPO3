package com.logistica.modelo;

import java.time.LocalDateTime;

public class Sesion {

    private Integer id;
    private Integer usuarioId;
    private String token;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaUltimaActividad;
    private LocalDateTime fechaFin; // null mientras la sesion esta activa
    private String estado;          // activa | cerrada | expirada

    public Sesion() {

    }

    public Sesion(Integer usuarioId, String token) {
        this.usuarioId = usuarioId;
        this.token = token;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaUltimaActividad() {
        return fechaUltimaActividad;
    }

    public void setFechaUltimaActividad(LocalDateTime fechaUltimaActividad) {
        this.fechaUltimaActividad = fechaUltimaActividad;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return id + " - usuario " + usuarioId + " - " + estado + " (inicio " + fechaInicio + ")";
    }

}
