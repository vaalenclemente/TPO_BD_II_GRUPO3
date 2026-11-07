package com.logistica.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.logistica.modelo.Permiso;

public class MySQLInicializacion {

    private static final String[] TABLAS = {

        """
        CREATE TABLE IF NOT EXISTS pais (
            id INT AUTO_INCREMENT PRIMARY KEY,
            nombre VARCHAR(100) UNIQUE
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS ciudad (
            id INT AUTO_INCREMENT PRIMARY KEY,
            nombre VARCHAR(100),
            pais_id INT,
            CONSTRAINT fk_ciudad_pais FOREIGN KEY (pais_id) REFERENCES pais(id),
            UNIQUE (nombre, pais_id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS parte (
            id INT AUTO_INCREMENT PRIMARY KEY,
            tipo VARCHAR(20),
            razon_social VARCHAR(150),
            cuit VARCHAR(20) UNIQUE,
            email VARCHAR(150),
            telefono VARCHAR(30),
            direccion VARCHAR(200),
            pais_id INT,
            rubro VARCHAR(100),
            estado VARCHAR(20),
            fecha_alta DATETIME,
            CONSTRAINT fk_parte_pais FOREIGN KEY (pais_id) REFERENCES pais(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS rol (
            id INT AUTO_INCREMENT PRIMARY KEY,
            descripcion VARCHAR(100) UNIQUE
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS permiso (
            id INT AUTO_INCREMENT PRIMARY KEY,
            codigo VARCHAR(30) UNIQUE,
            descripcion VARCHAR(150)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS rol_permiso (
            rol_id INT,
            permiso_id INT,
            PRIMARY KEY (rol_id, permiso_id),
            CONSTRAINT fk_rol_permiso_rol FOREIGN KEY (rol_id) REFERENCES rol(id),
            CONSTRAINT fk_rol_permiso_permiso FOREIGN KEY (permiso_id) REFERENCES permiso(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS usuario (
            id INT AUTO_INCREMENT PRIMARY KEY,
            nombre VARCHAR(100),
            apellido VARCHAR(100),
            email VARCHAR(150) UNIQUE,
            password_hash VARCHAR(255),
            estado VARCHAR(20),
            fecha_registro DATETIME,
            rol_id INT,
            CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS sesion (
            id INT AUTO_INCREMENT PRIMARY KEY,
            usuario_id INT,
            token VARCHAR(64) UNIQUE,
            fecha_inicio DATETIME,
            fecha_ultima_actividad DATETIME,
            fecha_fin DATETIME,
            estado VARCHAR(20),
            CONSTRAINT fk_sesion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS contenedor (
            id INT AUTO_INCREMENT PRIMARY KEY,
            codigo_internacional VARCHAR(25) UNIQUE,
            tipo VARCHAR(50),
            capacidad_kg DECIMAL(10,2),
            estado VARCHAR(20)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS sensor_iot (
            id INT AUTO_INCREMENT PRIMARY KEY,
            tipo VARCHAR(50),
            fabricante VARCHAR(100),
            fecha_instalacion DATE,
            estado VARCHAR(20),
            contenedor_id INT,
            CONSTRAINT fk_sensor_iot_contenedor FOREIGN KEY (contenedor_id) REFERENCES contenedor(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS vehiculo (
            id INT AUTO_INCREMENT PRIMARY KEY,
            tipo VARCHAR(30),
            identificacion VARCHAR(30) UNIQUE,
            capacidad_kg DECIMAL(10,2),
            estado VARCHAR(20),
            operador_id INT,
            CONSTRAINT fk_vehiculo_operador FOREIGN KEY (operador_id) REFERENCES parte(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS envio (
            id INT AUTO_INCREMENT PRIMARY KEY,
            cliente_id INT,
            origen_ciudad_id INT,
            destino_ciudad_id INT,
            fecha_creacion DATETIME,
            fecha_salida DATETIME,
            fecha_entrega_estimada DATETIME,
            fecha_entrega_real DATETIME,
            estado VARCHAR(20),
            prioridad VARCHAR(15),
            peso_kg DECIMAL(10,2),
            volumen_m3 DECIMAL(10,2),
            descripcion_carga VARCHAR(255),
            cantidad_incidentes INT,
            CONSTRAINT fk_envio_cliente FOREIGN KEY (cliente_id) REFERENCES parte(id),
            CONSTRAINT fk_envio_origen_ciudad FOREIGN KEY (origen_ciudad_id) REFERENCES ciudad(id),
            CONSTRAINT fk_envio_destino_ciudad FOREIGN KEY (destino_ciudad_id) REFERENCES ciudad(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS envio_contenedor (
            envio_id INT,
            contenedor_id INT,
            fecha_asignacion DATETIME,
            fecha_liberacion DATETIME,
            PRIMARY KEY (envio_id, contenedor_id),
            CONSTRAINT fk_envio_contenedor_envio FOREIGN KEY (envio_id) REFERENCES envio(id),
            CONSTRAINT fk_envio_contenedor_contenedor FOREIGN KEY (contenedor_id) REFERENCES contenedor(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS tramo (
            id INT AUTO_INCREMENT PRIMARY KEY,
            envio_id INT,
            orden INT,
            medio_transporte VARCHAR(50),
            origen_ciudad_id INT,
            destino_ciudad_id INT,
            vehiculo_id INT,
            fecha_salida DATETIME,
            fecha_llegada_estimada DATETIME,
            fecha_llegada_real DATETIME,
            costo DECIMAL(12,2),
            estado VARCHAR(20),
            CONSTRAINT fk_tramo_envio FOREIGN KEY (envio_id) REFERENCES envio(id),
            CONSTRAINT fk_tramo_origen_ciudad FOREIGN KEY (origen_ciudad_id) REFERENCES ciudad(id),
            CONSTRAINT fk_tramo_destino_ciudad FOREIGN KEY (destino_ciudad_id) REFERENCES ciudad(id),
            CONSTRAINT fk_tramo_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(id),
            UNIQUE (envio_id, orden)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS regla (
            id INT AUTO_INCREMENT PRIMARY KEY,
            nombre VARCHAR(100),
            metrica VARCHAR(20),
            operador VARCHAR(20),
            valor DECIMAL(10,2),
            severidad VARCHAR(20),
            envio_id INT,
            activa BOOLEAN,
            CONSTRAINT fk_regla_envio FOREIGN KEY (envio_id) REFERENCES envio(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS factura (
            id INT AUTO_INCREMENT PRIMARY KEY,
            numero VARCHAR(20) UNIQUE,
            parte_id INT,
            tipo VARCHAR(15),
            periodo CHAR(7),
            fecha_emision DATE,
            fecha_vencimiento DATE,
            importe_total DECIMAL(12,2),
            estado VARCHAR(20),
            CONSTRAINT fk_factura_parte FOREIGN KEY (parte_id) REFERENCES parte(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS factura_detalle (
            id INT AUTO_INCREMENT PRIMARY KEY,
            factura_id INT,
            concepto VARCHAR(255),
            importe DECIMAL(12,2),
            envio_id INT UNIQUE,
            tramo_id INT UNIQUE,
            reporte_id VARCHAR(24) UNIQUE,
            CONSTRAINT fk_factura_detalle_factura FOREIGN KEY (factura_id) REFERENCES factura(id),
            CONSTRAINT fk_factura_detalle_envio FOREIGN KEY (envio_id) REFERENCES envio(id),
            CONSTRAINT fk_factura_detalle_tramo FOREIGN KEY (tramo_id) REFERENCES tramo(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS cuenta_corriente (
            id INT AUTO_INCREMENT PRIMARY KEY,
            parte_id INT UNIQUE,
            saldo DECIMAL(12,2),
            fecha_actualizacion DATETIME,
            CONSTRAINT fk_cuenta_corriente_parte FOREIGN KEY (parte_id) REFERENCES parte(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS movimiento_cuenta (
            id INT AUTO_INCREMENT PRIMARY KEY,
            cuenta_id INT,
            fecha DATETIME,
            tipo VARCHAR(20),
            importe DECIMAL(12,2),
            concepto VARCHAR(255),
            factura_id INT,
            pago_id VARCHAR(24),
            CONSTRAINT fk_movimiento_cuenta_cuenta FOREIGN KEY (cuenta_id) REFERENCES cuenta_corriente(id),
            CONSTRAINT fk_movimiento_cuenta_factura FOREIGN KEY (factura_id) REFERENCES factura(id)
        )
        """,

        """
        CREATE TABLE IF NOT EXISTS tipo_reporte (
            id INT AUTO_INCREMENT PRIMARY KEY,
            codigo VARCHAR(30) UNIQUE,
            nombre VARCHAR(100),
            precio DECIMAL(12,2),
            activo BOOLEAN
        )
        """
    };

