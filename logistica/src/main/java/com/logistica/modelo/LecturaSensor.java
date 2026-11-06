package com.logistica.modelo;

import java.time.LocalDateTime;

public class LecturaSensor {

    private String id; // _id de mongo
    private LocalDateTime fechaHora;
    private Integer sensorId;
    private String sensorTipo;
    private String sensorFabricante;
    private Integer contenedorId;
    private Integer envioId;
    private Double temperatura;
    private Double humedad;
    private Double vibracion;
    private Double nivelBateria;
    private Double latitud;
    private Double longitud;
    private Integer anomaliaReglaId;
    private String anomaliaMetrica;
    private Double anomaliaLimite;
    private Double anomaliaExceso;

    public LecturaSensor() {

    }

    public LecturaSensor(Integer sensorId, Double temperatura, Double humedad, Double vibracion, Double nivelBateria,
            Double latitud, Double longitud) {
        this.sensorId = sensorId;
        this.temperatura = temperatura;
        this.humedad = humedad;
        this.vibracion = vibracion;
        this.nivelBateria = nivelBateria;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public void setSensorId(Integer sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorTipo() {
        return sensorTipo;
    }

    public void setSensorTipo(String sensorTipo) {
        this.sensorTipo = sensorTipo;
    }

    public String getSensorFabricante() {
        return sensorFabricante;
    }

    public void setSensorFabricante(String sensorFabricante) {
        this.sensorFabricante = sensorFabricante;
    }

    public Integer getContenedorId() {
        return contenedorId;
    }

    public void setContenedorId(Integer contenedorId) {
        this.contenedorId = contenedorId;
    }

    public Integer getEnvioId() {
        return envioId;
    }

    public void setEnvioId(Integer envioId) {
        this.envioId = envioId;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Double getHumedad() {
        return humedad;
    }

    public void setHumedad(Double humedad) {
        this.humedad = humedad;
    }

    public Double getVibracion() {
        return vibracion;
    }

    public void setVibracion(Double vibracion) {
        this.vibracion = vibracion;
    }

    public Double getNivelBateria() {
        return nivelBateria;
    }

    public void setNivelBateria(Double nivelBateria) {
        this.nivelBateria = nivelBateria;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Integer getAnomaliaReglaId() {
        return anomaliaReglaId;
    }

    public void setAnomaliaReglaId(Integer anomaliaReglaId) {
        this.anomaliaReglaId = anomaliaReglaId;
    }

    public String getAnomaliaMetrica() {
        return anomaliaMetrica;
    }

    public void setAnomaliaMetrica(String anomaliaMetrica) {
        this.anomaliaMetrica = anomaliaMetrica;
    }

    public Double getAnomaliaLimite() {
        return anomaliaLimite;
    }

    public void setAnomaliaLimite(Double anomaliaLimite) {
        this.anomaliaLimite = anomaliaLimite;
    }

    public Double getAnomaliaExceso() {
        return anomaliaExceso;
    }

    public void setAnomaliaExceso(Double anomaliaExceso) {
        this.anomaliaExceso = anomaliaExceso;
    }

    @Override
    public String toString() {
        return fechaHora + " - sensor " + sensorId + " | T " + temperatura + " °C, H " + humedad + " %, V " + vibracion
                + ", bat " + nivelBateria + " %"
                + (anomaliaReglaId == null ? "" : " [ANOMALIA " + anomaliaMetrica + " limite " + anomaliaLimite + "]");
    }

}
