package com.logistica.servicio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.logistica.modelo.Permiso;
import com.logistica.modelo.Rol;
import com.logistica.modelo.Usuario;
import com.logistica.repositorio.PermisoRepository;
import com.logistica.repositorio.RolRepository;
import com.logistica.repositorio.UsuarioRepository;

public class RolService {

    public static final String ROL_ADMIN = "ADMIN";
    private static final String PERMISO_USUARIOS = "USUARIOS";

    private final RolRepository repo = new RolRepository();
    private final PermisoRepository permisos = new PermisoRepository();
    private final UsuarioRepository usuarios = new UsuarioRepository();

    public int alta(Rol r) throws SQLException {
        return alta(r, List.of());
    }

    public int alta(Rol r, List<Integer> permisoIds) throws SQLException {

        validarDatos(r);

        if (repo.buscarPorDescripcion(r.getDescripcion()) != null) {
            throw new IllegalArgumentException("Ya existe un rol " + r.getDescripcion() + ".");
        }
        for (Integer permisoId : permisoIds) {
            buscarPermiso(permisoId);
        }

        int id = repo.guardar(r);
        r.setId(id);

        if (ROL_ADMIN.equals(r.getDescripcion())) {
            for (Permiso p : permisos.listar()) {
                permisos.asignar(id, p.getId());
            }
        } else {
            for (Integer permisoId : permisoIds) {
                if (!permisos.rolTiene(id, permisoId)) {
                    permisos.asignar(id, permisoId);
                }
            }
        }
        return id;
    }

    public void modificar(Rol r) throws SQLException {
        Rol actual = buscarExistente(r.getId());
        validarDatos(r);

        if (ROL_ADMIN.equals(actual.getDescripcion()) && !ROL_ADMIN.equals(r.getDescripcion())) {
            throw new IllegalArgumentException("El rol ADMIN no se puede renombrar.");
        }

        Rol otro = repo.buscarPorDescripcion(r.getDescripcion());
        if (otro != null && !otro.getId().equals(r.getId())) {
            throw new IllegalArgumentException("Ya existe un rol " + r.getDescripcion() + ".");
        }

        repo.actualizar(r);
    }

    public void eliminar(int id) throws SQLException {
        Rol r = buscarExistente(id);

        if (ROL_ADMIN.equals(r.getDescripcion())) {
            throw new IllegalArgumentException("El rol ADMIN no se puede eliminar.");
        }

        int cantidad = repo.contarUsuarios(id);
        if (cantidad > 0) {
            throw new IllegalArgumentException(
                    "El rol lo tienen " + cantidad + " usuarios: cambieles el rol antes de eliminarlo.");
        }

        repo.borrar(id);
    }

    public List<Rol> listar() throws SQLException {
        List<Rol> lista = repo.listar();
        for (Rol r : lista) {
            cargarPermisos(r);
        }
        return lista;
    }

    public Rol buscarPorId(int id) throws SQLException {
        Rol r = repo.buscarPorId(id);
        if (r != null) {
            cargarPermisos(r);
        }
        return r;
    }

    private void cargarPermisos(Rol r) throws SQLException {
        List<String> codigos = new ArrayList<>();
        for (Permiso p : permisos.listarDeRol(r.getId())) {
            codigos.add(p.getCodigo());
        }
        r.setPermisos(codigos);
    }

    // permisos

    public List<Permiso> listarPermisos() throws SQLException {
        return permisos.listar();
    }

    public List<Permiso> permisosDe(int rolId) throws SQLException {
        buscarExistente(rolId);
        return permisos.listarDeRol(rolId);
    }

    public List<Permiso> permisosQueLeFaltan(int rolId) throws SQLException {
        buscarExistente(rolId);
        return permisos.listarFaltantesDeRol(rolId);
    }

    // devuelve cuantos permisos se agregaron
    public int agregarPermisos(int rolId, List<Integer> permisoIds) throws SQLException {
        buscarExistente(rolId);
        if (permisoIds.isEmpty()) {
            throw new IllegalArgumentException("No se indico ningun permiso.");
        }
        for (Integer permisoId : permisoIds) {
            buscarPermiso(permisoId);
        }

        int agregados = 0;
        for (Integer permisoId : permisoIds) {
            if (!permisos.rolTiene(rolId, permisoId)) {
                permisos.asignar(rolId, permisoId);
                agregados++;
            }
        }
        return agregados;
    }

    // devuelve cuantos permisos se quitaron
    public int quitarPermisos(int rolId, List<Integer> permisoIds) throws SQLException {
        Rol r = buscarExistente(rolId);
        if (permisoIds.isEmpty()) {
            throw new IllegalArgumentException("No se indico ningun permiso.");
        }
        if (ROL_ADMIN.equals(r.getDescripcion())) {
            throw new IllegalArgumentException("El rol ADMIN tiene siempre todos los permisos.");
        }

        for (Integer permisoId : permisoIds) {
            Permiso p = buscarPermiso(permisoId);
            if (PERMISO_USUARIOS.equals(p.getCodigo()) && esRolDelUsuarioActual(rolId)) {
                throw new IllegalArgumentException(
                        "No podes quitarle " + PERMISO_USUARIOS + " a tu propio rol: perderias el acceso a este menu.");
            }
        }

        int quitados = 0;
        for (Integer permisoId : permisoIds) {
            if (permisos.quitar(rolId, permisoId)) {
                quitados++;
            }
        }
        return quitados;
    }

    private boolean esRolDelUsuarioActual(int rolId) throws SQLException {
        Integer usuarioId = UsuarioActual.get();
        if (usuarioId == null) {
            return false;
        }
        Usuario u = usuarios.buscarPorId(usuarioId);
        return u != null && u.getRolId() != null && u.getRolId() == rolId;
    }

    private Permiso buscarPermiso(Integer id) throws SQLException {
        Permiso p = id == null ? null : permisos.buscarPorId(id);
        if (p == null) {
            throw new IllegalArgumentException("No existe un permiso con id " + id + ".");
        }
        return p;
    }

    private void validarDatos(Rol r) {
        if (r.getDescripcion() == null || r.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripcion del rol es obligatoria.");
        }
        r.setDescripcion(r.getDescripcion().trim().toUpperCase());
    }

    private Rol buscarExistente(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Falta el id del rol.");
        }

        Rol r = repo.buscarPorId(id);
        if (r == null) {
            throw new IllegalArgumentException("No existe un rol con id " + id + ".");
        }
        return r;
    }

}
