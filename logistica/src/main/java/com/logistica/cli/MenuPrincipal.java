package com.logistica.cli;

import java.sql.SQLException;

import com.logistica.modelo.Permiso;
import com.logistica.modelo.Rol;
import com.logistica.modelo.Sesion;
import com.logistica.modelo.Usuario;
import com.logistica.servicio.AuditoriaService;
import com.logistica.servicio.PermisoService;
import com.logistica.servicio.RolService;
import com.logistica.servicio.SesionService;
import com.logistica.servicio.UsuarioActual;
import com.logistica.servicio.UsuarioService;

public class MenuPrincipal {

    private static final String EMAIL_ADMIN = "admin@logistica.com";
    private static final String PASSWORD_ADMIN = "admin123";

    private final SesionService sesiones = new SesionService();
    private final UsuarioService usuarios = new UsuarioService();
    private final RolService roles = new RolService();
    private final AuditoriaService auditoria = new AuditoriaService();
    private final PermisoService permisos = new PermisoService();

    public void iniciar() {
        Consola.ejecutar(this::crearAdministradorSiNoHayUsuarios);

        while (true) {
            Sesion sesion = login();
            if (sesion == null) {
                Consola.mostrar("Hasta luego.");
                return;
            }
            boolean salir = menu(sesion);
            Consola.ejecutar(() -> sesiones.cerrarSesion(sesion.getToken()));
            Consola.ejecutar(() -> auditoria.registrar("LOGOUT", "sesion", sesion.getId(), "OK"));
            UsuarioActual.cerrar();
            if (salir) {
                Consola.mostrar("Hasta luego.");
                return;
            }
        }
    }

    private Sesion login() {
        while (true) {
            Consola.mostrar("");
            Consola.mostrar("=== INICIAR SESION (Enter en el email para salir) ===");
            String email = Consola.leer("Email");
            if (email.isEmpty()) {
                return null;
            }
            String password = Consola.leer("Contraseña");
            try {
                Sesion s = sesiones.iniciarSesion(email, password);
                UsuarioActual.iniciar(s.getUsuarioId());
                auditoria.registrar("LOGIN", "sesion", s.getId(), "OK");
                Usuario u = usuarios.buscarPorId(s.getUsuarioId());
                Consola.mostrar("Bienvenido, " + u.getNombre() + ".");
                Consola.mostrar("Tus permisos: " + String.join(", ", permisos.delUsuarioActual()));
                return s;
            } catch (IllegalArgumentException e) {
                Consola.mostrar(e.getMessage());
            } catch (SQLException e) {
                Consola.mostrar("Error de MySQL: " + e.getMessage());
            }
        }
    }

    private boolean menu(Sesion sesion) {
        while (true) {
            int opcion = Consola.menu("MENU PRINCIPAL",
                    "Clientes / proveedores / operadores", "Usuarios, roles y sesiones",
                    "Activos (contenedores, sensores, vehiculos)", "Envios y tramos", "Monitoreo y alertas",
                    "Reglas e incidentes", "Comercial (facturas, cobros, pagos, cuentas)", "Reportes facturables",
                    "Analisis y prediccion", "Red y recorridos (Neo4j)", "Mensajeria", "Auditoria",
                    "Paises y ciudades", "Cerrar sesion");
            if (opcion == 0) {
                return true;
            }
            if (opcion == 14) {
                return false;
            }
            try {
                sesiones.validar(sesion.getToken());

                // cada opcion del menu pide el permiso del mismo numero en Permiso.CATALOGO
                String permiso = Permiso.CATALOGO[opcion - 1][0];
                if (!permisos.puede(permiso)) {
                    Consola.mostrar("Tu rol no tiene el permiso " + permiso + ": no podes entrar a este menu.");
                    Consola.ejecutar(() -> auditoria.registrar("ACCESO_DENEGADO", "permiso", permiso, "RECHAZADO"));
                    continue;
                }
            } catch (IllegalArgumentException e) {
                Consola.mostrar(e.getMessage());
                return false;
            } catch (SQLException e) {
                Consola.mostrar("Error de MySQL: " + e.getMessage());
                return false;
            }
            switch (opcion) {
                case 1 -> new MenuPartes().mostrar();
                case 2 -> new MenuUsuarios().mostrar();
                case 3 -> new MenuActivos().mostrar();
                case 4 -> new MenuEnvios().mostrar();
                case 5 -> new MenuMonitoreo().mostrar();
                case 6 -> new MenuIncidentes().mostrar();
                case 7 -> new MenuComercial().mostrar();
                case 8 -> new MenuReportes().mostrar();
                case 9 -> new MenuAnalisis().mostrar();
                case 10 -> new MenuRed().mostrar();
                case 11 -> new MenuMensajeria().mostrar();
                case 12 -> new MenuAuditoria().mostrar();
                case 13 -> new MenuUbicaciones().mostrar();
            }
        }
    }

    private void crearAdministradorSiNoHayUsuarios() throws SQLException {
        if (!usuarios.listar().isEmpty()) {
            return;
        }

        Integer rolId = null;
        for (Rol r : roles.listar()) {
            if (RolService.ROL_ADMIN.equals(r.getDescripcion())) {
                rolId = r.getId();
            }
        }
        if (rolId == null) {
            rolId = roles.alta(new Rol(null, RolService.ROL_ADMIN)); // ADMIN recibe todos los permisos
        }

        usuarios.alta(new Usuario("Administrador", "Sistema", EMAIL_ADMIN, null, rolId),
                PASSWORD_ADMIN);
        Consola.mostrar("No habia usuarios: se creo " + EMAIL_ADMIN + " con contraseña " + PASSWORD_ADMIN + ".");
    }

}
