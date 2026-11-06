package com.logistica.cli;

import java.time.LocalDateTime;
import java.util.List;

import com.logistica.modelo.Envio;
import com.logistica.modelo.Tramo;
import com.logistica.servicio.ContenedorService;
import com.logistica.servicio.EnvioService;
import com.logistica.servicio.EventoService;
import com.logistica.servicio.TramoService;
import com.logistica.servicio.VehiculoService;

public class MenuEnvios {

    private final EnvioService envios = new EnvioService();
    private final TramoService tramos = new TramoService();
    private final EventoService eventos = new EventoService();
    private final ContenedorService contenedores = new ContenedorService();
    private final VehiculoService vehiculos = new VehiculoService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Envíos y tramos",
                    "Listar envíos", "Ver un envío (contenedores, tramos y eventos)", "Crear envío",
                    "Modificar envío", "Cambiar estado (preparación / aduana / tránsito)",
                    "Asignar contenedor", "Quitar contenedor", "Agregar tramo", "Registrar salida de un tramo",
                    "Registrar llegada de un tramo", "Registrar entrega", "Cancelar envío",
                    "Registrar evento manual");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(this::listar);
                case 2 -> Consola.ejecutar(this::ver);
                case 3 -> Consola.ejecutar(this::crear);
                case 4 -> Consola.ejecutar(this::modificar);
                case 5 -> Consola.ejecutar(() -> {
                    int id = Selector.envio();
                    envios.cambiarEstado(id, Consola.elegir("Nuevo estado", "EN_PREPARACION", "EN_ADUANA", "EN_TRANSITO"));
                    Consola.mostrar("Estado cambiado.");
                });
                case 6 -> Consola.ejecutar(() -> {
                    int id = Selector.envio();
                    Consola.listar(contenedores.listarDisponibles());
                    envios.asignarContenedor(id, Consola.entero("Id del contenedor"));
                    Consola.mostrar("Contenedor asignado.");
                });
                case 7 -> Consola.ejecutar(() -> {
                    int id = Selector.envio();
                    Consola.listar(envios.listarContenedores(id));
                    envios.liberarContenedor(id, Consola.entero("Id del contenedor"));
                    Consola.mostrar("Contenedor quitado.");
                });
                case 8 -> Consola.ejecutar(this::agregarTramo);
                case 9 -> Consola.ejecutar(() -> {
                    int tramoId = elegirTramo();
                    tramos.registrarSalida(tramoId, Consola.fechaHoraOAhora("Fecha de salida"));
                    Consola.mostrar("Salida registrada.");
                });
                case 10 -> Consola.ejecutar(() -> {
                    int tramoId = elegirTramo();
                    tramos.registrarLlegada(tramoId, Consola.fechaHoraOAhora("Fecha de llegada"));
                    Consola.mostrar("Llegada registrada.");
                });
                case 11 -> Consola.ejecutar(() -> {
                    int id = Selector.envio();
                    envios.registrarEntrega(id, Consola.fechaHoraOAhora("Fecha de entrega"));
                    Consola.mostrar("Entrega registrada.");
                });
                case 12 -> Consola.ejecutar(() -> {
                    int id = Selector.envio();
                    if (Consola.confirmar("¿Seguro que quiere cancelar el envío " + id + "?")) {
                        envios.cancelar(id);
                        Consola.mostrar("Envío cancelado.");
                    }
                });
                case 13 -> Consola.ejecutar(() -> {
                    int id = Selector.envio();
                    String tipo = Consola.elegir("Tipo de evento", "DEMORA", "INSPECCION", "OTRO");
                    Integer ciudadId = Selector.ciudadOpcional("la ciudad");
                    eventos.registrarManual(id, tipo, ciudadId, Consola.texto("Descripción"));
                    Consola.mostrar("Evento registrado.");
                });
            }
        }
    }

    private void listar() throws Exception {
        String filtro = Consola.elegir("Listar", "Todos", "Por estado", "Por cliente");
        switch (filtro) {
            case "Todos" -> Consola.listar(envios.listar());
            case "Por estado" -> Consola.listar(envios.listarPorEstado(Consola.elegir("Estado", "CREADO",
                    "EN_PREPARACION", "EN_TRANSITO", "EN_ADUANA", "ENTREGADO", "CANCELADO")));
            default -> Consola.listar(envios.listarPorCliente(Selector.parte("CLIENTE")));
        }
    }

    private void ver() throws Exception {
        int id = Selector.envio();
        Envio e = envios.buscarPorId(id);
        if (e == null) {
            throw new IllegalArgumentException("No existe ese envío.");
        }
        Consola.mostrar(e.toString());
        Consola.mostrar("-- Contenedores");
        Consola.listar(envios.listarContenedores(id));
        Consola.mostrar("-- Tramos");
        Consola.listar(tramos.listarPorEnvio(id));
        Consola.mostrar("-- Eventos");
        Consola.listar(eventos.listarPorEnvio(id));
    }

    private void crear() throws Exception {
        int clienteId = Selector.parte("CLIENTE");
        int origen = Selector.ciudad("Ciudad de origen");
        int destino = Selector.ciudad("Ciudad de destino");
        LocalDateTime estimada = Consola.fechaHora("Fecha estimada de entrega");
        String prioridad = Consola.elegir("Prioridad", "ALTA", "MEDIA", "BAJA");
        var peso = Consola.decimal("Peso (kg)");
        var volumen = Consola.decimal("Volumen (m3)");
        String descripcion = Consola.texto("Descripción de la carga");
        Consola.mostrar("Contenedores disponibles:");
        Consola.listar(contenedores.listarDisponibles());
        List<Integer> contenedorIds = Consola.enteros("Ids de los contenedores");

        Envio e = new Envio(clienteId, origen, destino, estimada, prioridad, peso, volumen, descripcion);
        int id = envios.crear(e, contenedorIds);
        Consola.mostrar("Envío creado con id " + id + ".");
    }

    private void modificar() throws Exception {
        Envio e = envios.buscarPorId(Selector.envio());
        if (e == null) {
            throw new IllegalArgumentException("No existe ese envío.");
        }
        if (Consola.confirmar("¿Cambiar la fecha estimada de entrega (" + e.getFechaEntregaEstimada() + ")?")) {
            e.setFechaEntregaEstimada(Consola.fechaHora("Fecha estimada de entrega"));
        }
        if (Consola.confirmar("¿Cambiar la prioridad (" + e.getPrioridad() + ")?")) {
            e.setPrioridad(Consola.elegir("Prioridad", "ALTA", "MEDIA", "BAJA"));
        }
        e.setPesoKg(Consola.decimal("Peso (kg)", e.getPesoKg()));
        e.setVolumenM3(Consola.decimal("Volumen (m3)", e.getVolumenM3()));
        e.setDescripcionCarga(Consola.texto("Descripción de la carga", e.getDescripcionCarga()));
        envios.modificar(e);
        Consola.mostrar("Modificado.");
    }

    private void agregarTramo() throws Exception {
        int envioId = Selector.envio();
        Consola.mostrar("Tramos actuales:");
        Consola.listar(tramos.listarPorEnvio(envioId));
        String medio = Selector.medio();
        int origen = Selector.ciudad("Ciudad de origen del tramo");
        int destino = Selector.ciudad("Ciudad de destino del tramo");
        Consola.listar(vehiculos.listar());
        int vehiculoId = Consola.entero("Id del vehículo (" + medio + ")");
        LocalDateTime salida = Consola.fechaHora("Fecha de salida prevista");
        LocalDateTime llegada = Consola.fechaHora("Fecha de llegada estimada");
        var costo = Consola.decimal("Costo");

        int id = tramos.agregar(new Tramo(envioId, medio, origen, destino, vehiculoId, salida, llegada, costo));
        Consola.mostrar("Tramo creado con id " + id + ".");
    }

    private int elegirTramo() throws Exception {
        int envioId = Selector.envio();
        List<Tramo> lista = tramos.listarPorEnvio(envioId);
        Consola.listar(lista);
        return Consola.entero("Id del tramo");
    }

}
