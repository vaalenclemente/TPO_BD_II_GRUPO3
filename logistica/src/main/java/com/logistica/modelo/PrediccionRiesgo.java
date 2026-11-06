package com.logistica.modelo;

import java.time.LocalDateTime;

public class PrediccionRiesgo {

    private String id;
    private Integer envioId;
    private LocalDateTime fechaCalculo;
    private Double probabilidadDemora; // 0 a 1
    private Double riesgoClimatico; // 0 a 1
    private Double riesgoOperativo; // 0 a 1
    private String nivelRiesgo; // alto | medio | bajo
    private Double demoraActualHs; // factores usados
    private Integer incidentesRuta;
    private Integer anomaliasSensor;

    public PrediccionRiesgo() {

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

    public LocalDateTime getFechaCalculo() {
        return fechaCalculo;
    }

    public void setFechaCalculo(LocalDateTime fechaCalculo) {
        this.fechaCalculo = fechaCalculo;
    }

    public Double getProbabilidadDemora() {
        return probabilidadDemora;
    }

    public void setProbabilidadDemora(Double probabilidadDemora) {
        this.probabilidadDemora = probabilidadDemora;
    }

    public Double getRiesgoClimatico() {
        return riesgoClimatico;
    }

    public void setRiesgoClimatico(Double riesgoClimatico) {
        this.riesgoClimatico = riesgoClimatico;
    }

    public Double getRiesgoOperativo() {
        return riesgoOperativo;
    }

    public void setRiesgoOperativo(Double riesgoOperativo) {
        this.riesgoOperativo = riesgoOperativo;
    }

    public String getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(String nivelRiesgo) {
        this.nivelRiesgo = nivelRiesgo;
    }

    public Double getDemoraActualHs() {
        return demoraActualHs;
    }

    public void setDemoraActualHs(Double demoraActualHs) {
        this.demoraActualHs = demoraActualHs;
    }

    public Integer getIncidentesRuta() {
        return incidentesRuta;
    }

    public void setIncidentesRuta(Integer incidentesRuta) {
        this.incidentesRuta = incidentesRuta;
    }

    public Integer getAnomaliasSensor() {
        return anomaliasSensor;
    }

    public void setAnomaliasSensor(Integer anomaliasSensor) {
        this.anomaliasSensor = anomaliasSensor;
    }

    @Override
    public String toString() {
        return "envio " + envioId + " - " + nivelRiesgo + " (prob. demora " + Math.round(probabilidadDemora * 100) + " %, demora actual " + demoraActualHs + " h, incidentes en la ruta " + incidentesRuta + ", anomalias " + anomaliasSensor + ")";
    }

}
