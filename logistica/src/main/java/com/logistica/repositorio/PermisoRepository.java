package com.logistica.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;

import com.logistica.modelo.Permiso;

public class PermisoRepository {

    private Permiso mapear(ResultSet rs) throws SQLException {
        Permiso p = new Permiso();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setDescripcion(rs.getString("descripcion"));
        return p;
    }

    public List<Permiso> listar() throws SQLException {
        String sql = """
                SELECT * FROM permiso ORDER BY id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<Permiso> permisos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapear(rs));
                }
            }
            return permisos;
        }
    }

    public Permiso buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM permiso WHERE id = ?
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

    public List<Permiso> listarDeRol(int rolId) throws SQLException {
        String sql = """
                SELECT p.* FROM permiso p
                JOIN rol_permiso rp ON rp.permiso_id = p.id
                WHERE rp.rol_id = ?
                ORDER BY p.id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, rolId);

            List<Permiso> permisos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapear(rs));
                }
            }
            return permisos;
        }
    }

    public List<Permiso> listarFaltantesDeRol(int rolId) throws SQLException {
        String sql = """
                SELECT * FROM permiso
                WHERE id NOT IN (SELECT permiso_id FROM rol_permiso WHERE rol_id = ?)
                ORDER BY id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, rolId);

            List<Permiso> permisos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapear(rs));
                }
            }
            return permisos;
        }
    }

    public List<Permiso> listarDeUsuario(int usuarioId) throws SQLException {
        String sql = """
                SELECT p.* FROM permiso p
                JOIN rol_permiso rp ON rp.permiso_id = p.id
                JOIN usuario u ON u.rol_id = rp.rol_id
                WHERE u.id = ?
                ORDER BY p.id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);

            List<Permiso> permisos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapear(rs));
                }
            }
            return permisos;
        }
    }

    public boolean usuarioTiene(int usuarioId, String codigo) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS cantidad FROM usuario u
                JOIN rol_permiso rp ON rp.rol_id = u.rol_id
                JOIN permiso p ON p.id = rp.permiso_id
                WHERE u.id = ? AND p.codigo = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setString(2, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cantidad") > 0;
            }
        }
    }

    public boolean rolTiene(int rolId, int permisoId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS cantidad FROM rol_permiso WHERE rol_id = ? AND permiso_id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, rolId);
            ps.setInt(2, permisoId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cantidad") > 0;
            }
        }
    }

    public void asignar(int rolId, int permisoId) throws SQLException {
        String sql = """
                INSERT INTO rol_permiso (rol_id, permiso_id) VALUES (?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, rolId);
            ps.setInt(2, permisoId);

            ps.executeUpdate();
        }
    }

    public boolean quitar(int rolId, int permisoId) throws SQLException {
        String sql = """
                DELETE FROM rol_permiso WHERE rol_id = ? AND permiso_id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, rolId);
            ps.setInt(2, permisoId);

            return ps.executeUpdate() > 0;
        }
    }

}
