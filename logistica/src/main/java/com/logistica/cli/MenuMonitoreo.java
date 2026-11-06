package com.logistica.cli;

import java.util.List;

import com.logistica.modelo.Alerta;
import com.logistica.modelo.LecturaSensor;
import com.logistica.servicio.AlertaService;
import com.logistica.servicio.LecturaSensorService;
import com.logistica.servicio.MonitoreoService;

public class MenuMonitoreo {

    private final LecturaSensorService lecturas = new LecturaSensorService();
    private final AlertaService alertas = new AlertaService();
    private final MonitoreoService monitoreo = new MonitoreoService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Monitoreo",
                    "Registrar lectura de un sensor", "Ultima lectura de un sensor", "Estado actual de un contenedor",
                    "Ubicacion actual de un envio", "Historial de lecturas de un sensor",
                    "Recorrido GPS de un contenedor", "Anomalias de temperatura", "Alertas activas",
                    "Reconocer una alerta", "Cerrar una alerta", "Detectar sensores inactivos",
                    "Reconstruir Redis desde MongoDB");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(this::registrarLectura);
                case 2 -> Consola.ejecutar(() -> mostrarLectura(lecturas.ultimaLectura(Selector.sensor())));
                case 3 -> Consola.ejecutar(() -> mostrarLectura(lecturas.estadoContenedor(Selector.contenedor())));
                case 4 -> Consola.ejecutar(() -> {
                    LecturaSensor l = lecturas.ubicacionEnvio(Selector.envio());
                    Consola.mostrar(l == null ? "Sin posicion registrada."
                            : "Lat " + l.getLatitud() + ", long " + l.getLongitud() + " (" + l.getFechaHora()
                                    + ", contenedor " + l.getContenedorId() + ")");
                });
                case 5 -> Consola.ejecutar(() -> {
                    int sensorId = Selector.sensor();
                    var desde = Consola.fechaOpcional("Desde");
                    var hasta = Consola.fechaOpcional("Hasta");
                    Consola.listar(lecturas.historialSensor(sensorId, Consola.inicioDelDia(desde), Consola.finDelDia(hasta)));
                });
                case 6 -> Consola.ejecutar(() -> {
                    int contenedorId = Selector.contenedor();
                    var desde = Consola.fechaOpcional("Desde");
                    var hasta = Consola.fechaOpcional("Hasta");
                    for (LecturaSensor l : lecturas.recorridoContenedor(contenedorId, Consola.inicioDelDia(desde),
                            Consola.finDelDia(hasta))) {
                        if (l.getLatitud() != null) {
                            Consola.mostrar(l.getFechaHora() + " | lat " + l.getLatitud() + ", long " + l.getLongitud()
                                    + (l.getEnvioId() == null ? "" : " | envio " + l.getEnvioId()));
                        }
                    }
                });
                case 7 -> Consola.ejecutar(() -> {
                    var desde = Consola.fechaOpcional("Desde");
                    var hasta = Consola.fechaOpcional("Hasta");
                    Consola.listar(lecturas.anomalias("TEMPERATURA", Consola.inicioDelDia(desde), Consola.finDelDia(hasta)));
                });
                case 8 -> Consola.ejecutar(() -> Consola.listar(alertas.listarNoCerradas()));
                case 9 -> Consola.ejecutar(() -> {
                    alertas.reconocer(elegirAlerta());
                    Consola.mostrar("Alerta reconocida.");
                });
                case 10 -> Consola.ejecutar(() -> {
                    alertas.cerrar(elegirAlerta());
                    Consola.mostrar("Alerta cerrada.");
                });
                case 11 -> Consola.ejecutar(() -> {
                    List<Alerta> nuevas = monitoreo.detectarSensoresInactivos();
                    Consola.mostrar(nuevas.size() + " sensores inactivos nuevos.");
                    Consola.listar(nuevas);
                });
                case 12 -> Consola.ejecutar(() -> Consola.mostrar(monitoreo.reconstruir()));
            }
        }
    }

    private void registrarLectura() throws Exception {
        int sensorId = Selector.sensor();
        Double temperatura = Consola.medicion("Temperatura (°C)");
        Double humedad = Consola.medicion("Humedad (%)");
        Double vibracion = Consola.medicion("Vibracion");
        Double bateria = Consola.medicion("Bateria (%)");
        Double latitud = Consola.medicion("Latitud");
        Double longitud = (latitud == null) ? null : Consola.medicion("Longitud");

        LecturaSensor l = new LecturaSensor(sensorId, temperatura, humedad, vibracion, bateria, latitud, longitud);
        l.setFechaHora(Consola.fechaHoraOAhora("Fecha y hora de la lectura"));

        List<Alerta> nuevas = lecturas.registrar(l);
        Consola.mostrar("Lectura registrada." + (l.getAnomaliaMetrica() == null ? "" : " ANOMALIA de " + l.getAnomaliaMetrica() + "."));
        for (Alerta a : nuevas) {
            Consola.mostrar("ALERTA: " + a + (a.getIncidenteId() == null ? "" : " → incidente " + a.getIncidenteId()));
        }
    }

    private void mostrarLectura(LecturaSensor l) {
        Consola.mostrar(l == null ? "Sin lecturas." : l.toString()
                + (l.getContenedorId() == null ? "" : " | contenedor " + l.getContenedorId())
                + (l.getEnvioId() == null ? "" : " | envio " + l.getEnvioId()));
    }

    private String elegirAlerta() {
        Consola.listar(alertas.listarNoCerradas());
        return Consola.texto("Id de la alerta");
    }

}
