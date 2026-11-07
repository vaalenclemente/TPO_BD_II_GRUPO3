package com.logistica;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.Document;

import com.logistica.database.MongoInicializacion;
import com.logistica.database.MongodbConexion;
import com.logistica.database.MySQLConexion;
import com.logistica.database.MySQLInicializacion;
import com.logistica.database.Neo4jConexion;
import com.logistica.database.Neo4jInicializacion;
import com.logistica.modelo.Alerta;
import com.logistica.modelo.Contenedor;
import com.logistica.modelo.Envio;
import com.logistica.modelo.Factura;
import com.logistica.modelo.FacturaDetalle;
import com.logistica.modelo.Grupo;
import com.logistica.modelo.LecturaSensor;
import com.logistica.modelo.Parte;
import com.logistica.modelo.Permiso;
import com.logistica.modelo.Regla;
import com.logistica.modelo.Reporte;
import com.logistica.modelo.Rol;
import com.logistica.modelo.SensorIot;
import com.logistica.modelo.TipoReporte;
import com.logistica.modelo.Tramo;
import com.logistica.modelo.Usuario;
import com.logistica.modelo.Vehiculo;
import com.logistica.servicio.AlertaService;
import com.logistica.servicio.ContenedorService;
import com.logistica.servicio.EnvioService;
import com.logistica.servicio.EventoService;
import com.logistica.servicio.FacturaService;
import com.logistica.servicio.GrupoService;
import com.logistica.servicio.IncidenteService;
import com.logistica.servicio.LecturaSensorService;
import com.logistica.servicio.MensajeService;
import com.logistica.servicio.PagoService;
import com.logistica.servicio.ParteService;
import com.logistica.servicio.PrediccionRiesgoService;
import com.logistica.servicio.ReglaService;
import com.logistica.servicio.ReporteService;
import com.logistica.servicio.RolService;
import com.logistica.servicio.SensorIotService;
import com.logistica.servicio.TipoReporteService;
import com.logistica.servicio.TramoService;
import com.logistica.servicio.UbicacionService;
import com.logistica.servicio.UsuarioActual;
import com.logistica.servicio.UsuarioService;
import com.logistica.servicio.VehiculoService;

public class DatosDemo {

    private static final String EMAIL_ADMIN = "admin@logistica.com";
    private static final String PASSWORD_ADMIN = "admin123";
    private static final String PASSWORD_DEMO = "demo123";

    private final UbicacionService ubicaciones = new UbicacionService();
    private final ParteService partes = new ParteService();
    private final RolService roles = new RolService();
    private final UsuarioService usuarios = new UsuarioService();
    private final ContenedorService contenedores = new ContenedorService();
    private final SensorIotService sensores = new SensorIotService();
    private final VehiculoService vehiculos = new VehiculoService();
    private final EnvioService envios = new EnvioService();
    private final TramoService tramos = new TramoService();
    private final ReglaService reglas = new ReglaService();
    private final LecturaSensorService lecturas = new LecturaSensorService();
    private final AlertaService alertas = new AlertaService();
    private final IncidenteService incidentes = new IncidenteService();
    private final EventoService eventos = new EventoService();
    private final TipoReporteService tiposReporte = new TipoReporteService();
    private final ReporteService reportes = new ReporteService();
    private final FacturaService facturas = new FacturaService();
    private final PagoService pagos = new PagoService();
    private final GrupoService grupos = new GrupoService();
    private final MensajeService mensajes = new MensajeService();
    private final PrediccionRiesgoService predicciones = new PrediccionRiesgoService();

    private final Map<String, Integer> ciudad = new HashMap<>();
    private final LocalDateTime base = LocalDateTime.now().withSecond(0).withNano(0);

    public static void main(String[] args) {
        System.out.println("=== CARGA DE DATOS DE PRUEBA ===");
        try {
            if (!prepararBases()) {
                System.exit(1);
            }
            DatosDemo demo = new DatosDemo();
            if (!demo.basesVacias()) {
                System.out.println("Ya hay datos cargados (hay paises). Para cargar de cero: docker compose down -v y docker compose up -d.");
                System.exit(1);
            }
            demo.cargar();
            System.out.println();
            System.out.println("Carga terminada.");
            System.out.println("Usuarios: " + EMAIL_ADMIN + " / " + PASSWORD_ADMIN
                    + " | operaciones@logistica.com, comercial@logistica.com, auditoria@logistica.com / " + PASSWORD_DEMO);
            System.exit(0);
        } catch (Exception e) {
            System.out.println();
            System.out.println("La carga se corto: " + e.getMessage());
            System.out.println("Puede haber quedado a medias: docker compose down -v, docker compose up -d y volver a correrla.");
            System.exit(1);
        }
    }

