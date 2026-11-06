package com.logistica.servicio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.logistica.modelo.Contenedor;
import com.logistica.repositorio.ContenedorRepository;

public class ContenedorService {

    private final ContenedorRepository repo = new ContenedorRepository();

    public int alta(Contenedor c) throws SQLException {

        // a) validaciones
        validarDatos(c);

        if (c.getCodigoInternacional() == null || c.getCodigoInternacional().isBlank()) {
            throw new IllegalArgumentException("El codigo internacional es obligatorio.");
        }

        if (repo.buscarPorCodigo(c.getCodigoInternacional()) != null) {
            throw new IllegalArgumentException("Ya existe un contenedor con ese codigo.");
        }

        // b) valores del sistema
        c.setEstado("DISPONIBLE");

        // c) guardar
        int id = repo.guardar(c);
        c.setId(id);
        return id;
    }

    public void modificar(Contenedor c) throws SQLException {
        buscarExistente(c.getId());
        validarDatos(c);
        repo.actualizar(c);
    }

    public void darDeBaja(int id) throws SQLException {
        Contenedor c = buscarExistente(id);

        if ("EN_USO".equals(c.getEstado())) {
            throw new IllegalArgumentException("No se puede dar de baja un contenedor que esta en uso.");
        }

        repo.cambiarEstado(id, "BAJA");
    }

    public void reactivar(int id) throws SQLException {
        Contenedor c = buscarExistente(id);

        if (!"BAJA".equals(c.getEstado())) {
            throw new IllegalArgumentException("El contenedor no esta dado de baja.");
        }

        repo.cambiarEstado(id, "DISPONIBLE");
    }

    public void marcarEnUso(int id) throws SQLException {
        Contenedor c = buscarExistente(id);

        if (!"DISPONIBLE".equals(c.getEstado())) {
            throw new IllegalArgumentException("El contenedor no esta disponible.");
        }

        repo.cambiarEstado(id, "EN_USO");
    }

    public void liberar(int id) throws SQLException {
        buscarExistente(id);
        repo.cambiarEstado(id, "DISPONIBLE");
    }

    public List<Contenedor> listar() throws SQLException {
        return repo.listar();
    }

    public List<Contenedor> listarDisponibles() throws SQLException {
        return repo.listarPorEstado("DISPONIBLE");
    }

    public Contenedor buscarPorId(int id) throws SQLException {
        return repo.buscarPorId(id);
    }

    private void validarDatos(Contenedor c) {

        if (c.getTipo() == null || c.getTipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de contenedor es obligatorio.");
        }

        if (c.getCapacidadKg() == null || c.getCapacidadKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La capacidad tiene que ser mayor a 0.");
        }
    }

    private Contenedor buscarExistente(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Falta el id del contenedor.");
        }

        Contenedor c = repo.buscarPorId(id);
        if (c == null) {
            throw new IllegalArgumentException("No existe un contenedor con id " + id + ".");
        }
        return c;
    }

}
