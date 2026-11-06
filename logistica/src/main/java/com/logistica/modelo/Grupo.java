package com.logistica.modelo;

import java.time.LocalDateTime;
import java.util.List;

public class Grupo {

    private String id;
    private String nombre;
    private String descripcion;
    private List<Integer> integrantes;
    private Integer creadoPor;
    private LocalDateTime fechaCreacion;
    private String estado; // ACTIVO | INACTIVO

    public Grupo() {

    }

    public Grupo(String nombre, String descripcion, List<Integer> integrantes, Integer creadoPor) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.integrantes = integrantes;
        this.creadoPor = creadoPor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Integer> getIntegrantes() {
        return integrantes;
    }

    public void setIntegrantes(List<Integer> integrantes) {
        this.integrantes = integrantes;
    }

    public Integer getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(Integer creadoPor) {
        this.creadoPor = creadoPor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return id + " - " + nombre + " " + integrantes + ("ACTIVO".equals(estado) ? "" : " [" + estado + "]");
    }

}
