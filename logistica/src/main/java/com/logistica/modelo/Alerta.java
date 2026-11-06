package com.logistica.modelo;

import java.time.LocalDateTime;

public class Alerta {

    private String id;
    private Integer sensorId;
    private Integer envioId;
    private Integer reglaId;
    private LocalDateTime fechaLectura;
    private String tipo; // temp_fuera_de_rango | humedad | vibracion | bateria_baja | sensor_inactivo
    private Double valor;
    private Double umbral; // limite de la regla
    private String severidad; // alta | media | baja
    private LocalDateTime fecha;
    private String estado; // activa | reconocida | cerrada
    private String incidenteId;

    public Alerta() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public void setSensorId(Integer sensorId) {
        this.sensorId = sensorId;
    }

    public Integer getEnvioId() {
        return envioId;
    }

    public void setEnvioId(Integer envioId) {
        this.envioId = envioId;
    }

    public Integer getReglaId() {
        return reglaId;
    }

    public void setReglaId(Integer reglaId) {
        this.reglaId = reglaId;
    }

    public LocalDateTime getFechaLectura() {
        return fechaLectura;
    }

    public void setFechaLectura(LocalDateTime fechaLectura) {
        this.fechaLectura = fechaLectura;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Double getUmbral() {
        return umbral;
    }

    public void setUmbral(Double umbral) {
        this.umbral = umbral;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
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

    public String getIncidenteId() {
        return incidenteId;
    }

    public void setIncidenteId(String incidenteId) {
        this.incidenteId = incidenteId;
    }

    @Override
    public String toString() {
        return id + " - " + tipo + " (" + severidad + ") sensor " + sensorId
                + (envioId == null ? "" : ", envio " + envioId) + ": " + valor + " / limite " + umbral + " - " + estado;
    }

}
