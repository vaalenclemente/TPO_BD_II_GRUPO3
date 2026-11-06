package com.logistica.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;

import com.logistica.modelo.Ciudad;

public class CiudadRepository {

    private Ciudad mapear(ResultSet rs) throws SQLException {
        Ciudad c = new Ciudad();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setPaisId(rs.getObject("pais_id", Integer.class));
        return c;
    }

    public List<Ciudad> listarPorPais(int paisId) throws SQLException {
        String sql = """
                SELECT * FROM ciudad WHERE pais_id = ? ORDER BY nombre
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, paisId);
            List<Ciudad> ciudades = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next() == true) {
                    ciudades.add(mapear(rs));
                }
            }
            return ciudades;
        }
    }

    public Ciudad buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM ciudad WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() == true) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }

    }

    public int guardar(Ciudad ciudad) throws SQLException {
        String sql = """
                INSERT INTO ciudad (nombre, pais_id) VALUES (?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, ciudad.getNombre());
            ps.setInt(2, ciudad.getPaisId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Ciudad buscarPorNombre(String nombre, int paisId) throws SQLException {
        String sql = """
                SELECT * FROM ciudad WHERE nombre = ? AND pais_id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, paisId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public void actualizar(Ciudad ciudad) throws SQLException {
        String sql = """
                UPDATE ciudad SET nombre = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, ciudad.getNombre());
            ps.setInt(2, ciudad.getId());
            ps.executeUpdate();
        }
    }

    public int contarUsos(int id) throws SQLException {
        String sql = """
                SELECT (SELECT COUNT(*) FROM envio WHERE origen_ciudad_id = ? OR destino_ciudad_id = ?)
                     + (SELECT COUNT(*) FROM tramo WHERE origen_ciudad_id = ? OR destino_ciudad_id = ?) AS usos
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            for (int i = 1; i <= 4; i++) {
                ps.setInt(i, id);
            }

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("usos");
            }
        }
    }

    public void borrar(int id) throws SQLException {
        String sql = """
                DELETE FROM ciudad WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
