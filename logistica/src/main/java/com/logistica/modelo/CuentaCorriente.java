package com.logistica.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CuentaCorriente {

    private Integer id;
    private Integer parteId;
    private BigDecimal saldo;
    private LocalDateTime fechaActualizacion;

    public CuentaCorriente() {

    }

    public CuentaCorriente(Integer parteId) {
        this.parteId = parteId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParteId() {
        return parteId;
    }

    public void setParteId(Integer parteId) {
        this.parteId = parteId;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return "Cuenta " + id + " | parte " + parteId + " | saldo " + Formato.dinero(saldo);
    }

}
