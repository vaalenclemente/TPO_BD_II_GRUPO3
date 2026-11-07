package com.logistica.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pago {

    private String id;
    private String tipo;
    private Integer facturaId;
    private Integer parteId;
    private BigDecimal monto;
    private String medioPago; // transferencia | efectivo | cheque | tarjeta
    private LocalDateTime fecha;
    private String estado; // pendiente | confirmado | rechazado | anulado
    private Integer movimientoId;
    private List<CambioEstado> historialEstados = new ArrayList<>();

    public Pago() {

    }

    public Pago(Integer facturaId, String medioPago) {
        this.facturaId = facturaId;
        this.medioPago = medioPago;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getFacturaId() {
        return facturaId;
    }

    public void setFacturaId(Integer facturaId) {
        this.facturaId = facturaId;
    }

    public Integer getParteId() {
        return parteId;
    }

    public void setParteId(Integer parteId) {
        this.parteId = parteId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMedioPago() {
        return medioPago;
    }

    public void setMedioPago(String medioPago) {
        this.medioPago = medioPago;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getMovimientoId() {
        return movimientoId;
    }

    public void setMovimientoId(Integer movimientoId) {
        this.movimientoId = movimientoId;
    }

    public List<CambioEstado> getHistorialEstados() {
        return historialEstados;
    }

    public void setHistorialEstados(List<CambioEstado> historialEstados) {
        this.historialEstados = historialEstados;
    }

    // un cambio de estado del pago: que estado, cuando y quien.
    public static class CambioEstado {

        private String estado;
        private LocalDateTime fecha;
        private Integer usuarioId;

        public CambioEstado(String estado, LocalDateTime fecha, Integer usuarioId) {
            this.estado = estado;
            this.fecha = fecha;
            this.usuarioId = usuarioId;
        }

        public String getEstado() {
            return estado;
        }

        public LocalDateTime getFecha() {
            return fecha;
        }

        public Integer getUsuarioId() {
            return usuarioId;
        }

        @Override
        public String toString() {
            return Formato.fecha(fecha) + " " + estado + (usuarioId == null ? "" : " (usuario " + usuarioId + ")");
        }
    }

    @Override
    public String toString() {
        return id + " | " + tipo + " | factura " + facturaId + " | " + Formato.dinero(monto) + " | " + medioPago
                + " | " + Formato.fecha(fecha) + " | " + estado;
    }

}
