package com.logistica.repositorio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;

import com.logistica.modelo.Tramo;

public class TramoRepository {

    private Tramo mapear(ResultSet rs) throws SQLException {
        Tramo t = new Tramo();
        t.setId(rs.getInt("id"));
        t.setEnvioId(rs.getObject("envio_id", Integer.class));
        t.setOrden(rs.getObject("orden", Integer.class));
        t.setMedioTransporte(rs.getString("medio_transporte"));
        t.setOrigenCiudadId(rs.getObject("origen_ciudad_id", Integer.class));
        t.setDestinoCiudadId(rs.getObject("destino_ciudad_id", Integer.class));
        t.setVehiculoId(rs.getObject("vehiculo_id", Integer.class));
        t.setFechaSalida(rs.getObject("fecha_salida", LocalDateTime.class));
        t.setFechaLlegadaEstimada(rs.getObject("fecha_llegada_estimada", LocalDateTime.class));
        t.setFechaLlegadaReal(rs.getObject("fecha_llegada_real", LocalDateTime.class));
        t.setCosto(rs.getObject("costo", BigDecimal.class));
        t.setEstado(rs.getString("estado"));
        return t;
    }

    public int guardar(Tramo t) throws SQLException {
        String sql = """
                INSERT INTO tramo (envio_id, orden, medio_transporte, origen_ciudad_id, destino_ciudad_id, vehiculo_id,
                 fecha_salida, fecha_llegada_estimada, fecha_llegada_real, costo, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, t.getEnvioId());
            ps.setObject(2, t.getOrden());
            ps.setString(3, t.getMedioTransporte());
            ps.setObject(4, t.getOrigenCiudadId());
            ps.setObject(5, t.getDestinoCiudadId());
            ps.setObject(6, t.getVehiculoId());
            ps.setObject(7, t.getFechaSalida());
            ps.setObject(8, t.getFechaLlegadaEstimada());
            ps.setObject(9, t.getFechaLlegadaReal());
            ps.setObject(10, t.getCosto());
            ps.setString(11, t.getEstado());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Tramo buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM tramo WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public List<Tramo> listarPorEnvio(int envioId) throws SQLException {
        String sql = """
                SELECT * FROM tramo WHERE envio_id = ? ORDER BY orden
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, envioId);

            List<Tramo> tramos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tramos.add(mapear(rs));
                }
            }
            return tramos;
        }
    }

    public List<Tramo> listarCompletadosSinFacturar(LocalDateTime desde, LocalDateTime hasta) throws SQLException {

        String sql = """
                SELECT * FROM tramo
                WHERE estado = 'COMPLETADO' AND fecha_llegada_real >= ? AND fecha_llegada_real < ?
                  AND id NOT IN (SELECT tramo_id FROM factura_detalle WHERE tramo_id IS NOT NULL)
                ORDER BY vehiculo_id, id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, desde);
            ps.setObject(2, hasta);

            List<Tramo> tramos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tramos.add(mapear(rs));
                }
            }
            return tramos;
        }
    }

    public void actualizarSeguimiento(int id, String estado, LocalDateTime fechaSalida, LocalDateTime fechaLlegadaReal)
            throws SQLException {

        String sql = """
                UPDATE tramo SET estado = ?, fecha_salida = ?, fecha_llegada_real = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setObject(2, fechaSalida);
            ps.setObject(3, fechaLlegadaReal);
            ps.setInt(4, id);

            ps.executeUpdate();
        }
    }

    public void borrar(int id) throws SQLException {
        String sql = """
                DELETE FROM tramo WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

}
