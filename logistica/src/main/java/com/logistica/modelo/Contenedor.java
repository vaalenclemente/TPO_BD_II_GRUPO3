package com.logistica.modelo;

import java.math.BigDecimal;

public class Contenedor {

    private Integer id;
    private String codigoInternacional;
    private String tipo;
    private BigDecimal capacidadKg;
    private String estado; // disponible | en_uso | baja

    public Contenedor() {

    }

    public Contenedor(String codigoInternacional, String tipo, BigDecimal capacidadKg) {
        this.codigoInternacional = codigoInternacional;
        this.tipo = tipo;
        this.capacidadKg = capacidadKg;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigoInternacional() {
        return codigoInternacional;
    }

    public void setCodigoInternacional(String codigoInternacional) {
        this.codigoInternacional = codigoInternacional;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getCapacidadKg() {
        return capacidadKg;
    }

    public void setCapacidadKg(BigDecimal capacidadKg) {
        this.capacidadKg = capacidadKg;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return id + " - " + codigoInternacional + " (" + tipo + ", " + capacidadKg + " kg) - " + estado;
    }

}