    private static boolean prepararBases() {
        try (Connection c = MySQLConexion.getConexion()) {
            MongodbConexion.getDatabase().runCommand(new Document("ping", 1));
            Neo4jConexion.getDriver().verifyConnectivity();
            MySQLInicializacion.inicializar();
            MongoInicializacion.inicializar();
            Neo4jInicializacion.inicializar();
            System.out.println("Bases conectadas e inicializadas.");
            return true;
        } catch (Exception e) {
            System.out.println("No se pudo conectar o inicializar las bases: " + e.getMessage());
            return false;
        }
    }

    private boolean basesVacias() throws SQLException {
        return ubicaciones.listarPaises().isEmpty();
    }

    private void cargar() throws Exception {

        paso("Usuarios, roles y permisos");
        int adminId = cargarUsuarios();
        UsuarioActual.iniciar(adminId);
        int operacionesId = buscarUsuario("operaciones@logistica.com");
        int comercialId = buscarUsuario("comercial@logistica.com");

        paso("Paises y ciudades");
        int argentina = ubicaciones.altaPais("Argentina");
        int brasil = ubicaciones.altaPais("Brasil");
        int chile = ubicaciones.altaPais("Chile");
        int uruguay = ubicaciones.altaPais("Uruguay");
        ciudad("Buenos Aires", argentina);
        ciudad("Rosario", argentina);
        ciudad("Cordoba", argentina);
        ciudad("Mendoza", argentina);
        ciudad("Santos", brasil);
        ciudad("Sao Paulo", brasil);
        ciudad("Santiago", chile);
        ciudad("Valparaiso", chile);
        ciudad("Montevideo", uruguay);

        paso("Clientes, operadores y proveedores");
        int alimentos = partes.alta(new Parte("CLIENTE", "Alimentos del Sur SA", "30-71000001-1",
                "compras@alimentosdelsur.com", "011-4000-1000", "Av. Corrientes 1200", argentina, null));
        int farmaceutica = partes.alta(new Parte("CLIENTE", "Farmaceutica Andina SRL", "30-71000002-2",
                "logistica@farmandina.com", "0351-400-2000", "Bv. San Juan 450", argentina, null));
        int tecno = partes.alta(new Parte("CLIENTE", "Tecno Importaciones SA", "30-71000003-3",
                "importaciones@tecno.com", "011-4000-3000", "Av. Belgrano 980", argentina, null));
        int transportes = partes.alta(new Parte("OPERADOR", "Transportes Rio de la Plata SA", "30-71000004-4",
                "operaciones@trp.com", "011-4000-4000", "Ruta 9 km 25", argentina, null));
        int naviera = partes.alta(new Parte("OPERADOR", "Naviera Atlantica SA", "30-71000005-5",
                "reservas@navieraatlantica.com", "+598 2900 5000", "Rambla 25 de Agosto 300", uruguay, null));
        int aerocarga = partes.alta(new Parte("OPERADOR", "Aerocarga Sur SA", "30-71000006-6",
                "cargas@aerocargasur.com", "+56 2 2600 6000", "Aeropuerto AMB", chile, null));
        int proveedorSensores = partes.alta(new Parte("PROVEEDOR", "Sensores IoT Patagonia SRL", "30-71000007-7",
                "ventas@iotpatagonia.com", "0299-400-7000", "Parque Industrial Neuquen", argentina,
                "Sensores IoT"));
        partes.alta(new Parte("PROVEEDOR", "Mantenimiento de Contenedores SRL", "30-71000008-8",
                "taller@mantcont.com", "011-4000-8000", "Dock Sud", argentina, "Mantenimiento"));

        paso("Contenedores y sensores");
        String[][] datosContenedores = {
            { "MSCU1000001", "REFRIGERADO 40" }, { "MSCU1000002", "DRY 20" }, { "MSCU1000003", "DRY 40" },
            { "MSCU1000004", "REFRIGERADO 40" }, { "MSCU1000005", "REFRIGERADO 40" }, { "MSCU1000006", "DRY 40" },
            { "MSCU1000007", "DRY 20" }, { "MSCU1000008", "DRY 20" }, { "MSCU1000009", "DRY 40" },
            { "MSCU1000010", "REFRIGERADO 20" }
        };
        List<Integer> contenedor = new ArrayList<>();
        for (String[] d : datosContenedores) {
            BigDecimal capacidad = d[1].endsWith("40") ? new BigDecimal("26000") : new BigDecimal("21000");
            contenedor.add(contenedores.alta(new Contenedor(d[0], d[1], capacidad)));
        }
        List<Integer> sensor = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            String fabricante = (i % 2 == 0) ? "Bosch" : "Sensores IoT Patagonia";
            sensor.add(sensores.alta(new SensorIot("MULTISENSOR", fabricante, LocalDate.now().minusMonths(2),
                    contenedor.get(i))));
        }
        sensores.alta(new SensorIot("MULTISENSOR", "Bosch", LocalDate.now().minusDays(10), null));

