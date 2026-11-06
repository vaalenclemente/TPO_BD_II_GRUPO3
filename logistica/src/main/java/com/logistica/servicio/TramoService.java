package com.logistica.servicio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.logistica.modelo.Ciudad;
import com.logistica.modelo.Envio;
import com.logistica.modelo.Parte;
import com.logistica.modelo.Tramo;
import com.logistica.modelo.Vehiculo;
import com.logistica.repositorio.CiudadRepository;
import com.logistica.repositorio.EnvioGrafoRepository;
import com.logistica.repositorio.EnvioRepository;
import com.logistica.repositorio.PaisRepository;
import com.logistica.repositorio.ParteRepository;
import com.logistica.repositorio.TramoRepository;
import com.logistica.repositorio.VehiculoRepository;

// escribe en mysql (tramo, envio) y en neo4j (pasa_por, gestionado_por)
public class TramoService {

    private final TramoRepository repo = new TramoRepository();
    private final EnvioRepository envioRepo = new EnvioRepository();
    private final VehiculoRepository vehiculoRepo = new VehiculoRepository();
    private final ParteRepository parteRepo = new ParteRepository();
    private final CiudadRepository ciudadRepo = new CiudadRepository();
    private final PaisRepository paisRepo = new PaisRepository();
    private final EnvioGrafoRepository grafo = new EnvioGrafoRepository();
    private final EventoService eventos = new EventoService();

    public int agregar(Tramo t) throws SQLException {

        // a) validaciones
        if (t.getEnvioId() == null) {
            throw new IllegalArgumentException("Falta indicar el envio.");
        }
        Envio envio = envioRepo.buscarPorId(t.getEnvioId());
        if (envio == null) {
            throw new IllegalArgumentException("No existe un envio con id " + t.getEnvioId() + ".");
        }
        if ("ENTREGADO".equals(envio.getEstado()) || "CANCELADO".equals(envio.getEstado())) {
            throw new IllegalArgumentException("El envio ya esta " + envio.getEstado() + ".");
        }

        if (!"BARCO".equals(t.getMedioTransporte()) && !"AVION".equals(t.getMedioTransporte())
                && !"TREN".equals(t.getMedioTransporte()) && !"CAMION".equals(t.getMedioTransporte())) {
            throw new IllegalArgumentException("El medio de transporte no es correcto (BARCO, AVION, TREN, CAMION).");
        }

        if (t.getVehiculoId() == null) {
            throw new IllegalArgumentException("Falta indicar el vehiculo.");
        }
        Vehiculo vehiculo = vehiculoRepo.buscarPorId(t.getVehiculoId());
        if (vehiculo == null) {
            throw new IllegalArgumentException("No existe un vehiculo con id " + t.getVehiculoId() + ".");
        }
        if (!"ACTIVO".equals(vehiculo.getEstado())) {
            throw new IllegalArgumentException("El vehiculo no esta activo.");
        }
        if (!vehiculo.getTipo().equals(t.getMedioTransporte())) {
            throw new IllegalArgumentException("El vehiculo es " + vehiculo.getTipo()
                    + " y el tramo es " + t.getMedioTransporte() + ".");
        }

        Ciudad origen = buscarCiudad(t.getOrigenCiudadId(), "la ciudad de origen");
        Ciudad destino = buscarCiudad(t.getDestinoCiudadId(), "la ciudad de destino");
        if (origen.getId().equals(destino.getId())) {
            throw new IllegalArgumentException("El origen y el destino del tramo no pueden ser la misma ciudad.");
        }

        List<Tramo> tramos = repo.listarPorEnvio(envio.getId());
        Tramo anterior = ultimoNoCancelado(tramos);
        int origenEsperado = (anterior == null) ? envio.getOrigenCiudadId() : anterior.getDestinoCiudadId();
        if (origen.getId() != origenEsperado) {
            throw new IllegalArgumentException("El tramo tiene que salir de la ciudad " + origenEsperado
                    + (anterior == null ? " (origen del envio)." : " (destino del tramo anterior)."));
        }

        if (t.getFechaSalida() == null || t.getFechaLlegadaEstimada() == null) {
            throw new IllegalArgumentException("Las fechas de salida y llegada estimada son obligatorias.");
        }
        if (!t.getFechaLlegadaEstimada().isAfter(t.getFechaSalida())) {
            throw new IllegalArgumentException("La llegada estimada tiene que ser posterior a la salida.");
        }

        if (t.getCosto() == null || t.getCosto().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo.");
        }

        // b) valores del sistema
        t.setOrden(tramos.size() + 1);
        t.setFechaLlegadaReal(null);
        t.setEstado("PENDIENTE");

        Parte operador = parteRepo.buscarPorId(vehiculo.getOperadorId());
        String destinoPais = paisRepo.buscarPorId(destino.getPaisId()).getNombre();

        // c) saga: mysql + neo4j
        Saga saga = new Saga("AGREGAR_TRAMO", "tramo");
        try {
            int id = repo.guardar(t);
            t.setId(id);
            saga.objeto(id);
            saga.paso("MYSQL_TRAMO_GUARDADO", () -> repo.borrar(id));

            grafo.agregarTramo(envio.getId(), id, t.getOrden(), t.getMedioTransporte(),
                    destino.getId(), destino.getNombre(), destinoPais,
                    operador.getId(), operador.getRazonSocial(),
                    t.getFechaSalida(), t.getFechaLlegadaEstimada(), t.getEstado());
            saga.paso("NEO4J_PASA_POR_CREADA", null);

            saga.completar();
            return id;

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }
    }

