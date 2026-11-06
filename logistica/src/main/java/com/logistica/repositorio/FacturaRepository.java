package com.logistica.repositorio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;

import com.logistica.modelo.Factura;
import com.logistica.modelo.FacturaDetalle;
import com.logistica.modelo.MovimientoCuenta;

public class FacturaRepository {

    private Factura mapear(ResultSet rs) throws SQLException {
        Factura f = new Factura();
        f.setId(rs.getInt("id"));
        f.setNumero(rs.getString("numero"));
        f.setParteId(rs.getObject("parte_id", Integer.class));
        f.setTipo(rs.getString("tipo"));
        f.setPeriodo(rs.getString("periodo"));
        f.setFechaEmision(rs.getObject("fecha_emision", LocalDate.class));
        f.setFechaVencimiento(rs.getObject("fecha_vencimiento", LocalDate.class));
        f.setImporteTotal(rs.getObject("importe_total", BigDecimal.class));
        f.setEstado(rs.getString("estado"));
        return f;
    }

    private FacturaDetalle mapearDetalle(ResultSet rs) throws SQLException {
        FacturaDetalle d = new FacturaDetalle();
        d.setId(rs.getInt("id"));
        d.setFacturaId(rs.getObject("factura_id", Integer.class));
        d.setConcepto(rs.getString("concepto"));
        d.setImporte(rs.getObject("importe", BigDecimal.class));
        d.setEnvioId(rs.getObject("envio_id", Integer.class));
        d.setTramoId(rs.getObject("tramo_id", Integer.class));
        d.setReporteId(rs.getString("reporte_id"));
        return d;
    }

    public int guardar(Factura f, List<FacturaDetalle> detalles, MovimientoCuenta movimiento)
            throws SQLException {

        String sqlFactura = """
                INSERT INTO factura (numero, parte_id, tipo, periodo, fecha_emision, fecha_vencimiento, importe_total, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        String sqlDetalle = """
                INSERT INTO factura_detalle (factura_id, concepto, importe, envio_id, tramo_id, reporte_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = MySQLConexion.getConexion()) {
            c.setAutoCommit(false);

            try {
                int facturaId;
                try (PreparedStatement ps = c.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, f.getNumero());
                    ps.setObject(2, f.getParteId());
                    ps.setString(3, f.getTipo());
                    ps.setString(4, f.getPeriodo());
                    ps.setObject(5, f.getFechaEmision());
                    ps.setObject(6, f.getFechaVencimiento());
                    ps.setObject(7, f.getImporteTotal());
                    ps.setString(8, f.getEstado());

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        facturaId = rs.getInt(1);
                    }
                }

                for (FacturaDetalle d : detalles) {
                    try (PreparedStatement ps = c.prepareStatement(sqlDetalle)) {
                        ps.setInt(1, facturaId);
                        ps.setString(2, d.getConcepto());
                        ps.setObject(3, d.getImporte());
                        ps.setObject(4, d.getEnvioId());
                        ps.setObject(5, d.getTramoId());
                        ps.setString(6, d.getReporteId());
                        ps.executeUpdate();
                    }
                }

                movimiento.setFacturaId(facturaId);
                CuentaCorrienteRepository.insertarMovimiento(c, movimiento);

                c.commit();
                return facturaId;

            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void anular(int facturaId, MovimientoCuenta contraMovimiento) throws SQLException {
        String sqlLineas = """
                UPDATE factura_detalle SET envio_id = NULL, tramo_id = NULL, reporte_id = NULL WHERE factura_id = ?
                """;

        try (Connection c = MySQLConexion.getConexion()) {
            c.setAutoCommit(false);

            try {
                cambiarEstado(c, facturaId, "ANULADA");

                try (PreparedStatement ps = c.prepareStatement(sqlLineas)) {
                    ps.setInt(1, facturaId);
                    ps.executeUpdate();
                }

                CuentaCorrienteRepository.insertarMovimiento(c, contraMovimiento);

                c.commit();

            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public Factura buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM factura WHERE id = ?
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

    public Factura buscarPorNumero(String numero) throws SQLException {
        String sql = """
                SELECT * FROM factura WHERE numero = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, numero);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public List<Factura> listarPorParte(int parteId) throws SQLException {
        String sql = """
                SELECT * FROM factura WHERE parte_id = ? ORDER BY fecha_emision DESC, id DESC
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, parteId);

            List<Factura> facturas = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    facturas.add(mapear(rs));
                }
            }
            return facturas;
        }
    }

    public List<Factura> listarPorTipoYEstado(String tipo, String estado) throws SQLException {
        String sql = """
                SELECT * FROM factura WHERE tipo = ? AND estado = ? ORDER BY fecha_vencimiento
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, tipo);
            ps.setString(2, estado);

            List<Factura> facturas = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    facturas.add(mapear(rs));
                }
            }
            return facturas;
        }
    }

    public List<FacturaDetalle> listarDetalle(int facturaId) throws SQLException {
        String sql = """
                SELECT * FROM factura_detalle WHERE factura_id = ? ORDER BY id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, facturaId);

            List<FacturaDetalle> detalle = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalle.add(mapearDetalle(rs));
                }
            }
            return detalle;
        }
    }

    // numeracion facturas propias: fe-00000001 (emitidas), fr-00000001 (recibidas).
    public int contarPorTipo(String tipo) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS cantidad FROM factura WHERE tipo = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, tipo);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cantidad");
            }
        }
    }

    public boolean reporteFacturado(String reporteId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS cantidad FROM factura_detalle WHERE reporte_id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, reporteId);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cantidad") > 0;
            }
        }
    }

    public void cambiarEstado(int id, String estado) throws SQLException {
        try (Connection c = MySQLConexion.getConexion()) {
            cambiarEstado(c, id, estado);
        }
    }

    static void cambiarEstado(Connection c, int id, String estado) throws SQLException {
        String sql = """
                UPDATE factura SET estado = ? WHERE id = ?
                """;

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

}
