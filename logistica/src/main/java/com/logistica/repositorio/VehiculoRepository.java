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

import com.logistica.modelo.Vehiculo;

public class VehiculoRepository {

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();
        v.setId(rs.getInt("id"));
        v.setTipo(rs.getString("tipo"));
        v.setIdentificacion(rs.getString("identificacion"));
        v.setCapacidadKg(rs.getObject("capacidad_kg", BigDecimal.class));
        v.setEstado(rs.getString("estado"));
        v.setOperadorId(rs.getObject("operador_id", Integer.class));
        return v;
    }

    public int guardar(Vehiculo v) throws SQLException {
        String sql = """
                INSERT INTO vehiculo (tipo, identificacion, capacidad_kg, estado, operador_id) VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, v.getTipo());
            ps.setString(2, v.getIdentificacion());
            ps.setObject(3, v.getCapacidadKg());
            ps.setString(4, v.getEstado());
            ps.setObject(5, v.getOperadorId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Vehiculo buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM vehiculo WHERE id = ?
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

    public Vehiculo buscarPorIdentificacion(String identificacion) throws SQLException {
        String sql = """
                SELECT * FROM vehiculo WHERE identificacion = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, identificacion);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public List<Vehiculo> listar() throws SQLException {
        String sql = """
                SELECT * FROM vehiculo ORDER BY tipo, identificacion
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<Vehiculo> vehiculos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehiculos.add(mapear(rs));
                }
            }
            return vehiculos;
        }
    }

    public List<Vehiculo> listarPorOperador(int operadorId) throws SQLException {
        String sql = """
                SELECT * FROM vehiculo WHERE operador_id = ? ORDER BY tipo, identificacion
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, operadorId);

            List<Vehiculo> vehiculos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehiculos.add(mapear(rs));
                }
            }
            return vehiculos;
        }
    }

    public void actualizar(Vehiculo v) throws SQLException {
        String sql = """
                UPDATE vehiculo SET tipo = ?, capacidad_kg = ?, operador_id = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, v.getTipo());
            ps.setObject(2, v.getCapacidadKg());
            ps.setObject(3, v.getOperadorId());
            ps.setInt(4, v.getId());

            ps.executeUpdate();
        }
    }

    public void cambiarEstado(int id, String estado) throws SQLException {
        String sql = """
                UPDATE vehiculo SET estado = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, id);

            ps.executeUpdate();
        }
    }

}
