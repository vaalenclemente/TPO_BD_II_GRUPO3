package com.logistica.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;

import com.logistica.modelo.Sesion;

public class SesionRepository {

    private Sesion mapear(ResultSet rs) throws SQLException {
        Sesion s = new Sesion();
        s.setId(rs.getInt("id"));
        s.setUsuarioId(rs.getObject("usuario_id", Integer.class));
        s.setToken(rs.getString("token"));
        s.setFechaInicio(rs.getObject("fecha_inicio", LocalDateTime.class));
        s.setFechaUltimaActividad(rs.getObject("fecha_ultima_actividad", LocalDateTime.class));
        s.setFechaFin(rs.getObject("fecha_fin", LocalDateTime.class));
        s.setEstado(rs.getString("estado"));
        return s;
    }

    public int guardar(Sesion s) throws SQLException {
        String sql = """
                INSERT INTO sesion (usuario_id, token, fecha_inicio, fecha_ultima_actividad, fecha_fin, estado) VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, s.getUsuarioId());
            ps.setString(2, s.getToken());
            ps.setObject(3, s.getFechaInicio());
            ps.setObject(4, s.getFechaUltimaActividad());
            ps.setObject(5, s.getFechaFin());
            ps.setString(6, s.getEstado());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Sesion buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM sesion WHERE id = ?
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

    public Sesion buscarPorToken(String token) throws SQLException {
        String sql = """
                SELECT * FROM sesion WHERE token = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, token);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public List<Sesion> listarActivas() throws SQLException {
        String sql = """
                SELECT * FROM sesion WHERE estado = 'ACTIVA' ORDER BY fecha_inicio DESC
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<Sesion> sesiones = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sesiones.add(mapear(rs));
                }
            }
            return sesiones;
        }
    }

    // Se llama en cada operacion del usuario
    public void actualizarActividad(int id, LocalDateTime fecha) throws SQLException {
        String sql = """
                UPDATE sesion SET fecha_ultima_actividad = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, fecha);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
    }

    public void finalizar(int id, String estado, LocalDateTime fechaFin) throws SQLException {
        String sql = """
                UPDATE sesion SET estado = ?, fecha_fin = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setObject(2, fechaFin);
            ps.setInt(3, id);

            ps.executeUpdate();
        }
    }

    public int expirarInactivas(LocalDateTime limite, LocalDateTime ahora) throws SQLException {
        String sql = """
                UPDATE sesion SET estado = 'EXPIRADA', fecha_fin = ?
                WHERE estado = 'ACTIVA' AND fecha_ultima_actividad < ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, ahora);
            ps.setObject(2, limite);

            return ps.executeUpdate();
        }
    }

}
