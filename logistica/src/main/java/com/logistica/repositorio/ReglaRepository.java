package com.logistica.repositorio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;

import com.logistica.modelo.Regla;

public class ReglaRepository {

    private Regla mapear(ResultSet rs) throws SQLException {
        Regla r = new Regla();
        r.setId(rs.getInt("id"));
        r.setNombre(rs.getString("nombre"));
        r.setMetrica(rs.getString("metrica"));
        r.setOperador(rs.getString("operador"));
        r.setValor(rs.getObject("valor", BigDecimal.class));
        r.setSeveridad(rs.getString("severidad"));
        r.setEnvioId(rs.getObject("envio_id", Integer.class));
        r.setActiva(rs.getObject("activa", Boolean.class));
        return r;
    }

    public int guardar(Regla r) throws SQLException {
        String sql = """
                INSERT INTO regla (nombre, metrica, operador, valor, severidad, envio_id, activa) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getNombre());
            ps.setString(2, r.getMetrica());
            ps.setString(3, r.getOperador());
            ps.setObject(4, r.getValor());
            ps.setString(5, r.getSeveridad());
            ps.setObject(6, r.getEnvioId());
            ps.setObject(7, r.getActiva());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Regla buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM regla WHERE id = ?
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

    public List<Regla> listar() throws SQLException {
        String sql = """
                SELECT * FROM regla ORDER BY metrica, nombre
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<Regla> reglas = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reglas.add(mapear(rs));
                }
            }
            return reglas;
        }
    }

    public List<Regla> listarAplicables(int envioId) throws SQLException {
        String sql = """
                SELECT * FROM regla WHERE activa = TRUE AND (envio_id IS NULL OR envio_id = ?) ORDER BY metrica
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, envioId);

            List<Regla> reglas = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reglas.add(mapear(rs));
                }
            }
            return reglas;
        }
    }

    public List<Regla> listarGeneralesActivas(String metrica) throws SQLException {
        String sql = """
                SELECT * FROM regla WHERE activa = TRUE AND envio_id IS NULL AND metrica = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, metrica);

            List<Regla> reglas = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reglas.add(mapear(rs));
                }
            }
            return reglas;
        }
    }

    public void actualizar(Regla r) throws SQLException {
        String sql = """
                UPDATE regla SET nombre = ?, metrica = ?, operador = ?, valor = ?, severidad = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.getNombre());
            ps.setString(2, r.getMetrica());
            ps.setString(3, r.getOperador());
            ps.setObject(4, r.getValor());
            ps.setString(5, r.getSeveridad());
            ps.setInt(6, r.getId());

            ps.executeUpdate();
        }
    }

    public void cambiarActiva(int id, boolean activa) throws SQLException {
        String sql = """
                UPDATE regla SET activa = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBoolean(1, activa);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
    }

}