        paso("Vehiculos");
        int camion1 = vehiculos.alta(new Vehiculo("CAMION", "AB123CD", new BigDecimal("30000"), transportes));
        int camion2 = vehiculos.alta(new Vehiculo("CAMION", "AC456EF", new BigDecimal("30000"), transportes));
        int tren = vehiculos.alta(new Vehiculo("TREN", "TREN-BELGRANO-07", new BigDecimal("400000"), transportes));
        int barco1 = vehiculos.alta(new Vehiculo("BARCO", "IMO 9301001", new BigDecimal("5000000"), naviera));
        int barco2 = vehiculos.alta(new Vehiculo("BARCO", "IMO 9301002", new BigDecimal("5000000"), naviera));
        int avion = vehiculos.alta(new Vehiculo("AVION", "CC-AWS", new BigDecimal("40000"), aerocarga));

        paso("Reglas de alerta generales");
        reglas.alta(new Regla("Bateria baja", "BATERIA", "MENOR", new BigDecimal("15"), "MEDIA", null));
        reglas.alta(new Regla("Vibracion alta", "VIBRACION", "MAYOR", new BigDecimal("2.5"), "MEDIA", null));
        reglas.alta(new Regla("Sensor sin reportar", "INACTIVIDAD", "MAYOR", new BigDecimal("30"), "ALTA", null));

        paso("Envio 1: entregado con demora (Buenos Aires - Santos)");
        int e1 = envios.crear(new Envio(alimentos, ciudad.get("Buenos Aires"), ciudad.get("Santos"), h(72), "ALTA",
                new BigDecimal("18000"), new BigDecimal("60"), "Carne congelada"), List.of(contenedor.get(0)));
        int e1t1 = tramos.agregar(new Tramo(e1, "CAMION", ciudad.get("Buenos Aires"), ciudad.get("Rosario"), camion1,
                h(1), h(6), new BigDecimal("800")));
        int e1t2 = tramos.agregar(new Tramo(e1, "BARCO", ciudad.get("Rosario"), ciudad.get("Santos"), barco1,
                h(8), h(60), new BigDecimal("4200")));
        tramos.registrarSalida(e1t1, h(1));
        String demoraE1 = incidentes.abrir(e1, "DEMORA", "BAJA", "Corte de ruta en la autopista a Rosario", List.of());
        tramos.registrarLlegada(e1t1, h(7));
        tramos.registrarSalida(e1t2, h(9));
        tramos.registrarLlegada(e1t2, h(80));
        incidentes.cerrar(demoraE1, "Se desvio por la ruta 8, llego con una hora de demora");
        envios.registrarEntrega(e1, h(82));

