package com.logistica.servicio;

import java.sql.SQLException;
import java.util.List;

import com.logistica.modelo.Rol;
import com.logistica.repositorio.RolRepository;

public class RolService {

    private final RolRepository repo = new RolRepository();

    public int alta(Rol r) throws SQLException {

        validarDatos(r);

        if (repo.buscarPorDescripcion(r.getDescripcion()) != null) {
            throw new IllegalArgumentException("Ya existe un rol " + r.getDescripcion() + ".");
        }

        int id = repo.guardar(r);
        r.setId(id);
        return id;
    }

    public void modificar(Rol r) throws SQLException {
        buscarExistente(r.getId());
        validarDatos(r);

        Rol otro = repo.buscarPorDescripcion(r.getDescripcion());
        if (otro != null && !otro.getId().equals(r.getId())) {
            throw new IllegalArgumentException("Ya existe un rol " + r.getDescripcion() + ".");
        }

        repo.actualizar(r);
    }

    public void eliminar(int id) throws SQLException {
        buscarExistente(id);

        int usuarios = repo.contarUsuarios(id);
        if (usuarios > 0) {
            throw new IllegalArgumentException(
                    "El rol lo tienen " + usuarios + " usuarios: cambieles el rol antes de eliminarlo.");
        }

        repo.borrar(id);
    }

    public List<Rol> listar() throws SQLException {
        return repo.listar();
    }

    public Rol buscarPorId(int id) throws SQLException {
        return repo.buscarPorId(id);
    }

    private void validarDatos(Rol r) {
        if (r.getDescripcion() == null || r.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripcion del rol es obligatoria.");
        }
        r.setDescripcion(r.getDescripcion().trim().toUpperCase());

        if (r.getNivelPermisos() == null || r.getNivelPermisos() < 1) {
            throw new IllegalArgumentException("El nivel de permisos tiene que ser 1 o mas.");
        }
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
