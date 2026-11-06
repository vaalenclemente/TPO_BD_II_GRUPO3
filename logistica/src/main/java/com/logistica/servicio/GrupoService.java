package com.logistica.servicio;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.logistica.modelo.Grupo;
import com.logistica.modelo.Usuario;
import com.logistica.repositorio.GrupoRepository;
import com.logistica.repositorio.UsuarioRepository;

public class GrupoService {

    private final GrupoRepository repo = new GrupoRepository();
    private final UsuarioRepository usuarioRepo = new UsuarioRepository();

    public String crear(Grupo g) throws SQLException {

        // a) validaciones
        validarNombre(g);
        if (g.getCreadoPor() == null) {
            g.setCreadoPor(UsuarioActual.get());
        }
        validarInterno(g.getCreadoPor());

        LinkedHashSet<Integer> integrantes = new LinkedHashSet<>();
        integrantes.add(g.getCreadoPor());
        if (g.getIntegrantes() != null) {
            integrantes.addAll(g.getIntegrantes());
        }
        for (Integer usuarioId : integrantes) {
            validarInterno(usuarioId);
        }
        if (integrantes.size() < 2) {
            throw new IllegalArgumentException("Un grupo necesita al menos dos integrantes.");
        }

        // b) valores del sistema
        g.setIntegrantes(new ArrayList<>(integrantes));
        g.setFechaCreacion(LocalDateTime.now());
        g.setEstado("ACTIVO");

        // c) guardar
        String id = repo.guardar(g);
        g.setId(id);
        return id;
    }

    public void modificar(Grupo g) {
        buscarExistente(g.getId());
        validarNombre(g);
        repo.actualizar(g);
    }

    public void agregarIntegrante(String grupoId, int usuarioId) throws SQLException {
        Grupo g = buscarActivo(grupoId);
        validarInterno(usuarioId);
        if (g.getIntegrantes().contains(usuarioId)) {
            throw new IllegalArgumentException("El usuario ya está en el grupo.");
        }
        repo.agregarIntegrante(grupoId, usuarioId);
    }

    public void quitarIntegrante(String grupoId, int usuarioId) {
        Grupo g = buscarActivo(grupoId);
        if (!g.getIntegrantes().contains(usuarioId)) {
            throw new IllegalArgumentException("El usuario no está en el grupo.");
        }
        if (g.getIntegrantes().size() <= 2) {
            throw new IllegalArgumentException("Un grupo necesita al menos dos integrantes: dé de baja el grupo.");
        }
        repo.quitarIntegrante(grupoId, usuarioId);
    }

    public void darDeBaja(String grupoId) {
        buscarExistente(grupoId);
        repo.cambiarEstado(grupoId, "INACTIVO");
    }

    public void reactivar(String grupoId) {
        buscarExistente(grupoId);
        repo.cambiarEstado(grupoId, "ACTIVO");
    }

    public List<Grupo> listar() {
        return repo.listar();
    }

    public List<Grupo> listarPorUsuario(int usuarioId) {
        return repo.listarPorIntegrante(usuarioId);
    }

    public Grupo buscarPorId(String id) {
        return repo.buscarPorId(id);
    }

    Grupo buscarActivo(String id) {
        Grupo g = buscarExistente(id);
        if (!"ACTIVO".equals(g.getEstado())) {
            throw new IllegalArgumentException("El grupo está dado de baja.");
        }
        return g;
    }

    private void validarNombre(Grupo g) {
        if (g.getNombre() == null || g.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del grupo es obligatorio.");
        }
        g.setNombre(g.getNombre().trim());
    }

    private void validarInterno(Integer usuarioId) throws SQLException {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Falta indicar el usuario.");
        }
        Usuario u = usuarioRepo.buscarPorId(usuarioId);
        if (u == null) {
            throw new IllegalArgumentException("No existe un usuario con id " + usuarioId + ".");
        }
        if (!"INTERNO".equals(u.getTipoUsuario())) {
            throw new IllegalArgumentException(
                    "El usuario " + usuarioId + " no es interno: los grupos son solo internos.");
        }
        if (!"ACTIVO".equals(u.getEstado())) {
            throw new IllegalArgumentException("El usuario " + usuarioId + " está dado de baja.");
        }
    }

    private Grupo buscarExistente(String id) {
        Grupo g = repo.buscarPorId(id);
        if (g == null) {
            throw new IllegalArgumentException("No existe un grupo con id " + id + ".");
        }
        return g;
    }

}
