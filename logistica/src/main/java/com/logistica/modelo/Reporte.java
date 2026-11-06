package com.logistica.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reporte {

    private String id;
    private Integer parteId; // cliente que lo pide (a quien se le factura)
    private String nombreSolicitante;
    private Integer tipoReporteId;
    private String codigo;
    private LocalDate desde;
    private LocalDate hasta;
    private String estado; // solicitado | generado | error
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaGeneracion;
    private String contenido;
    private BigDecimal costo;
    private Integer facturaId;
    private String error;

    public Reporte() {

    }

    public Reporte(Integer parteId, String nombreSolicitante, Integer tipoReporteId, LocalDate desde, LocalDate hasta) {
        this.parteId = parteId;
        this.nombreSolicitante = nombreSolicitante;
        this.tipoReporteId = tipoReporteId;
        this.desde = desde;
        this.hasta = hasta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getParteId() {
        return parteId;
    }

    public void setParteId(Integer parteId) {
        this.parteId = parteId;
    }

    public String getNombreSolicitante() {
        return nombreSolicitante;
    }

    public void setNombreSolicitante(String nombreSolicitante) {
        this.nombreSolicitante = nombreSolicitante;
    }

    public Integer getTipoReporteId() {
        return tipoReporteId;
    }

    public void setTipoReporteId(Integer tipoReporteId) {
        this.tipoReporteId = tipoReporteId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public Integer getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(Integer facturaId) {
        this.facturaId = facturaId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return id + " - " + codigo + " (" + desde + " a " + hasta + ") - " + estado + " - $" + costo
                + (facturaId == null ? "" : " - factura " + facturaId);
    }

}
