package com.logistica.cli;

import com.logistica.servicio.GrafoService;

public class MenuRed {

    private final GrafoService grafo = new GrafoService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Red y recorridos (Neo4j)",
                    "Ruta completa de un envio (seguimiento de trayectos)",
                    "Reconstruccion del recorrido de un contenedor",
                    "Red de relaciones clientes - operadores - envios");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> Consola.listar(grafo.rutaEnvio(Selector.envio())));
                case 2 -> Consola.ejecutar(() -> Consola.listar(grafo.recorridoContenedor(Selector.contenedor())));
                case 3 -> Consola.ejecutar(() -> {
                    Integer cliente = Selector.parteOpcional("CLIENTE");
                    Integer operador = Selector.parteOpcional("OPERADOR");
                    Consola.listar(grafo.redRelaciones(cliente, operador));
                });
            }
        }
    }

}
