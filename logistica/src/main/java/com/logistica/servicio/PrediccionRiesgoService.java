package com.logistica.servicio;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.logistica.modelo.Envio;
import com.logistica.modelo.PrediccionRiesgo;
import com.logistica.modelo.Tramo;
import com.logistica.repositorio.CiudadRepository;
import com.logistica.repositorio.EnvioRepository;
import com.logistica.repositorio.IncidenteRepository;
import com.logistica.repositorio.LecturaSensorRepository;
import com.logistica.repositorio.PrediccionRiesgoRepository;
import com.logistica.repositorio.TramoRepository;

//   factorDemora      = horas de atraso / 48 (tope 1)
//   riesgoOperativo   = 0,20 × incidentes del envío + 0,05 × incidentes históricos en sus rutas
//                       + 0,05 × anomalías de sus sensores (tope 1)
//   riesgoClimatico   = exposición al clima del medio del tramo actual o próximo
//                       (BARCO 0,5 · AVION 0,3 · CAMION 0,2 · TREN 0,1)
//   probabilidadDemora = 0,5 × factorDemora + 0,3 × riesgoOperativo + 0,2 × riesgoClimatico
//   nivelRiesgo        = ALTO ≥ 0,6 · MEDIO ≥ 0,3 · BAJO
public class PrediccionRiesgoService {

    private static final List<String> ESTADOS_EN_CURSO = List.of("CREADO", "EN_PREPARACION", "EN_TRANSITO",
            "EN_ADUANA");

    private final PrediccionRiesgoRepository repo = new PrediccionRiesgoRepository();
    private final EnvioRepository envioRepo = new EnvioRepository();
    private final TramoRepository tramoRepo = new TramoRepository();
    private final CiudadRepository ciudadRepo = new CiudadRepository();
    private final IncidenteRepository incidenteRepo = new IncidenteRepository();
    private final LecturaSensorRepository lecturaRepo = new LecturaSensorRepository();

    public List<PrediccionRiesgo> calcularDiaria() throws SQLException {
        LocalDateTime ahora = LocalDateTime.now();
        List<PrediccionRiesgo> resultado = new ArrayList<>();

        for (String estado : ESTADOS_EN_CURSO) {
            for (Envio e : envioRepo.listarPorEstado(estado)) {
                PrediccionRiesgo p = calcular(e, ahora);
                p.setId(repo.guardar(p));
                resultado.add(p);
            }
        }
        return resultado;
    }

    public List<PrediccionRiesgo> ultimas(String nivelRiesgo) {
        if (nivelRiesgo != null && !"ALTO".equals(nivelRiesgo) && !"MEDIO".equals(nivelRiesgo)
                && !"BAJO".equals(nivelRiesgo)) {
            throw new IllegalArgumentException("El nivel no es correcto (ALTO, MEDIO, BAJO).");
        }
        return repo.listarUltimas(nivelRiesgo);
    }

    public List<PrediccionRiesgo> historialEnvio(int envioId) {
        return repo.listarPorEnvio(envioId);
    }

    private PrediccionRiesgo calcular(Envio e, LocalDateTime ahora) throws SQLException {

        List<Tramo> tramos = tramoRepo.listarPorEnvio(e.getId());

        // 1) Demora
        double demoraEnvio = horasDeAtraso(e.getFechaEntregaEstimada(), ahora);
        double demoraTramos = 0;
        String medioActual = null;
        int incidentesRuta = 0;

        for (Tramo t : tramos) {
            if ("CANCELADO".equals(t.getEstado())) {
                continue;
            }
            if ("COMPLETADO".equals(t.getEstado())) {
                demoraTramos += horasDeAtraso(t.getFechaLlegadaEstimada(), t.getFechaLlegadaReal());
            } else {
                if ("EN_CURSO".equals(t.getEstado())) {
                    demoraTramos += horasDeAtraso(t.getFechaLlegadaEstimada(), ahora);
                }
                if (medioActual == null) {
                    medioActual = t.getMedioTransporte(); // el primero que no terminó = el actual o el próximo
                }
            }
            // 2) Historial de incidentes en la misma ruta
            String origen = ciudadRepo.buscarPorId(t.getOrigenCiudadId()).getNombre();
            String destino = ciudadRepo.buscarPorId(t.getDestinoCiudadId()).getNombre();
            incidentesRuta += (int) incidenteRepo.contarPorRuta(origen, destino);
        }
        double demora = Math.max(demoraEnvio, demoraTramos);

        // 3) Anomalias de los sensores del envio
        int anomalias = (int) lecturaRepo.contarAnomaliasPorEnvio(e.getId(), e.getFechaCreacion());

        int incidentesEnvio = (e.getCantidadIncidentes() == null) ? 0 : e.getCantidadIncidentes();

        double factorDemora = Math.min(1, demora / 48);
        double riesgoOperativo = Math.min(1, 0.20 * incidentesEnvio + 0.05 * incidentesRuta + 0.05 * anomalias);
        double riesgoClimatico = riesgoClimatico(medioActual);
        double probabilidad = 0.5 * factorDemora + 0.3 * riesgoOperativo + 0.2 * riesgoClimatico;

        PrediccionRiesgo p = new PrediccionRiesgo();
        p.setEnvioId(e.getId());
        p.setFechaCalculo(ahora);
        p.setProbabilidadDemora(redondear(probabilidad));
        p.setRiesgoClimatico(redondear(riesgoClimatico));
        p.setRiesgoOperativo(redondear(riesgoOperativo));
        p.setNivelRiesgo(probabilidad >= 0.6 ? "ALTO" : probabilidad >= 0.3 ? "MEDIO" : "BAJO");
        p.setDemoraActualHs(redondear(demora));
        p.setIncidentesRuta(incidentesRuta);
        p.setAnomaliasSensor(anomalias);
        return p;
    }

    // Horas que "real" paso a "estimada" (0 si llego a tiempo o todavia no vencio).
    private double horasDeAtraso(LocalDateTime estimada, LocalDateTime real) {
        if (estimada == null || real == null || !real.isAfter(estimada)) {
            return 0;
        }
        return Duration.between(estimada, real).toMinutes() / 60.0;
    }

    private double riesgoClimatico(String medio) {
        if (medio == null) {
            return 0.1;
        }
        return switch (medio) {
            case "BARCO" -> 0.5;
            case "AVION" -> 0.3;
            case "CAMION" -> 0.2;
            default -> 0.1; // TREN
        };
    }

    private double redondear(double valor) {
        return Math.round(valor * 100) / 100.0;
    }

}
