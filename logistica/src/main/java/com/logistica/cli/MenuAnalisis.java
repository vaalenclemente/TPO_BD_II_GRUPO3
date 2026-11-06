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
            int opcion = Consola.menu("Análisis y predicción (consultas internas, no se facturan)",
                    "Envíos por país de destino", "Envíos por estado", "Envíos por cliente",
                    "Ranking de clientes por volumen transportado", "Envíos demorados",
                    "Tiempos de entrega (mín / máx / promedio) por cliente", "Promedio de incidentes",
                    "Costos por tipo de transporte", "Desempeño histórico mensual",
                    "Ranking de rutas con más incidentes", "Calcular predicción diaria de atrasos",
                    "Últimas predicciones");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    Consola.listar(estadisticas.enviosPorPais(cliente, desde(), hasta()));
                });
                case 2 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    Consola.listar(estadisticas.enviosPorEstado(cliente, desde(), hasta()));
                });
                case 3 -> Consola.ejecutar(() -> Consola.listar(estadisticas.enviosPorCliente(desde(), hasta())));
                case 4 -> Consola.ejecutar(() -> {
                    LocalDate desde = desde();
                    LocalDate hasta = hasta();
                    Consola.listar(estadisticas.rankingClientesPorVolumen(desde, hasta, Consola.entero("¿Cuántos?")));
                });
                case 5 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    Consola.listar(estadisticas.enviosDemorados(cliente, desde(), hasta()));
                });
                case 6 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    LocalDate desde = desde();
                    LocalDate hasta = hasta();
                    Consola.listar(estadisticas.tiemposEntregaPorCliente(cliente, desde, hasta));
                    Consola.mostrar("Total → " + estadisticas.tiempoPromedioEntrega(cliente, desde, hasta));
                });
                case 7 -> Consola.ejecutar(() -> {
                    String agrupar = Consola.elegir("Agrupar por", "País de destino", "Estado del envío", "Cliente");
                    LocalDate desde = desde();
                    LocalDate hasta = hasta();
                    switch (agrupar) {
                        case "País de destino" -> Consola.listar(estadisticas.promedioIncidentesPorPais(null, desde, hasta));
                        case "Estado del envío" -> Consola.listar(estadisticas.promedioIncidentesPorEstado(null, desde, hasta));
                        default -> Consola.listar(estadisticas.promedioIncidentesPorCliente(desde, hasta));
                    }
                });
                case 8 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    Consola.listar(estadisticas.costosPorTransporte(cliente, desde(), hasta()));
                });
                case 9 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    Consola.listar(estadisticas.desempenioMensual(cliente, desde(), hasta()));
                });
                case 10 -> Consola.ejecutar(() -> {
                    LocalDate desde = desde();
                    LocalDate hasta = hasta();
                    Consola.listar(incidentes.rankingRutas(Consola.inicioDelDia(desde), Consola.finDelDia(hasta), 10));
                });
                case 11 -> Consola.ejecutar(() -> {
                    var calculadas = predicciones.calcularDiaria();
                    Consola.mostrar(calculadas.size() + " envíos evaluados.");
                    Consola.listar(calculadas);
                });
                case 12 -> Consola.ejecutar(() -> {
                    String nivel = Consola.elegir("Nivel", "TODOS", "ALTO", "MEDIO", "BAJO");
                    Consola.listar(predicciones.ultimas("TODOS".equals(nivel) ? null : nivel));
                });
            }
        }
    }

    private LocalDate desde() {
        return Consola.fechaOpcional("Desde (Enter = sin límite)");
    }

    private LocalDate hasta() {
        return Consola.fechaOpcional("Hasta (Enter = sin límite)");
    }

}
