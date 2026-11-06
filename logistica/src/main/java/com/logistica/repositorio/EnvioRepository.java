package com.logistica.repositorio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;

import com.logistica.modelo.Envio;

public class EnvioRepository {

    private Envio mapear(ResultSet rs) throws SQLException {
        Envio e = new Envio();
        e.setId(rs.getInt("id"));
        e.setClienteId(rs.getObject("cliente_id", Integer.class));
        e.setOrigenCiudadId(rs.getObject("origen_ciudad_id", Integer.class));
        e.setDestinoCiudadId(rs.getObject("destino_ciudad_id", Integer.class));
        e.setFechaCreacion(rs.getObject("fecha_creacion", LocalDateTime.class));
        e.setFechaSalida(rs.getObject("fecha_salida", LocalDateTime.class));
        e.setFechaEntregaEstimada(rs.getObject("fecha_entrega_estimada", LocalDateTime.class));
        e.setFechaEntregaReal(rs.getObject("fecha_entrega_real", LocalDateTime.class));
        e.setEstado(rs.getString("estado"));
        e.setPrioridad(rs.getString("prioridad"));
        e.setPesoKg(rs.getObject("peso_kg", BigDecimal.class));
        e.setVolumenM3(rs.getObject("volumen_m3", BigDecimal.class));
        e.setDescripcionCarga(rs.getString("descripcion_carga"));
        e.setCantidadIncidentes(rs.getObject("cantidad_incidentes", Integer.class));
        return e;
    }

    public int guardar(Envio e) throws SQLException {
        String sql = """
                INSERT INTO envio (cliente_id, origen_ciudad_id, destino_ciudad_id, fecha_creacion, fecha_salida, fecha_entrega_estimada, fecha_entrega_real, estado,
                prioridad, peso_kg, volumen_m3, descripcion_carga, cantidad_incidentes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setObject(1, e.getClienteId());
            ps.setObject(2, e.getOrigenCiudadId());
            ps.setObject(3, e.getDestinoCiudadId());
            ps.setObject(4, e.getFechaCreacion());
            ps.setObject(5, e.getFechaSalida());
            ps.setObject(6, e.getFechaEntregaEstimada());
            ps.setObject(7, e.getFechaEntregaReal());
            ps.setString(8, e.getEstado());
            ps.setString(9, e.getPrioridad());
            ps.setObject(10, e.getPesoKg());
            ps.setObject(11, e.getVolumenM3());
            ps.setString(12, e.getDescripcionCarga());
            ps.setObject(13, e.getCantidadIncidentes());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Envio buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM envio WHERE id = ?
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

    public List<Envio> listar() throws SQLException {
        String sql = """
                SELECT * FROM envio ORDER BY fecha_creacion DESC
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<Envio> envios = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    envios.add(mapear(rs));
                }
            }
            return envios;
        }
    }

    public List<Envio> listarPorCliente(int clienteId) throws SQLException {
        String sql = """
                SELECT * FROM envio WHERE cliente_id = ? ORDER BY fecha_creacion DESC
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, clienteId);

            List<Envio> envios = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    envios.add(mapear(rs));
                }
            }
            return envios;
        }
    }

    public List<Envio> listarPorEstado(String estado) throws SQLException {
        String sql = """
                SELECT * FROM envio WHERE estado = ? ORDER BY fecha_creacion DESC
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, estado);

            List<Envio> envios = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    envios.add(mapear(rs));
                }
            }
            return envios;
        }
    }

    public List<Envio> listarEntregadosSinFacturar(LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {

        String sql = """
                SELECT * FROM envio
                WHERE estado = 'ENTREGADO' AND fecha_entrega_real >= ? AND fecha_entrega_real < ?
                  AND id NOT IN (SELECT envio_id FROM factura_detalle WHERE envio_id IS NOT NULL)
                ORDER BY cliente_id, id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setObject(1, desde);
            ps.setObject(2, hasta);

            List<Envio> envios = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    envios.add(mapear(rs));
                }
            }
            return envios;
        }
    }

    public void actualizar(Envio e) throws SQLException {
        String sql = """
                UPDATE envio SET prioridad = ?, fecha_entrega_estimada = ?, peso_kg = ?, volumen_m3 = ?, descripcion_carga = ?
                WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, e.getPrioridad());
            ps.setObject(2, e.getFechaEntregaEstimada());
            ps.setObject(3, e.getPesoKg());
            ps.setObject(4, e.getVolumenM3());
            ps.setString(5, e.getDescripcionCarga());
            ps.setInt(6, e.getId());

            ps.executeUpdate();
        }
    }

    public void actualizarSeguimiento(int id, String estado, LocalDateTime fechaSalida,
            LocalDateTime fechaEntregaReal) throws SQLException {

        String sql = """
                UPDATE envio SET estado = ?, fecha_salida = ?, fecha_entrega_real = ? WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setObject(2, fechaSalida);
            ps.setObject(3, fechaEntregaReal);
            ps.setInt(4, id);

            ps.executeUpdate();
        }
    }

    public void incrementarIncidentes(int id) throws SQLException {
        String sql = """
                UPDATE envio SET cantidad_incidentes = cantidad_incidentes + 1 WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Compensacion de incrementarIncidentes (saga de apertura de incidente).
    public void decrementarIncidentes(int id) throws SQLException {
        String sql = """
                UPDATE envio SET cantidad_incidentes = cantidad_incidentes - 1 WHERE id = ? AND cantidad_incidentes > 0
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void borrar(int id) throws SQLException {
        String sql = """
                DELETE FROM envio WHERE id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

}
