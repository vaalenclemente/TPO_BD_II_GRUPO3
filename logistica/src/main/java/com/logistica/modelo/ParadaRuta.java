package com.logistica.modelo;

import java.time.LocalDateTime;

public class ParadaRuta {

    private Integer envioId;
    private Integer tramoId;
    private Integer orden;
    private String ciudad;
    private String pais;
    private String medio;
    private String operador;
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaLlegada;
    private String estado;

    public ParadaRuta() {

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

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getMedio() {
        return medio;
    }

    public void setMedio(String medio) {
        this.medio = medio;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDateTime getFechaLlegada() {
        return fechaLlegada;
    }

    public void setFechaLlegada(LocalDateTime fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        if (tramoId == null) {
            return "Envio " + envioId + " | origen | " + ciudad + ", " + pais;
        }
        return "Envio " + envioId + " | " + orden + ". " + ciudad + ", " + pais + " | " + medio + " (" + operador
                + ") | salida " + Formato.fecha(fechaSalida) + " | llegada " + Formato.fecha(fechaLlegada) + " | " + estado;
    }

}
