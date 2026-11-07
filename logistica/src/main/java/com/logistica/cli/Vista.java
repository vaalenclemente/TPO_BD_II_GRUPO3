package com.logistica.cli;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.logistica.modelo.CierreCuenta;
import com.logistica.modelo.Ciudad;
import com.logistica.modelo.Contenedor;
import com.logistica.modelo.CuentaCorriente;
import com.logistica.modelo.Envio;
import com.logistica.modelo.EnvioContenedor;
import com.logistica.modelo.Factura;
import com.logistica.modelo.Formato;
import com.logistica.modelo.Grupo;
import com.logistica.modelo.Mensaje;
import com.logistica.modelo.Pago;
import com.logistica.modelo.Pais;
import com.logistica.modelo.Parte;
import com.logistica.modelo.RegistroAuditoria;
import com.logistica.modelo.SensorIot;
import com.logistica.modelo.Sesion;
import com.logistica.modelo.Tramo;
import com.logistica.modelo.Usuario;
import com.logistica.modelo.Vehiculo;
import com.logistica.servicio.ContenedorService;
import com.logistica.servicio.FacturaService;
import com.logistica.servicio.ParteService;
import com.logistica.servicio.UbicacionService;
import com.logistica.servicio.UsuarioService;
import com.logistica.servicio.VehiculoService;

public class Vista {

    private static final UbicacionService ubicaciones = new UbicacionService();
    private static final ParteService partes = new ParteService();
    private static final UsuarioService usuarios = new UsuarioService();
    private static final ContenedorService contenedores = new ContenedorService();
    private static final VehiculoService vehiculos = new VehiculoService();
    private static final FacturaService facturas = new FacturaService();

    private Map<Integer, String> ciudad;
    private Map<Integer, String> parte;
    private Map<Integer, String> usuario;
    private Map<Integer, String> contenedor;
    private Map<Integer, Vehiculo> vehiculo;
    private final Map<Integer, String> factura = new HashMap<>();

    public static void listar(List<?> lista) throws SQLException {
        if (lista == null || lista.isEmpty()) {
            Consola.mostrar("(sin resultados)");
            return;
        }
        Vista v = new Vista();
        for (Object o : lista) {
            Consola.mostrar(v.linea(o));
        }
    }

    public static void mostrar(Object o) throws SQLException {
        Consola.mostrar(new Vista().linea(o));
    }

    private String linea(Object o) throws SQLException {
        if (o instanceof Envio) {
            return envio((Envio) o);
        }
        if (o instanceof Tramo) {
            return tramo((Tramo) o);
        }
        if (o instanceof EnvioContenedor) {
            return envioContenedor((EnvioContenedor) o);
        }
        if (o instanceof Vehiculo) {
            return vehiculo((Vehiculo) o);
        }
        if (o instanceof SensorIot) {
            return sensor((SensorIot) o);
        }
        if (o instanceof Factura) {
            return factura((Factura) o);
        }
        if (o instanceof Pago) {
            return pago((Pago) o);
        }
        if (o instanceof CuentaCorriente) {
            return cuenta((CuentaCorriente) o);
        }
        if (o instanceof CierreCuenta) {
            return cierre((CierreCuenta) o);
        }
        if (o instanceof Mensaje) {
            return mensaje((Mensaje) o);
        }
        if (o instanceof Grupo) {
            return grupo((Grupo) o);
        }
        if (o instanceof Sesion) {
            return sesion((Sesion) o);
        }
        if (o instanceof RegistroAuditoria) {
            return auditoria((RegistroAuditoria) o);
        }
        return String.valueOf(o);
    }

    private String envio(Envio e) throws SQLException {
        return "Envio " + e.getId() + " | " + nombreParte(e.getClienteId()) + " | " + nombreCiudad(e.getOrigenCiudadId())
                + " -> " + nombreCiudad(e.getDestinoCiudadId()) + " | " + e.getEstado() + " | prioridad "
                + e.getPrioridad() + " | " + Formato.kg(e.getPesoKg()) + " | " + e.getDescripcionCarga()
                + " | entrega estimada " + Formato.fecha(e.getFechaEntregaEstimada())
                + (e.getFechaEntregaReal() == null ? "" : " | entregado " + Formato.fecha(e.getFechaEntregaReal()))
                + (e.getCantidadIncidentes() == null || e.getCantidadIncidentes() == 0 ? ""
                        : " | incidentes " + e.getCantidadIncidentes());
    }

