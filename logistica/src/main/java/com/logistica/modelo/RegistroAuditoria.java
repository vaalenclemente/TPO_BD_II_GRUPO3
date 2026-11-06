package com.logistica.modelo;

import java.time.LocalDateTime;

public class RegistroAuditoria {

    private String id;
    private LocalDateTime fecha;
    private Integer usuarioId;
    private String accion; // crear_envio, confirmar_pago, login
    private String entidad;
    private String entidadId;
    private String campo;
    private String antes;
    private String despues;
    private String resultado; // ok | compensada | error
    private String sagaId;

    public RegistroAuditoria() {

    }

    public RegistroAuditoria(Integer usuarioId, String accion, String entidad, String entidadId, String resultado) {
        this.usuarioId = usuarioId;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.resultado = resultado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(String entidadId) {
        this.entidadId = entidadId;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getAntes() {
        return antes;
    }

    public void setAntes(String antes) {
        this.antes = antes;
    }

    public String getDespues() {
        return despues;
    }

    public void setDespues(String despues) {
        this.despues = despues;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    @Override
    public String toString() {
        return fecha + " - usuario " + usuarioId + " - " + accion + " " + entidad + " " + entidadId
                + (campo == null ? "" : " (" + campo + ": " + antes + " → " + despues + ")") + " - " + resultado
                + (sagaId == null ? "" : " [saga " + sagaId + "]");
    }

}
