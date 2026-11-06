package com.logistica.modelo;

import java.math.BigDecimal;

public class Vehiculo {

    private Integer id;
    private String tipo; // barco | avion | tren | camion
    private String identificacion;
    private BigDecimal capacidadKg;
    private String estado; // activo | inactivo | en_mantenimiento
    private Integer operadorId; // parte de tipo operador

    public Vehiculo() {

    }

    public Vehiculo(String tipo, String identificacion, BigDecimal capacidadKg, Integer operadorId) {
        this.tipo = tipo;
        this.identificacion = identificacion;
        this.capacidadKg = capacidadKg;
        this.operadorId = operadorId;
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

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
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

    public Integer getOperadorId() {
        return operadorId;
    }

    public void setOperadorId(Integer operadorId) {
        this.operadorId = operadorId;
    }

    @Override
    public String toString() {
        return id + " - " + tipo + " " + identificacion + " (" + capacidadKg + " kg, operador "
                + operadorId + ") - " + estado;
    }

}
