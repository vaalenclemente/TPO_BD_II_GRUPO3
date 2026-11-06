package com.logistica.modelo;

public class Rol {

    private Integer id;
    private String descripcion;
    private Integer nivelPermisos;

    public Rol() {

    }

    public Rol(Integer id, String descripcion, Integer nivelPermisos) {
        this.id = id;
        this.descripcion = descripcion;
        this.nivelPermisos = nivelPermisos;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getNivelPermisos() {
        return nivelPermisos;
    }

    public void setNivelPermisos(Integer nivelPermisos) {
        this.nivelPermisos = nivelPermisos;
    }

    @Override
    public String toString() {
        return id + " - " + descripcion;
    }

}
