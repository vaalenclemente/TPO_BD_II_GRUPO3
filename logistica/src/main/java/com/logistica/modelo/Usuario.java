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
    private String tipoUsuario;
    private Integer parteId; // viene de usuario_externo, null para los internos

    public Usuario() {

    }

    public Usuario(String nombre, String apellido, String email, String passwordHash, Integer rolId, String tipoUsuario,
            Integer parteId) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rolId = rolId;
        this.tipoUsuario = tipoUsuario;
        this.parteId = parteId;
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

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public Integer getParteId() {
        return parteId;
    }

    public void setParteId(Integer parteId) {
        this.parteId = parteId;
    }

    @Override
    public String toString() {
        return id + " - " + nombre + " " + apellido + " (" + email + ")";
    }

}
