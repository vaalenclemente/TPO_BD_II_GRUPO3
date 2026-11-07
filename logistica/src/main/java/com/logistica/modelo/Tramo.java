package com.logistica.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Tramo {

    private Integer id;
    private Integer envioId;
    private Integer orden;
    private String medioTransporte; // barco | avion | tren | camion
    private Integer origenCiudadId;
    private Integer destinoCiudadId;
    private Integer vehiculoId;
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaLlegadaEstimada;
    private LocalDateTime fechaLlegadaReal;
    private BigDecimal costo;
    private String estado; // pendiente | en_curso | completado | cancelado

    public Tramo() {

    }

    public Tramo(Integer envioId, String medioTransporte, Integer origenCiudadId, Integer destinoCiudadId,
            Integer vehiculoId, LocalDateTime fechaSalida, LocalDateTime fechaLlegadaEstimada, BigDecimal costo) {
        this.envioId = envioId;
        this.medioTransporte = medioTransporte;
        this.origenCiudadId = origenCiudadId;
        this.destinoCiudadId = destinoCiudadId;
        this.vehiculoId = vehiculoId;
        this.fechaSalida = fechaSalida;
        this.fechaLlegadaEstimada = fechaLlegadaEstimada;
        this.costo = costo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEnvioId() {
        return envioId;
    }

    public void setEnvioId(Integer envioId) {
        this.envioId = envioId;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getMedioTransporte() {
        return medioTransporte;
    }

    public void setMedioTransporte(String medioTransporte) {
        this.medioTransporte = medioTransporte;
    }

    public Integer getOrigenCiudadId() {
        return origenCiudadId;
    }

    public void setOrigenCiudadId(Integer origenCiudadId) {
        this.origenCiudadId = origenCiudadId;
    }

    public Integer getDestinoCiudadId() {
        return destinoCiudadId;
    }

    public void setDestinoCiudadId(Integer destinoCiudadId) {
        this.destinoCiudadId = destinoCiudadId;
    }

    public Integer getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Integer vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDateTime getFechaLlegadaEstimada() {
        return fechaLlegadaEstimada;
    }

    public void setFechaLlegadaEstimada(LocalDateTime fechaLlegadaEstimada) {
        this.fechaLlegadaEstimada = fechaLlegadaEstimada;
    }

    public LocalDateTime getFechaLlegadaReal() {
        return fechaLlegadaReal;
    }

    public void setFechaLlegadaReal(LocalDateTime fechaLlegadaReal) {
        this.fechaLlegadaReal = fechaLlegadaReal;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Tramo " + orden + " (id " + id + ") | " + medioTransporte + " | ciudad " + origenCiudadId + " -> ciudad "
                + destinoCiudadId + " | vehiculo " + vehiculoId + " | salida " + Formato.fecha(fechaSalida)
                + " | llegada estimada " + Formato.fecha(fechaLlegadaEstimada) + " | llegada real "
                + Formato.fecha(fechaLlegadaReal) + " | " + Formato.dinero(costo) + " | " + estado;
    }

}
