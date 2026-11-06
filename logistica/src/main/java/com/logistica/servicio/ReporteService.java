package com.logistica.servicio;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.logistica.modelo.DesempenioMensual;
import com.logistica.modelo.Envio;
import com.logistica.modelo.EnvioDemorado;
import com.logistica.modelo.Incidente;
import com.logistica.modelo.LecturaSensor;
import com.logistica.modelo.Parte;
import com.logistica.modelo.PrediccionRiesgo;
import com.logistica.modelo.Reporte;
import com.logistica.modelo.ResultadoAgrupado;
import com.logistica.modelo.RutaIncidentes;
import com.logistica.modelo.TipoReporte;
import com.logistica.repositorio.EnvioRepository;
import com.logistica.repositorio.EstadisticaRepository;
import com.logistica.repositorio.IncidenteRepository;
import com.logistica.repositorio.LecturaSensorRepository;
import com.logistica.repositorio.ParteRepository;
import com.logistica.repositorio.PrediccionRiesgoRepository;
import com.logistica.repositorio.ReporteRepository;
import com.logistica.repositorio.TipoReporteRepository;

public class ReporteService {

    public static final List<String> CODIGOS = List.of("INCIDENTES_ABIERTOS", "RANKING_RUTAS_INCIDENTES",
            "ANOMALIAS_TEMPERATURA", "PREDICCION_DEMORAS", "ENVIOS_POR_PAIS", "ENVIOS_POR_ESTADO",
            "ENVIOS_DEL_CLIENTE", "ENVIOS_DEMORADOS", "TIEMPOS_ENTREGA", "PROMEDIO_INCIDENTES",
            "COSTOS_POR_TRANSPORTE", "DESEMPENIO_HISTORICO");

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ReporteRepository repo = new ReporteRepository();
    private final TipoReporteRepository tipoRepo = new TipoReporteRepository();
    private final ParteRepository parteRepo = new ParteRepository();
    private final EnvioRepository envioRepo = new EnvioRepository();
    private final IncidenteRepository incidenteRepo = new IncidenteRepository();
    private final LecturaSensorRepository lecturaRepo = new LecturaSensorRepository();
    private final PrediccionRiesgoRepository prediccionRepo = new PrediccionRiesgoRepository();
    private final EstadisticaRepository estadisticaRepo = new EstadisticaRepository();

    public Reporte solicitar(Reporte r) throws SQLException {

        Parte cliente = (r.getParteId() == null) ? null : parteRepo.buscarPorId(r.getParteId());
        if (cliente == null || !"CLIENTE".equals(cliente.getTipo())) {
            throw new IllegalArgumentException("Los reportes los pide un cliente.");
        }
        if (!"ACTIVO".equals(cliente.getEstado())) {
            throw new IllegalArgumentException("El cliente esta dado de baja.");
        }
        if (r.getNombreSolicitante() == null || r.getNombreSolicitante().isBlank()) {
            throw new IllegalArgumentException("El nombre de quien lo pide es obligatorio.");
        }
        TipoReporte tipo = (r.getTipoReporteId() == null) ? null : tipoRepo.buscarPorId(r.getTipoReporteId());
        if (tipo == null || !Boolean.TRUE.equals(tipo.getActivo())) {
            throw new IllegalArgumentException("El tipo de reporte no existe o no esta activo.");
        }
        if (!CODIGOS.contains(tipo.getCodigo())) {
            throw new IllegalArgumentException("El reporte " + tipo.getCodigo() + " todavia no se puede generar.");
        }
        if (r.getDesde() == null || r.getHasta() == null || r.getHasta().isBefore(r.getDesde())) {
            throw new IllegalArgumentException(
                    "El periodo (desde - hasta) es obligatorio y desde no puede ser posterior a hasta.");
        }

        r.setNombreSolicitante(r.getNombreSolicitante().trim());
        r.setCodigo(tipo.getCodigo());
        r.setCosto(tipo.getPrecio());
        r.setEstado("SOLICITADO");
        r.setFechaSolicitud(LocalDateTime.now());
        r.setFacturaId(null);

        r.setId(repo.guardar(r));

        try {
            r.setContenido(generar(cliente, r));
            r.setEstado("GENERADO");
            r.setFechaGeneracion(LocalDateTime.now());
            r.setError(null);
        } catch (Exception ex) {
            r.setEstado("ERROR");
            r.setError(ex.getMessage());
        }
        repo.guardarResultado(r.getId(), r.getEstado(), r.getContenido(), r.getFechaGeneracion(), r.getError());
        return r;
    }

    public List<Reporte> listarPorParte(int parteId) {
        return repo.listarPorParte(parteId);
    }

    public Reporte buscarPorId(String id) {
        return repo.buscarPorId(id);
    }

