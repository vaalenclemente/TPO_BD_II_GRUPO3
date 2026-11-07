package com.logistica.cli;

import com.logistica.servicio.AuditoriaService;

public class MenuAuditoria {

    private final AuditoriaService auditoria = new AuditoriaService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Auditoria", "Ultimos registros", "Registros de un usuario",
                    "Registros de una entidad");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> Vista.listar(auditoria.listarUltimos(Consola.entero("¿Cuantos?"))));
                case 2 -> Consola.ejecutar(() -> Vista.listar(auditoria.listarPorUsuario(Selector.usuario())));
                case 3 -> Consola.ejecutar(() -> {
                    String entidad = Consola.elegir("Entidad", "envio", "tramo", "incidente", "pago", "reporte", "sensor", "sesion");
                    Vista.listar(auditoria.listarPorEntidad(entidad, Consola.texto("Id")));
                });
            }
        }
    }

}
