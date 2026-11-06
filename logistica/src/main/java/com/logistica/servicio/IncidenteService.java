package com.logistica.servicio;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.logistica.modelo.Alerta;
import com.logistica.modelo.Ciudad;
import com.logistica.modelo.Envio;
import com.logistica.modelo.Incidente;
import com.logistica.modelo.Parte;
import com.logistica.modelo.RutaIncidentes;
import com.logistica.modelo.Tramo;
import com.logistica.repositorio.AlertaRedisRepository;
import com.logistica.repositorio.AlertaRepository;
import com.logistica.repositorio.CiudadRepository;
import com.logistica.repositorio.EnvioRepository;
import com.logistica.repositorio.IncidenteRepository;
import com.logistica.repositorio.PaisRepository;
import com.logistica.repositorio.ParteRepository;
import com.logistica.repositorio.TramoRepository;

public class IncidenteService {

    private final IncidenteRepository repo = new IncidenteRepository();
    private final AlertaRepository alertaRepo = new AlertaRepository();
    private final AlertaRedisRepository alertaRedis = new AlertaRedisRepository();
    private final EnvioRepository envioRepo = new EnvioRepository();
    private final TramoRepository tramoRepo = new TramoRepository();
    private final ParteRepository parteRepo = new ParteRepository();
    private final CiudadRepository ciudadRepo = new CiudadRepository();
    private final PaisRepository paisRepo = new PaisRepository();
    private final EventoService eventos = new EventoService();

