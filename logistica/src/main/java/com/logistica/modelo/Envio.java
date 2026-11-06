package com.logistica.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Envio {

    private Integer id;
    private Integer clienteId;
    private Integer origenCiudadId;
    private Integer destinoCiudadId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaEntregaEstimada;
    private LocalDateTime fechaEntregaReal;
    private String estado; // creado | en_preparacion | en_transito | en_aduana | entregado | cancelado
    private String prioridad; // alta | media | baja
    private BigDecimal pesoKg;
    private BigDecimal volumenM3;
    private String descripcionCarga;
    private Integer cantidadIncidentes;

    public Envio() {

    }

    public Envio(Integer clienteId, Integer origenCiudadId, Integer destinoCiudadId, LocalDateTime fechaEntregaEstimada,
            String prioridad, BigDecimal pesoKg, BigDecimal volumenM3, String descripcionCarga) {
        this.clienteId = clienteId;
        this.origenCiudadId = origenCiudadId;
        this.destinoCiudadId = destinoCiudadId;
        this.fechaEntregaEstimada = fechaEntregaEstimada;
        this.prioridad = prioridad;
        this.pesoKg = pesoKg;
        this.volumenM3 = volumenM3;
        this.descripcionCarga = descripcionCarga;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDateTime getFechaEntregaEstimada() {
        return fechaEntregaEstimada;
    }

    public void setFechaEntregaEstimada(LocalDateTime fechaEntregaEstimada) {
        this.fechaEntregaEstimada = fechaEntregaEstimada;
    }

    public LocalDateTime getFechaEntregaReal() {
        return fechaEntregaReal;
    }

    public void setFechaEntregaReal(LocalDateTime fechaEntregaReal) {
        this.fechaEntregaReal = fechaEntregaReal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public BigDecimal getVolumenM3() {
        return volumenM3;
    }

    public void setVolumenM3(BigDecimal volumenM3) {
        this.volumenM3 = volumenM3;
    }

    public String getDescripcionCarga() {
        return descripcionCarga;
    }

    public void setDescripcionCarga(String descripcionCarga) {
        this.descripcionCarga = descripcionCarga;
    }

    public Integer getCantidadIncidentes() {
        return cantidadIncidentes;
    }

    public void setCantidadIncidentes(Integer cantidadIncidentes) {
        this.cantidadIncidentes = cantidadIncidentes;
    }

    @Override
    public String toString() {
        return id + " - cliente " + clienteId + " - " + estado + " (" + prioridad + ", " + pesoKg
                + " kg) - entrega estimada " + fechaEntregaEstimada;
    }

}
