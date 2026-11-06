package com.logistica.modelo;

import java.math.BigDecimal;

public class Regla {

    private Integer id;
    private String nombre;
    private String metrica; // temperatura | humedad | vibracion | bateria | inactividad
    private String operador; // mayor | menor
    private BigDecimal valor; // limite
    private String severidad; // alta | media | baja
    private Integer envioId;
    private Boolean activa;

    public Regla() {

    }

    public Regla(String nombre, String metrica, String operador, BigDecimal valor, String severidad,
            Integer envioId) {
        this.nombre = nombre;
        this.metrica = metrica;
        this.operador = operador;
        this.valor = valor;
        this.severidad = severidad;
        this.envioId = envioId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMetrica() {
        return metrica;
    }

    public void setMetrica(String metrica) {
        this.metrica = metrica;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public Integer getEnvioId() {
        return envioId;
    }

    public void setEnvioId(Integer envioId) {
        this.envioId = envioId;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    @Override
    public String toString() {
        String alcance = (envioId == null) ? "general" : "envio " + envioId;
        return id + " - " + nombre + ": " + metrica + " " + operador + " " + valor
                + " (" + severidad + ", " + alcance + ")" + (Boolean.TRUE.equals(activa) ? "" : " [inactiva]");
    }

}
