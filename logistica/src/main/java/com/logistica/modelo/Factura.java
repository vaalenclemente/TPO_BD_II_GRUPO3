package com.logistica.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Factura {

    private Integer id;
    private String numero;
    private Integer parteId;
    private String tipo;
    private String periodo;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private BigDecimal importeTotal;
    private String estado; // PENDIENTE | PAGADA | ANULADA

    public Factura() {

    }

    public Factura(Integer parteId, String tipo, String periodo) {
        this.parteId = parteId;
        this.tipo = tipo;
        this.periodo = periodo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getParteId() {
        return parteId;
    }

    public void setParteId(Integer parteId) {
        this.parteId = parteId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(BigDecimal importeTotal) {
        this.importeTotal = importeTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return id + " - " + numero + " (" + tipo + ", parte " + parteId + ", " + periodo + ") $" + importeTotal + " - "
                + estado;
    }

}
