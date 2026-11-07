package com.logistica.modelo;

import java.time.LocalDateTime;

public class Parte {

    private Integer id;
    private String tipo;
    private String razonSocial;
    private String cuit;
    private String email;
    private String telefono;
    private String direccion;
    private Integer paisId;
    private String rubro;
    private String estado;
    private LocalDateTime fechaAlta;

    public Parte() {
    }

    public Parte(String tipo, String razonSocial, String cuit, String email,
            String telefono, String direccion, Integer paisId, String rubro) {
        this.tipo = tipo;
        this.razonSocial = razonSocial;
        this.cuit = cuit;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.paisId = paisId;
        this.rubro = rubro;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getPaisId() {
        return paisId;
    }

    public void setPaisId(Integer paisId) {
        this.paisId = paisId;
    }

    public String getRubro() {
        return rubro;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDateTime fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    @Override
    public String toString() {
        return id + " | " + razonSocial + " | " + tipo + " | CUIT " + cuit + " | " + email
                + (rubro == null ? "" : " | rubro " + rubro) + " | " + estado;
    }

}
