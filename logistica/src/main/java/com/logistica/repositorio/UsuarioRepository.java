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

import com.logistica.modelo.Usuario;

public class UsuarioRepository {

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setApellido(rs.getString("apellido"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setEstado(rs.getString("estado"));
        u.setFechaRegistro(rs.getObject("fecha_registro", LocalDateTime.class));
        u.setRolId(rs.getObject("rol_id", Integer.class));
        return u;
    }

    public int guardar(Usuario u) throws SQLException {
        String sql = """
                INSERT INTO usuario (nombre, apellido, email, password_hash, estado, fecha_registro, rol_id) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPasswordHash());
            ps.setString(5, u.getEstado());
            ps.setObject(6, u.getFechaRegistro());
            ps.setObject(7, u.getRolId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM usuario WHERE id = ?
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

    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = """
                SELECT * FROM usuario WHERE email = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public List<Usuario> listar() throws SQLException {
        String sql = """
                SELECT * FROM usuario ORDER BY apellido, nombre
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<Usuario> usuarios = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapear(rs));
                }
            }
            return usuarios;
        }
    }

    public void actualizar(Usuario u) throws SQLException {
        String sql = """
                UPDATE usuario SET nombre = ?, apellido = ?, email = ?, rol_id = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, u.getNombre());
            ps.setString(2, u.getApellido());
            ps.setString(3, u.getEmail());
            ps.setObject(4, u.getRolId());
            ps.setInt(5, u.getId());

            ps.executeUpdate();
        }
    }

    public void cambiarEstado(int id, String estado) throws SQLException {
        String sql = """
                UPDATE usuario SET estado = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
    }

}
