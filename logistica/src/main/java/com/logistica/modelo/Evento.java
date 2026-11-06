package com.logistica.modelo;

import java.time.LocalDateTime;
import java.util.List;

public class Evento {

    private String id;
    private Integer envioId;
    private List<Integer> contenedorIds;
    private String tipo; // CREADO | EN_PREPARACION | SALIDA_TRAMO | LLEGADA_TRAMO | EN_ADUANA |
                         // EN_TRANSITO | ENTREGADO | CANCELADO | INCIDENTE | OTRO
    private LocalDateTime fechaHora;
    private Integer ciudadId;
    private String ciudad;
    private String pais;
    private String descripcion;
    private Integer usuarioId;

    public Evento() {

    }

    public Evento(Integer envioId, String tipo, String descripcion) {
        this.envioId = envioId;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getEnvioId() {
        return envioId;
    }

    public void setEnvioId(Integer envioId) {
        this.envioId = envioId;
    }

    public List<Integer> getContenedorIds() {
        return contenedorIds;
    }

    public void setContenedorIds(List<Integer> contenedorIds) {
        this.contenedorIds = contenedorIds;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getCiudadId() {
        return ciudadId;
    }

    public void setCiudadId(Integer ciudadId) {
        this.ciudadId = ciudadId;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() {
        return fechaHora + " - " + tipo + (ciudad == null ? "" : " en " + ciudad + ", " + pais) + " - " + descripcion;
    }

}