        paso("Envio 2: entregado a tiempo (Cordoba - Santiago)");
        int e2 = envios.crear(new Envio(farmaceutica, ciudad.get("Cordoba"), ciudad.get("Santiago"), h(24), "ALTA",
                new BigDecimal("1200"), new BigDecimal("8"), "Medicamentos"), List.of(contenedor.get(1)));
        int e2t1 = tramos.agregar(new Tramo(e2, "AVION", ciudad.get("Cordoba"), ciudad.get("Santiago"), avion,
                h(2), h(6), new BigDecimal("3500")));
        tramos.registrarSalida(e2t1, h(2));
        tramos.registrarLlegada(e2t1, h(5));
        envios.registrarEntrega(e2, h(6));

        paso("Envio 3: entregado a tiempo (Montevideo - Buenos Aires)");
        int e3 = envios.crear(new Envio(tecno, ciudad.get("Montevideo"), ciudad.get("Buenos Aires"), h(30), "MEDIA",
                new BigDecimal("9500"), new BigDecimal("45"), "Notebooks y monitores"), List.of(contenedor.get(2)));
        int e3t1 = tramos.agregar(new Tramo(e3, "BARCO", ciudad.get("Montevideo"), ciudad.get("Buenos Aires"), barco2,
                h(3), h(15), new BigDecimal("1900")));
        tramos.registrarSalida(e3t1, h(3));
        tramos.registrarLlegada(e3t1, h(14));
        envios.registrarEntrega(e3, h(15));

        paso("Envio 4: en transito con anomalia de temperatura (Buenos Aires - Sao Paulo)");
        int e4 = envios.crear(new Envio(alimentos, ciudad.get("Buenos Aires"), ciudad.get("Sao Paulo"), h(72), "ALTA",
                new BigDecimal("36000"), new BigDecimal("120"), "Lacteos refrigerados"),
                List.of(contenedor.get(3), contenedor.get(4)));
        int e4t1 = tramos.agregar(new Tramo(e4, "CAMION", ciudad.get("Buenos Aires"), ciudad.get("Rosario"), camion2,
                base, h(6), new BigDecimal("950")));
        tramos.agregar(new Tramo(e4, "CAMION", ciudad.get("Rosario"), ciudad.get("Sao Paulo"), camion1,
                h(8), h(56), new BigDecimal("5200")));
        tramos.registrarSalida(e4t1, base);
        reglas.alta(new Regla("Cadena de frio lacteos", "TEMPERATURA", "MAYOR", new BigDecimal("8"), "ALTA", e4));
        lectura(sensor.get(3), 4.8, 62.0, 0.4, 91.0, -34.6037, -58.3816);
        lectura(sensor.get(3), 5.2, 63.0, 0.6, 90.0, -34.2000, -58.9500);
        lectura(sensor.get(3), 9.4, 66.0, 0.5, 90.0, -33.7500, -59.8000);
        lectura(sensor.get(3), 6.1, 64.0, 0.3, 89.0, -33.3300, -60.2200);
        lectura(sensor.get(4), 4.5, 60.0, 0.4, 18.0, -34.6037, -58.3816);
        lectura(sensor.get(4), 4.7, 61.0, 0.5, 12.0, -33.7500, -59.8000);

        paso("Envio 5: en aduana (Valparaiso - Buenos Aires)");
        int e5 = envios.crear(new Envio(farmaceutica, ciudad.get("Valparaiso"), ciudad.get("Buenos Aires"), h(48),
                "MEDIA", new BigDecimal("7000"), new BigDecimal("30"), "Insumos hospitalarios"),
                List.of(contenedor.get(5)));
        int e5t1 = tramos.agregar(new Tramo(e5, "CAMION", ciudad.get("Valparaiso"), ciudad.get("Mendoza"), camion2,
                base, h(10), new BigDecimal("1400")));
        tramos.agregar(new Tramo(e5, "TREN", ciudad.get("Mendoza"), ciudad.get("Buenos Aires"), tren,
                h(14), h(40), new BigDecimal("1600")));
        tramos.registrarSalida(e5t1, base);
        envios.cambiarEstado(e5, "EN_ADUANA");
        incidentes.abrir(e5, "ADUANA", "MEDIA", "Falta el certificado sanitario en el paso Cristo Redentor", List.of());
        eventos.registrarManual(e5, "INSPECCION", ciudad.get("Mendoza"), "Inspeccion de la carga en el paso fronterizo");
        lectura(sensor.get(5), 18.0, 45.0, 0.8, 76.0, -33.0472, -71.6127);
        lectura(sensor.get(5), 17.5, 44.0, 3.1, 75.0, -32.8300, -70.0500);

