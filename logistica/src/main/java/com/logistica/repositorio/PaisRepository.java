package com.logistica.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;

import com.logistica.modelo.Pais;

public class PaisRepository {

    // recorre el resultado set en objetos pais
    private Pais mapear(ResultSet rs) throws SQLException {
        Pais p = new Pais();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        return p;
    }

    // mostrar paises
    public List<Pais> listar() throws SQLException {
        String sql = """
                SELECT * FROM pais ORDER BY nombre
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<Pais> paises = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next() == true) {
                    paises.add(mapear(rs));
                }
            }
            return paises;
        }
    }

    public Pais buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM pais WHERE id=?
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

    public int guardar(Pais p) throws SQLException {
        String sql = """
                INSERT INTO pais (nombre) VALUES (?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Pais buscarPorNombre(String nombre) throws SQLException {
        String sql = """
                SELECT * FROM pais WHERE nombre = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public void actualizar(Pais p) throws SQLException {
        String sql = """
                UPDATE pais SET nombre = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getId());
            ps.executeUpdate();
        }
    }

    public int contarUsos(int id) throws SQLException {
        String sql = """
                SELECT (SELECT COUNT(*) FROM ciudad WHERE pais_id = ?)
                     + (SELECT COUNT(*) FROM parte WHERE pais_id = ?) AS usos
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, id);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("usos");
            }
        }
    }

    public void borrar(int id) throws SQLException {
        String sql = """
                DELETE FROM pais WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
