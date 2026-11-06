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

//   factordemora      = horas de atraso / 48 (tope 1)
//   riesgooperativo   = 0,20 × incidentes del envio + 0,05 × incidentes historicos en sus rutas
//                       + 0,05 × anomalias de sus sensores (tope 1)
//   riesgoclimatico   = (barco 0,5 · avion 0,3 · camion 0,2 · tren 0,1)
//   probabilidaddemora = 0,5 × factordemora + 0,3 × riesgooperativo + 0,2 × riesgoclimatico
//   nivelriesgo        = alto ≥ 0,6 · medio ≥ 0,3 · bajo
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

        // demora
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
                    medioActual = t.getMedioTransporte();
                }
            }
            // historial de incidentes en la misma ruta
            String origen = ciudadRepo.buscarPorId(t.getOrigenCiudadId()).getNombre();
            String destino = ciudadRepo.buscarPorId(t.getDestinoCiudadId()).getNombre();
            incidentesRuta += (int) incidenteRepo.contarPorRuta(origen, destino);
        }
        double demora = Math.max(demoraEnvio, demoraTramos);

        // anomalias de los sensores
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

    // horas que "real" paso a "estimada" ,0 si llego a tiempo
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
            default -> 0.1;
        };
    }

    private double redondear(double valor) {
        return Math.round(valor * 100) / 100.0;
    }

}