    private String tramo(Tramo t) throws SQLException {
        Vehiculo v = buscarVehiculo(t.getVehiculoId());
        String transporte = (v == null) ? t.getMedioTransporte() + " (vehiculo " + t.getVehiculoId() + ")"
                : v.getTipo() + " " + v.getIdentificacion() + " - " + nombreParte(v.getOperadorId());
        return "Tramo " + t.getOrden() + " (id " + t.getId() + ") | " + nombreCiudad(t.getOrigenCiudadId()) + " -> "
                + nombreCiudad(t.getDestinoCiudadId()) + " | " + transporte + " | salida " + Formato.fecha(t.getFechaSalida())
                + " | llegada estimada " + Formato.fecha(t.getFechaLlegadaEstimada()) + " | llegada real "
                + Formato.fecha(t.getFechaLlegadaReal()) + " | " + Formato.dinero(t.getCosto()) + " | " + t.getEstado();
    }

    private String envioContenedor(EnvioContenedor ec) throws SQLException {
        return "Contenedor " + ec.getContenedorId() + " " + nombreContenedor(ec.getContenedorId()) + " | asignado "
                + Formato.fecha(ec.getFechaAsignacion())
                + (ec.getFechaLiberacion() == null ? " | en el envio" : " | liberado " + Formato.fecha(ec.getFechaLiberacion()));
    }

    private String vehiculo(Vehiculo v) throws SQLException {
        return v.getId() + " | " + v.getTipo() + " " + v.getIdentificacion() + " | " + Formato.kg(v.getCapacidadKg())
                + " | " + nombreParte(v.getOperadorId()) + " | " + v.getEstado();
    }

    private String sensor(SensorIot s) throws SQLException {
        String ubicacion = (s.getContenedorId() == null) ? "sin contenedor"
                : "contenedor " + s.getContenedorId() + " " + nombreContenedor(s.getContenedorId());
        return s.getId() + " | " + s.getTipo() + " " + s.getFabricante() + " | " + s.getEstado() + " | " + ubicacion
                + " | instalado " + Formato.fecha(s.getFechaInstalacion());
    }

    private String factura(Factura f) throws SQLException {
        return f.getId() + " | " + f.getNumero() + " | " + f.getTipo() + " | " + nombreParte(f.getParteId())
                + " | periodo " + f.getPeriodo() + " | emitida " + Formato.fecha(f.getFechaEmision()) + " | vence "
                + Formato.fecha(f.getFechaVencimiento()) + " | " + Formato.dinero(f.getImporteTotal()) + " | "
                + f.getEstado();
    }

    private String pago(Pago p) throws SQLException {
        return p.getId() + " | " + p.getTipo() + " | factura " + numeroFactura(p.getFacturaId()) + " | "
                + nombreParte(p.getParteId()) + " | " + Formato.dinero(p.getMonto()) + " | " + p.getMedioPago() + " | "
                + Formato.fecha(p.getFecha()) + " | " + p.getEstado();
    }

    private String cuenta(CuentaCorriente c) throws SQLException {
        BigDecimal saldo = (c.getSaldo() == null) ? BigDecimal.ZERO : c.getSaldo();
        String situacion = saldo.signum() > 0 ? "nos debe" : saldo.signum() < 0 ? "le debemos" : "al dia";
        return "Cuenta " + c.getId() + " | " + nombreParte(c.getParteId()) + " | saldo " + Formato.dinero(saldo)
                + " (" + situacion + ")";
    }

    private String cierre(CierreCuenta c) throws SQLException {
        return "Cierre " + c.getPeriodo() + " | " + nombreParte(c.getParteId()) + " | saldo inicial "
                + Formato.dinero(c.getSaldoInicial()) + " | debitos " + Formato.dinero(c.getTotalDebitos())
                + " | creditos " + Formato.dinero(c.getTotalCreditos()) + " | saldo final "
                + Formato.dinero(c.getSaldoFinal());
    }

