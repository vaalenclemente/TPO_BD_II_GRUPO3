package com.logistica.modelo;

import java.time.LocalDateTime;

public class Usuario {

    private Integer id;
    private String nombre;
    private String apellido;
    private String email;
    private String passwordHash;
    private String estado;
    private LocalDateTime fechaRegistro;
    private Integer rolId;

    public Usuario() {

    }

    public Usuario(String nombre, String apellido, String email, String passwordHash, Integer rolId) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rolId = rolId;
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getRolId() {
        return rolId;
    }

    public void setRolId(Integer rolId) {
        this.rolId = rolId;
    }

    @Override
    public String toString() {
        return id + " | " + nombre + " " + apellido + " | " + email + ("ACTIVO".equals(estado) ? "" : " | " + estado);
    }

}
