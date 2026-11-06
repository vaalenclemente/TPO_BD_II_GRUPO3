package com.logistica.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.logistica.modelo.CuentaCorriente;
import com.logistica.modelo.Envio;
import com.logistica.modelo.Factura;
import com.logistica.modelo.FacturaDetalle;
import com.logistica.modelo.MovimientoCuenta;
import com.logistica.modelo.Parte;
import com.logistica.modelo.Reporte;
import com.logistica.modelo.Tramo;
import com.logistica.modelo.Vehiculo;
import com.logistica.repositorio.EnvioRepository;
import com.logistica.repositorio.FacturaRepository;
import com.logistica.repositorio.ParteRepository;
import com.logistica.repositorio.ReporteRepository;
import com.logistica.repositorio.TramoRepository;
import com.logistica.repositorio.VehiculoRepository;

//   EMITIDA  (a un cliente)            -> DEBITO  en su cuenta (nos debe)
//   RECIBIDA (de proveedor u operador) -> CREDITO en su cuenta (le debemos)
public class FacturaService {

    private static final int DIAS_VENCIMIENTO = 30;

    private static final BigDecimal MARGEN_ENVIO = new BigDecimal("1.30");

    private final FacturaRepository repo = new FacturaRepository();
    private final EnvioRepository envioRepo = new EnvioRepository();
    private final TramoRepository tramoRepo = new TramoRepository();
    private final VehiculoRepository vehiculoRepo = new VehiculoRepository();
    private final ParteRepository parteRepo = new ParteRepository();
    private final CuentaCorrienteService cuentaService = new CuentaCorrienteService();
    private final ReporteRepository reporteRepo = new ReporteRepository();

    // ---------- Facturacion automatica ----------

    public List<Factura> facturacionAutomatica(String periodo) throws SQLException {

        YearMonth mes = validarPeriodo(periodo);
        LocalDateTime desde = mes.atDay(1).atStartOfDay();
        LocalDateTime hasta = mes.plusMonths(1).atDay(1).atStartOfDay();
        LocalDate hoy = LocalDate.now();

        List<Factura> generadas = new ArrayList<>();

        // a) Envios entregados -> una factura EMITIDA por cliente
        Map<Integer, List<FacturaDetalle>> porCliente = new LinkedHashMap<>();
        for (Envio e : envioRepo.listarEntregadosSinFacturar(desde, hasta)) {
            FacturaDetalle d = new FacturaDetalle("Envío N° " + e.getId(), tarifaEnvio(e.getId()));
            d.setEnvioId(e.getId());
            porCliente.computeIfAbsent(e.getClienteId(), k -> new ArrayList<>()).add(d);
        }
        // a2) Reportes GENERADO en el mes (Mongo) -> en la misma factura del cliente.
        //     Lo que manda es MySQL: un reporte está facturado si tiene una línea en factura_detalle (UQ).
        //     reportes.facturaId en Mongo es una copia de referencia que se completa después.
        for (Reporte r : reporteRepo.listarGeneradosEntre(desde, hasta)) {
            if (r.getCosto() == null || r.getCosto().signum() <= 0 || repo.reporteFacturado(r.getId())) {
                continue;
            }
            FacturaDetalle d = new FacturaDetalle("Reporte " + r.getCodigo() + " (" + r.getDesde() + " a "
                    + r.getHasta() + ")", r.getCosto());
            d.setReporteId(r.getId());
            porCliente.computeIfAbsent(r.getParteId(), k -> new ArrayList<>()).add(d);
        }

        for (Map.Entry<Integer, List<FacturaDetalle>> item : porCliente.entrySet()) {
            String numero = String.format("FE-%08d", repo.contarPorTipo("EMITIDA") + 1);
            Factura f = emitir(item.getKey(), "EMITIDA", numero, mes.toString(), hoy, item.getValue());
            generadas.add(f);
            marcarReportes(item.getValue(), f.getId());
        }

        // b) Tramos completados -> una factura RECIBIDA por operador
        Map<Integer, List<FacturaDetalle>> porOperador = new LinkedHashMap<>();
        for (Tramo t : tramoRepo.listarCompletadosSinFacturar(desde, hasta)) {
            Vehiculo v = vehiculoRepo.buscarPorId(t.getVehiculoId());
            FacturaDetalle d = new FacturaDetalle("Tramo " + t.getOrden() + " del envío N° " + t.getEnvioId()
                    + " (" + t.getMedioTransporte() + ")", t.getCosto());
            d.setTramoId(t.getId());
            porOperador.computeIfAbsent(v.getOperadorId(), k -> new ArrayList<>()).add(d);
        }
        for (Map.Entry<Integer, List<FacturaDetalle>> item : porOperador.entrySet()) {
            String numero = String.format("FR-%08d", repo.contarPorTipo("RECIBIDA") + 1);
            generadas.add(emitir(item.getKey(), "RECIBIDA", numero, mes.toString(), hoy, item.getValue()));
        }

        return generadas;
    }

