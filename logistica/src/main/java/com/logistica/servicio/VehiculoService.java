package com.logistica.servicio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.logistica.modelo.Parte;
import com.logistica.modelo.Vehiculo;
import com.logistica.repositorio.ParteRepository;
import com.logistica.repositorio.VehiculoRepository;

public class VehiculoService {

    private final VehiculoRepository repo = new VehiculoRepository();
    private final ParteRepository parteRepo = new ParteRepository();

    public int alta(Vehiculo v) throws SQLException {

        // a) validaciones
        validarDatos(v);

        if (v.getIdentificacion() == null || v.getIdentificacion().isBlank()) {
            throw new IllegalArgumentException("La identificación es obligatoria.");
        }

        if (repo.buscarPorIdentificacion(v.getIdentificacion()) != null) {
            throw new IllegalArgumentException("Ya existe un vehículo con esa identificación.");
        }

        // b) valores del sistema
        v.setEstado("ACTIVO");

        // c) guardar
        int id = repo.guardar(v);
        v.setId(id);
        return id;
    }

    public void modificar(Vehiculo v) throws SQLException {
        buscarExistente(v.getId());
        validarDatos(v);
        repo.actualizar(v);
    }

    public void cambiarEstado(int id, String estado) throws SQLException {
        buscarExistente(id);

        if (!"ACTIVO".equals(estado) && !"INACTIVO".equals(estado)
                && !"EN_MANTENIMIENTO".equals(estado)) {
            throw new IllegalArgumentException("El estado no es correcto (ACTIVO, INACTIVO, EN_MANTENIMIENTO).");
        }

        repo.cambiarEstado(id, estado);
    }

    public void darDeBaja(int id) throws SQLException {
        cambiarEstado(id, "INACTIVO");
    }

    public List<Vehiculo> listar() throws SQLException {
        return repo.listar();
    }

    public List<Vehiculo> listarPorOperador(int operadorId) throws SQLException {
        return repo.listarPorOperador(operadorId);
    }

    public Vehiculo buscarPorId(int id) throws SQLException {
        return repo.buscarPorId(id);
    }

    private void validarDatos(Vehiculo v) throws SQLException {

        if (!"BARCO".equals(v.getTipo()) && !"AVION".equals(v.getTipo())
                && !"TREN".equals(v.getTipo()) && !"CAMION".equals(v.getTipo())) {
            throw new IllegalArgumentException("El tipo no es correcto (BARCO, AVION, TREN, CAMION).");
        }

        if (v.getCapacidadKg() == null || v.getCapacidadKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La capacidad tiene que ser mayor a 0.");
        }

        if (v.getOperadorId() == null) {
            throw new IllegalArgumentException("El operador logístico es obligatorio.");
        }

        Parte operador = parteRepo.buscarPorId(v.getOperadorId());

        if (operador == null || !"OPERADOR".equals(operador.getTipo())) {
            throw new IllegalArgumentException("La parte elegida no es un operador logístico.");
        }

        if (!"ACTIVO".equals(operador.getEstado())) {
            throw new IllegalArgumentException("El operador está dado de baja.");
        }
    }

    private Vehiculo buscarExistente(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Falta el id del vehículo.");
        }

        Vehiculo v = repo.buscarPorId(id);
        if (v == null) {
            throw new IllegalArgumentException("No existe un vehículo con id " + id + ".");
        }
        return v;
    }

}
