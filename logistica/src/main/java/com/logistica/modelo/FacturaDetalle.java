package com.logistica.modelo;

import java.math.BigDecimal;

public class FacturaDetalle {

    private Integer id;
    private Integer facturaId;
    private String concepto;
    private BigDecimal importe;
    private Integer envioId;
    private Integer tramoId;
    private String reporteId;

    public FacturaDetalle() {

    }

    public FacturaDetalle(String concepto, BigDecimal importe) {
        this.concepto = concepto;
        this.importe = importe;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(Integer facturaId) {
        this.facturaId = facturaId;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public Integer getEnvioId() {
        return envioId;
    }

    public void setEnvioId(Integer envioId) {
        this.envioId = envioId;
    }

    public Integer getTramoId() {
        return tramoId;
    }

    public void setTramoId(Integer tramoId) {
        this.tramoId = tramoId;
    }

    public String getReporteId() {
        return reporteId;
    }

    public void setReporteId(String reporteId) {
        this.reporteId = reporteId;
    }

    @Override
    public String toString() {
        return concepto + " | " + Formato.dinero(importe);
    }

}
