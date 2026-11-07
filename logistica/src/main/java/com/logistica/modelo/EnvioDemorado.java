package com.logistica.modelo;

import java.time.LocalDateTime;

public class EnvioDemorado {

    private Integer envioId;
    private String cliente;
    private String destino;
    private String estado;
    private LocalDateTime fechaEntregaEstimada;
    private LocalDateTime fechaEntregaReal;
    private Long horasDemora;

    public EnvioDemorado() {

    }

    public Integer getEnvioId() {
        return envioId;
    }

    public void setEnvioId(Integer envioId) {
        this.envioId = envioId;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public Long getHorasDemora() {
        return horasDemora;
    }

    public void setHorasDemora(Long horasDemora) {
        this.horasDemora = horasDemora;
    }

    @Override
    public String toString() {
        return "Envio " + envioId + " | " + cliente + " -> " + destino + " | " + estado + " | estimada "
                + Formato.fecha(fechaEntregaEstimada)
                + (fechaEntregaReal == null ? " | sin entregar" : " | entregado " + Formato.fecha(fechaEntregaReal))
                + " | " + horasDemora + " h de demora";
    }

}
