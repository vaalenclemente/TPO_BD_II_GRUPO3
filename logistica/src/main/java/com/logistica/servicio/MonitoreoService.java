package com.logistica.servicio;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.logistica.modelo.Alerta;
import com.logistica.modelo.LecturaSensor;
import com.logistica.modelo.Regla;
import com.logistica.modelo.SensorIot;
import com.logistica.repositorio.AlertaRedisRepository;
import com.logistica.repositorio.AlertaRepository;
import com.logistica.repositorio.EnvioContenedorRepository;
import com.logistica.repositorio.LecturaSensorRepository;
import com.logistica.repositorio.MonitoreoRedisRepository;
import com.logistica.repositorio.ReglaRepository;
import com.logistica.repositorio.SensorIotRepository;

import redis.clients.jedis.exceptions.JedisException;

public class MonitoreoService {

    private final MonitoreoRedisRepository redis = new MonitoreoRedisRepository();
    private final AlertaRedisRepository alertaRedis = new AlertaRedisRepository();
    private final AlertaRepository alertaRepo = new AlertaRepository();
    private final LecturaSensorRepository lecturaRepo = new LecturaSensorRepository();
    private final SensorIotRepository sensorRepo = new SensorIotRepository();
    private final ReglaRepository reglaRepo = new ReglaRepository();
    private final EnvioContenedorRepository ecRepo = new EnvioContenedorRepository();

    public List<Alerta> detectarSensoresInactivos() throws SQLException {

        List<Regla> reglas = reglaRepo.listarGeneralesActivas("INACTIVIDAD");
        if (reglas.isEmpty()) {
            throw new IllegalArgumentException("No hay ninguna regla de INACTIVIDAD activa.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        List<Alerta> nuevas = new ArrayList<>();

        for (Regla r : reglas) {
            List<Integer> sensores;
            try {
                sensores = redis.listarSinLatidoDesde(ahora.minusMinutes(r.getValor().longValue()));
            } catch (JedisException e) {
                throw new IllegalStateException(
                        "Redis no esta disponible: sin el heartbeat no se pueden detectar sensores inactivos.");
            }

            for (Integer sensorId : sensores) {
                SensorIot s = sensorRepo.buscarPorId(sensorId);
                if (s == null || "BAJA".equals(s.getEstado()) || s.getContenedorId() == null) {
                    RedisOpcional.escribir("el heartbeat", () -> redis.quitarLatido(sensorId));
                    continue;
                }
                if (alertaRepo.buscarAbierta(sensorId, r.getId()) != null) {
                    continue;
                }
                nuevas.add(marcarInactivo(s, r, ahora));
            }
        }
        return nuevas;
    }

    void reactivarSensor(SensorIot s) throws SQLException {
        int sensorId = s.getId();
        List<Alerta> abiertas = alertaRepo.listarAbiertasPorSensor(sensorId, "SENSOR_INACTIVO");

        Saga saga = new Saga("REACTIVAR_SENSOR", "sensor");
        try {
            saga.objeto(sensorId);

            sensorRepo.cambiarEstado(sensorId, "ACTIVO");
            saga.paso("MYSQL_SENSOR_ACTIVO", () -> sensorRepo.cambiarEstado(sensorId, "INACTIVO"));

            for (Alerta a : abiertas) {
                alertaRepo.cambiarEstado(a.getId(), "CERRADA");
                saga.paso("MONGO_ALERTA_" + a.getId() + "_CERRADA",
                        () -> alertaRepo.cambiarEstado(a.getId(), a.getEstado()));
            }

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }

        for (Alerta a : abiertas) {
            RedisOpcional.escribir("las alertas activas", () -> alertaRedis.quitar(a.getId()));
        }
    }

    public boolean redisVacio() {
        return !redis.hayLatidos() && alertaRedis.contar() == 0;
    }

    public String reconstruir() throws SQLException {
        int sensores = 0;
        for (SensorIot s : sensorRepo.listar()) {
            if ("BAJA".equals(s.getEstado()) || s.getContenedorId() == null) {
                continue;
            }

            LecturaSensor l = lecturaRepo.buscarUltimaPorSensor(s.getId());
            if (l == null) {
                redis.registrarLatido(s.getId(), LocalDateTime.now());
            } else {
                redis.guardarUltimaLectura(l);
                redis.guardarEstadoContenedor(l);
                redis.registrarLatido(s.getId(), l.getFechaHora());
                if (l.getEnvioId() != null) {
                    LecturaSensor conPosicion = lecturaRepo.buscarUltimaConPosicion(l.getEnvioId());
                    if (conPosicion != null) {
                        redis.guardarUbicacionEnvio(conPosicion);
                    }
                }
            }
            sensores++;
        }

        alertaRedis.borrarTodas();
        int alertas = 0;
        for (Alerta a : alertaRepo.listarNoCerradas()) {
            alertaRedis.guardar(a);
            alertas++;
        }

        return "Redis reconstruido desde MongoDB: " + sensores + " sensores y " + alertas + " alertas activas.";
    }

    private Alerta marcarInactivo(SensorIot s, Regla r, LocalDateTime ahora) throws SQLException {
        int sensorId = s.getId();
        LocalDateTime ultima = redis.buscarLatido(sensorId);

        Alerta a = new Alerta();
        a.setSensorId(sensorId);
        a.setEnvioId(ecRepo.buscarEnvioActivo(s.getContenedorId()));
        a.setReglaId(r.getId());
        a.setFechaLectura(ultima);
        a.setTipo("SENSOR_INACTIVO");
        a.setValor((ultima == null) ? null : (double) Duration.between(ultima, ahora).toMinutes());
        a.setUmbral(r.getValor().doubleValue());
        a.setSeveridad(r.getSeveridad());
        a.setFecha(ahora);
        a.setEstado("ACTIVA");

        Saga saga = new Saga("SENSOR_INACTIVO", "sensor");
        try {
            saga.objeto(sensorId);

            if ("ACTIVO".equals(s.getEstado())) {
                sensorRepo.cambiarEstado(sensorId, "INACTIVO");
                saga.paso("MYSQL_SENSOR_INACTIVO", () -> sensorRepo.cambiarEstado(sensorId, "ACTIVO"));
            }

            String alertaId = alertaRepo.guardar(a);
            a.setId(alertaId);
            saga.paso("MONGO_ALERTA_SENSOR_INACTIVO", () -> alertaRepo.borrar(alertaId));

            saga.completar();

        } catch (Exception ex) {
            throw saga.fallar(ex);
        }

        RedisOpcional.escribir("las alertas activas", () -> alertaRedis.guardar(a));
        return a;
    }

}
