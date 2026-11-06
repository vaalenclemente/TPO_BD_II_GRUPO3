package com.logistica.cli;

import com.logistica.modelo.Reporte;
import com.logistica.modelo.TipoReporte;
import com.logistica.servicio.ReporteService;
import com.logistica.servicio.TipoReporteService;

public class MenuReportes {

    private final ReporteService reportes = new ReporteService();
    private final TipoReporteService tipos = new TipoReporteService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Reportes facturables",
                    "Solicitar un reporte", "Reportes de un cliente", "Ver un reporte",
                    "Tipos de reporte (catalogo)", "Alta de tipo de reporte", "Modificar tipo de reporte",
                    "Activar tipo de reporte", "Desactivar tipo de reporte");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(this::solicitar);
                case 2 -> Consola.ejecutar(() -> Consola.listar(reportes.listarPorParte(Selector.parte("CLIENTE"))));
                case 3 -> Consola.ejecutar(() -> {
                    Reporte r = reportes.buscarPorId(Consola.texto("Id del reporte"));
                    if (r == null) {
                        throw new IllegalArgumentException("No existe ese reporte.");
                    }
                    Consola.mostrar(r.toString());
                    Consola.mostrar(r.getContenido() == null ? "Error: " + r.getError() : r.getContenido());
                });
                case 4 -> Consola.ejecutar(() -> {
                    Consola.listar(tipos.listar());
                    Consola.mostrar("Codigos que el sistema sabe generar: " + ReporteService.CODIGOS);
                });
                case 5 -> Consola.ejecutar(() -> {
                    int id = tipos.alta(new TipoReporte(Consola.texto("Codigo"), Consola.texto("Nombre"),
                            Consola.decimal("Precio")));
                    Consola.mostrar("Tipo de reporte creado con id " + id + ".");
                });
                case 6 -> Consola.ejecutar(() -> {
                    TipoReporte t = tipos.buscarPorId(elegirTipo());
                    if (t == null) {
                        throw new IllegalArgumentException("No existe ese tipo de reporte.");
                    }
                    t.setNombre(Consola.texto("Nombre", t.getNombre()));
                    t.setPrecio(Consola.decimal("Precio", t.getPrecio()));
                    tipos.modificar(t);
                    Consola.mostrar("Modificado.");
                });
                case 7 -> Consola.ejecutar(() -> {
                    tipos.activar(elegirTipo());
                    Consola.mostrar("Activado.");
                });
                case 8 -> Consola.ejecutar(() -> {
                    tipos.desactivar(elegirTipo());
                    Consola.mostrar("Desactivado.");
                });
            }
        }
    }

    private void solicitar() throws Exception {
        int clienteId = Selector.parte("CLIENTE");
        String solicitante = Consola.texto("Nombre de quien lo pide");
        Consola.listar(tipos.listarActivos());
        int tipoId = Consola.entero("Id del tipo de reporte");
        var desde = Consola.fecha("Desde");
        var hasta = Consola.fecha("Hasta");

        Reporte r = reportes.solicitar(new Reporte(clienteId, solicitante, tipoId, desde, hasta));
        Consola.mostrar("Reporte " + r.getId() + " - " + r.getEstado() + " - costo $" + r.getCosto());
        Consola.mostrar(r.getContenido() == null ? "Error: " + r.getError() : r.getContenido());
    }

    private int elegirTipo() throws Exception {
        Consola.listar(tipos.listar());
        return Consola.entero("Id del tipo de reporte");
    }

}
