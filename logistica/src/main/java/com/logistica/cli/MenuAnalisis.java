package com.logistica.cli;

import java.time.LocalDate;

import com.logistica.servicio.EstadisticaService;
import com.logistica.servicio.IncidenteService;
import com.logistica.servicio.PrediccionRiesgoService;

public class MenuAnalisis {

    private final EstadisticaService estadisticas = new EstadisticaService();
    private final IncidenteService incidentes = new IncidenteService();
    private final PrediccionRiesgoService predicciones = new PrediccionRiesgoService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Analisis y prediccion (consultas internas, no se facturan)",
                    "Envios por pais de destino", "Envios por estado", "Envios por cliente",
                    "Ranking de clientes por volumen transportado", "Envios demorados",
                    "Tiempos de entrega (min / max / promedio) por cliente", "Promedio de incidentes",
                    "Costos por tipo de transporte", "Desempeño historico mensual",
                    "Ranking de rutas con mas incidentes", "Calcular prediccion diaria de atrasos",
                    "Ultimas predicciones");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    Vista.listar(estadisticas.enviosPorPais(cliente, desde(), hasta()));
                });
                case 2 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    Vista.listar(estadisticas.enviosPorEstado(cliente, desde(), hasta()));
                });
                case 3 -> Consola.ejecutar(() -> Vista.listar(estadisticas.enviosPorCliente(desde(), hasta())));
                case 4 -> Consola.ejecutar(() -> {
                    LocalDate desde = desde();
                    LocalDate hasta = hasta();
                    Vista.listar(estadisticas.rankingClientesPorVolumen(desde, hasta, Consola.entero("¿Cuantos?")));
                });
                case 5 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    Vista.listar(estadisticas.enviosDemorados(cliente, desde(), hasta()));
                });
                case 6 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    LocalDate desde = desde();
                    LocalDate hasta = hasta();
                    Vista.listar(estadisticas.tiemposEntregaPorCliente(cliente, desde, hasta));
                    Consola.mostrar("Total -> " + estadisticas.tiempoPromedioEntrega(cliente, desde, hasta));
                });
                case 7 -> Consola.ejecutar(() -> {
                    String agrupar = Consola.elegir("Agrupar por", "Pais de destino", "Estado del envio", "Cliente");
                    LocalDate desde = desde();
                    LocalDate hasta = hasta();
                    switch (agrupar) {
                        case "Pais de destino" -> Vista.listar(estadisticas.promedioIncidentesPorPais(null, desde, hasta));
                        case "Estado del envio" -> Vista.listar(estadisticas.promedioIncidentesPorEstado(null, desde, hasta));
                        default -> Vista.listar(estadisticas.promedioIncidentesPorCliente(desde, hasta));
                    }
                });
                case 8 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    Vista.listar(estadisticas.costosPorTransporte(cliente, desde(), hasta()));
                });
                case 9 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    Vista.listar(estadisticas.desempenioMensual(cliente, desde(), hasta()));
                });
                case 10 -> Consola.ejecutar(() -> {
                    LocalDate desde = desde();
                    LocalDate hasta = hasta();
                    Vista.listar(incidentes.rankingRutas(Consola.inicioDelDia(desde), Consola.finDelDia(hasta), 10));
                });
                case 11 -> Consola.ejecutar(() -> {
                    var calculadas = predicciones.calcularDiaria();
                    Consola.mostrar(calculadas.size() + " envios evaluados.");
                    Vista.listar(calculadas);
                });
                case 12 -> Consola.ejecutar(() -> {
                    String nivel = Consola.elegir("Nivel", "TODOS", "ALTO", "MEDIO", "BAJO");
                    Vista.listar(predicciones.ultimas("TODOS".equals(nivel) ? null : nivel));
                });
            }
        }
    }

    private LocalDate desde() {
        return Consola.fechaOpcional("Desde (Enter = sin limite)");
    }

    private LocalDate hasta() {
        return Consola.fechaOpcional("Hasta (Enter = sin limite)");
    }

}