        paso("Envio 6: en preparacion (Buenos Aires - Montevideo)");
        int e6 = envios.crear(new Envio(tecno, ciudad.get("Buenos Aires"), ciudad.get("Montevideo"), h(48), "BAJA",
                new BigDecimal("4000"), new BigDecimal("20"), "Repuestos de impresoras"), List.of(contenedor.get(6)));
        tramos.agregar(new Tramo(e6, "BARCO", ciudad.get("Buenos Aires"), ciudad.get("Montevideo"), barco2,
                h(24), h(36), new BigDecimal("1100")));
        envios.cambiarEstado(e6, "EN_PREPARACION");

        paso("Envio 7: cancelado (Rosario - Cordoba)");
        int e7 = envios.crear(new Envio(alimentos, ciudad.get("Rosario"), ciudad.get("Cordoba"), h(20), "MEDIA",
                new BigDecimal("5000"), new BigDecimal("25"), "Harina en bolsas"), List.of(contenedor.get(7)));
        envios.cancelar(e7);

        paso("Alertas");
        for (Alerta a : alertas.listarNoCerradas()) {
            if ("MEDIA".equals(a.getSeveridad())) {
                alertas.reconocer(a.getId());
                break;
            }
        }

        paso("Tipos de reporte y reportes");
        Map<String, Integer> tipo = cargarTiposReporte();
        LocalDate desde = LocalDate.now().minusDays(30);
        LocalDate hasta = LocalDate.now().plusDays(30);
        reportes.solicitar(new Reporte(alimentos, "Laura Fernandez", tipo.get("INCIDENTES_ABIERTOS"), desde, hasta));
        reportes.solicitar(new Reporte(alimentos, "Laura Fernandez", tipo.get("ENVIOS_DEL_CLIENTE"), desde, hasta));
        reportes.solicitar(new Reporte(farmaceutica, "Martin Rios", tipo.get("TIEMPOS_ENTREGA"), desde, hasta));

        paso("Facturacion, factura de proveedor y pagos");
        Set<String> periodos = new LinkedHashSet<>();
        periodos.add(YearMonth.from(base).toString());
        periodos.add(YearMonth.from(h(82)).toString());
        List<Factura> generadas = new ArrayList<>();
        for (String periodo : periodos) {
            generadas.addAll(facturas.facturacionAutomatica(periodo));
        }
        facturas.registrarFacturaProveedor(proveedorSensores, "0001-00004521", LocalDate.now(),
                List.of(new FacturaDetalle("10 multisensores de temperatura y GPS", new BigDecimal("850000")),
                        new FacturaDetalle("Instalacion y calibracion", new BigDecimal("120000"))));
        boolean pagoOperador = false;
        for (Factura f : generadas) {
            if ("EMITIDA".equals(f.getTipo()) && f.getParteId() == alimentos) {
                pagos.confirmar(pagos.registrar(f.getId(), "TRANSFERENCIA"));
            } else if ("EMITIDA".equals(f.getTipo()) && f.getParteId() == farmaceutica) {
                pagos.registrar(f.getId(), "CHEQUE");
            } else if ("RECIBIDA".equals(f.getTipo()) && !pagoOperador) {
                pagos.confirmar(pagos.registrar(f.getId(), "TRANSFERENCIA"));
                pagoOperador = true;
            }
        }

        paso("Mensajes y grupos");
        mensajes.enviarAParte(alimentos, comercialId, "SALIENTE",
                "Hola Laura, les enviamos la factura del mes. Cualquier consulta estamos a disposicion.");
        mensajes.enviarAParte(alimentos, comercialId, "ENTRANTE",
                "Laura (por mail): recibida, la pagamos por transferencia esta semana.");
        mensajes.enviarAParte(farmaceutica, operacionesId, "SALIENTE",
                "El envio de insumos quedo retenido en aduana por el certificado sanitario.");
        mensajes.enviarAParte(farmaceutica, operacionesId, "ENTRANTE",
                "Martin (por telefono): manda el certificado hoy a la tarde.");
        mensajes.enviarAParte(naviera, operacionesId, "SALIENTE",
                "Confirmamos la reserva para el contenedor a Montevideo.");
        String grupo = grupos.crear(new Grupo("Operaciones y Comercial", "Seguimiento de envios y cobranzas",
                List.of(operacionesId, comercialId), adminId));
        mensajes.enviarAGrupo(grupo, operacionesId, "El envio de lacteos tuvo un pico de temperatura, ya hay incidente abierto.");
        mensajes.enviarAGrupo(grupo, comercialId, "Gracias, le aviso al cliente.");

