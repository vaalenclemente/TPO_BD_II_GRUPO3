package com.logistica.servicio;

// usuario que esta usando el sistema.
public class UsuarioActual {

    private static Integer id;

    public static void iniciar(Integer usuarioId) {
        id = usuarioId;
    }

    public static void cerrar() {
        id = null;
    }

    public static Integer get() {
        return id;
    }

}
