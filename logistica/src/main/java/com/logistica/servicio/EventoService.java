package com.logistica.servicio;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.logistica.modelo.Ciudad;
import com.logistica.modelo.Envio;
import com.logistica.modelo.EnvioContenedor;
import com.logistica.modelo.Evento;
import com.logistica.modelo.Tramo;
import com.logistica.repositorio.CiudadRepository;
import com.logistica.repositorio.EnvioContenedorRepository;
import com.logistica.repositorio.EnvioRepository;
import com.logistica.repositorio.EventoRepository;
import com.logistica.repositorio.PaisRepository;
import com.logistica.repositorio.TramoRepository;

public class EventoService {

    private final EventoRepository repo = new EventoRepository();
    private final EnvioRepository envioRepo = new EnvioRepository();
    private final EnvioContenedorRepository ecRepo = new EnvioContenedorRepository();
    private final TramoRepository tramoRepo = new TramoRepository();
    private final CiudadRepository ciudadRepo = new CiudadRepository();
    private final PaisRepository paisRepo = new PaisRepository();

    public String registrarManual(int envioId, String tipo, Integer ciudadId, String descripcion)
            throws SQLException {

        Envio e = envioRepo.buscarPorId(envioId);
        if (e == null) {
            throw new IllegalArgumentException("No existe un envío con id " + envioId + ".");
        }
        if (!"DEMORA".equals(tipo) && !"INSPECCION".equals(tipo) && !"OTRO".equals(tipo)) {
            throw new IllegalArgumentException("El tipo de evento no es correcto (DEMORA, INSPECCION, OTRO).");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        if (ciudadId != null && ciudadRepo.buscarPorId(ciudadId) == null) {
            throw new IllegalArgumentException("No existe la ciudad " + ciudadId + ".");
        }

        Integer ciudad = (ciudadId != null) ? ciudadId : ciudadActual(e);
        return registrar(envioId, tipo, ciudad, descripcion.trim());
    }

    String registrar(int envioId, String tipo, Integer ciudadId, String descripcion) throws SQLException {

        List<Integer> contenedorIds = new ArrayList<>();
        for (EnvioContenedor ec : ecRepo.listarAsignados(envioId)) {
            contenedorIds.add(ec.getContenedorId());
        }

        Evento ev = new Evento(envioId, tipo, descripcion);
        ev.setContenedorIds(contenedorIds);
        ev.setFechaHora(LocalDateTime.now());
        ev.setUsuarioId(UsuarioActual.get());

        if (ciudadId != null) {
            Ciudad c = ciudadRepo.buscarPorId(ciudadId);
            ev.setCiudadId(c.getId());
            ev.setCiudad(c.getNombre());
            ev.setPais(paisRepo.buscarPorId(c.getPaisId()).getNombre());
        }

        return repo.guardar(ev);
    }

    void borrar(String eventoId) {
        repo.borrar(eventoId);
    }

    Integer ciudadActual(Envio e) throws SQLException {
        Integer ciudad = e.getOrigenCiudadId();
        for (Tramo t : tramoRepo.listarPorEnvio(e.getId())) {
            if ("EN_CURSO".equals(t.getEstado()) || "COMPLETADO".equals(t.getEstado())) {
                ciudad = t.getDestinoCiudadId();
            }
        }
        return ciudad;
    }

    public List<Evento> listarPorEnvio(int envioId) {
        return repo.listarPorEnvio(envioId);
    }

    public List<Evento> listarPorContenedor(int contenedorId) {
        return repo.listarPorContenedor(contenedorId);
    }

}