    // ---------- Factura de un proveedor (carga manual) ----------
    public Factura registrarFacturaProveedor(int proveedorId, String numeroProveedor,
            LocalDate fechaEmision, List<FacturaDetalle> lineas) throws SQLException {

        Parte p = parteRepo.buscarPorId(proveedorId);
        if (p == null || !"PROVEEDOR".equals(p.getTipo())) {
            throw new IllegalArgumentException("La parte elegida no es un proveedor.");
        }
        if (numeroProveedor == null || numeroProveedor.isBlank()) {
            throw new IllegalArgumentException("El número de la factura del proveedor es obligatorio.");
        }
        if (fechaEmision == null || fechaEmision.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de emisión es obligatoria y no puede ser futura.");
        }
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("La factura tiene que tener al menos una línea.");
        }
        for (FacturaDetalle d : lineas) {
            if (d.getConcepto() == null || d.getConcepto().isBlank()) {
                throw new IllegalArgumentException("Cada línea tiene que tener un concepto.");
            }
            if (d.getImporte() == null || d.getImporte().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("El importe de cada línea tiene que ser mayor a 0.");
            }
            d.setEnvioId(null);
            d.setTramoId(null);
            d.setReporteId(null);
        }

        String numero = "FP-" + proveedorId + "-" + numeroProveedor.trim();
        if (repo.buscarPorNumero(numero) != null) {
            throw new IllegalArgumentException("Esa factura del proveedor ya está cargada.");
        }

        return emitir(proveedorId, "RECIBIDA", numero, YearMonth.from(fechaEmision).toString(),
                fechaEmision, lineas);
    }

    public void anular(int facturaId) throws SQLException {

        Factura f = repo.buscarPorId(facturaId);
        if (f == null) {
            throw new IllegalArgumentException("No existe una factura con id " + facturaId + ".");
        }
        if (!"PENDIENTE".equals(f.getEstado())) {
            throw new IllegalArgumentException("Solo se anula una factura PENDIENTE (esta está "
                    + f.getEstado() + ").");
        }

        List<FacturaDetalle> lineas = repo.listarDetalle(facturaId);
        CuentaCorriente cuenta = cuentaService.obtenerCuenta(f.getParteId());

        String tipoContrario = "EMITIDA".equals(f.getTipo()) ? "CREDITO" : "DEBITO";
        MovimientoCuenta contra = new MovimientoCuenta(cuenta.getId(), tipoContrario, f.getImporteTotal(),
                "Anulación de la factura " + f.getNumero(), f.getId(), null);
        contra.setFecha(LocalDateTime.now());

        repo.anular(facturaId, contra);

        // Los reportes de la factura quedan otra vez sin facturar (también la copia en Mongo).
        marcarReportes(lineas, null);
    }

    public Factura buscarPorId(int id) throws SQLException {
        return repo.buscarPorId(id);
    }

    public List<FacturaDetalle> listarDetalle(int facturaId) throws SQLException {
        return repo.listarDetalle(facturaId);
    }

    public List<Factura> listarPorParte(int parteId) throws SQLException {
        return repo.listarPorParte(parteId);
    }

    public List<Factura> listarPendientes(String tipo) throws SQLException {
        return repo.listarPorTipoYEstado(tipo, "PENDIENTE");
    }

    private Factura emitir(int parteId, String tipo, String numero, String periodo, LocalDate fechaEmision,
            List<FacturaDetalle> lineas) throws SQLException {

        BigDecimal total = BigDecimal.ZERO;
        for (FacturaDetalle d : lineas) {
            total = total.add(d.getImporte());
        }

        Factura f = new Factura(parteId, tipo, periodo);
        f.setNumero(numero);
        f.setFechaEmision(fechaEmision);
        f.setFechaVencimiento(fechaEmision.plusDays(DIAS_VENCIMIENTO));
        f.setImporteTotal(total);
        f.setEstado("PENDIENTE");

        CuentaCorriente cuenta = cuentaService.obtenerCuenta(parteId);
        String tipoMovimiento = "EMITIDA".equals(tipo) ? "DEBITO" : "CREDITO";
        MovimientoCuenta m = new MovimientoCuenta(cuenta.getId(), tipoMovimiento, total,
                "Factura " + numero, null, null);
        m.setFecha(LocalDateTime.now());

        int id = repo.guardar(f, lineas, m);
        f.setId(id);
        return f;
    }

    // Copia en Mongo (reportes.facturaId). Si falla no se deshace la factura: MySQL es la fuente
    // de verdad y la copia se puede volver a completar.
    private void marcarReportes(List<FacturaDetalle> lineas, Integer facturaId) {
        for (FacturaDetalle d : lineas) {
            if (d.getReporteId() != null) {
                try {
                    reporteRepo.asignarFactura(d.getReporteId(), facturaId);
                } catch (Exception e) {
                    System.out.println("Aviso: no se pudo actualizar el reporte " + d.getReporteId()
                            + " en Mongo: " + e.getMessage());
                }
            }
        }
    }

    private BigDecimal tarifaEnvio(int envioId) throws SQLException {
        BigDecimal costo = BigDecimal.ZERO;
        for (Tramo t : tramoRepo.listarPorEnvio(envioId)) {
            if ("COMPLETADO".equals(t.getEstado())) {
                costo = costo.add(t.getCosto());
            }
        }
        return costo.multiply(MARGEN_ENVIO).setScale(2, RoundingMode.HALF_UP);
    }

    private YearMonth validarPeriodo(String periodo) {
        try {
            return YearMonth.parse(periodo);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("El período tiene que tener el formato AAAA-MM (ej. 2026-10).");
        }
    }

}
