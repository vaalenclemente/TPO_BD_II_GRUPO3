package com.logistica.modelo;

import java.math.BigDecimal;

public class ResultadoAgrupado {

    private String etiqueta;
    private Integer cantidad;
    private BigDecimal total;
    private BigDecimal promedio;
    private BigDecimal minimo;
    private BigDecimal maximo;

    public ResultadoAgrupado() {

    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPromedio() {
        return promedio;
    }

    public void setPromedio(BigDecimal promedio) {
        this.promedio = promedio;
    }

    public BigDecimal getMinimo() {
        return minimo;
    }

    public void setMinimo(BigDecimal minimo) {
        this.minimo = minimo;
    }

    public BigDecimal getMaximo() {
        return maximo;
    }

    public void setMaximo(BigDecimal maximo) {
        this.maximo = maximo;
    }

    @Override
    public String toString() {
        return etiqueta + ": " + cantidad + (total == null ? "" : " | total " + total) + (promedio == null ? "" : " | promedio " + promedio) + (minimo == null ? "" : " | mín " + minimo) + (maximo == null ? "" : " | máx " + maximo);
    }

}
