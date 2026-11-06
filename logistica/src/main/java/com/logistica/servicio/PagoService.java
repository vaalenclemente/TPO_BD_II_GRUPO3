package com.logistica.servicio;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.logistica.modelo.CuentaCorriente;
import com.logistica.modelo.Factura;
import com.logistica.modelo.MovimientoCuenta;
import com.logistica.modelo.Pago;
import com.logistica.repositorio.CuentaCorrienteRepository;
import com.logistica.repositorio.FacturaRepository;
import com.logistica.repositorio.PagoRepository;

public class PagoService {

    // pagos en mongo, facturas y cuenta corriente en mysql
    private final PagoRepository repo = new PagoRepository();
    private final FacturaRepository facturaRepo = new FacturaRepository();
    private final CuentaCorrienteRepository cuentaRepo = new CuentaCorrienteRepository();
    private final CuentaCorrienteService cuentaService = new CuentaCorrienteService();

    public String registrar(int facturaId, String medioPago) throws SQLException {

        // a) valida sila factura existe y esta pendiente
        Factura f = facturaRepo.buscarPorId(facturaId);
        if (f == null) {
            throw new IllegalArgumentException("No existe una factura con id " + facturaId + ".");
        }
        if (!"PENDIENTE".equals(f.getEstado())) {
            throw new IllegalArgumentException(
                    "La factura esta " + f.getEstado() + ": no se le puede registrar un pago.");
        }
        // el medio de pago tiene que ser valido
        if (!"TRANSFERENCIA".equals(medioPago) && !"EFECTIVO".equals(medioPago) && !"CHEQUE".equals(medioPago)
                && !"TARJETA".equals(medioPago)) {
            throw new IllegalArgumentException(
                    "El medio de pago no es correcto (TRANSFERENCIA, EFECTIVO, CHEQUE, TARJETA).");
        }
        // una factura se paga con un pago
        for (Pago otro : repo.listarPorFactura(facturaId)) {
            if ("PENDIENTE".equals(otro.getEstado()) || "CONFIRMADO".equals(otro.getEstado())) {
                throw new IllegalArgumentException("La factura ya tiene un pago " + otro.getEstado() + ".");
            }
        }

        // seteo valores del sist
        LocalDateTime ahora = LocalDateTime.now();
        Pago p = new Pago(facturaId, medioPago);
        p.setTipo("EMITIDA".equals(f.getTipo()) ? "COBRO" : "PAGO");
        p.setParteId(f.getParteId());
        p.setMonto(f.getImporteTotal());
        p.setFecha(ahora);
        p.setEstado("PENDIENTE");
        p.getHistorialEstados().add(new Pago.CambioEstado("PENDIENTE", ahora, UsuarioActual.get()));

        // guardo en mongo
        String id = repo.guardar(p);
        p.setId(id);
        return id;
    }

    public void confirmar(String pagoId) throws SQLException {

        // el pago tiene que estar pendiente y la factura todavia sin pagar
        Pago p = buscarConEstado(pagoId, "PENDIENTE");
        Factura f = facturaRepo.buscarPorId(p.getFacturaId());
        if (!"PENDIENTE".equals(f.getEstado())) {
            throw new IllegalArgumentException("La factura esta " + f.getEstado() + ".");
        }

        // armo movimiento antes de empezar la saa
        CuentaCorriente cuenta = cuentaService.obtenerCuenta(p.getParteId());
        String tipoMovimiento = "COBRO".equals(p.getTipo()) ? "CREDITO" : "DEBITO";
        MovimientoCuenta m = new MovimientoCuenta(cuenta.getId(), tipoMovimiento, p.getMonto(),
                ("COBRO".equals(p.getTipo()) ? "Cobro" : "Pago") + " de la factura " + f.getNumero()
                        + " (" + p.getMedioPago() + ")",
                f.getId(), pagoId);
        m.setFecha(LocalDateTime.now());

        Saga saga = new Saga("CONFIRMAR_PAGO", "pago");
        try {
            saga.objeto(pagoId);

            // paso 1 mongo
            repo.cambiarEstado(pagoId, new Pago.CambioEstado("CONFIRMADO", LocalDateTime.now(), UsuarioActual.get()));
            saga.paso("MONGO_PAGO_CONFIRMADO", () -> repo.deshacerCambio(pagoId, "PENDIENTE"));

            // paso 2 mysql, transaccion con movimiento + saldo + factura pagada.
            int movimientoId = cuentaRepo.registrarMovimiento(m, f.getId(), "PAGADA");
            saga.paso("MYSQL_MOVIMIENTO_" + tipoMovimiento,
                    () -> cuentaRepo.revertirMovimiento(movimientoId, f.getId()));

            // paso 3 mongo
            repo.asignarMovimiento(pagoId, movimientoId);
            saga.paso("MONGO_MOVIMIENTO_ASIGNADO", null);

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }
    }

