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

import com.logistica.modelo.Contenedor;

public class ContenedorRepository {

    private Contenedor mapear(ResultSet rs) throws SQLException {
        Contenedor c = new Contenedor();
        c.setId(rs.getInt("id"));
        c.setCodigoInternacional(rs.getString("codigo_internacional"));
        c.setTipo(rs.getString("tipo"));
        c.setCapacidadKg(rs.getObject("capacidad_kg", BigDecimal.class));
        c.setEstado(rs.getString("estado"));
        return c;
    }

    public int guardar(Contenedor c) throws SQLException {
        String sql = """
                INSERT INTO contenedor (codigo_internacional, tipo, capacidad_kg, estado) VALUES (?, ?, ?, ?)
                """;

        try (Connection con = MySQLConexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getCodigoInternacional());
            ps.setString(2, c.getTipo());
            ps.setObject(3, c.getCapacidadKg());
            ps.setString(4, c.getEstado());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Contenedor buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM contenedor WHERE id = ?
                """;

        try (Connection con = MySQLConexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

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

    public Contenedor buscarPorCodigo(String codigo) throws SQLException {
        String sql = """
                SELECT * FROM contenedor WHERE codigo_internacional = ?
                """;

        try (Connection con = MySQLConexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

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

    public List<Contenedor> listar() throws SQLException {
        String sql = """
                SELECT * FROM contenedor ORDER BY codigo_internacional
                """;

        try (Connection con = MySQLConexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            List<Contenedor> contenedores = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contenedores.add(mapear(rs));
                }
            }
            return contenedores;
        }
    }

    public List<Contenedor> listarPorEstado(String estado) throws SQLException {
        String sql = """
                SELECT * FROM contenedor WHERE estado = ? ORDER BY codigo_internacional
                """;

        try (Connection con = MySQLConexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estado);

            List<Contenedor> contenedores = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    contenedores.add(mapear(rs));
                }
            }
            return contenedores;
        }
    }

    public void actualizar(Contenedor c) throws SQLException {
        String sql = """
                UPDATE contenedor SET tipo = ?, capacidad_kg = ? WHERE id = ?
                """;

        try (Connection con = MySQLConexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getTipo());
            ps.setObject(2, c.getCapacidadKg());
            ps.setInt(3, c.getId());

            ps.executeUpdate();
        }
    }

    public void cambiarEstado(int id, String estado) throws SQLException {
        String sql = """
                UPDATE contenedor SET estado = ? WHERE id = ?
                """;

        try (Connection con = MySQLConexion.getConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
    }

}