    public String abrir(int envioId, String tipo, String severidad, String descripcion, List<String> alertaIds)
            throws SQLException {

        Envio e = envioRepo.buscarPorId(envioId);
        if (e == null) {
            throw new IllegalArgumentException("No existe un envío con id " + envioId + ".");
        }
        if ("CANCELADO".equals(e.getEstado())) {
            throw new IllegalArgumentException("El envío está CANCELADO.");
        }
        validarTipo(tipo);
        if (!"ALTA".equals(severidad) && !"MEDIA".equals(severidad) && !"BAJA".equals(severidad)) {
            throw new IllegalArgumentException("La severidad no es correcta (ALTA, MEDIA, BAJA).");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        List<String> alertas = validarAlertas(envioId, alertaIds);

        Parte cliente = parteRepo.buscarPorId(e.getClienteId());

        Incidente i = new Incidente(envioId, tipo, severidad, descripcion.trim());
        i.setClienteId(cliente.getId());
        i.setClienteNombre(cliente.getRazonSocial());
        i.setEnvioOrigen(lugar(e.getOrigenCiudadId()));
        i.setEnvioDestino(lugar(e.getDestinoCiudadId()));

        Tramo enCurso = tramoEnCurso(envioId);
        if (enCurso != null) {
            i.setTramoId(enCurso.getId());
            i.setTramoMedio(enCurso.getMedioTransporte());
            i.setTramoOrigen(ciudadRepo.buscarPorId(enCurso.getOrigenCiudadId()).getNombre());
            i.setTramoDestino(ciudadRepo.buscarPorId(enCurso.getDestinoCiudadId()).getNombre());
        }

        i.setEstado("ABIERTO");
        i.setAlertaIds(alertas);
        i.setFechaApertura(LocalDateTime.now());

        Saga saga = new Saga("ABRIR_INCIDENTE", "incidente");
        String id;
        try {
            id = repo.guardar(i);
            i.setId(id);
            saga.objeto(id);
            saga.paso("MONGO_INCIDENTE_GUARDADO", () -> repo.borrar(id));

            envioRepo.incrementarIncidentes(envioId);
            saga.paso("MYSQL_CANTIDAD_INCIDENTES", () -> envioRepo.decrementarIncidentes(envioId));

            if (!alertas.isEmpty()) {
                alertaRepo.asignarIncidente(alertas, id);
                saga.paso("MONGO_ALERTAS_VINCULADAS", () -> alertaRepo.asignarIncidente(alertas, null));
            }

            String eventoId = eventos.registrar(envioId, "INCIDENTE", eventos.ciudadActual(e),
                    "Incidente " + tipo + " (" + severidad + "): " + i.getDescripcion());
            saga.paso("MONGO_EVENTO_INCIDENTE", () -> eventos.borrar(eventoId));

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }

        actualizarEnRedis(alertas);
        return id;
    }

    public void sumarAlertas(String incidenteId, List<String> alertaIds) throws SQLException {
        Incidente i = buscarExistente(incidenteId);
        if (!"ABIERTO".equals(i.getEstado())) {
            throw new IllegalArgumentException("El incidente no está abierto.");
        }
        List<String> alertas = validarAlertas(i.getEnvioId(), alertaIds);
        if (alertas.isEmpty()) {
            return;
        }
        repo.agregarAlertas(incidenteId, alertas);
        alertaRepo.asignarIncidente(alertas, incidenteId);
        actualizarEnRedis(alertas);
    }

    public void cerrar(String incidenteId, String resolucion) {
        Incidente i = buscarExistente(incidenteId);
        if (!"ABIERTO".equals(i.getEstado())) {
            throw new IllegalArgumentException("El incidente ya está cerrado.");
        }
        if (resolucion == null || resolucion.isBlank()) {
            throw new IllegalArgumentException("La resolución es obligatoria para cerrar el incidente.");
        }
        repo.cerrar(incidenteId, LocalDateTime.now(), resolucion.trim());
        for (String alertaId : i.getAlertaIds()) {
            alertaRepo.cambiarEstado(alertaId, "CERRADA");
            RedisOpcional.escribir("las alertas activas", () -> alertaRedis.quitar(alertaId));
        }
    }

    private void actualizarEnRedis(List<String> alertaIds) {
        for (String alertaId : alertaIds) {
            RedisOpcional.escribir("las alertas activas", () -> alertaRedis.guardar(alertaRepo.buscarPorId(alertaId)));
        }
    }

    public Incidente buscarAbierto(int envioId, String tipo) {
        return repo.buscarAbierto(envioId, tipo);
    }

    public List<Incidente> listarAbiertos() {
        return repo.listarPorEstado("ABIERTO");
    }

    public List<Incidente> listarPorEnvio(int envioId) {
        return repo.listarPorEnvio(envioId);
    }

    public Incidente buscarPorId(String id) {
        return repo.buscarPorId(id);
    }

    public List<RutaIncidentes> rankingRutas(LocalDateTime desde, LocalDateTime hasta, int limite) {
        return repo.rankingRutas(null, desde, hasta, limite);
    }

    private List<String> validarAlertas(int envioId, List<String> alertaIds) {
        List<String> alertas = new ArrayList<>();
        if (alertaIds == null) {
            return alertas;
        }
        for (String alertaId : new HashSet<>(alertaIds)) {
            Alerta a = alertaRepo.buscarPorId(alertaId);
            if (a == null) {
                throw new IllegalArgumentException("No existe la alerta " + alertaId + ".");
            }
            if (a.getEnvioId() == null || a.getEnvioId() != envioId) {
                throw new IllegalArgumentException("La alerta " + alertaId + " no es de este envío.");
            }
            if (a.getIncidenteId() != null) {
                throw new IllegalArgumentException("La alerta " + alertaId + " ya está en otro incidente.");
            }
            alertas.add(alertaId);
        }
        return alertas;
    }

    private void validarTipo(String tipo) {
        List<String> tipos = List.of("TEMPERATURA", "HUMEDAD", "VIBRACION", "BATERIA", "DEMORA", "DANIO",
                "ROBO", "ADUANA", "OTRO");
        if (!tipos.contains(tipo)) {
            throw new IllegalArgumentException("El tipo de incidente no es correcto " + tipos + ".");
        }
    }

    private Tramo tramoEnCurso(int envioId) throws SQLException {
        for (Tramo t : tramoRepo.listarPorEnvio(envioId)) {
            if ("EN_CURSO".equals(t.getEstado())) {
                return t;
            }
        }
        return null;
    }

    private String lugar(int ciudadId) throws SQLException {
        Ciudad c = ciudadRepo.buscarPorId(ciudadId);
        return c.getNombre() + ", " + paisRepo.buscarPorId(c.getPaisId()).getNombre();
    }

    private Incidente buscarExistente(String id) {
        Incidente i = repo.buscarPorId(id);
        if (i == null) {
            throw new IllegalArgumentException("No existe un incidente con id " + id + ".");
        }
        return i;
    }

}
