package com.logistica.servicio;

// Usuario que está usando el sistema. Lo carga el menú después del login y lo leen
// la auditoría, los eventos y los pagos para saber quién hizo cada cosa.
// null = acción del sistema (ej. una alerta generada por una lectura).
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
