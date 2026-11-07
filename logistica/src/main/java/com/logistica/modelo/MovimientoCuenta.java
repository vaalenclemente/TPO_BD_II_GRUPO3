package com.logistica.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoCuenta {

    private Integer id;
    private Integer cuentaId;
    private LocalDateTime fecha;
    private String tipo; // debito (suma al saldo), credito (resta)
    private BigDecimal importe;
    private String concepto;
    private Integer facturaId;
    private String pagoId;

    public MovimientoCuenta() {

    }

    public MovimientoCuenta(Integer cuentaId, String tipo, BigDecimal importe, String concepto, Integer facturaId,
            String pagoId) {
        this.cuentaId = cuentaId;
        this.tipo = tipo;
        this.importe = importe;
        this.concepto = concepto;
        this.facturaId = facturaId;
        this.pagoId = pagoId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Integer cuentaId) {
        this.cuentaId = cuentaId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Integer getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(Integer facturaId) {
        this.facturaId = facturaId;
    }

    public String getPagoId() {
        return pagoId;
    }

    public void setPagoId(String pagoId) {
        this.pagoId = pagoId;
    }

    @Override
    public String toString() {
        return Formato.fecha(fecha) + " | " + tipo + " | " + Formato.dinero(importe) + " | " + concepto;
    }

}
