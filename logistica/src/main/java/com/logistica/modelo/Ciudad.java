package com.logistica.modelo;

public class Ciudad {

    private Integer id;
    private String nombre;
    private Integer paisId;

    public Ciudad() {

    }

    public Ciudad(Integer id, String nombre, Integer paisId) {
        this.id = id;
        this.nombre = nombre;
        this.paisId = paisId;
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

    public Integer getPaisId() {
        return paisId;
    }

    public void setPaisId(Integer paisId) {
        this.paisId = paisId;
    }

    public String toString() {
        return id + " - " + nombre;
    }

}
