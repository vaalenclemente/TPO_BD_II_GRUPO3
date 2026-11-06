package com.logistica.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;

import com.logistica.modelo.SensorIot;

public class SensorIotRepository {

    private SensorIot mapear(ResultSet rs) throws SQLException {
        SensorIot s = new SensorIot();
        s.setId(rs.getInt("id"));
        s.setTipo(rs.getString("tipo"));
        s.setFabricante(rs.getString("fabricante"));
        s.setFechaInstalacion(rs.getObject("fecha_instalacion", LocalDate.class));
        s.setEstado(rs.getString("estado"));
        s.setContenedorId(rs.getObject("contenedor_id", Integer.class));
        return s;
    }

    public int guardar(SensorIot s) throws SQLException {
        String sql = """
                INSERT INTO sensor_iot (tipo, fabricante, fecha_instalacion, estado, contenedor_id) VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getTipo());
            ps.setString(2, s.getFabricante());
            ps.setObject(3, s.getFechaInstalacion());
            ps.setString(4, s.getEstado());
            ps.setObject(5, s.getContenedorId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public SensorIot buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM sensor_iot WHERE id = ?
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

    public List<SensorIot> listar() throws SQLException {
        String sql = """
                SELECT * FROM sensor_iot ORDER BY id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<SensorIot> sensores = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sensores.add(mapear(rs));
                }
            }
            return sensores;
        }
    }

    public List<SensorIot> listarPorContenedor(int contenedorId) throws SQLException {
        String sql = """
                SELECT * FROM sensor_iot WHERE contenedor_id = ? ORDER BY id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, contenedorId);

            List<SensorIot> sensores = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sensores.add(mapear(rs));
                }
            }
            return sensores;
        }
    }

    public void actualizar(SensorIot s) throws SQLException {
        String sql = """
                UPDATE sensor_iot SET tipo = ?, fabricante = ?, fecha_instalacion = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, s.getTipo());
            ps.setString(2, s.getFabricante());
            ps.setObject(3, s.getFechaInstalacion());
            ps.setInt(4, s.getId());

            ps.executeUpdate();
        }
    }

    public void cambiarEstado(int id, String estado) throws SQLException {
        String sql = """
                UPDATE sensor_iot SET estado = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
    }

    public void asignarContenedor(int id, Integer contenedorId) throws SQLException {
        String sql = """
                UPDATE sensor_iot SET contenedor_id = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, contenedorId);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
    }

}
