package com.logistica.cli;

import java.sql.SQLException;
import java.util.List;

import com.logistica.modelo.Ciudad;

import com.logistica.servicio.ContenedorService;
import com.logistica.servicio.EnvioService;
import com.logistica.servicio.ParteService;
import com.logistica.servicio.RolService;
import com.logistica.servicio.SensorIotService;
import com.logistica.servicio.UbicacionService;
import com.logistica.servicio.UsuarioService;

public class Selector {

    private static final UbicacionService ubicaciones = new UbicacionService();
    private static final ParteService partes = new ParteService();
    private static final ContenedorService contenedores = new ContenedorService();
    private static final SensorIotService sensores = new SensorIotService();
    private static final EnvioService envios = new EnvioService();
    private static final UsuarioService usuarios = new UsuarioService();
    private static final RolService roles = new RolService();

    public static int pais() throws SQLException {
        Vista.listar(ubicaciones.listarPaises());
        return Consola.entero("Id del pais");
    }

    public static int ciudad(String etiqueta) throws SQLException {
        Consola.mostrar(etiqueta + ":");
        int paisId = pais();
        List<Ciudad> ciudades = ubicaciones.listarCiudades(paisId);
        if (ciudades.isEmpty()) {
            throw new IllegalArgumentException("Ese pais no tiene ciudades cargadas.");
        }
        Vista.listar(ciudades);
        while (true) {
            int id = Consola.entero("Id de la ciudad");
            for (Ciudad c : ciudades) {
                if (c.getId() == id) {
                    return id;
                }
            }
            Consola.mostrar("Elija una ciudad de la lista.");
        }
    }

    public static Integer ciudadOpcional(String etiqueta) throws SQLException {
        if (!Consola.confirmar("¿Indicar " + etiqueta + "?")) {
            return null;
        }
        return ciudad(etiqueta);
    }

    public static int parte(String tipo) throws SQLException {
        Vista.listar(partes.listarPorTipo(tipo));
        return Consola.entero("Id del " + tipo.toLowerCase());
    }

    public static Integer parteOpcional(String tipo) throws SQLException {
        Vista.listar(partes.listarPorTipo(tipo));
        return Consola.enteroOpcional("Id del " + tipo.toLowerCase() + " (Enter = todos)");
    }

    public static int cualquierParte() throws SQLException {
        String tipo = Consola.elegir("Tipo de parte", "CLIENTE", "PROVEEDOR", "OPERADOR");
        return parte(tipo);
    }

    public static int contenedor() throws SQLException {
        Vista.listar(contenedores.listar());
        return Consola.entero("Id del contenedor");
    }

    public static int sensor() throws SQLException {
        Vista.listar(sensores.listar());
        return Consola.entero("Id del sensor");
    }

    public static int envio() throws SQLException {
        Vista.listar(envios.listar());
        return Consola.entero("Id del envio");
    }

    public static Integer envioOpcional() throws SQLException {
        if (!Consola.confirmar("¿Es de un envio en particular?")) {
            return null;
        }
        return envio();
    }

    public static int usuario() throws SQLException {
        Vista.listar(usuarios.listar());
        return Consola.entero("Id del usuario");
    }

    public static int rol() throws SQLException {
        Vista.listar(roles.listar());
        return Consola.entero("Id del rol");
    }

    public static String medio() {
        return Consola.elegir("Medio de transporte", "BARCO", "AVION", "TREN", "CAMION");
    }

    public static String severidad() {
        return Consola.elegir("Severidad", "ALTA", "MEDIA", "BAJA");
    }

    public static String periodo() {
        while (true) {
            String valor = Consola.texto("Periodo (AAAA-MM)");
            if (valor.matches("\\d{4}-\\d{2}")) {
                return valor;
            }
            Consola.mostrar("Formato AAAA-MM, por ejemplo 2026-10.");
        }
    }

}