        paso("Prediccion de riesgo");
        predicciones.calcularDiaria();
    }

    private int cargarUsuarios() throws SQLException {
        Map<String, Integer> permiso = new HashMap<>();
        for (Permiso p : roles.listarPermisos()) {
            permiso.put(p.getCodigo(), p.getId());
        }

        int admin = rol("ADMIN", List.of(), permiso);
        int operaciones = rol("OPERACIONES", List.of("PARTES", "ACTIVOS", "ENVIOS", "MONITOREO", "INCIDENTES", "RED",
                "MENSAJERIA", "UBICACIONES"), permiso);
        int comercial = rol("COMERCIAL", List.of("PARTES", "COMERCIAL", "REPORTES", "ANALISIS", "MENSAJERIA"), permiso);
        int auditor = rol("AUDITOR", List.of("AUDITORIA", "ANALISIS"), permiso);

        int adminId = usuario("Administrador", "Sistema", EMAIL_ADMIN, PASSWORD_ADMIN, admin);
        usuario("Juan", "Perez", "operaciones@logistica.com", PASSWORD_DEMO, operaciones);
        usuario("Ana", "Gomez", "comercial@logistica.com", PASSWORD_DEMO, comercial);
        usuario("Carlos", "Diaz", "auditoria@logistica.com", PASSWORD_DEMO, auditor);
        return adminId;
    }

    private int rol(String descripcion, List<String> codigos, Map<String, Integer> permiso) throws SQLException {
        for (Rol r : roles.listar()) {
            if (descripcion.equals(r.getDescripcion())) {
                return r.getId();
            }
        }
        List<Integer> ids = new ArrayList<>();
        for (String codigo : codigos) {
            ids.add(permiso.get(codigo));
        }
        return roles.alta(new Rol(null, descripcion), ids);
    }

    private int usuario(String nombre, String apellido, String email, String password, int rolId)
            throws SQLException {
        for (Usuario u : usuarios.listar()) {
            if (email.equals(u.getEmail())) {
                return u.getId();
            }
        }
        return usuarios.alta(new Usuario(nombre, apellido, email, null, rolId), password);
    }

    private int buscarUsuario(String email) throws SQLException {
        for (Usuario u : usuarios.listar()) {
            if (email.equals(u.getEmail())) {
                return u.getId();
            }
        }
        throw new IllegalStateException("No se encontro el usuario " + email + ".");
    }

    private Map<String, Integer> cargarTiposReporte() throws SQLException {
        Map<String, Integer> tipo = new HashMap<>();
        for (TipoReporte t : tiposReporte.listar()) {
            tipo.put(t.getCodigo(), t.getId());
        }
        for (String codigo : ReporteService.CODIGOS) {
            if (!tipo.containsKey(codigo)) {
                String nombre = codigo.charAt(0) + codigo.substring(1).toLowerCase().replace('_', ' ');
                BigDecimal precio = codigo.startsWith("PREDICCION") || codigo.startsWith("RANKING")
                        ? new BigDecimal("8000") : new BigDecimal("5000");
                tipo.put(codigo, tiposReporte.alta(new TipoReporte(codigo, nombre, precio)));
            }
        }
        return tipo;
    }

    private void ciudad(String nombre, int paisId) throws SQLException {
        ciudad.put(nombre, ubicaciones.altaCiudad(nombre, paisId));
    }

    private void lectura(int sensorId, Double temperatura, Double humedad, Double vibracion, Double bateria,
            Double latitud, Double longitud) throws SQLException {
        lecturas.registrar(new LecturaSensor(sensorId, temperatura, humedad, vibracion, bateria, latitud, longitud));
    }

    private LocalDateTime h(int horas) {
        return base.plusHours(horas);
    }

    private void paso(String texto) {
        System.out.println("- " + texto);
    }

}