    public static void inicializar() throws SQLException {
        try (Connection c = MySQLConexion.getConexion();
             Statement st = c.createStatement()) {

            for (String sql : TABLAS) {
                st.execute(sql);
            }

            quitarNivelPermisos(c, st);
            quitarUsuariosExternos(st);
            cargarPermisos(c);
            completarPermisosAdmin(c);
        }
    }

    // bases creadas antes de los permisos tienen la columna rol.nivel_permisos: se reemplazo por rol_permiso
    private static void quitarNivelPermisos(Connection c, Statement st) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'rol' AND COLUMN_NAME = 'nivel_permisos'
                """;
        try (ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            if (rs.getInt(1) == 0) {
                return;
            }
        }
        st.execute("ALTER TABLE rol DROP COLUMN nivel_permisos");
    }

    private static void quitarUsuariosExternos(Statement st) throws SQLException {
        String sqlTabla = """
                SELECT COUNT(*) AS cantidad FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario_externo'
                """;
        String sqlColumna = """
                SELECT COUNT(*) AS cantidad FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'tipo_usuario'
                """;

        int tabla;
        try (ResultSet rs = st.executeQuery(sqlTabla)) {
            rs.next();
            tabla = rs.getInt("cantidad");
        }
        if (tabla > 0) {
            st.execute("DROP TABLE usuario_externo");
        }

        int columna;
        try (ResultSet rs = st.executeQuery(sqlColumna)) {
            rs.next();
            columna = rs.getInt("cantidad");
        }
        if (columna > 0) {
            st.execute("UPDATE usuario SET estado = 'INACTIVO' WHERE tipo_usuario = 'EXTERNO'");
            st.execute("ALTER TABLE usuario DROP COLUMN tipo_usuario");
        }
    }

    private static void cargarPermisos(Connection c) throws SQLException {
        String sqlBuscar = """
                SELECT COUNT(*) AS cantidad FROM permiso WHERE codigo = ?
                """;
        String sqlInsertar = """
                INSERT INTO permiso (codigo, descripcion) VALUES (?, ?)
                """;

        try (PreparedStatement psBuscar = c.prepareStatement(sqlBuscar);
                PreparedStatement psInsertar = c.prepareStatement(sqlInsertar)) {

            for (String[] p : Permiso.CATALOGO) {
                psBuscar.setString(1, p[0]);

                int cantidad;
                try (ResultSet rs = psBuscar.executeQuery()) {
                    rs.next();
                    cantidad = rs.getInt("cantidad");
                }

                if (cantidad == 0) {
                    psInsertar.setString(1, p[0]);
                    psInsertar.setString(2, p[1]);
                    psInsertar.executeUpdate();
                }
            }
        }
    }

    private static void completarPermisosAdmin(Connection c) throws SQLException {
        String sqlAdmin = """
                SELECT id FROM rol WHERE descripcion = 'ADMIN'
                """;
        String sqlFaltantes = """
                SELECT id FROM permiso
                WHERE id NOT IN (SELECT permiso_id FROM rol_permiso WHERE rol_id = ?)
                """;
        String sqlInsertar = """
                INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (?, ?)
                """;

        Integer adminId = null;
        try (PreparedStatement ps = c.prepareStatement(sqlAdmin);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                adminId = rs.getInt("id");
            }
        }
        if (adminId == null) {
            return;
        }

        List<Integer> faltantes = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sqlFaltantes)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    faltantes.add(rs.getInt("id"));
                }
            }
        }

        try (PreparedStatement ps = c.prepareStatement(sqlInsertar)) {
            for (Integer permisoId : faltantes) {
                ps.setInt(1, adminId);
                ps.setInt(2, permisoId);
                ps.executeUpdate();
            }
        }
    }
}
