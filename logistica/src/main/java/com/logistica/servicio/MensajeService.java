package com.logistica.servicio;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.logistica.modelo.Grupo;
import com.logistica.modelo.Mensaje;
import com.logistica.modelo.Parte;
import com.logistica.modelo.Usuario;
import com.logistica.repositorio.MensajeRepository;
import com.logistica.repositorio.ParteRepository;
import com.logistica.repositorio.UsuarioRepository;

public class MensajeService {

    private static final int LARGO_MAXIMO = 2000;

    private final MensajeRepository repo = new MensajeRepository();
    private final ParteRepository parteRepo = new ParteRepository();
    private final UsuarioRepository usuarioRepo = new UsuarioRepository();
    private final GrupoService grupoService = new GrupoService();

    public String enviarAParte(int parteId, int remitenteId, String contenido) throws SQLException {

        Parte p = parteRepo.buscarPorId(parteId);
        if (p == null) {
            throw new IllegalArgumentException("No existe una parte con id " + parteId + ".");
        }
        Usuario remitente = buscarUsuarioActivo(remitenteId);
        validarAcceso(remitente, parteId);

        Mensaje m = new Mensaje();
        m.setTipoConversacion("PARTE");
        m.setParteId(parteId);
        m.setParteTipo(p.getTipo());
        m.setDireccion("INTERNO".equals(remitente.getTipoUsuario()) ? "SALIENTE" : "ENTRANTE");
        return guardar(m, remitenteId, contenido);
    }

    public String enviarAGrupo(String grupoId, int remitenteId, String contenido) throws SQLException {

        Grupo g = grupoService.buscarActivo(grupoId);
        buscarUsuarioActivo(remitenteId);
        if (!g.getIntegrantes().contains(remitenteId)) {
            throw new IllegalArgumentException("Solo los integrantes del grupo pueden escribir en el.");
        }

        Mensaje m = new Mensaje();
        m.setTipoConversacion("GRUPO");
        m.setGrupoId(grupoId);
        return guardar(m, remitenteId, contenido);
    }

    public List<Mensaje> conversacionConParte(int parteId, int usuarioId) throws SQLException {
        validarAcceso(buscarUsuarioActivo(usuarioId), parteId);
        return repo.listarPorParte(parteId);
    }

    public List<Mensaje> conversacionDeGrupo(String grupoId, int usuarioId) {
        Grupo g = grupoService.buscarPorId(grupoId);
        if (g == null) {
            throw new IllegalArgumentException("No existe un grupo con id " + grupoId + ".");
        }
        if (!g.getIntegrantes().contains(usuarioId)) {
            throw new IllegalArgumentException("Solo los integrantes del grupo pueden leerlo.");
        }
        return repo.listarPorGrupo(grupoId);
    }

    private String guardar(Mensaje m, int remitenteId, String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException("El mensaje esta vacio.");
        }
        if (contenido.length() > LARGO_MAXIMO) {
            throw new IllegalArgumentException("El mensaje no puede superar los " + LARGO_MAXIMO + " caracteres.");
        }
        m.setRemitenteId(remitenteId);
        m.setContenido(contenido.trim());
        m.setFechaHora(LocalDateTime.now());
        m.setTipo("TEXTO");

        String id = repo.guardar(m);
        m.setId(id);
        return id;
    }

    private void validarAcceso(Usuario u, int parteId) throws SQLException {
        if (!"EXTERNO".equals(u.getTipoUsuario())) {
            return;
        }
        if (u.getParteId() == null || u.getParteId() != parteId) {
            throw new IllegalArgumentException("Un usuario externo solo accede a la conversacion de su empresa.");
        }
        Parte p = parteRepo.buscarPorId(parteId);
        if (p == null || !"ACTIVO".equals(p.getEstado())) {
            throw new IllegalArgumentException("La empresa del usuario esta dada de baja.");
        }
    }

    private Usuario buscarUsuarioActivo(int usuarioId) throws SQLException {
        Usuario u = usuarioRepo.buscarPorId(usuarioId);
        if (u == null) {
            throw new IllegalArgumentException("No existe un usuario con id " + usuarioId + ".");
        }
        if (!"ACTIVO".equals(u.getEstado())) {
            throw new IllegalArgumentException("El usuario esta dado de baja.");
        }
        return u;
    }

}
