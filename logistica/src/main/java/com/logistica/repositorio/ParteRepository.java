package com.logistica.repositorio;

import com.logistica.database.MySQLConexion;
import com.logistica.modelo.Parte;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ParteRepository {

    private Parte mapear(ResultSet rs) throws SQLException {
        Parte p = new Parte();
        p.setId(rs.getInt("id"));
        p.setTipo(rs.getString("tipo"));
        p.setRazonSocial(rs.getString("razon_social"));
        p.setCuit(rs.getString("cuit"));
        p.setEmail(rs.getString("email"));
        p.setTelefono(rs.getString("telefono"));
        p.setDireccion(rs.getString("direccion"));
        p.setRubro(rs.getString("rubro"));
        p.setEstado(rs.getString("estado"));
        p.setPaisId(rs.getObject("pais_id", Integer.class));
        p.setFechaAlta(rs.getObject("fecha_alta", LocalDateTime.class));
        return p;
    }

    public int guardar(Parte p) throws SQLException {
        String sql = """
                INSERT INTO parte (tipo, razon_social, cuit, email, telefono, direccion, pais_id, rubro, estado, fecha_alta)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getTipo());
            ps.setString(2, p.getRazonSocial());
            ps.setString(3, p.getCuit());
            ps.setString(4, p.getEmail());
            ps.setString(5, p.getTelefono());
            ps.setString(6, p.getDireccion());
            ps.setObject(7, p.getPaisId());
            ps.setString(8, p.getRubro());
            ps.setString(9, p.getEstado());
            ps.setObject(10, p.getFechaAlta());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Parte buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM parte WHERE id=?
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

    public Parte buscarPorCuit(String cuit) throws SQLException {
        String sql = """
                SELECT * FROM parte WHERE cuit=?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, cuit);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() == true) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }

    }

    public List<Parte> listarPorTipo(String tipo) throws SQLException {
        String sql = """
                SELECT * FROM parte WHERE tipo=? ORDER BY razon_social
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, tipo);

            List<Parte> partes = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next() == true) {
                    partes.add(mapear(rs));
                }
            }
            return partes;
        }

    }

    public void actualizar(Parte p) throws SQLException {
        String sql = """
                UPDATE parte SET razon_social = ?, email = ?, telefono = ?, direccion = ?, pais_id = ?, rubro = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.getRazonSocial());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getDireccion());
            ps.setObject(5, p.getPaisId());
            ps.setString(6, p.getRubro());
            ps.setInt(7, p.getId());

            ps.executeUpdate();
        }

    }

    public void cambiarEstado(int id, String estado) throws SQLException {
        String sql = """
                UPDATE parte SET estado = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, id);

            ps.executeUpdate();
        }

    }
}