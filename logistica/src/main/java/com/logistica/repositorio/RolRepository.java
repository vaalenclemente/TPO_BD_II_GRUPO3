package com.logistica.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;

import com.logistica.modelo.Rol;

public class RolRepository {

    private Rol mapear(ResultSet rs) throws SQLException {
        Rol r = new Rol();
        r.setId(rs.getInt("id"));
        r.setDescripcion(rs.getString("descripcion"));
        r.setNivelPermisos(rs.getObject("nivel_permisos", Integer.class));
        return r;
    }

    public int guardar(Rol r) throws SQLException {
        String sql = """
                INSERT INTO rol (descripcion, nivel_permisos) VALUES (?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getDescripcion());
            ps.setObject(2, r.getNivelPermisos());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public List<Rol> listar() throws SQLException {
        String sql = """
                SELECT * FROM rol ORDER BY descripcion
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<Rol> roles = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(mapear(rs));
                }
            }
            return roles;
        }
    }

    public Rol buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM rol WHERE id = ?
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

    public Rol buscarPorDescripcion(String descripcion) throws SQLException {
        String sql = """
                SELECT * FROM rol WHERE descripcion = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, descripcion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public int contarUsuarios(int id) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS cantidad FROM usuario WHERE rol_id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cantidad");
            }
        }
    }

    public void actualizar(Rol r) throws SQLException {
        String sql = """
                UPDATE rol SET descripcion = ?, nivel_permisos = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, r.getDescripcion());
            ps.setObject(2, r.getNivelPermisos());
            ps.setInt(3, r.getId());

            ps.executeUpdate();
        }
    }

    public void borrar(int id) throws SQLException {
        String sql = """
                DELETE FROM rol WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

}