    public void rechazar(String pagoId) {
        buscarConEstado(pagoId, "PENDIENTE");
        repo.cambiarEstado(pagoId, new Pago.CambioEstado("RECHAZADO", LocalDateTime.now(), UsuarioActual.get()));
    }

    public void anular(String pagoId) throws SQLException {

        // el pago tiene que estar confirmado y la factura pagada
        Pago p = buscarConEstado(pagoId, "CONFIRMADO");
        Factura f = facturaRepo.buscarPorId(p.getFacturaId());
        if (!"PAGADA".equals(f.getEstado())) {
            throw new IllegalArgumentException("La factura esta " + f.getEstado() + ".");
        }

        CuentaCorriente cuenta = cuentaService.obtenerCuenta(p.getParteId());
        String tipoContrario = "COBRO".equals(p.getTipo()) ? "DEBITO" : "CREDITO";
        MovimientoCuenta contra = new MovimientoCuenta(cuenta.getId(), tipoContrario, p.getMonto(),
                "Anulacion del " + ("COBRO".equals(p.getTipo()) ? "cobro" : "pago") + " de la factura " + f.getNumero(),
                f.getId(), pagoId);
        contra.setFecha(LocalDateTime.now());

        Saga saga = new Saga("ANULAR_PAGO", "pago");
        try {
            saga.objeto(pagoId);

            // paso 1 mongo
            repo.cambiarEstado(pagoId, new Pago.CambioEstado("ANULADO", LocalDateTime.now(), UsuarioActual.get()));
            saga.paso("MONGO_PAGO_ANULADO", () -> repo.deshacerCambio(pagoId, "CONFIRMADO"));

            // paso 2 mysql
            int movimientoId = cuentaRepo.registrarMovimiento(contra, f.getId(), "PENDIENTE");
            saga.paso("MYSQL_CONTRAMOVIMIENTO_" + tipoContrario, () -> {
                cuentaRepo.revertirMovimiento(movimientoId, null);
                facturaRepo.cambiarEstado(f.getId(), "PAGADA");
            });

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }
    }

    // consultas mongo
    public Pago buscarPorId(String id) {
        return repo.buscarPorId(id);
    }

    public List<Pago> listarPorFactura(int facturaId) {
        return repo.listarPorFactura(facturaId);
    }

    public List<Pago> listarPorParte(int parteId) {
        return repo.listarPorParte(parteId);
    }

    public List<Pago> listarPendientes() {
        return repo.listarPorEstado("PENDIENTE");
    }

    // busca el pago y controla que este en el estado
    private Pago buscarConEstado(String pagoId, String estado) {
        Pago p = repo.buscarPorId(pagoId);
        if (p == null) {
            throw new IllegalArgumentException("No existe un pago con id " + pagoId + ".");
        }
        if (!estado.equals(p.getEstado())) {
            throw new IllegalArgumentException("El pago tiene que estar " + estado + " (esta " + p.getEstado() + ").");
        }
        return p;
    }

}