    public void registrarSalida(int tramoId, LocalDateTime fecha) throws SQLException {

        Tramo t = buscarExistente(tramoId);
        if (!"PENDIENTE".equals(t.getEstado())) {
            throw new IllegalArgumentException("Solo sale un tramo PENDIENTE (este esta " + t.getEstado() + ").");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("Falta la fecha de salida.");
        }

        Envio envio = envioRepo.buscarPorId(t.getEnvioId());
        if ("ENTREGADO".equals(envio.getEstado()) || "CANCELADO".equals(envio.getEstado())) {
            throw new IllegalArgumentException("El envio ya esta " + envio.getEstado() + ".");
        }

        for (Tramo otro : repo.listarPorEnvio(t.getEnvioId())) {
            if (otro.getOrden() < t.getOrden() && !"COMPLETADO".equals(otro.getEstado())
                    && !"CANCELADO".equals(otro.getEstado())) {
                throw new IllegalArgumentException("El tramo " + otro.getOrden() + " todavia no llego.");
            }
        }

        String estadoEnvioAnterior = envio.getEstado();
        LocalDateTime salidaEnvio = (envio.getFechaSalida() == null) ? fecha : envio.getFechaSalida();
        double pesoKg = envio.getPesoKg().doubleValue();

        Saga saga = new Saga("SALIDA_TRAMO", "tramo");
        try {
            saga.objeto(tramoId);

            repo.actualizarSeguimiento(tramoId, "EN_CURSO", fecha, null);
            saga.paso("MYSQL_TRAMO_EN_CURSO", () -> repo.actualizarSeguimiento(tramoId, "PENDIENTE",
                    t.getFechaSalida(), null));

            envioRepo.actualizarSeguimiento(envio.getId(), "EN_TRANSITO", salidaEnvio, null);
            saga.paso("MYSQL_ENVIO_EN_TRANSITO", () -> envioRepo.actualizarSeguimiento(envio.getId(),
                    estadoEnvioAnterior, envio.getFechaSalida(), null));

            grafo.actualizarTramo(tramoId, "EN_CURSO", fecha, t.getFechaLlegadaEstimada());
            saga.paso("NEO4J_TRAMO_EN_CURSO", () -> grafo.actualizarTramo(tramoId, "PENDIENTE",
                    t.getFechaSalida(), t.getFechaLlegadaEstimada()));

            grafo.actualizarEnvio(envio.getId(), "EN_TRANSITO", pesoKg);
            saga.paso("NEO4J_ENVIO_EN_TRANSITO", () -> grafo.actualizarEnvio(envio.getId(),
                    estadoEnvioAnterior, pesoKg));

            String eventoId = eventos.registrar(envio.getId(), "SALIDA_TRAMO", t.getOrigenCiudadId(),
                    "Salio el tramo " + t.getOrden() + " (" + t.getMedioTransporte() + ")");
            saga.paso("MONGO_EVENTO_SALIDA", () -> eventos.borrar(eventoId));

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }
    }

    public void registrarLlegada(int tramoId, LocalDateTime fecha) throws SQLException {

        Tramo t = buscarExistente(tramoId);
        if (!"EN_CURSO".equals(t.getEstado())) {
            throw new IllegalArgumentException("Solo llega un tramo EN_CURSO (este esta " + t.getEstado() + ").");
        }
        if (fecha == null || fecha.isBefore(t.getFechaSalida())) {
            throw new IllegalArgumentException("La fecha de llegada no puede ser anterior a la salida.");
        }

        Saga saga = new Saga("LLEGADA_TRAMO", "tramo");
        try {
            saga.objeto(tramoId);

            repo.actualizarSeguimiento(tramoId, "COMPLETADO", t.getFechaSalida(), fecha);
            saga.paso("MYSQL_TRAMO_COMPLETADO", () -> repo.actualizarSeguimiento(tramoId, "EN_CURSO",
                    t.getFechaSalida(), null));

            grafo.actualizarTramo(tramoId, "COMPLETADO", t.getFechaSalida(), fecha);
            saga.paso("NEO4J_TRAMO_COMPLETADO", () -> grafo.actualizarTramo(tramoId, "EN_CURSO",
                    t.getFechaSalida(), t.getFechaLlegadaEstimada()));

            String eventoId = eventos.registrar(t.getEnvioId(), "LLEGADA_TRAMO", t.getDestinoCiudadId(),
                    "Llego el tramo " + t.getOrden() + " (" + t.getMedioTransporte() + ")");
            saga.paso("MONGO_EVENTO_LLEGADA", () -> eventos.borrar(eventoId));

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }
    }

    public List<Tramo> listarPorEnvio(int envioId) throws SQLException {
        return repo.listarPorEnvio(envioId);
    }

    public Tramo buscarPorId(int id) throws SQLException {
        return repo.buscarPorId(id);
    }

    private Tramo ultimoNoCancelado(List<Tramo> tramos) {
        Tramo ultimo = null;
        for (Tramo t : tramos) {
            if (!"CANCELADO".equals(t.getEstado())) {
                ultimo = t;
            }
        }
        return ultimo;
    }

    private Ciudad buscarCiudad(Integer id, String nombreCampo) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Falta indicar " + nombreCampo + ".");
        }
        Ciudad c = ciudadRepo.buscarPorId(id);
        if (c == null) {
            throw new IllegalArgumentException("No existe " + nombreCampo + ".");
        }
        return c;
    }

    private Tramo buscarExistente(int id) throws SQLException {
        Tramo t = repo.buscarPorId(id);
        if (t == null) {
            throw new IllegalArgumentException("No existe un tramo con id " + id + ".");
        }
        return t;
    }

}
