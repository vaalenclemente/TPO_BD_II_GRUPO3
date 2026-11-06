package com.logistica.cli;

import com.logistica.modelo.Rol;
import com.logistica.modelo.Usuario;
import com.logistica.servicio.RolService;
import com.logistica.servicio.SesionService;
import com.logistica.servicio.UsuarioService;

public class MenuUsuarios {

    private final UsuarioService usuarios = new UsuarioService();
    private final RolService roles = new RolService();
    private final SesionService sesiones = new SesionService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Usuarios, roles y sesiones",
                    "Listar usuarios", "Alta de usuario", "Modificar usuario", "Dar de baja usuario",
                    "Reactivar usuario", "Listar roles", "Alta de rol", "Modificar rol", "Eliminar rol",
                    "Sesiones activas", "Cerrar una sesion");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> Consola.listar(usuarios.listar()));
                case 2 -> Consola.ejecutar(this::altaUsuario);
                case 3 -> Consola.ejecutar(this::modificarUsuario);
                case 4 -> Consola.ejecutar(() -> {
                    usuarios.darDeBaja(Selector.usuario());
                    Consola.mostrar("Dado de baja.");
                });
                case 5 -> Consola.ejecutar(() -> {
                    usuarios.reactivar(Selector.usuario());
                    Consola.mostrar("Reactivado.");
                });
                case 6 -> Consola.ejecutar(() -> Consola.listar(roles.listar()));
                case 7 -> Consola.ejecutar(() -> {
                    int id = roles.alta(new Rol(null, Consola.texto("Descripcion"), Consola.entero("Nivel de permisos")));
                    Consola.mostrar("Rol creado con id " + id + ".");
                });
                case 8 -> Consola.ejecutar(() -> {
                    Rol r = roles.buscarPorId(Selector.rol());
                    if (r == null) {
                        throw new IllegalArgumentException("No existe ese rol.");
                    }
                    r.setDescripcion(Consola.texto("Descripcion", r.getDescripcion()));
                    r.setNivelPermisos(Integer.valueOf(Consola.texto("Nivel de permisos", String.valueOf(r.getNivelPermisos()))));
                    roles.modificar(r);
                    Consola.mostrar("Modificado.");
                });
                case 9 -> Consola.ejecutar(() -> {
                    roles.eliminar(Selector.rol());
                    Consola.mostrar("Eliminado.");
                });
                case 10 -> Consola.ejecutar(() -> Consola.listar(sesiones.listarActivas()));
                case 11 -> Consola.ejecutar(() -> {
                    Consola.listar(sesiones.listarActivas());
                    sesiones.forzarCierre(Consola.entero("Id de la sesion"));
                    Consola.mostrar("Sesion cerrada.");
                });
            }
        }
    }

    private void altaUsuario() throws Exception {
        String nombre = Consola.texto("Nombre");
        String apellido = Consola.texto("Apellido");
        String email = Consola.texto("Email");
        String password = Consola.texto("Contraseña");
        int rolId = Selector.rol();
        String tipo = Consola.elegir("Tipo de usuario", "INTERNO", "EXTERNO");
        Integer parteId = "EXTERNO".equals(tipo) ? Selector.cualquierParte() : null;

        int id = usuarios.alta(new Usuario(nombre, apellido, email, null, rolId, tipo, parteId), password);
        Consola.mostrar("Usuario creado con id " + id + ".");
    }

    private void modificarUsuario() throws Exception {
        Usuario u = usuarios.buscarPorId(Selector.usuario());
        if (u == null) {
            throw new IllegalArgumentException("No existe ese usuario.");
        }
        u.setNombre(Consola.texto("Nombre", u.getNombre()));
        u.setApellido(Consola.texto("Apellido", u.getApellido()));
        u.setEmail(Consola.texto("Email", u.getEmail()));
        if (Consola.confirmar("¿Cambiar el rol?")) {
            u.setRolId(Selector.rol());
        }
        usuarios.modificar(u);
        Consola.mostrar("Modificado.");
    }

}
