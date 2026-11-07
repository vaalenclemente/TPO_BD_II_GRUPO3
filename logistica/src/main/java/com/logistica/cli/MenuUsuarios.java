package com.logistica.cli;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.logistica.modelo.Permiso;
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
            int opcion = Consola.menu("Usuarios, roles, permisos y sesiones",
                    "Listar usuarios", "Alta de usuario", "Modificar usuario", "Dar de baja usuario",
                    "Reactivar usuario",
                    "Listar roles (con sus permisos)", "Alta de rol", "Modificar rol", "Eliminar rol",
                    "Listar permisos", "Agregar permisos a un rol", "Quitar permisos a un rol",
                    "Sesiones activas", "Cerrar una sesion");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(this::listarUsuarios);
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
                case 6 -> Consola.ejecutar(() -> Vista.listar(roles.listar()));
                case 7 -> Consola.ejecutar(this::altaRol);
                case 8 -> Consola.ejecutar(() -> {
                    Rol r = roles.buscarPorId(Selector.rol());
                    if (r == null) {
                        throw new IllegalArgumentException("No existe ese rol.");
                    }
                    r.setDescripcion(Consola.texto("Descripcion", r.getDescripcion()));
                    roles.modificar(r);
                    Consola.mostrar("Modificado. Los permisos se cambian con las opciones 11 y 12.");
                });
                case 9 -> Consola.ejecutar(() -> {
                    roles.eliminar(Selector.rol());
                    Consola.mostrar("Eliminado.");
                });
                case 10 -> Consola.ejecutar(() -> Vista.listar(roles.listarPermisos()));
                case 11 -> Consola.ejecutar(this::agregarPermisos);
                case 12 -> Consola.ejecutar(this::quitarPermisos);
                case 13 -> Consola.ejecutar(() -> Vista.listar(sesiones.listarActivas()));
                case 14 -> Consola.ejecutar(() -> {
                    Vista.listar(sesiones.listarActivas());
                    sesiones.forzarCierre(Consola.entero("Id de la sesion"));
                    Consola.mostrar("Sesion cerrada.");
                });
            }
        }
    }

    private void listarUsuarios() throws SQLException {
        List<Usuario> lista = usuarios.listar();
        if (lista.isEmpty()) {
            Consola.mostrar("(sin resultados)");
            return;
        }

        Map<Integer, String> nombreRol = new HashMap<>();
        for (Rol r : roles.listar()) {
            nombreRol.put(r.getId(), r.getDescripcion());
        }

        String formato = "%-5s %-28s %-32s %-18s %s";
        Consola.mostrar(String.format(formato, "ID", "NOMBRE", "EMAIL", "ROL (id)", "ESTADO"));
        for (Usuario u : lista) {
            String rol = u.getRolId() == null ? "-"
                    : nombreRol.getOrDefault(u.getRolId(), "?") + " (" + u.getRolId() + ")";
            Consola.mostrar(String.format(formato, u.getId(), u.getNombre() + " " + u.getApellido(),
                    u.getEmail(), rol, u.getEstado()));
        }
    }

    private void altaRol() throws Exception {
        String descripcion = Consola.texto("Descripcion");
        Consola.mostrar("Permisos disponibles:");
        Vista.listar(roles.listarPermisos());
        List<Integer> permisoIds = Consola.enteros("Ids de los permisos del rol (Enter para ninguno)");

        int id = roles.alta(new Rol(null, descripcion), permisoIds);
        Consola.mostrar("Rol creado con id " + id + ": " + roles.buscarPorId(id));
    }

    private void agregarPermisos() throws Exception {
        int rolId = Selector.rol();
        List<Permiso> faltantes = roles.permisosQueLeFaltan(rolId);
        if (faltantes.isEmpty()) {
            Consola.mostrar("El rol ya tiene todos los permisos.");
            return;
        }
        Consola.mostrar("Permisos que el rol NO tiene:");
        Vista.listar(faltantes);

        int agregados = roles.agregarPermisos(rolId, Consola.enteros("Ids de los permisos a agregar"));
        Consola.mostrar(agregados + " permisos agregados. Ahora: " + roles.buscarPorId(rolId));
    }

    private void quitarPermisos() throws Exception {
        int rolId = Selector.rol();
        List<Permiso> actuales = roles.permisosDe(rolId);
        if (actuales.isEmpty()) {
            Consola.mostrar("El rol no tiene permisos.");
            return;
        }
        Consola.mostrar("Permisos que el rol tiene:");
        Vista.listar(actuales);

        int quitados = roles.quitarPermisos(rolId, Consola.enteros("Ids de los permisos a quitar"));
        Consola.mostrar(quitados + " permisos quitados. Ahora: " + roles.buscarPorId(rolId));
    }

    private void altaUsuario() throws Exception {
        String nombre = Consola.texto("Nombre");
        String apellido = Consola.texto("Apellido");
        String email = Consola.texto("Email");
        String password = Consola.texto("Contraseña");
        int rolId = Selector.rol();

        int id = usuarios.alta(new Usuario(nombre, apellido, email, null, rolId), password);
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