    private String generar(Parte cliente, Reporte r) throws SQLException {
        LocalDateTime desde = r.getDesde().atStartOfDay();
        LocalDateTime hasta = r.getHasta().plusDays(1).atStartOfDay(); // hasta incluido

        String titulo = tituloDe(r.getCodigo()) + " - " + cliente.getRazonSocial()
                + " (" + r.getDesde().format(FECHA) + " - " + r.getHasta().format(FECHA) + ")\n";

        return switch (r.getCodigo()) {
            case "INCIDENTES_ABIERTOS" -> titulo + incidentesAbiertos(cliente.getId(), desde, hasta);
            case "RANKING_RUTAS_INCIDENTES" -> titulo + rankingRutas(cliente.getId(), desde, hasta);
            case "ANOMALIAS_TEMPERATURA" -> titulo + anomaliasTemperatura(cliente.getId(), desde, hasta);
            case "PREDICCION_DEMORAS" -> titulo + prediccionDemoras(cliente.getId());
            case "ENVIOS_POR_PAIS" -> titulo + enviosAgrupados(estadisticaRepo.enviosPorPais(cliente.getId(), desde, hasta));
            case "ENVIOS_POR_ESTADO" -> titulo + enviosAgrupados(estadisticaRepo.enviosPorEstado(cliente.getId(), desde, hasta));
            case "ENVIOS_DEL_CLIENTE" -> titulo + enviosDelCliente(cliente.getId(), desde, hasta);
            case "ENVIOS_DEMORADOS" -> titulo + enviosDemorados(cliente.getId(), desde, hasta);
            case "TIEMPOS_ENTREGA" -> titulo + tiemposEntrega(cliente.getId(), desde, hasta);
            case "PROMEDIO_INCIDENTES" -> titulo + promedioIncidentes(cliente.getId(), desde, hasta);
            case "COSTOS_POR_TRANSPORTE" -> titulo + costosPorTransporte(cliente.getId(), desde, hasta);
            case "DESEMPENIO_HISTORICO" -> titulo + desempenioHistorico(cliente.getId(), desde, hasta);
            default -> throw new IllegalArgumentException("El reporte " + r.getCodigo() + " no se puede generar.");
        };
    }

    private String enviosAgrupados(List<ResultadoAgrupado> filas) {
        String texto = "";
        for (ResultadoAgrupado f : filas) {
            texto += f.getEtiqueta() + ": " + f.getCantidad() + " envios (" + f.getTotal() + " kg)\n";
        }
        return conDatos(texto);
    }

    private String enviosDelCliente(int clienteId, LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String texto = "";
        int cantidad = 0;
        for (Envio e : envioRepo.listarPorCliente(clienteId)) {
            if (e.getFechaCreacion().isBefore(desde) || !e.getFechaCreacion().isBefore(hasta)) {
                continue;
            }
            cantidad++;
            texto += "Envio " + e.getId() + " | " + e.getEstado() + " | prioridad " + e.getPrioridad()
                    + " | creado " + e.getFechaCreacion().format(FECHA)
                    + " | entrega estimada " + e.getFechaEntregaEstimada().format(FECHA)
                    + (e.getFechaEntregaReal() == null ? "" : " | entregado " + e.getFechaEntregaReal().format(FECHA))
                    + " | incidentes " + e.getCantidadIncidentes() + "\n";
        }
        return texto + "Total: " + cantidad + "\n";
    }

    private String enviosDemorados(int clienteId, LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String texto = "";
        List<EnvioDemorado> demorados = estadisticaRepo.enviosDemorados(clienteId, desde, hasta, LocalDateTime.now());
        for (EnvioDemorado d : demorados) {
            texto += "Envio " + d.getEnvioId() + " → " + d.getDestino() + " | " + d.getEstado()
                    + " | estimada " + d.getFechaEntregaEstimada().format(FECHA_HORA)
                    + (d.getFechaEntregaReal() == null ? " | sin entregar" : " | entregado " + d.getFechaEntregaReal().format(FECHA_HORA))
                    + " | " + d.getHorasDemora() + " h de demora\n";
        }
        return texto + "Total: " + demorados.size() + "\n";
    }

    private String tiemposEntrega(int clienteId, LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        ResultadoAgrupado t = estadisticaRepo.tiempoPromedioEntrega(clienteId, desde, hasta);
        if (t.getCantidad() == 0) {
            return "Sin entregas en el periodo.\n";
        }
        return "Entregas: " + t.getCantidad() + "\n"
                + "Tiempo minimo: " + t.getMinimo() + " h\n"
                + "Tiempo maximo: " + t.getMaximo() + " h\n"
                + "Tiempo promedio: " + t.getPromedio() + " h\n";
    }

    private String promedioIncidentes(int clienteId, LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String porPais = "";
        for (ResultadoAgrupado f : estadisticaRepo.promedioIncidentesPorPais(clienteId, desde, hasta)) {
            porPais += f.getEtiqueta() + ": " + f.getPromedio() + " incidentes por envio (" + f.getCantidad()
                    + " envios, " + f.getTotal() + " incidentes)\n";
        }
        String porEstado = "";
        for (ResultadoAgrupado f : estadisticaRepo.promedioIncidentesPorEstado(clienteId, desde, hasta)) {
            porEstado += f.getEtiqueta() + ": " + f.getPromedio() + " incidentes por envio (" + f.getCantidad()
                    + " envios, " + f.getTotal() + " incidentes)\n";
        }
        return "Por pais de destino:\n" + conDatos(porPais) + "Por estado del envio:\n" + conDatos(porEstado);
    }

