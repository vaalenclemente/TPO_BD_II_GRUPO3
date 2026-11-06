package com.logistica.repositorio;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.logistica.database.RedisConexion;
import com.logistica.modelo.LecturaSensor;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ZAddParams;

public class MonitoreoRedisRepository {

    private static final String HEARTBEAT = "sensores:heartbeat";
    private static final ZoneId ZONA = ZoneId.systemDefault();

    public void guardarUltimaLectura(LecturaSensor l) {
        guardar("sensor:" + l.getSensorId() + ":ultima_lectura", l);
    }

    public LecturaSensor buscarUltimaLectura(int sensorId) {
        return buscar("sensor:" + sensorId + ":ultima_lectura");
    }

    public void guardarEstadoContenedor(LecturaSensor l) {
        guardar("contenedor:" + l.getContenedorId() + ":estado", l);
    }

    public LecturaSensor buscarEstadoContenedor(int contenedorId) {
        return buscar("contenedor:" + contenedorId + ":estado");
    }

    public void guardarUbicacionEnvio(LecturaSensor l) {
        guardar("envio:" + l.getEnvioId() + ":ubicacion", l);
    }

    public LecturaSensor buscarUbicacionEnvio(int envioId) {
        return buscar("envio:" + envioId + ":ubicacion");
    }

    public void registrarLatido(int sensorId, LocalDateTime fecha) {
        try (Jedis j = RedisConexion.getConexion()) {
            j.zadd(HEARTBEAT, aMilis(fecha), String.valueOf(sensorId), ZAddParams.zAddParams().gt());
        }
    }

    public void quitarLatido(int sensorId) {
        try (Jedis j = RedisConexion.getConexion()) {
            j.zrem(HEARTBEAT, String.valueOf(sensorId));
        }
    }

    public LocalDateTime buscarLatido(int sensorId) {
        try (Jedis j = RedisConexion.getConexion()) {
            Double score = j.zscore(HEARTBEAT, String.valueOf(sensorId));
            return (score == null) ? null : aFecha(score);
        }
    }

    public List<Integer> listarSinLatidoDesde(LocalDateTime limite) {
        try (Jedis j = RedisConexion.getConexion()) {
            List<Integer> sensores = new ArrayList<>();
            for (String id : j.zrangeByScore(HEARTBEAT, 0, aMilis(limite))) {
                sensores.add(Integer.valueOf(id));
            }
            return sensores;
        }
    }

    public boolean hayLatidos() {
        try (Jedis j = RedisConexion.getConexion()) {
            return j.exists(HEARTBEAT);
        }
    }

    private void guardar(String clave, LecturaSensor l) {
        try (Jedis j = RedisConexion.getConexion()) {
            String guardada = j.hget(clave, "fechaHora");
            if (guardada != null && LocalDateTime.parse(guardada).isAfter(l.getFechaHora())) {
                return;
            }
            j.del(clave);
            j.hset(clave, aMapa(l));
        }
    }

    private LecturaSensor buscar(String clave) {
        try (Jedis j = RedisConexion.getConexion()) {
            Map<String, String> m = j.hgetAll(clave);
            return m.isEmpty() ? null : desdeMapa(m);
        }
    }

    private Map<String, String> aMapa(LecturaSensor l) {
        Map<String, String> m = new HashMap<>();
        agregar(m, "sensorId", l.getSensorId());
        agregar(m, "contenedorId", l.getContenedorId());
        agregar(m, "envioId", l.getEnvioId());
        agregar(m, "fechaHora", l.getFechaHora());
        agregar(m, "temperatura", l.getTemperatura());
        agregar(m, "humedad", l.getHumedad());
        agregar(m, "vibracion", l.getVibracion());
        agregar(m, "nivelBateria", l.getNivelBateria());
        agregar(m, "latitud", l.getLatitud());
        agregar(m, "longitud", l.getLongitud());
        agregar(m, "anomaliaMetrica", l.getAnomaliaMetrica());
        return m;
    }

    private LecturaSensor desdeMapa(Map<String, String> m) {
        LecturaSensor l = new LecturaSensor();
        l.setSensorId(entero(m.get("sensorId")));
        l.setContenedorId(entero(m.get("contenedorId")));
        l.setEnvioId(entero(m.get("envioId")));
        l.setFechaHora(m.get("fechaHora") == null ? null : LocalDateTime.parse(m.get("fechaHora")));
        l.setTemperatura(decimal(m.get("temperatura")));
        l.setHumedad(decimal(m.get("humedad")));
        l.setVibracion(decimal(m.get("vibracion")));
        l.setNivelBateria(decimal(m.get("nivelBateria")));
        l.setLatitud(decimal(m.get("latitud")));
        l.setLongitud(decimal(m.get("longitud")));
        l.setAnomaliaMetrica(m.get("anomaliaMetrica"));
        return l;
    }

    private void agregar(Map<String, String> m, String campo, Object valor) {
        if (valor != null) {
            m.put(campo, valor.toString());
        }
    }

    private Integer entero(String valor) {
        return (valor == null) ? null : Integer.valueOf(valor);
    }

    private Double decimal(String valor) {
        return (valor == null) ? null : Double.valueOf(valor);
    }

    private double aMilis(LocalDateTime fecha) {
        return fecha.atZone(ZONA).toInstant().toEpochMilli();
    }

    private LocalDateTime aFecha(double milis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli((long) milis), ZONA);
    }

}
