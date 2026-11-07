package com.logistica.servicio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.logistica.modelo.Ciudad;
import com.logistica.modelo.Contenedor;
import com.logistica.modelo.Envio;
import com.logistica.modelo.EnvioContenedor;
import com.logistica.modelo.Parte;
import com.logistica.modelo.Tramo;
import com.logistica.repositorio.CiudadRepository;
import com.logistica.repositorio.ContenedorRepository;
import com.logistica.repositorio.EnvioContenedorRepository;
import com.logistica.repositorio.EnvioGrafoRepository;
import com.logistica.repositorio.EnvioRepository;
import com.logistica.repositorio.PaisRepository;
import com.logistica.repositorio.ParteRepository;
import com.logistica.repositorio.TramoRepository;

public class EnvioService {

    private final EnvioRepository repo = new EnvioRepository();
    private final EnvioContenedorRepository ecRepo = new EnvioContenedorRepository();
    private final TramoRepository tramoRepo = new TramoRepository();
    private final ContenedorRepository contenedorRepo = new ContenedorRepository();
    private final ParteRepository parteRepo = new ParteRepository();
    private final CiudadRepository ciudadRepo = new CiudadRepository();
    private final PaisRepository paisRepo = new PaisRepository();
    private final EnvioGrafoRepository grafo = new EnvioGrafoRepository();
    private final EventoService eventos = new EventoService();