    private String costosPorTransporte(int clienteId, LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String texto = "";
        for (ResultadoAgrupado f : estadisticaRepo.costosPorTransporte(clienteId, desde, hasta)) {
            texto += f.getEtiqueta() + ": " + f.getCantidad() + " tramos | total $" + f.getTotal()
                    + " | promedio $" + f.getPromedio() + " | min $" + f.getMinimo() + " | max $" + f.getMaximo() + "\n";
        }
        return conDatos(texto);
    }

    private String desempenioHistorico(int clienteId, LocalDateTime desde, LocalDateTime hasta) throws SQLException {
        String texto = "";
        for (DesempenioMensual d : estadisticaRepo.desempenioMensual(clienteId, desde, hasta)) {
            texto += d + "\n";
        }
        return conDatos(texto);
    }

    private String conDatos(String texto) {
        return texto.isEmpty() ? "Sin datos en el periodo.\n" : texto;
    }

    private String incidentesAbiertos(int clienteId, LocalDateTime desde, LocalDateTime hasta) {
        String texto = "";
        int cantidad = 0;
        for (Incidente i : incidenteRepo.listarAbiertosPorCliente(clienteId)) {
            if (i.getFechaApertura().isBefore(desde) || !i.getFechaApertura().isBefore(hasta)) {
                continue;
            }
            cantidad++;
            texto += i.getFechaApertura().format(FECHA_HORA) + " | envio " + i.getEnvioId() + " | " + i.getTipo()
                    + " (" + i.getSeveridad() + ")"
                    + (i.getTramoId() == null ? "" : " | tramo " + i.getTramoOrigen() + " → " + i.getTramoDestino())
                    + " | " + i.getDescripcion() + "\n";
        }
        return texto + "Total: " + cantidad + "\n";
    }

    private String rankingRutas(int clienteId, LocalDateTime desde, LocalDateTime hasta) {
        String texto = "";
        int puesto = 1;
        for (RutaIncidentes ruta : incidenteRepo.rankingRutas(clienteId, desde, hasta, 10)) {
            texto += puesto + ". " + ruta + "\n";
            puesto++;
        }
        return texto.isEmpty() ? "Sin incidentes en el periodo.\n" : texto;
    }

    private String anomaliasTemperatura(int clienteId, LocalDateTime desde, LocalDateTime hasta)
            throws SQLException {
        List<Integer> envioIds = idsDeEnvios(clienteId);
        String texto = "";
        int cantidad = 0;
        for (LecturaSensor l : lecturaRepo.listarAnomalias("TEMPERATURA", envioIds, desde, hasta)) {
            cantidad++;
            texto += l.getFechaHora().format(FECHA_HORA) + " | envio " + l.getEnvioId() + " | contenedor "
                    + l.getContenedorId() + " | " + l.getTemperatura() + " °C (limite " + l.getAnomaliaLimite()
                    + ", exceso " + l.getAnomaliaExceso() + ")\n";
        }
        return texto + "Total: " + cantidad + "\n";
    }

    private String prediccionDemoras(int clienteId) throws SQLException {
        List<Integer> envioIds = idsDeEnvios(clienteId);
        String texto = "";
        for (PrediccionRiesgo p : prediccionRepo.listarUltimas(null)) {
            if (envioIds.contains(p.getEnvioId())) {
                texto += p + " - calculado " + p.getFechaCalculo().format(FECHA_HORA) + "\n";
            }
        }
        return texto.isEmpty() ? "No hay predicciones para los envios en curso.\n" : texto;
    }

    private List<Integer> idsDeEnvios(int clienteId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        for (Envio e : envioRepo.listarPorCliente(clienteId)) {
            ids.add(e.getId());
        }
        return ids;
    }

    private String tituloDe(String codigo) {
        return switch (codigo) {
            case "INCIDENTES_ABIERTOS" -> "INCIDENTES ABIERTOS";
            case "RANKING_RUTAS_INCIDENTES" -> "RUTAS CON MAS INCIDENTES";
            case "ANOMALIAS_TEMPERATURA" -> "ANOMALIAS DE TEMPERATURA";
            case "PREDICCION_DEMORAS" -> "PREDICCION DE DEMORAS";
            case "ENVIOS_POR_PAIS" -> "ENVIOS POR PAIS DE DESTINO";
            case "ENVIOS_POR_ESTADO" -> "ENVIOS POR ESTADO";
            case "ENVIOS_DEL_CLIENTE" -> "ENVIOS DEL CLIENTE";
            case "ENVIOS_DEMORADOS" -> "ENVIOS DEMORADOS";
            case "TIEMPOS_ENTREGA" -> "TIEMPOS DE ENTREGA";
            case "PROMEDIO_INCIDENTES" -> "PROMEDIO DE INCIDENTES";
            case "COSTOS_POR_TRANSPORTE" -> "COSTOS POR TIPO DE TRANSPORTE";
            default -> "DESEMPEÑO HISTORICO";
        };
    }

}