    private String mensaje(Mensaje m) throws SQLException {
        return Formato.fecha(m.getFechaHora()) + " | " + nombreUsuario(m.getRemitenteId())
                + (m.getDireccion() == null ? "" : " | " + m.getDireccion()) + " | " + m.getContenido();
    }

    private String grupo(Grupo g) throws SQLException {
        List<String> nombres = new ArrayList<>();
        for (Integer id : g.getIntegrantes()) {
            nombres.add(nombreUsuario(id));
        }
        return g.getId() + " | " + g.getNombre() + " | integrantes: " + String.join(", ", nombres)
                + ("ACTIVO".equals(g.getEstado()) ? "" : " | " + g.getEstado());
    }

    private String sesion(Sesion s) throws SQLException {
        return s.getId() + " | " + nombreUsuario(s.getUsuarioId()) + " | " + s.getEstado() + " | inicio "
                + Formato.fecha(s.getFechaInicio()) + " | ultima actividad " + Formato.fecha(s.getFechaUltimaActividad());
    }

    private String auditoria(RegistroAuditoria r) throws SQLException {
        return Formato.fecha(r.getFecha()) + " | " + (r.getUsuarioId() == null ? "sistema" : nombreUsuario(r.getUsuarioId()))
                + " | " + r.getAccion() + " | " + r.getEntidad() + " " + r.getEntidadId()
                + (r.getCampo() == null ? "" : " | " + r.getCampo() + ": " + r.getAntes() + " -> " + r.getDespues())
                + " | " + r.getResultado();
    }

    private String nombreCiudad(Integer id) throws SQLException {
        if (ciudad == null) {
            ciudad = new HashMap<>();
            for (Pais p : ubicaciones.listarPaises()) {
                for (Ciudad c : ubicaciones.listarCiudades(p.getId())) {
                    ciudad.put(c.getId(), c.getNombre());
                }
            }
        }
        return nombre(ciudad, id, "ciudad");
    }

    private String nombreParte(Integer id) throws SQLException {
        if (parte == null) {
            parte = new HashMap<>();
            for (String tipo : List.of("CLIENTE", "PROVEEDOR", "OPERADOR")) {
                for (Parte p : partes.listarPorTipo(tipo)) {
                    parte.put(p.getId(), p.getRazonSocial());
                }
            }
        }
        return nombre(parte, id, "parte");
    }

    private String nombreUsuario(Integer id) throws SQLException {
        if (usuario == null) {
            usuario = new HashMap<>();
            for (Usuario u : usuarios.listar()) {
                usuario.put(u.getId(), u.getNombre() + " " + u.getApellido());
            }
        }
        return nombre(usuario, id, "usuario");
    }

    private String nombreContenedor(Integer id) throws SQLException {
        if (contenedor == null) {
            contenedor = new HashMap<>();
            for (Contenedor c : contenedores.listar()) {
                contenedor.put(c.getId(), c.getCodigoInternacional() + " (" + c.getTipo() + ")");
            }
        }
        return nombre(contenedor, id, "");
    }

    private Vehiculo buscarVehiculo(Integer id) throws SQLException {
        if (vehiculo == null) {
            vehiculo = new HashMap<>();
            for (Vehiculo v : vehiculos.listar()) {
                vehiculo.put(v.getId(), v);
            }
        }
        return (id == null) ? null : vehiculo.get(id);
    }

    private String numeroFactura(Integer id) throws SQLException {
        if (id == null) {
            return "-";
        }
        if (!factura.containsKey(id)) {
            Factura f = facturas.buscarPorId(id);
            factura.put(id, (f == null) ? String.valueOf(id) : f.getNumero());
        }
        return factura.get(id);
    }

    private String nombre(Map<Integer, String> mapa, Integer id, String tipo) {
        if (id == null) {
            return "-";
        }
        String n = mapa.get(id);
        return (n != null) ? n : (tipo + " " + id).trim();
    }

}
