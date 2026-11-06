package com.logistica.repositorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.logistica.database.MySQLConexion;
import com.logistica.modelo.DesempenioMensual;
import com.logistica.modelo.EnvioDemorado;
import com.logistica.modelo.ResultadoAgrupado;

public class EstadisticaRepository {

    private static final String POR_CREACION =
            " e.fecha_creacion >= ? AND e.fecha_creacion < ? AND (? IS NULL OR e.cliente_id = ?)\n";

    private static final String POR_ENTREGA = " e.estado = 'ENTREGADO' AND e.fecha_salida IS NOT NULL"
            + " AND e.fecha_entrega_real >= ? AND e.fecha_entrega_real < ? AND (? IS NULL OR e.cliente_id = ?)\n";

    private static final String HORAS_DE_ENTREGA = "TIMESTAMPDIFF(MINUTE, e.fecha_salida, e.fecha_entrega_real) / 60";

    public List<ResultadoAgrupado> enviosPorPais(Integer clienteId, LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {
        String sql = """
                SELECT p.nombre AS etiqueta, COUNT(*) AS cantidad, SUM(e.peso_kg) AS total,
                       NULL AS promedio, NULL AS minimo, NULL AS maximo
                FROM envio e
                JOIN ciudad c ON c.id = e.destino_ciudad_id
                JOIN pais p ON p.id = c.pais_id
                WHERE e.estado <> 'CANCELADO' AND """ + POR_CREACION + """
                GROUP BY p.nombre
                ORDER BY cantidad DESC, etiqueta
                """;
        return agrupados(sql, desde, hasta, clienteId, clienteId);
    }

    public List<ResultadoAgrupado> enviosPorEstado(Integer clienteId, LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {
        String sql = """
                SELECT e.estado AS etiqueta, COUNT(*) AS cantidad, SUM(e.peso_kg) AS total,
                       NULL AS promedio, NULL AS minimo, NULL AS maximo
                FROM envio e
                WHERE """ + POR_CREACION + """
                GROUP BY e.estado
                ORDER BY cantidad DESC, etiqueta
                """;
        return agrupados(sql, desde, hasta, clienteId, clienteId);
    }

    public List<ResultadoAgrupado> enviosPorCliente(LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String sql = """
                SELECT pa.razon_social AS etiqueta, COUNT(*) AS cantidad, SUM(e.peso_kg) AS total,
                       NULL AS promedio, NULL AS minimo, NULL AS maximo
                FROM envio e
                JOIN parte pa ON pa.id = e.cliente_id
                WHERE e.estado <> 'CANCELADO' AND """ + POR_CREACION + """
                GROUP BY pa.id, pa.razon_social
                ORDER BY cantidad DESC, etiqueta
                """;
        return agrupados(sql, desde, hasta, null, null);
    }

    public List<ResultadoAgrupado> rankingClientesPorVolumen(LocalDateTime desde, LocalDateTime hasta, int limite)
            throws SQLException {
        String sql = """
                SELECT pa.razon_social AS etiqueta, COUNT(*) AS cantidad, SUM(e.peso_kg) AS total,
                       AVG(e.peso_kg) AS promedio, NULL AS minimo, NULL AS maximo
                FROM envio e
                JOIN parte pa ON pa.id = e.cliente_id
                WHERE e.estado <> 'CANCELADO' AND """ + POR_CREACION + """
                GROUP BY pa.id, pa.razon_social
                ORDER BY total DESC, etiqueta
                LIMIT ?
                """;
        return agrupados(sql, desde, hasta, null, null, limite);
    }

    public List<EnvioDemorado> enviosDemorados(Integer clienteId, LocalDateTime desde, LocalDateTime hasta,
            LocalDateTime ahora) throws SQLException {
        String sql = """
                SELECT e.id, pa.razon_social, c.nombre AS destino, e.estado,
                       e.fecha_entrega_estimada, e.fecha_entrega_real
                FROM envio e
                JOIN parte pa ON pa.id = e.cliente_id
                JOIN ciudad c ON c.id = e.destino_ciudad_id
                WHERE e.estado <> 'CANCELADO'
                  AND (   (e.fecha_entrega_real IS NOT NULL AND e.fecha_entrega_real > e.fecha_entrega_estimada)
                       OR (e.fecha_entrega_real IS NULL AND e.fecha_entrega_estimada < ?) )
                  AND""" + POR_CREACION;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            completar(ps, ahora, desde, hasta, clienteId, clienteId);

            List<EnvioDemorado> demorados = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EnvioDemorado d = new EnvioDemorado();
                    d.setEnvioId(rs.getInt("id"));
                    d.setCliente(rs.getString("razon_social"));
                    d.setDestino(rs.getString("destino"));
                    d.setEstado(rs.getString("estado"));
                    LocalDateTime estimada = rs.getObject("fecha_entrega_estimada", LocalDateTime.class);
                    LocalDateTime real = rs.getObject("fecha_entrega_real", LocalDateTime.class);
                    d.setFechaEntregaEstimada(estimada);
                    d.setFechaEntregaReal(real);

                    LocalDateTime finDemora = ahora;
                    if (real != null) {
                        finDemora = real;
                    }
                    d.setHorasDemora(Duration.between(estimada, finDemora).toHours());

                    demorados.add(d);
                }
            }
            demorados.sort((a, b) -> b.getHorasDemora().compareTo(a.getHorasDemora()));
            return demorados;
        }
    }

    public List<ResultadoAgrupado> tiemposEntregaPorCliente(Integer clienteId, LocalDateTime desde,
            LocalDateTime hasta) throws SQLException {
        String sql = """
                SELECT pa.razon_social AS etiqueta, COUNT(*) AS cantidad, NULL AS total,
                       AVG(""" + HORAS_DE_ENTREGA + ") AS promedio, MIN(" + HORAS_DE_ENTREGA + ") AS minimo, MAX("
                + HORAS_DE_ENTREGA + """
                ) AS maximo
                FROM envio e
                JOIN parte pa ON pa.id = e.cliente_id
                WHERE """ + POR_ENTREGA + """
                GROUP BY pa.id, pa.razon_social
                ORDER BY etiqueta
                """;
        return agrupados(sql, desde, hasta, clienteId, clienteId);
    }

    public ResultadoAgrupado tiempoPromedioEntrega(Integer clienteId, LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {
        String sql = """
                SELECT 'TOTAL' AS etiqueta, COUNT(*) AS cantidad, NULL AS total,
                       AVG(""" + HORAS_DE_ENTREGA + ") AS promedio, MIN(" + HORAS_DE_ENTREGA + ") AS minimo, MAX("
                + HORAS_DE_ENTREGA + """
                ) AS maximo
                FROM envio e
                WHERE """ + POR_ENTREGA;
        return agrupados(sql, desde, hasta, clienteId, clienteId).get(0);
    }

    public List<ResultadoAgrupado> promedioIncidentesPorPais(Integer clienteId, LocalDateTime desde,
            LocalDateTime hasta) throws SQLException {
        String sql = """
                SELECT p.nombre AS etiqueta, COUNT(*) AS cantidad, SUM(e.cantidad_incidentes) AS total,
                       AVG(e.cantidad_incidentes) AS promedio, NULL AS minimo, MAX(e.cantidad_incidentes) AS maximo
                FROM envio e
                JOIN ciudad c ON c.id = e.destino_ciudad_id
                JOIN pais p ON p.id = c.pais_id
                WHERE e.estado <> 'CANCELADO' AND """ + POR_CREACION + """
                GROUP BY p.nombre
                ORDER BY promedio DESC, etiqueta
                """;
        return agrupados(sql, desde, hasta, clienteId, clienteId);
    }

    public List<ResultadoAgrupado> promedioIncidentesPorEstado(Integer clienteId, LocalDateTime desde,
            LocalDateTime hasta) throws SQLException {
        String sql = """
                SELECT e.estado AS etiqueta, COUNT(*) AS cantidad, SUM(e.cantidad_incidentes) AS total,
                       AVG(e.cantidad_incidentes) AS promedio, NULL AS minimo, MAX(e.cantidad_incidentes) AS maximo
                FROM envio e
                WHERE """ + POR_CREACION + """
                GROUP BY e.estado
                ORDER BY promedio DESC, etiqueta
                """;
        return agrupados(sql, desde, hasta, clienteId, clienteId);
    }

    public List<ResultadoAgrupado> promedioIncidentesPorCliente(LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {
        String sql = """
                SELECT pa.razon_social AS etiqueta, COUNT(*) AS cantidad, SUM(e.cantidad_incidentes) AS total,
                       AVG(e.cantidad_incidentes) AS promedio, NULL AS minimo, MAX(e.cantidad_incidentes) AS maximo
                FROM envio e
                JOIN parte pa ON pa.id = e.cliente_id
                WHERE e.estado <> 'CANCELADO' AND """ + POR_CREACION + """
                GROUP BY pa.id, pa.razon_social
                ORDER BY promedio DESC, etiqueta
                """;
        return agrupados(sql, desde, hasta, null, null);
    }

    public List<ResultadoAgrupado> costosPorTransporte(Integer clienteId, LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {
        String sql = """
                SELECT t.medio_transporte AS etiqueta, COUNT(*) AS cantidad, SUM(t.costo) AS total,
                       AVG(t.costo) AS promedio, MIN(t.costo) AS minimo, MAX(t.costo) AS maximo
                FROM tramo t
                JOIN envio e ON e.id = t.envio_id
                WHERE t.estado = 'COMPLETADO'
                  AND t.fecha_llegada_real >= ? AND t.fecha_llegada_real < ? AND (? IS NULL OR e.cliente_id = ?)
                GROUP BY t.medio_transporte
                ORDER BY total DESC
                """;
        return agrupados(sql, desde, hasta, clienteId, clienteId);
    }

    public List<DesempenioMensual> desempenioMensual(Integer clienteId, LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {
        String sql = """
                SELECT DATE_FORMAT(e.fecha_entrega_real, '%Y-%m') AS periodo, COUNT(*) AS entregados,
                       SUM(CASE WHEN e.fecha_entrega_real <= e.fecha_entrega_estimada THEN 1 ELSE 0 END) AS a_tiempo,
                       AVG(""" + HORAS_DE_ENTREGA + """
                ) AS tiempo_promedio,
                       AVG(CASE WHEN e.fecha_entrega_real > e.fecha_entrega_estimada
                           THEN TIMESTAMPDIFF(MINUTE, e.fecha_entrega_estimada, e.fecha_entrega_real) / 60 END) AS demora_promedio
                FROM envio e
                WHERE """ + POR_ENTREGA + """
                GROUP BY periodo
                ORDER BY periodo
                """;

        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            completar(ps, desde, hasta, clienteId, clienteId);

            List<DesempenioMensual> meses = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DesempenioMensual d = new DesempenioMensual();
                    d.setPeriodo(rs.getString("periodo"));
                    d.setEntregados(rs.getInt("entregados"));
                    d.setATiempo(rs.getInt("a_tiempo"));
                    d.setTiempoPromedioHs(redondear(rs.getBigDecimal("tiempo_promedio")));
                    d.setDemoraPromedioHs(redondear(rs.getBigDecimal("demora_promedio")));
                    meses.add(d);
                }
            }
            return meses;
        }
    }

    private List<ResultadoAgrupado> agrupados(String sql, Object... parametros) throws SQLException {
        try (Connection c = MySQLConexion.getConexion();
                PreparedStatement ps = c.prepareStatement(sql)) {

            completar(ps, parametros);

            List<ResultadoAgrupado> filas = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ResultadoAgrupado r = new ResultadoAgrupado();
                    r.setEtiqueta(rs.getString("etiqueta"));
                    r.setCantidad(rs.getInt("cantidad"));
                    r.setTotal(redondear(rs.getBigDecimal("total")));
                    r.setPromedio(redondear(rs.getBigDecimal("promedio")));
                    r.setMinimo(redondear(rs.getBigDecimal("minimo")));
                    r.setMaximo(redondear(rs.getBigDecimal("maximo")));
                    filas.add(r);
                }
            }
            return filas;
        }
    }

    private void completar(PreparedStatement ps, Object... parametros) throws SQLException {
        for (int i = 0; i < parametros.length; i++) {
            ps.setObject(i + 1, parametros[i]);
        }
    }

    private BigDecimal redondear(BigDecimal valor) {
        return (valor == null) ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }

}
