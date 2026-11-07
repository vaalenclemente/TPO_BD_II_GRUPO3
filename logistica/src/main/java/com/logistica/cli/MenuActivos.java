package com.logistica.cli;

import com.logistica.modelo.Contenedor;
import com.logistica.modelo.SensorIot;
import com.logistica.modelo.Vehiculo;
import com.logistica.servicio.ContenedorService;
import com.logistica.servicio.SensorIotService;
import com.logistica.servicio.VehiculoService;

public class MenuActivos {

    private final ContenedorService contenedores = new ContenedorService();
    private final SensorIotService sensores = new SensorIotService();
    private final VehiculoService vehiculos = new VehiculoService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Activos", "Contenedores", "Sensores", "Vehiculos");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> contenedores();
                case 2 -> sensores();
                case 3 -> vehiculos();
            }
        }
    }

    private void contenedores() {
        while (true) {
            int opcion = Consola.menu("Contenedores", "Listar", "Listar disponibles", "Alta", "Modificar",
                    "Dar de baja", "Reactivar");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> Vista.listar(contenedores.listar()));
                case 2 -> Consola.ejecutar(() -> Vista.listar(contenedores.listarDisponibles()));
                case 3 -> Consola.ejecutar(() -> {
                    int id = contenedores.alta(new Contenedor(Consola.texto("Codigo internacional"),
                            Consola.texto("Tipo"), Consola.decimal("Capacidad (kg)")));
                    Consola.mostrar("Contenedor creado con id " + id + ".");
                });
                case 4 -> Consola.ejecutar(() -> {
                    Contenedor c = contenedores.buscarPorId(Selector.contenedor());
                    if (c == null) {
                        throw new IllegalArgumentException("No existe ese contenedor.");
                    }
                    c.setTipo(Consola.texto("Tipo", c.getTipo()));
                    c.setCapacidadKg(Consola.decimal("Capacidad (kg)", c.getCapacidadKg()));
                    contenedores.modificar(c);
                    Consola.mostrar("Modificado.");
                });
                case 5 -> Consola.ejecutar(() -> {
                    contenedores.darDeBaja(Selector.contenedor());
                    Consola.mostrar("Dado de baja.");
                });
                case 6 -> Consola.ejecutar(() -> {
                    contenedores.reactivar(Selector.contenedor());
                    Consola.mostrar("Reactivado.");
                });
            }
        }
    }

    private void sensores() {
        while (true) {
            int opcion = Consola.menu("Sensores", "Listar", "Listar los de un contenedor", "Alta", "Modificar",
                    "Instalar en un contenedor", "Quitar del contenedor", "Cambiar estado", "Dar de baja");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> Vista.listar(sensores.listar()));
                case 2 -> Consola.ejecutar(() -> Vista.listar(sensores.listarPorContenedor(Selector.contenedor())));
                case 3 -> Consola.ejecutar(() -> {
                    String tipo = Consola.texto("Tipo");
                    String fabricante = Consola.texto("Fabricante");
                    var fecha = Consola.fecha("Fecha de instalacion");
                    Integer contenedorId = Consola.confirmar("¿Instalarlo ya en un contenedor?") ? Selector.contenedor() : null;
                    int id = sensores.alta(new SensorIot(tipo, fabricante, fecha, contenedorId));
                    Consola.mostrar("Sensor creado con id " + id + ".");
                });
                case 4 -> Consola.ejecutar(() -> {
                    SensorIot s = sensores.buscarPorId(Selector.sensor());
                    if (s == null) {
                        throw new IllegalArgumentException("No existe ese sensor.");
                    }
                    s.setTipo(Consola.texto("Tipo", s.getTipo()));
                    s.setFabricante(Consola.texto("Fabricante", s.getFabricante()));
                    sensores.modificar(s);
                    Consola.mostrar("Modificado.");
                });
                case 5 -> Consola.ejecutar(() -> {
                    int sensorId = Selector.sensor();
                    sensores.instalarEnContenedor(sensorId, Selector.contenedor());
                    Consola.mostrar("Instalado.");
                });
                case 6 -> Consola.ejecutar(() -> {
                    sensores.quitarDeContenedor(Selector.sensor());
                    Consola.mostrar("Quitado.");
                });
                case 7 -> Consola.ejecutar(() -> {
                    int sensorId = Selector.sensor();
                    sensores.cambiarEstado(sensorId, Consola.elegir("Estado", "ACTIVO", "INACTIVO", "FALLA"));
                    Consola.mostrar("Estado cambiado.");
                });
                case 8 -> Consola.ejecutar(() -> {
                    sensores.darDeBaja(Selector.sensor());
                    Consola.mostrar("Dado de baja.");
                });
            }
        }
    }

    private void vehiculos() {
        while (true) {
            int opcion = Consola.menu("Vehiculos", "Listar", "Listar los de un operador", "Alta", "Modificar",
                    "Cambiar estado", "Dar de baja");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> Vista.listar(vehiculos.listar()));
                case 2 -> Consola.ejecutar(() -> Vista.listar(vehiculos.listarPorOperador(Selector.parte("OPERADOR"))));
                case 3 -> Consola.ejecutar(() -> {
                    String tipo = Selector.medio();
                    String identificacion = Consola.texto("Identificacion (patente, matricula)");
                    var capacidad = Consola.decimal("Capacidad (kg)");
                    int operadorId = Selector.parte("OPERADOR");
                    int id = vehiculos.alta(new Vehiculo(tipo, identificacion, capacidad, operadorId));
                    Consola.mostrar("Vehiculo creado con id " + id + ".");
                });
                case 4 -> Consola.ejecutar(() -> {
                    Vista.listar(vehiculos.listar());
                    Vehiculo v = vehiculos.buscarPorId(Consola.entero("Id del vehiculo"));
                    if (v == null) {
                        throw new IllegalArgumentException("No existe ese vehiculo.");
                    }
                    v.setIdentificacion(Consola.texto("Identificacion", v.getIdentificacion()));
                    v.setCapacidadKg(Consola.decimal("Capacidad (kg)", v.getCapacidadKg()));
                    vehiculos.modificar(v);
                    Consola.mostrar("Modificado.");
                });
                case 5 -> Consola.ejecutar(() -> {
                    Vista.listar(vehiculos.listar());
                    int id = Consola.entero("Id del vehiculo");
                    vehiculos.cambiarEstado(id, Consola.elegir("Estado", "ACTIVO", "INACTIVO", "EN_MANTENIMIENTO"));
                    Consola.mostrar("Estado cambiado.");
                });
                case 6 -> Consola.ejecutar(() -> {
                    Vista.listar(vehiculos.listar());
                    vehiculos.darDeBaja(Consola.entero("Id del vehiculo"));
                    Consola.mostrar("Dado de baja.");
                });
            }
        }
    }

}
