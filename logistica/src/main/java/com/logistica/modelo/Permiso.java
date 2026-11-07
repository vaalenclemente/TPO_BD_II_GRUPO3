package com.logistica.modelo;

public class Permiso {

    public static final String[][] CATALOGO = {
            { "PARTES", "Clientes / proveedores / operadores" },
            { "USUARIOS", "Usuarios, roles, permisos y sesiones" },
            { "ACTIVOS", "Contenedores, sensores y vehiculos" },
            { "ENVIOS", "Envios y tramos" },
            { "MONITOREO", "Monitoreo y alertas" },
            { "INCIDENTES", "Reglas e incidentes" },
            { "COMERCIAL", "Facturas, cobros, pagos y cuentas" },
            { "REPORTES", "Reportes facturables" },
            { "ANALISIS", "Analisis y prediccion" },
            { "RED", "Red y recorridos (Neo4j)" },
            { "MENSAJERIA", "Mensajeria" },
            { "AUDITORIA", "Auditoria" },
            { "UBICACIONES", "Paises y ciudades" }
    };

    private Integer id;
    private String codigo;
    private String descripcion;

    public Permiso() {

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return id + " - " + codigo + ": " + descripcion;
    }

}