    public int crear(Envio e, List<Integer> contenedorIds) throws SQLException {

        // a) validaciones
        Parte cliente = parteRepo.buscarPorId(validarObligatorio(e.getClienteId(), "el cliente"));
        if (cliente == null || !"CLIENTE".equals(cliente.getTipo())) {
            throw new IllegalArgumentException("La parte elegida no es un cliente.");
        }
        if (!"ACTIVO".equals(cliente.getEstado())) {
            throw new IllegalArgumentException("El cliente esta dado de baja.");
        }

        Ciudad origen = buscarCiudad(e.getOrigenCiudadId(), "la ciudad de origen");
        Ciudad destino = buscarCiudad(e.getDestinoCiudadId(), "la ciudad de destino");
        if (origen.getId().equals(destino.getId())) {
            throw new IllegalArgumentException("El origen y el destino no pueden ser la misma ciudad.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        validarDatosCarga(e, ahora);

        List<Contenedor> contenedores = validarContenedoresNuevos(contenedorIds);

        // b) valores del sistema
        e.setFechaCreacion(ahora);
        e.setFechaSalida(null);
        e.setFechaEntregaReal(null);
        e.setEstado("CREADO");
        e.setCantidadIncidentes(0);

        String origenPais = paisRepo.buscarPorId(origen.getPaisId()).getNombre();
        String destinoPais = paisRepo.buscarPorId(destino.getPaisId()).getNombre();

        // c) saga: mysql + neo4j (envio y cada contenedor)
        Saga saga = new Saga("CREAR_ENVIO", "envio");
        try {
            int id = repo.guardar(e);
            e.setId(id);
            saga.objeto(id);
            saga.paso("MYSQL_ENVIO_GUARDADO", () -> repo.borrar(id));

            grafo.crearEnvio(id, e.getEstado(), e.getFechaCreacion(), e.getPesoKg().doubleValue(),
                    cliente.getId(), cliente.getRazonSocial(),
                    origen.getId(), origen.getNombre(), origenPais,
                    destino.getId(), destino.getNombre(), destinoPais);
            saga.paso("NEO4J_ENVIO_CREADO", () -> grafo.borrarEnvio(id));

            for (Contenedor c : contenedores) {
                asignarEnSaga(saga, id, c, ahora);
            }

            String eventoId = eventos.registrar(id, "CREADO", origen.getId(),
                    "Envio creado: " + origen.getNombre() + " -> " + destino.getNombre());
            saga.paso("MONGO_EVENTO_CREADO", () -> eventos.borrar(eventoId));

            saga.completar();
            return id;

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }
    }

    public void modificar(Envio e) throws SQLException {

        Envio anterior = buscarExistente(e.getId());

        if (!"CREADO".equals(anterior.getEstado()) && !"EN_PREPARACION".equals(anterior.getEstado())) {
            throw new IllegalArgumentException("Solo se puede modificar un envio que todavia no salio.");
        }

        validarDatosCarga(e, anterior.getFechaCreacion());

        Saga saga = new Saga("MODIFICAR_ENVIO", "envio");
        try {
            saga.objeto(e.getId());

            repo.actualizar(e);
            saga.paso("MYSQL_ENVIO_MODIFICADO", () -> repo.actualizar(anterior));

            grafo.actualizarEnvio(e.getId(), anterior.getEstado(), e.getPesoKg().doubleValue());
            saga.paso("NEO4J_ENVIO_MODIFICADO", null);

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }
    }

    public void cambiarEstado(int envioId, String nuevoEstado) throws SQLException {

        Envio e = buscarExistente(envioId);
        String actual = e.getEstado();

        boolean permitido = ("CREADO".equals(actual) && "EN_PREPARACION".equals(nuevoEstado))
                || ("EN_TRANSITO".equals(actual) && "EN_ADUANA".equals(nuevoEstado))
                || ("EN_ADUANA".equals(actual) && "EN_TRANSITO".equals(nuevoEstado));

        if (!permitido) {
            throw new IllegalArgumentException("No se puede pasar de " + actual + " a " + nuevoEstado + ".");
        }

        Saga saga = new Saga("CAMBIAR_ESTADO_ENVIO", "envio");
        try {
            saga.objeto(envioId);

            repo.actualizarSeguimiento(envioId, nuevoEstado, e.getFechaSalida(), e.getFechaEntregaReal());
            saga.paso("MYSQL_ESTADO_" + nuevoEstado, () -> repo.actualizarSeguimiento(envioId, actual,
                    e.getFechaSalida(), e.getFechaEntregaReal()));

            grafo.actualizarEnvio(envioId, nuevoEstado, e.getPesoKg().doubleValue());
            saga.paso("NEO4J_ESTADO_" + nuevoEstado, () -> grafo.actualizarEnvio(envioId, actual,
                    e.getPesoKg().doubleValue()));

            String eventoId = eventos.registrar(envioId, nuevoEstado, eventos.ciudadActual(e),
                    "Cambio de estado: " + actual + " -> " + nuevoEstado);
            saga.paso("MONGO_EVENTO_" + nuevoEstado, () -> eventos.borrar(eventoId));

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }
    }

    public void asignarContenedor(int envioId, int contenedorId) throws SQLException {

        Envio e = buscarExistente(envioId);
        if (!"CREADO".equals(e.getEstado()) && !"EN_PREPARACION".equals(e.getEstado())) {
            throw new IllegalArgumentException("Solo se asignan contenedores antes de que el envio salga.");
        }

        Contenedor c = contenedorRepo.buscarPorId(contenedorId);
        if (c == null) {
            throw new IllegalArgumentException("No existe un contenedor con id " + contenedorId + ".");
        }
        if (!"DISPONIBLE".equals(c.getEstado())) {
            throw new IllegalArgumentException("El contenedor no esta disponible.");
        }
        if (ecRepo.buscar(envioId, contenedorId) != null) {
            throw new IllegalArgumentException("Ese contenedor ya fue asignado a este envio.");
        }

        Saga saga = new Saga("ASIGNAR_CONTENEDOR", "envio");
        try {
            saga.objeto(envioId);

            asignarEnSaga(saga, envioId, c, LocalDateTime.now());

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }
    }

    public void liberarContenedor(int envioId, int contenedorId) throws SQLException {

        Envio e = buscarExistente(envioId);
        if (!"CREADO".equals(e.getEstado()) && !"EN_PREPARACION".equals(e.getEstado())) {
            throw new IllegalArgumentException("Solo se quitan contenedores antes de que el envio salga.");
        }

        EnvioContenedor ec = ecRepo.buscar(envioId, contenedorId);
        if (ec == null || ec.getFechaLiberacion() != null) {
            throw new IllegalArgumentException("Ese contenedor no esta asignado a este envio.");
        }

        if (ecRepo.listarAsignados(envioId).size() == 1) {
            throw new IllegalArgumentException("Un envio tiene que tener al menos un contenedor: "
                    + "asigne otro antes de quitar este, o cancele el envio.");
        }

        ecRepo.actualizarLiberacion(envioId, contenedorId, LocalDateTime.now());
        contenedorRepo.cambiarEstado(contenedorId, "DISPONIBLE");
    }

    public List<EnvioContenedor> listarContenedores(int envioId) throws SQLException {
        return ecRepo.listarPorEnvio(envioId);
    }

    public void registrarEntrega(int envioId, LocalDateTime fecha) throws SQLException {

        Envio e = buscarExistente(envioId);
        if (!"EN_TRANSITO".equals(e.getEstado()) && !"EN_ADUANA".equals(e.getEstado())) {
            throw new IllegalArgumentException("Solo se entrega un envio que esta en viaje.");
        }
        if (fecha == null || (e.getFechaSalida() != null && fecha.isBefore(e.getFechaSalida()))) {
            throw new IllegalArgumentException("La fecha de entrega no puede ser anterior a la salida.");
        }

        Tramo ultimo = null;
        for (Tramo t : tramoRepo.listarPorEnvio(envioId)) {
            if ("CANCELADO".equals(t.getEstado())) {
                continue;
            }
            if (!"COMPLETADO".equals(t.getEstado())) {
                throw new IllegalArgumentException("Hay tramos que todavia no llegaron.");
            }
            ultimo = t;
        }
        if (ultimo == null || !ultimo.getDestinoCiudadId().equals(e.getDestinoCiudadId())) {
            throw new IllegalArgumentException("El ultimo tramo no llega al destino del envio.");
        }

        String estadoAnterior = e.getEstado();

        Saga saga = new Saga("REGISTRAR_ENTREGA", "envio");
        try {
            saga.objeto(envioId);

            repo.actualizarSeguimiento(envioId, "ENTREGADO", e.getFechaSalida(), fecha);
            saga.paso("MYSQL_ENVIO_ENTREGADO", () -> repo.actualizarSeguimiento(envioId, estadoAnterior,
                    e.getFechaSalida(), null));

            grafo.actualizarEnvio(envioId, "ENTREGADO", e.getPesoKg().doubleValue());
            saga.paso("NEO4J_ENVIO_ENTREGADO", () -> grafo.actualizarEnvio(envioId, estadoAnterior,
                    e.getPesoKg().doubleValue()));

            // antes de liberar los contenedores, para que el evento los incluya.
            String eventoId = eventos.registrar(envioId, "ENTREGADO", e.getDestinoCiudadId(), "Envio entregado");
            saga.paso("MONGO_EVENTO_ENTREGADO", () -> eventos.borrar(eventoId));

            liberarContenedores(saga, envioId, fecha);

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }
    }

    public void cancelar(int envioId) throws SQLException {

        Envio e = buscarExistente(envioId);
        if ("ENTREGADO".equals(e.getEstado()) || "CANCELADO".equals(e.getEstado())) {
            throw new IllegalArgumentException("El envio ya esta " + e.getEstado() + ".");
        }

        String estadoAnterior = e.getEstado();
        LocalDateTime ahora = LocalDateTime.now();

        Saga saga = new Saga("CANCELAR_ENVIO", "envio");
        try {
            saga.objeto(envioId);

            repo.actualizarSeguimiento(envioId, "CANCELADO", e.getFechaSalida(), e.getFechaEntregaReal());
            saga.paso("MYSQL_ENVIO_CANCELADO", () -> repo.actualizarSeguimiento(envioId, estadoAnterior,
                    e.getFechaSalida(), e.getFechaEntregaReal()));

            grafo.actualizarEnvio(envioId, "CANCELADO", e.getPesoKg().doubleValue());
            saga.paso("NEO4J_ENVIO_CANCELADO", () -> grafo.actualizarEnvio(envioId, estadoAnterior,
                    e.getPesoKg().doubleValue()));

            for (Tramo t : tramoRepo.listarPorEnvio(envioId)) {
                if (!"PENDIENTE".equals(t.getEstado()) && !"EN_CURSO".equals(t.getEstado())) {
                    continue;
                }
                tramoRepo.actualizarSeguimiento(t.getId(), "CANCELADO", t.getFechaSalida(), t.getFechaLlegadaReal());
                saga.paso("MYSQL_TRAMO_" + t.getId() + "_CANCELADO", () -> tramoRepo.actualizarSeguimiento(
                        t.getId(), t.getEstado(), t.getFechaSalida(), t.getFechaLlegadaReal()));

                grafo.actualizarTramo(t.getId(), "CANCELADO", t.getFechaSalida(), t.getFechaLlegadaEstimada());
                saga.paso("NEO4J_TRAMO_" + t.getId() + "_CANCELADO", () -> grafo.actualizarTramo(
                        t.getId(), t.getEstado(), t.getFechaSalida(), t.getFechaLlegadaEstimada()));
            }

            String eventoId = eventos.registrar(envioId, "CANCELADO", eventos.ciudadActual(e),
                    "Envio cancelado (estaba " + estadoAnterior + ")");
            saga.paso("MONGO_EVENTO_CANCELADO", () -> eventos.borrar(eventoId));

            liberarContenedores(saga, envioId, ahora);

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }
    }

    public List<Envio> listar() throws SQLException {
        return repo.listar();
    }

    public List<Envio> listarPorCliente(int clienteId) throws SQLException {
        return repo.listarPorCliente(clienteId);
    }

    public List<Envio> listarPorEstado(String estado) throws SQLException {
        return repo.listarPorEstado(estado);
    }

    public Envio buscarPorId(int id) throws SQLException {
        return repo.buscarPorId(id);
    }

    private void asignarEnSaga(Saga saga, int envioId, Contenedor c, LocalDateTime fecha) throws SQLException {
        int contenedorId = c.getId();

        EnvioContenedor ec = new EnvioContenedor(envioId, contenedorId);
        ec.setFechaAsignacion(fecha);

        ecRepo.guardar(ec);
        saga.paso("MYSQL_CONTENEDOR_" + contenedorId + "_ASIGNADO",
                () -> ecRepo.borrar(envioId, contenedorId));

        contenedorRepo.cambiarEstado(contenedorId, "EN_USO");
        saga.paso("MYSQL_CONTENEDOR_" + contenedorId + "_EN_USO",
                () -> contenedorRepo.cambiarEstado(contenedorId, "DISPONIBLE"));

        grafo.asignarContenedor(envioId, contenedorId, c.getCodigoInternacional(), fecha);
        saga.paso("NEO4J_CONTENEDOR_" + contenedorId + "_UTILIZA",
                () -> grafo.quitarContenedor(envioId, contenedorId));
    }

    private List<Contenedor> validarContenedoresNuevos(List<Integer> contenedorIds) throws SQLException {

        if (contenedorIds == null || contenedorIds.isEmpty()) {
            throw new IllegalArgumentException("Un envio tiene que llevar al menos un contenedor.");
        }

        if (new HashSet<>(contenedorIds).size() != contenedorIds.size()) {
            throw new IllegalArgumentException("Hay contenedores repetidos en la lista.");
        }

        List<Contenedor> contenedores = new ArrayList<>();
        for (Integer id : contenedorIds) {
            Contenedor c = (id == null) ? null : contenedorRepo.buscarPorId(id);
            if (c == null) {
                throw new IllegalArgumentException("No existe un contenedor con id " + id + ".");
            }
            if (!"DISPONIBLE".equals(c.getEstado())) {
                throw new IllegalArgumentException("El contenedor " + c.getCodigoInternacional()
                        + " no esta disponible.");
            }
            contenedores.add(c);
        }
        return contenedores;
    }

    private void liberarContenedores(Saga saga, int envioId, LocalDateTime fecha) throws SQLException {
        for (EnvioContenedor ec : ecRepo.listarAsignados(envioId)) {
            int contenedorId = ec.getContenedorId();

            ecRepo.actualizarLiberacion(envioId, contenedorId, fecha);
            saga.paso("MYSQL_CONTENEDOR_" + contenedorId + "_LIBERADO",
                    () -> ecRepo.actualizarLiberacion(envioId, contenedorId, null));

            contenedorRepo.cambiarEstado(contenedorId, "DISPONIBLE");
            saga.paso("MYSQL_CONTENEDOR_" + contenedorId + "_DISPONIBLE",
                    () -> contenedorRepo.cambiarEstado(contenedorId, "EN_USO"));
        }
    }

    private void validarDatosCarga(Envio e, LocalDateTime fechaCreacion) {

        if (!"ALTA".equals(e.getPrioridad()) && !"MEDIA".equals(e.getPrioridad())
                && !"BAJA".equals(e.getPrioridad())) {
            throw new IllegalArgumentException("La prioridad no es correcta (ALTA, MEDIA, BAJA).");
        }

        if (e.getFechaEntregaEstimada() == null || !e.getFechaEntregaEstimada().isAfter(fechaCreacion)) {
            throw new IllegalArgumentException("La fecha estimada de entrega tiene que ser posterior a la creacion.");
        }

        if (e.getPesoKg() == null || e.getPesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El peso tiene que ser mayor a 0.");
        }

        if (e.getVolumenM3() == null || e.getVolumenM3().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El volumen tiene que ser mayor a 0.");
        }

        if (e.getDescripcionCarga() == null || e.getDescripcionCarga().isBlank()) {
            throw new IllegalArgumentException("La descripcion de la carga es obligatoria.");
        }
    }

    private Ciudad buscarCiudad(Integer id, String nombreCampo) throws SQLException {
        Ciudad c = ciudadRepo.buscarPorId(validarObligatorio(id, nombreCampo));
        if (c == null) {
            throw new IllegalArgumentException("No existe " + nombreCampo + ".");
        }
        return c;
    }

    private int validarObligatorio(Integer id, String nombreCampo) {
        if (id == null) {
            throw new IllegalArgumentException("Falta indicar " + nombreCampo + ".");
        }
        return id;
    }

    private Envio buscarExistente(Integer id) throws SQLException {
        Envio e = repo.buscarPorId(validarObligatorio(id, "el id del envio"));
        if (e == null) {
            throw new IllegalArgumentException("No existe un envio con id " + id + ".");
        }
        return e;
    }

}
