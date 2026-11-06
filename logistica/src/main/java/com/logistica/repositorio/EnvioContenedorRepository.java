package com.logistica.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;

import com.logistica.modelo.EnvioContenedor;

public class EnvioContenedorRepository {

    private EnvioContenedor mapear(ResultSet rs) throws SQLException {
        EnvioContenedor ec = new EnvioContenedor();
        ec.setEnvioId(rs.getObject("envio_id", Integer.class));
        ec.setContenedorId(rs.getObject("contenedor_id", Integer.class));
        ec.setFechaAsignacion(rs.getObject("fecha_asignacion", LocalDateTime.class));
        ec.setFechaLiberacion(rs.getObject("fecha_liberacion", LocalDateTime.class));
        return ec;
    }

    public void guardar(EnvioContenedor ec) throws SQLException {
        String sql = """
                INSERT INTO envio_contenedor (envio_id, contenedor_id, fecha_asignacion, fecha_liberacion) VALUES (?, ?, ?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, ec.getEnvioId());
            ps.setObject(2, ec.getContenedorId());
            ps.setObject(3, ec.getFechaAsignacion());
            ps.setObject(4, ec.getFechaLiberacion());

            ps.executeUpdate();
        }
    }

    public EnvioContenedor buscar(int envioId, int contenedorId) throws SQLException {
        String sql = """
                SELECT * FROM envio_contenedor WHERE envio_id = ? AND contenedor_id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, envioId);
            ps.setInt(2, contenedorId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public List<EnvioContenedor> listarPorEnvio(int envioId) throws SQLException {
        String sql = """
                SELECT * FROM envio_contenedor WHERE envio_id = ? ORDER BY fecha_asignacion
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, envioId);

            List<EnvioContenedor> lista = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    public List<EnvioContenedor> listarAsignados(int envioId) throws SQLException {
        String sql = """
                SELECT * FROM envio_contenedor WHERE envio_id = ? AND fecha_liberacion IS NULL
                ORDER BY fecha_asignacion
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, envioId);

            List<EnvioContenedor> lista = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        }
    }

    // Envio en el que viaja un contenedor (asignacion sin liberar), o null si esta
    // libre.
    public Integer buscarEnvioActivo(int contenedorId) throws SQLException {
        String sql = """
                SELECT envio_id FROM envio_contenedor WHERE contenedor_id = ? AND fecha_liberacion IS NULL
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, contenedorId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("envio_id");
                } else {
                    return null;
                }
            }
        }
    }

    public void actualizarLiberacion(int envioId, int contenedorId, LocalDateTime fecha)
            throws SQLException {

        String sql = """
                UPDATE envio_contenedor SET fecha_liberacion = ? WHERE envio_id = ? AND contenedor_id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, fecha);
            ps.setInt(2, envioId);
            ps.setInt(3, contenedorId);

            ps.executeUpdate();
        }
    }

    public void borrar(int envioId, int contenedorId) throws SQLException {
        String sql = """
                DELETE FROM envio_contenedor WHERE envio_id = ? AND contenedor_id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, envioId);
            ps.setInt(2, contenedorId);

            ps.executeUpdate();
        }
    }

}
