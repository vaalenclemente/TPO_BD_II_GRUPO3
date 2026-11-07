package com.logistica.cli;

import com.logistica.servicio.UbicacionService;

public class MenuUbicaciones {

    private final UbicacionService ubicaciones = new UbicacionService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Paises y ciudades",
                    "Listar paises", "Alta de pais", "Modificar pais", "Eliminar pais",
                    "Listar ciudades de un pais", "Alta de ciudad", "Modificar ciudad", "Eliminar ciudad");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> Vista.listar(ubicaciones.listarPaises()));
                case 2 -> Consola.ejecutar(() -> {
                    int id = ubicaciones.altaPais(Consola.texto("Nombre del pais"));
                    Consola.mostrar("Pais creado con id " + id + ".");
                });
                case 3 -> Consola.ejecutar(() -> {
                    int id = Selector.pais();
                    ubicaciones.modificarPais(id, Consola.texto("Nuevo nombre"));
                    Consola.mostrar("Modificado.");
                });
                case 4 -> Consola.ejecutar(() -> {
                    ubicaciones.eliminarPais(Selector.pais());
                    Consola.mostrar("Eliminado.");
                });
                case 5 -> Consola.ejecutar(() -> Vista.listar(ubicaciones.listarCiudades(Selector.pais())));
                case 6 -> Consola.ejecutar(() -> {
                    int paisId = Selector.pais();
                    int id = ubicaciones.altaCiudad(Consola.texto("Nombre de la ciudad"), paisId);
                    Consola.mostrar("Ciudad creada con id " + id + ".");
                });
                case 7 -> Consola.ejecutar(() -> {
                    int id = Selector.ciudad("Ciudad a modificar");
                    ubicaciones.modificarCiudad(id, Consola.texto("Nuevo nombre"));
                    Consola.mostrar("Modificada.");
                });
                case 8 -> Consola.ejecutar(() -> {
                    ubicaciones.eliminarCiudad(Selector.ciudad("Ciudad a eliminar"));
                    Consola.mostrar("Eliminada.");
                });
            }
        }
    }

}
