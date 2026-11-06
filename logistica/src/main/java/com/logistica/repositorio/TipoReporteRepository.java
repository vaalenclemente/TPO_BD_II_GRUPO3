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

import com.logistica.modelo.TipoReporte;

public class TipoReporteRepository {

    private TipoReporte mapear(ResultSet rs) throws SQLException {
        TipoReporte t = new TipoReporte();
        t.setId(rs.getInt("id"));
        t.setCodigo(rs.getString("codigo"));
        t.setNombre(rs.getString("nombre"));
        t.setPrecio(rs.getObject("precio", BigDecimal.class));
        t.setActivo(rs.getObject("activo", Boolean.class));
        return t;
    }

    public int guardar(TipoReporte t) throws SQLException {
        String sql = """
                INSERT INTO tipo_reporte (codigo, nombre, precio, activo) VALUES (?, ?, ?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, t.getCodigo());
            ps.setString(2, t.getNombre());
            ps.setObject(3, t.getPrecio());
            ps.setObject(4, t.getActivo());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public TipoReporte buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM tipo_reporte WHERE id = ?
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

    public TipoReporte buscarPorCodigo(String codigo) throws SQLException {
        String sql = """
                SELECT * FROM tipo_reporte WHERE codigo = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public List<TipoReporte> listar() throws SQLException {
        String sql = """
                SELECT * FROM tipo_reporte ORDER BY nombre
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<TipoReporte> tipos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tipos.add(mapear(rs));
                }
            }
            return tipos;
        }
    }

    public List<TipoReporte> listarActivos() throws SQLException {
        String sql = """
                SELECT * FROM tipo_reporte WHERE activo = TRUE ORDER BY nombre
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<TipoReporte> tipos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tipos.add(mapear(rs));
                }
            }
            return tipos;
        }
    }

    public void actualizar(TipoReporte t) throws SQLException {
        String sql = """
                UPDATE tipo_reporte SET nombre = ?, precio = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, t.getNombre());
            ps.setObject(2, t.getPrecio());
            ps.setInt(3, t.getId());

            ps.executeUpdate();
        }
    }

    public void cambiarActivo(int id, boolean activo) throws SQLException {
        String sql = """
                UPDATE tipo_reporte SET activo = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBoolean(1, activo);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
    }

}
