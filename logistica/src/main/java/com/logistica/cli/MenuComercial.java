package com.logistica.cli;

import java.util.ArrayList;
import java.util.List;

import com.logistica.modelo.Factura;
import com.logistica.modelo.FacturaDetalle;
import com.logistica.servicio.CierreCuentaService;
import com.logistica.servicio.CuentaCorrienteService;
import com.logistica.servicio.FacturaService;
import com.logistica.servicio.PagoService;

public class MenuComercial {

    private final CuentaCorrienteService cuentas = new CuentaCorrienteService();
    private final FacturaService facturas = new FacturaService();
    private final PagoService pagos = new PagoService();
    private final CierreCuentaService cierres = new CierreCuentaService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Comercial",
                    "Cuentas corrientes", "Movimientos de una parte", "Facturacion automatica del mes",
                    "Cargar factura de un proveedor", "Facturas de una parte", "Facturas pendientes",
                    "Detalle de una factura", "Anular factura", "Registrar cobro / pago de una factura",
                    "Confirmar cobro / pago", "Rechazar cobro / pago", "Anular cobro / pago confirmado",
                    "Cobros / pagos pendientes", "Cierre mensual de cuentas", "Cierres de una parte");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> Consola.listar(cuentas.listar()));
                case 2 -> Consola.ejecutar(() -> {
                    int parteId = Selector.cualquierParte();
                    Consola.mostrar(cuentas.obtenerCuenta(parteId).toString());
                    Consola.listar(cuentas.listarMovimientos(parteId));
                });
                case 3 -> Consola.ejecutar(() -> {
                    List<Factura> nuevas = facturas.facturacionAutomatica(Selector.periodo());
                    Consola.mostrar(nuevas.size() + " facturas generadas.");
                    Consola.listar(nuevas);
                });
                case 4 -> Consola.ejecutar(this::facturaProveedor);
                case 5 -> Consola.ejecutar(() -> Consola.listar(facturas.listarPorParte(Selector.cualquierParte())));
                case 6 -> Consola.ejecutar(() -> Consola.listar(facturas.listarPendientes(
                        Consola.elegir("Tipo", "EMITIDA", "RECIBIDA"))));
                case 7 -> Consola.ejecutar(() -> {
                    Factura f = facturas.buscarPorId(Consola.entero("Id de la factura"));
                    if (f == null) {
                        throw new IllegalArgumentException("No existe esa factura.");
                    }
                    Consola.mostrar(f.toString());
                    Consola.listar(facturas.listarDetalle(f.getId()));
                    Consola.mostrar("-- Cobros / pagos");
                    Consola.listar(pagos.listarPorFactura(f.getId()));
                });
                case 8 -> Consola.ejecutar(() -> {
                    int id = Consola.entero("Id de la factura");
                    if (Consola.confirmar("¿Seguro que quiere anular la factura " + id + "?")) {
                        facturas.anular(id);
                        Consola.mostrar("Factura anulada.");
                    }
                });
                case 9 -> Consola.ejecutar(() -> {
                    String tipo = Consola.elegir("Tipo de factura", "EMITIDA", "RECIBIDA");
                    Consola.listar(facturas.listarPendientes(tipo));
                    int facturaId = Consola.entero("Id de la factura");
                    String medio = Consola.elegir("Medio de pago", "TRANSFERENCIA", "EFECTIVO", "CHEQUE", "TARJETA");
                    Consola.mostrar("Registrado (PENDIENTE) con id " + pagos.registrar(facturaId, medio) + ".");
                });
                case 10 -> Consola.ejecutar(() -> {
                    pagos.confirmar(elegirPendiente());
                    Consola.mostrar("Confirmado: la factura quedo PAGADA.");
                });
                case 11 -> Consola.ejecutar(() -> {
                    pagos.rechazar(elegirPendiente());
                    Consola.mostrar("Rechazado.");
                });
                case 12 -> Consola.ejecutar(() -> {
                    int facturaId = Consola.entero("Id de la factura");
                    Consola.listar(pagos.listarPorFactura(facturaId));
                    pagos.anular(Consola.texto("Id del cobro / pago"));
                    Consola.mostrar("Anulado: la factura volvio a PENDIENTE.");
                });
                case 13 -> Consola.ejecutar(() -> Consola.listar(pagos.listarPendientes()));
                case 14 -> Consola.ejecutar(() -> {
                    var nuevos = cierres.cerrarMes(Selector.periodo());
                    Consola.mostrar(nuevos.size() + " cuentas cerradas.");
                    Consola.listar(nuevos);
                });
                case 15 -> Consola.ejecutar(() -> Consola.listar(cierres.listarPorParte(Selector.cualquierParte())));
            }
        }
    }

    private void facturaProveedor() throws Exception {
        int proveedorId = Selector.parte("PROVEEDOR");
        String numero = Consola.texto("Numero de la factura del proveedor");
        var fecha = Consola.fecha("Fecha de emision");
        List<FacturaDetalle> lineas = new ArrayList<>();
        do {
            lineas.add(new FacturaDetalle(Consola.texto("Concepto"), Consola.decimal("Importe")));
        } while (Consola.confirmar("¿Otra linea?"));

        Factura f = facturas.registrarFacturaProveedor(proveedorId, numero, fecha, lineas);
        Consola.mostrar("Factura cargada: " + f);
    }

    private String elegirPendiente() {
        Consola.listar(pagos.listarPendientes());
        return Consola.texto("Id del cobro / pago");
    }

}
