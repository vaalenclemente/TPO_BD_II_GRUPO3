package com.logistica.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.logistica.modelo.Alerta;
import com.logistica.modelo.Incidente;
import com.logistica.modelo.LecturaSensor;
import com.logistica.modelo.Regla;
import com.logistica.modelo.SensorIot;
import com.logistica.repositorio.AlertaRedisRepository;
import com.logistica.repositorio.AlertaRepository;
import com.logistica.repositorio.EnvioContenedorRepository;
import com.logistica.repositorio.LecturaSensorRepository;
import com.logistica.repositorio.MonitoreoRedisRepository;
import com.logistica.repositorio.SensorIotRepository;

// Registro continuo de lecturas y detección de anomalias:
//   lectura → se evaluan las reglas activas (MySQL) → lectura con/sin anomalia (Mongo)
//           → una alerta por regla incumplida (Mongo)
//           → si la alerta es ALTA y el contenedor viaja: incidente automatico (saga Mongo + MySQL)
public class LecturaSensorService {

    private static final List<String> METRICAS = List.of("TEMPERATURA", "HUMEDAD", "VIBRACION", "BATERIA");

    private final LecturaSensorRepository repo = new LecturaSensorRepository();
    private final AlertaRepository alertaRepo = new AlertaRepository();
    private final SensorIotRepository sensorRepo = new SensorIotRepository();
    private final EnvioContenedorRepository ecRepo = new EnvioContenedorRepository();
    private final ReglaService reglaService = new ReglaService();
    private final IncidenteService incidenteService = new IncidenteService();
    private final MonitoreoService monitoreoService = new MonitoreoService();
    private final MonitoreoRedisRepository redis = new MonitoreoRedisRepository();
    private final AlertaRedisRepository alertaRedis = new AlertaRedisRepository();

    public List<Alerta> registrar(LecturaSensor l) throws SQLException {

        if (l.getSensorId() == null) {
            throw new IllegalArgumentException("Falta indicar el sensor.");
        }
        SensorIot sensor = sensorRepo.buscarPorId(l.getSensorId());
        if (sensor == null) {
            throw new IllegalArgumentException("No existe un sensor con id " + l.getSensorId() + ".");
        }
        if ("BAJA".equals(sensor.getEstado())) {
            throw new IllegalArgumentException("El sensor está dado de baja.");
        }
        if (sensor.getContenedorId() == null) {
            throw new IllegalArgumentException("El sensor no está instalado en ningún contenedor.");
        }
        validarMediciones(l);

        LocalDateTime ahora = LocalDateTime.now();
        if (l.getFechaHora() == null) {
            l.setFechaHora(ahora);
        } else if (l.getFechaHora().isAfter(ahora)) {
            throw new IllegalArgumentException("La fecha de la lectura no puede ser futura.");
        }
        l.setSensorTipo(sensor.getTipo());
        l.setSensorFabricante(sensor.getFabricante());
        l.setContenedorId(sensor.getContenedorId());
        l.setEnvioId(ecRepo.buscarEnvioActivo(sensor.getContenedorId()));

        List<Regla> incumplidas = new ArrayList<>();
        for (Regla r : reglasAplicables(l.getEnvioId())) {
            Double medicion = medicion(l, r.getMetrica());
            if (medicion != null && reglaService.seIncumple(r, BigDecimal.valueOf(medicion))) {
                incumplidas.add(r);
            }
        }

        Regla masGrave = null;
        for (Regla r : incumplidas) {
            if (masGrave == null || peso(r.getSeveridad()) > peso(masGrave.getSeveridad())) {
                masGrave = r;
            }
        }
        if (masGrave != null) {
            double limite = masGrave.getValor().doubleValue();
            double medida = medicion(l, masGrave.getMetrica());
            l.setAnomaliaReglaId(masGrave.getId());
            l.setAnomaliaMetrica(masGrave.getMetrica());
            l.setAnomaliaLimite(limite);
            l.setAnomaliaExceso(BigDecimal.valueOf(Math.abs(medida - limite)).setScale(2, RoundingMode.HALF_UP)
                    .doubleValue());
        }

        l.setId(repo.guardar(l));

        RedisOpcional.escribir("la última lectura", () -> {
            redis.guardarUltimaLectura(l);
            redis.guardarEstadoContenedor(l);
            if (l.getEnvioId() != null && l.getLatitud() != null) {
                redis.guardarUbicacionEnvio(l);
            }
            redis.registrarLatido(l.getSensorId(), l.getFechaHora());
        });

        List<Alerta> nuevas = new ArrayList<>();
        for (Regla r : incumplidas) {
            if (alertaRepo.buscarAbierta(l.getSensorId(), r.getId()) != null) {
                continue;
            }
            Alerta a = new Alerta();
            a.setSensorId(l.getSensorId());
            a.setEnvioId(l.getEnvioId());
            a.setReglaId(r.getId());
            a.setFechaLectura(l.getFechaHora());
            a.setTipo(tipoAlerta(r.getMetrica()));
            a.setValor(medicion(l, r.getMetrica()));
            a.setUmbral(r.getValor().doubleValue());
            a.setSeveridad(r.getSeveridad());
            a.setFecha(ahora);
            a.setEstado("ACTIVA");
            a.setId(alertaRepo.guardar(a));
            nuevas.add(a);
        }

        for (Alerta a : nuevas) {
            if (!"ALTA".equals(a.getSeveridad()) || a.getEnvioId() == null) {
                continue;
            }
            String tipoIncidente = metrica(a.getTipo());
            Incidente abierto = incidenteService.buscarAbierto(a.getEnvioId(), tipoIncidente);
            if (abierto != null) {
                incidenteService.sumarAlertas(abierto.getId(), List.of(a.getId()));
                a.setIncidenteId(abierto.getId());
            } else {
                String id = incidenteService.abrir(a.getEnvioId(), tipoIncidente, "ALTA",
                        "Detectado automáticamente: " + a.getTipo() + " " + a.getValor() + " (límite " + a.getUmbral()
                                + ") en el sensor " + a.getSensorId(),
                        List.of(a.getId()));
                a.setIncidenteId(id);
            }
        }

        for (Alerta a : nuevas) {
            RedisOpcional.escribir("las alertas activas", () -> alertaRedis.guardar(a));
        }

        if ("INACTIVO".equals(sensor.getEstado())) {
            monitoreoService.reactivarSensor(sensor);
        }

        return nuevas;
    }

