package com.logistica.servicio;

import com.logistica.modelo.Envio;
import com.logistica.modelo.Parte;
import com.logistica.modelo.Vehiculo;
import com.logistica.repositorio.EnvioRepository;
import com.logistica.repositorio.PaisRepository;
import com.logistica.repositorio.ParteRepository;
import com.logistica.repositorio.VehiculoRepository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ParteService {

    private final ParteRepository repo = new ParteRepository();
    private final PaisRepository paisRepo = new PaisRepository();
    private final EnvioRepository envioRepo = new EnvioRepository();
    private final VehiculoRepository vehiculoRepo = new VehiculoRepository();

    public int alta(Parte p) throws SQLException {
        // validaciones

        validarTipo(p.getTipo());

        if (p.getCuit() == null || p.getCuit().isBlank()) {
            throw new IllegalArgumentException("El CUIT es obligatorio.");
        }

        if (repo.buscarPorCuit(p.getCuit()) != null) {
            throw new IllegalArgumentException("El CUIT ya existe en la base de datos.");
        }

        validarDatos(p);

        // valores del sistema

        p.setFechaAlta(LocalDateTime.now());
        p.setEstado("ACTIVO");

        // guardar

        int id = repo.guardar(p);
        p.setId(id);
        return id;
    }

    public void modificar(Parte p) throws SQLException {
        Parte anterior = buscarExistente(p.getId());

        p.setTipo(anterior.getTipo());
        validarDatos(p);

        repo.actualizar(p);
    }

    public void darDeBaja(int id) throws SQLException {
        Parte p = buscarExistente(id);

        if ("INACTIVO".equals(p.getEstado())) {
            throw new IllegalArgumentException("La parte ya está dada de baja.");
        }

        if ("CLIENTE".equals(p.getTipo())) {
            for (Envio e : envioRepo.listarPorCliente(id)) {
                if (!"ENTREGADO".equals(e.getEstado()) && !"CANCELADO".equals(e.getEstado())) {
                    throw new IllegalArgumentException("El cliente tiene envíos en curso (envío " + e.getId() + ").");
                }
            }
        }

        if ("OPERADOR".equals(p.getTipo())) {
            for (Vehiculo v : vehiculoRepo.listarPorOperador(id)) {
                if (!"INACTIVO".equals(v.getEstado())) {
                    throw new IllegalArgumentException("El operador tiene vehículos sin dar de baja (vehículo "
                            + v.getIdentificacion() + ").");
                }
            }
        }

        repo.cambiarEstado(id, "INACTIVO");
    }

    public void reactivar(int id) throws SQLException {
        Parte p = buscarExistente(id);

        if ("ACTIVO".equals(p.getEstado())) {
            throw new IllegalArgumentException("La parte ya está activa.");
        }

        repo.cambiarEstado(id, "ACTIVO");
    }

    public List<Parte> listarPorTipo(String tipo) throws SQLException {
        validarTipo(tipo);
        return repo.listarPorTipo(tipo);
    }

    public Parte buscarPorId(int id) throws SQLException {
        return repo.buscarPorId(id);
    }

    private void validarTipo(String tipo) {
        if (!"CLIENTE".equals(tipo) && !"OPERADOR".equals(tipo) && !"PROVEEDOR".equals(tipo)) {
            throw new IllegalArgumentException("El tipo no es correcto (CLIENTE, OPERADOR, PROVEEDOR)");
        }
    }

    private void validarDatos(Parte p) throws SQLException {

        if (p.getRazonSocial() == null || p.getRazonSocial().isBlank()) {
            throw new IllegalArgumentException("La razón social es obligatoria.");
        }

        if (p.getEmail() == null || p.getEmail().isBlank()) {
            throw new IllegalArgumentException("El Mail es obligatorio.");
        }

        if (p.getPaisId() == null) {
            throw new IllegalArgumentException("El Pais de origen es obligatorio.");
        }

        if (paisRepo.buscarPorId(p.getPaisId()) == null) {
            throw new IllegalArgumentException("No existe un país con id " + p.getPaisId() + ".");
        }

        if (p.getRubro() != null && !"PROVEEDOR".equals(p.getTipo())) {
            throw new IllegalArgumentException("Solo los proveedores pueden tener rubro.");
        }
    }

    private Parte buscarExistente(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Falta el id de la parte.");
        }

        Parte p = repo.buscarPorId(id);
        if (p == null) {
            throw new IllegalArgumentException("No existe una parte con id " + id + ".");
        }
        return p;
    }
}
