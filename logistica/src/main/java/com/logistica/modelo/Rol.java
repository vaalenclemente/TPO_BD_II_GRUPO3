package com.logistica.modelo;

import java.util.ArrayList;
import java.util.List;

public class Rol {

    private Integer id;
    private String descripcion;
    // codigos de los permisos del rol (solo lectura: se cargan desde rol_permiso)
    private List<String> permisos = new ArrayList<>();

    public Rol() {

    }

    public Rol(Integer id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
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

    public List<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<String> permisos) {
        this.permisos = permisos;
    }

    @Override
    public String toString() {
        String lista = permisos.isEmpty() ? "sin permisos" : String.join(", ", permisos);
        return id + " - " + descripcion + " [" + lista + "]";
    }

}
