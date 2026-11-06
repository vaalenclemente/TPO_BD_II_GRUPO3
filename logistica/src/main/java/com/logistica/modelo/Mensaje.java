package com.logistica.modelo;

import java.time.LocalDateTime;

public class Mensaje {

    private String id;
    private String tipoConversacion; // parte | grupo
    private Integer parteId;
    private String parteTipo;
    private String direccion; // saliente (interno → parte) entrante (parte → interno)
    private String grupoId;
    private Integer remitenteId;
    private String contenido;
    private LocalDateTime fechaHora;
    private String tipo;

    public Mensaje() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipoConversacion() {
        return tipoConversacion;
    }

    public void setTipoConversacion(String tipoConversacion) {
        this.tipoConversacion = tipoConversacion;
    }

    public Integer getParteId() {
        return parteId;
    }

    public void setParteId(Integer parteId) {
        this.parteId = parteId;
    }

    public String getParteTipo() {
        return parteTipo;
    }

    public void setParteTipo(String parteTipo) {
        this.parteTipo = parteTipo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(String grupoId) {
        this.grupoId = grupoId;
    }

    public Integer getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(Integer remitenteId) {
        this.remitenteId = remitenteId;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return fechaHora + " [usuario " + remitenteId + (direccion == null ? "" : ", " + direccion) + "] " + contenido;
    }

}
