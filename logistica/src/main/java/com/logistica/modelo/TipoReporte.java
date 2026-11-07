package com.logistica.modelo;

import java.math.BigDecimal;

public class TipoReporte {

    private Integer id;
    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private Boolean activo;

    public TipoReporte() {

    }

    public TipoReporte(String codigo, String nombre, BigDecimal precio) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return id + " | " + codigo + " | " + nombre + " | " + Formato.dinero(precio)
                + (Boolean.TRUE.equals(activo) ? "" : " | inactivo");
    }

}
