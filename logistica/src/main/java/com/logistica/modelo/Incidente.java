package com.logistica.modelo;

import java.time.LocalDateTime;
import java.util.List;

public class Incidente {

    private String id;
    private Integer envioId;
    private Integer clienteId;
    private String clienteNombre;
    private String envioOrigen;
    private String envioDestino;
    private Integer tramoId;
    private String tramoMedio;
    private String tramoOrigen;
    private String tramoDestino;
    private String tipo; // temperatura | humedad | vibracion | bateria | demora | danio | robo | aduana
    private String severidad; // alta | media | baja
    private String estado; // abierto | cerrado
    private String descripcion;
    private List<String> alertaIds;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private String resolucion;

    public Incidente() {

    }

    public Incidente(Integer envioId, String tipo, String severidad, String descripcion) {
        this.envioId = envioId;
        this.tipo = tipo;
        this.severidad = severidad;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getEnvioId() {
        return envioId;
    }

    public void setEnvioId(Integer envioId) {
        this.envioId = envioId;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getEnvioOrigen() {
        return envioOrigen;
    }

    public void setEnvioOrigen(String envioOrigen) {
        this.envioOrigen = envioOrigen;
    }

    public String getEnvioDestino() {
        return envioDestino;
    }

    public void setEnvioDestino(String envioDestino) {
        this.envioDestino = envioDestino;
    }

    public Integer getTramoId() {
        return tramoId;
    }

    public void setTramoId(Integer tramoId) {
        this.tramoId = tramoId;
    }

    public String getTramoMedio() {
        return tramoMedio;
    }

    public void setTramoMedio(String tramoMedio) {
        this.tramoMedio = tramoMedio;
    }

    public String getTramoOrigen() {
        return tramoOrigen;
    }

    public void setTramoOrigen(String tramoOrigen) {
        this.tramoOrigen = tramoOrigen;
    }

    public String getTramoDestino() {
        return tramoDestino;
    }

    public void setTramoDestino(String tramoDestino) {
        this.tramoDestino = tramoDestino;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getAlertaIds() {
        return alertaIds;
    }

    public void setAlertaIds(List<String> alertaIds) {
        this.alertaIds = alertaIds;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    @Override
    public String toString() {
        return id + " | envio " + envioId + " (" + clienteNombre + ") | " + tipo + " " + severidad + " | " + estado
                + (tramoId == null ? "" : " | tramo " + tramoOrigen + " -> " + tramoDestino + " (" + tramoMedio + ")")
                + " | abierto " + Formato.fecha(fechaApertura)
                + (fechaCierre == null ? "" : " | cerrado " + Formato.fecha(fechaCierre));
    }

}
