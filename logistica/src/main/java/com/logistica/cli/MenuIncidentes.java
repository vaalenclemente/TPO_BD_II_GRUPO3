package com.logistica.cli;

import com.logistica.modelo.Regla;
import com.logistica.servicio.AlertaService;
import com.logistica.servicio.IncidenteService;
import com.logistica.servicio.ReglaService;

public class MenuIncidentes {

    private final ReglaService reglas = new ReglaService();
    private final IncidenteService incidentes = new IncidenteService();
    private final AlertaService alertas = new AlertaService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Reglas e incidentes",
                    "Listar reglas", "Alta de regla", "Modificar regla", "Activar regla", "Desactivar regla",
                    "Incidentes abiertos", "Incidentes de un envio", "Abrir incidente",
                    "Sumar alertas a un incidente", "Cerrar incidente");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> Vista.listar(reglas.listar()));
                case 2 -> Consola.ejecutar(this::altaRegla);
                case 3 -> Consola.ejecutar(this::modificarRegla);
                case 4 -> Consola.ejecutar(() -> {
                    reglas.activar(elegirRegla());
                    Consola.mostrar("Regla activada.");
                });
                case 5 -> Consola.ejecutar(() -> {
                    reglas.desactivar(elegirRegla());
                    Consola.mostrar("Regla desactivada.");
                });
                case 6 -> Consola.ejecutar(() -> Vista.listar(incidentes.listarAbiertos()));
                case 7 -> Consola.ejecutar(() -> Vista.listar(incidentes.listarPorEnvio(Selector.envio())));
                case 8 -> Consola.ejecutar(() -> {
                    int envioId = Selector.envio();
                    String tipo = Consola.elegir("Tipo", "TEMPERATURA", "HUMEDAD", "VIBRACION", "BATERIA", "DEMORA",
                            "DANIO", "ROBO", "ADUANA", "OTRO");
                    String severidad = Selector.severidad();
                    String descripcion = Consola.texto("Descripcion");
                    Consola.mostrar("Alertas del envio:");
                    Vista.listar(alertas.listarPorEnvio(envioId));
                    String id = incidentes.abrir(envioId, tipo, severidad, descripcion, Consola.textos("Ids de alertas"));
                    Consola.mostrar("Incidente abierto con id " + id + ".");
                });
                case 9 -> Consola.ejecutar(() -> {
                    String id = elegirIncidente();
                    Consola.mostrar("Alertas sin cerrar:");
                    Vista.listar(alertas.listarNoCerradas());
                    incidentes.sumarAlertas(id, Consola.textos("Ids de alertas"));
                    Consola.mostrar("Alertas sumadas.");
                });
                case 10 -> Consola.ejecutar(() -> {
                    String id = elegirIncidente();
                    incidentes.cerrar(id, Consola.texto("Resolucion"));
                    Consola.mostrar("Incidente cerrado.");
                });
            }
        }
    }

    private void altaRegla() throws Exception {
        String nombre = Consola.texto("Nombre");
        String metrica = Consola.elegir("Metrica", "TEMPERATURA", "HUMEDAD", "VIBRACION", "BATERIA", "INACTIVIDAD");
        String operador = Consola.elegir("Se dispara cuando el valor es", "MAYOR", "MENOR");
        var valor = Consola.decimal("Valor limite" + ("INACTIVIDAD".equals(metrica) ? " (minutos)" : ""));
        String severidad = Selector.severidad();
        Integer envioId = Selector.envioOpcional();
        int id = reglas.alta(new Regla(nombre, metrica, operador, valor, severidad, envioId));
        Consola.mostrar("Regla creada con id " + id + ".");
    }

    private void modificarRegla() throws Exception {
        Regla r = reglas.buscarPorId(elegirRegla());
        if (r == null) {
            throw new IllegalArgumentException("No existe esa regla.");
        }
        r.setNombre(Consola.texto("Nombre", r.getNombre()));
        r.setValor(Consola.decimal("Valor limite", r.getValor()));
        if (Consola.confirmar("¿Cambiar la severidad (" + r.getSeveridad() + ")?")) {
            r.setSeveridad(Selector.severidad());
        }
        reglas.modificar(r);
        Consola.mostrar("Modificada.");
    }

    private int elegirRegla() throws Exception {
        Vista.listar(reglas.listar());
        return Consola.entero("Id de la regla");
    }

    private String elegirIncidente() throws Exception {
        Vista.listar(incidentes.listarAbiertos());
        return Consola.texto("Id del incidente");
    }

}