    public LecturaSensor ultimaLectura(int sensorId) {
        LecturaSensor l = RedisOpcional.leer(() -> redis.buscarUltimaLectura(sensorId));
        return (l != null) ? l : repo.buscarUltimaPorSensor(sensorId);
    }

    public LecturaSensor estadoContenedor(int contenedorId) {
        LecturaSensor l = RedisOpcional.leer(() -> redis.buscarEstadoContenedor(contenedorId));
        return (l != null) ? l : repo.buscarUltimaPorContenedor(contenedorId);
    }

    public LecturaSensor ubicacionEnvio(int envioId) {
        LecturaSensor l = RedisOpcional.leer(() -> redis.buscarUbicacionEnvio(envioId));
        return (l != null) ? l : repo.buscarUltimaConPosicion(envioId);
    }

    public List<LecturaSensor> historialSensor(int sensorId, LocalDateTime desde, LocalDateTime hasta) {
        return repo.listarPorSensor(sensorId, desde, hasta);
    }

    public List<LecturaSensor> recorridoContenedor(int contenedorId, LocalDateTime desde, LocalDateTime hasta) {
        return repo.listarPorContenedor(contenedorId, desde, hasta);
    }

    public List<LecturaSensor> ultimasLecturas(int sensorId, int cantidad) {
        return repo.listarUltimasPorSensor(sensorId, cantidad);
    }

    public List<LecturaSensor> anomalias(String metrica, LocalDateTime desde, LocalDateTime hasta) {
        return repo.listarAnomalias(metrica, null, desde, hasta);
    }

    private List<Regla> reglasAplicables(Integer envioId) throws SQLException {
        if (envioId != null) {
            return reglaService.listarAplicables(envioId);
        }
        List<Regla> reglas = new ArrayList<>();
        for (String m : METRICAS) {
            reglas.addAll(reglaService.listarGeneralesActivas(m));
        }
        return reglas;
    }

    private Double medicion(LecturaSensor l, String metrica) {
        return switch (metrica) {
            case "TEMPERATURA" -> l.getTemperatura();
            case "HUMEDAD" -> l.getHumedad();
            case "VIBRACION" -> l.getVibracion();
            case "BATERIA" -> l.getNivelBateria();
            default -> null;
        };
    }

    private String tipoAlerta(String metrica) {
        return switch (metrica) {
            case "TEMPERATURA" -> "TEMP_FUERA_DE_RANGO";
            case "BATERIA" -> "BATERIA_BAJA";
            default -> metrica; // HUMEDAD, VIBRACION
        };
    }

    private String metrica(String tipoAlerta) {
        return switch (tipoAlerta) {
            case "TEMP_FUERA_DE_RANGO" -> "TEMPERATURA";
            case "BATERIA_BAJA" -> "BATERIA";
            default -> tipoAlerta;
        };
    }

    private int peso(String severidad) {
        return switch (severidad) {
            case "ALTA" -> 3;
            case "MEDIA" -> 2;
            default -> 1;
        };
    }

    private void validarMediciones(LecturaSensor l) {
        if (l.getTemperatura() == null && l.getHumedad() == null && l.getVibracion() == null
                && l.getNivelBateria() == null) {
            throw new IllegalArgumentException("La lectura no tiene ninguna medición.");
        }
        enRango(l.getHumedad(), 0, 100, "La humedad");
        enRango(l.getNivelBateria(), 0, 100, "El nivel de batería");
        if (l.getVibracion() != null && l.getVibracion() < 0) {
            throw new IllegalArgumentException("La vibración no puede ser negativa.");
        }
        enRango(l.getLatitud(), -90, 90, "La latitud");
        enRango(l.getLongitud(), -180, 180, "La longitud");
        if ((l.getLatitud() == null) != (l.getLongitud() == null)) {
            throw new IllegalArgumentException("La posición necesita latitud y longitud.");
        }
    }

    private void enRango(Double valor, double min, double max, String nombre) {
        if (valor != null && (valor < min || valor > max)) {
            throw new IllegalArgumentException(nombre + " tiene que estar entre " + min + " y " + max + ".");
        }
    }

}
