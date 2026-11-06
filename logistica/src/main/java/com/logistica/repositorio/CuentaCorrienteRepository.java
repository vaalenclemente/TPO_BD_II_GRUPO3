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

import com.logistica.modelo.CuentaCorriente;
import com.logistica.modelo.MovimientoCuenta;

public class CuentaCorrienteRepository {

    private CuentaCorriente mapear(ResultSet rs) throws SQLException {
        CuentaCorriente c = new CuentaCorriente();
        c.setId(rs.getInt("id"));
        c.setParteId(rs.getObject("parte_id", Integer.class));
        c.setSaldo(rs.getObject("saldo", BigDecimal.class));
        c.setFechaActualizacion(rs.getObject("fecha_actualizacion", LocalDateTime.class));
        return c;
    }

    private MovimientoCuenta mapearMovimiento(ResultSet rs) throws SQLException {
        MovimientoCuenta m = new MovimientoCuenta();
        m.setId(rs.getInt("id"));
        m.setCuentaId(rs.getObject("cuenta_id", Integer.class));
        m.setFecha(rs.getObject("fecha", LocalDateTime.class));
        m.setTipo(rs.getString("tipo"));
        m.setImporte(rs.getObject("importe", BigDecimal.class));
        m.setConcepto(rs.getString("concepto"));
        m.setFacturaId(rs.getObject("factura_id", Integer.class));
        m.setPagoId(rs.getString("pago_id"));
        return m;
    }

    public int crear(int parteId) throws SQLException {
        String sql = """
                INSERT INTO cuenta_corriente (parte_id, saldo, fecha_actualizacion) VALUES (?, 0, ?)
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, parteId);
            ps.setObject(2, LocalDateTime.now());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public CuentaCorriente buscarPorId(int id) throws SQLException {
        String sql = """
                SELECT * FROM cuenta_corriente WHERE id = ?
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

    public CuentaCorriente buscarPorParte(int parteId) throws SQLException {
        String sql = """
                SELECT * FROM cuenta_corriente WHERE parte_id = ?
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, parteId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                } else {
                    return null;
                }
            }
        }
    }

    public List<CuentaCorriente> listar() throws SQLException {
        String sql = """
                SELECT * FROM cuenta_corriente ORDER BY parte_id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            List<CuentaCorriente> cuentas = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cuentas.add(mapear(rs));
                }
            }
            return cuentas;
        }
    }

    public int registrarMovimiento(MovimientoCuenta m, Integer facturaPagada) throws SQLException {
        return registrarMovimiento(m, facturaPagada, "PAGADA");
    }

    // Movimiento + saldo + estado de la factura en una sola transaccion.
    // Lo usa el cobro/pago (factura -> PAGADA) y la anulacion de un pago (factura
    // -> PENDIENTE).
    public int registrarMovimiento(MovimientoCuenta m, Integer facturaId, String estadoFactura) throws SQLException {
        try (Connection c = MySQLConexion.getConexion()) {
            c.setAutoCommit(false);

            try {
                int id = insertarMovimiento(c, m);

                if (facturaId != null) {
                    FacturaRepository.cambiarEstado(c, facturaId, estadoFactura);
                }

                c.commit();
                return id;

            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void revertirMovimiento(int movimientoId, Integer facturaPendiente) throws SQLException {
        try (Connection c = MySQLConexion.getConexion()) {
            c.setAutoCommit(false);

            try {
                MovimientoCuenta m = null;
                try (PreparedStatement ps = c.prepareStatement("SELECT * FROM movimiento_cuenta WHERE id = ?")) {
                    ps.setInt(1, movimientoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            m = mapearMovimiento(rs);
                        }
                    }
                }

                if (m != null) {
                    try (PreparedStatement ps = c.prepareStatement("DELETE FROM movimiento_cuenta WHERE id = ?")) {
                        ps.setInt(1, movimientoId);
                        ps.executeUpdate();
                    }
                    BigDecimal efecto = "DEBITO".equals(m.getTipo()) ? m.getImporte() : m.getImporte().negate();
                    sumarAlSaldo(c, m.getCuentaId(), efecto.negate());
                }

                if (facturaPendiente != null) {
                    FacturaRepository.cambiarEstado(c, facturaPendiente, "PENDIENTE");
                }

                c.commit();

            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public List<MovimientoCuenta> listarMovimientos(int cuentaId) throws SQLException {
        String sql = """
                SELECT * FROM movimiento_cuenta WHERE cuenta_id = ? ORDER BY fecha, id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, cuentaId);

            List<MovimientoCuenta> movimientos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapearMovimiento(rs));
                }
            }
            return movimientos;
        }
    }

    public List<MovimientoCuenta> listarMovimientosEntre(int cuentaId, LocalDateTime desde,
            LocalDateTime hasta) throws SQLException {

        String sql = """
                SELECT * FROM movimiento_cuenta
                WHERE cuenta_id = ? AND fecha >= ? AND fecha < ?
                ORDER BY fecha, id
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, cuentaId);
            ps.setObject(2, desde);
            ps.setObject(3, hasta);

            List<MovimientoCuenta> movimientos = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapearMovimiento(rs));
                }
            }
            return movimientos;
        }
    }

    public BigDecimal saldoAntesDe(int cuentaId, LocalDateTime fecha) throws SQLException {
        String sql = """
                SELECT tipo, SUM(importe) AS total
                FROM movimiento_cuenta
                WHERE cuenta_id = ? AND fecha < ?
                GROUP BY tipo
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, cuentaId);
            ps.setObject(2, fecha);

            BigDecimal debitos = BigDecimal.ZERO;
            BigDecimal creditos = BigDecimal.ZERO;

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if ("DEBITO".equals(rs.getString("tipo"))) {
                        debitos = rs.getBigDecimal("total");
                    } else {
                        creditos = rs.getBigDecimal("total");
                    }
                }
            }
            return debitos.subtract(creditos);
        }
    }

    static int insertarMovimiento(Connection c, MovimientoCuenta m) throws SQLException {
        String sql = """
                INSERT INTO movimiento_cuenta (cuenta_id, fecha, tipo, importe, concepto, factura_id, pago_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        int id;
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, m.getCuentaId());
            ps.setObject(2, m.getFecha());
            ps.setString(3, m.getTipo());
            ps.setObject(4, m.getImporte());
            ps.setString(5, m.getConcepto());
            ps.setObject(6, m.getFacturaId());
            ps.setString(7, m.getPagoId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getInt(1);
            }
        }

        BigDecimal efecto = "DEBITO".equals(m.getTipo()) ? m.getImporte() : m.getImporte().negate();
        sumarAlSaldo(c, m.getCuentaId(), efecto);
        return id;
    }

    private static void sumarAlSaldo(Connection c, int cuentaId, BigDecimal importe) throws SQLException {
        String sql = """
                UPDATE cuenta_corriente SET saldo = saldo + ?, fecha_actualizacion = ? WHERE id = ?
                """;

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, importe);
            ps.setObject(2, LocalDateTime.now());
            ps.setInt(3, cuentaId);
            ps.executeUpdate();
        }
    }

}
