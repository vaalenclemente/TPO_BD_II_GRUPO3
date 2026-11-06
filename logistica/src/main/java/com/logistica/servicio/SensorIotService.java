package com.logistica.servicio;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.logistica.modelo.Contenedor;
import com.logistica.modelo.SensorIot;
import com.logistica.repositorio.ContenedorRepository;
import com.logistica.repositorio.MonitoreoRedisRepository;
import com.logistica.repositorio.SensorIotRepository;

public class SensorIotService {

    private final SensorIotRepository repo = new SensorIotRepository();
    private final ContenedorRepository contenedorRepo = new ContenedorRepository();
    private final MonitoreoRedisRepository redis = new MonitoreoRedisRepository();

    public int alta(SensorIot s) throws SQLException {

        // a) validaciones
        validarDatos(s);

        if (s.getContenedorId() != null) {
            validarContenedor(s.getContenedorId());
        }

        // b) valores del sistema
        s.setEstado("ACTIVO");

        // c) guardar
        int id = repo.guardar(s);
        s.setId(id);

        if (s.getContenedorId() != null) {
            RedisOpcional.escribir("el heartbeat", () -> redis.registrarLatido(id, LocalDateTime.now()));
        }
        return id;
    }

    public void modificar(SensorIot s) throws SQLException {
        buscarExistente(s.getId());
        validarDatos(s);
        repo.actualizar(s);
    }

    public void instalarEnContenedor(int sensorId, int contenedorId) throws SQLException {
        SensorIot s = buscarExistente(sensorId);

        if ("BAJA".equals(s.getEstado())) {
            throw new IllegalArgumentException("No se puede instalar un sensor dado de baja.");
        }

        validarContenedor(contenedorId);
        repo.asignarContenedor(sensorId, contenedorId);
        RedisOpcional.escribir("el heartbeat", () -> redis.registrarLatido(sensorId, LocalDateTime.now()));
    }

    public void quitarDeContenedor(int sensorId) throws SQLException {
        buscarExistente(sensorId);
        repo.asignarContenedor(sensorId, null);
        RedisOpcional.escribir("el heartbeat", () -> redis.quitarLatido(sensorId));
    }

    public List<SensorIot> listarPorContenedor(int contenedorId) throws SQLException {
        return repo.listarPorContenedor(contenedorId);
    }

    public void cambiarEstado(int id, String estado) throws SQLException {
        SensorIot s = buscarExistente(id);

        if (!"ACTIVO".equals(estado) && !"INACTIVO".equals(estado) && !"FALLA".equals(estado)) {
            throw new IllegalArgumentException("El estado no es correcto (ACTIVO, INACTIVO, FALLA).");
        }

        if ("BAJA".equals(s.getEstado())) {
            throw new IllegalArgumentException("El sensor esta dado de baja.");
        }

        repo.cambiarEstado(id, estado);
    }

    public void darDeBaja(int id) throws SQLException {
        buscarExistente(id);
        repo.cambiarEstado(id, "BAJA");
        repo.asignarContenedor(id, null);
        RedisOpcional.escribir("el heartbeat", () -> redis.quitarLatido(id));
    }

    public List<SensorIot> listar() throws SQLException {
        return repo.listar();
    }

    public SensorIot buscarPorId(int id) throws SQLException {
        return repo.buscarPorId(id);
    }

    private void validarDatos(SensorIot s) {

        if (s.getTipo() == null || s.getTipo().isBlank()) {
            throw new IllegalArgumentException("El tipo de sensor es obligatorio.");
        }

        if (s.getFabricante() == null || s.getFabricante().isBlank()) {
            throw new IllegalArgumentException("El fabricante es obligatorio.");
        }

        if (s.getFechaInstalacion() == null) {
            throw new IllegalArgumentException("La fecha de instalacion es obligatoria.");
        }

        if (s.getFechaInstalacion().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de instalacion no puede ser futura.");
        }
    }

    private void validarContenedor(int contenedorId) throws SQLException {
        Contenedor c = contenedorRepo.buscarPorId(contenedorId);

        if (c == null) {
            throw new IllegalArgumentException("No existe un contenedor con id " + contenedorId + ".");
        }

        if ("BAJA".equals(c.getEstado())) {
            throw new IllegalArgumentException("El contenedor esta dado de baja.");
        }
    }

    private SensorIot buscarExistente(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Falta el id del sensor.");
        }

        SensorIot s = repo.buscarPorId(id);
        if (s == null) {
            throw new IllegalArgumentException("No existe un sensor con id " + id + ".");
        }
        return s;
    }

}
