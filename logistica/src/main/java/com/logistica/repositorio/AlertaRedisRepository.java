package com.logistica.repositorio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.logistica.database.RedisConexion;
import com.logistica.modelo.Alerta;

import redis.clients.jedis.Jedis;

public class AlertaRedisRepository {

    private static final String ACTIVAS = "alertas:activas";

    public void guardar(Alerta a) {
        try (Jedis j = RedisConexion.getConexion()) {
            j.del(clave(a.getId()));
            j.hset(clave(a.getId()), aMapa(a));
            j.sadd(ACTIVAS, a.getId());
        }
    }

    public void quitar(String alertaId) {
        try (Jedis j = RedisConexion.getConexion()) {
            j.del(clave(alertaId));
            j.srem(ACTIVAS, alertaId);
        }
    }

    public List<Alerta> listarActivas() {
        try (Jedis j = RedisConexion.getConexion()) {
            List<Alerta> alertas = new ArrayList<>();
            for (String id : j.smembers(ACTIVAS)) {
                Map<String, String> m = j.hgetAll(clave(id));
                if (!m.isEmpty()) {
                    alertas.add(desdeMapa(m));
                }
            }
            alertas.sort(Comparator.comparing(Alerta::getFecha).reversed());
            return alertas;
        }
    }

    public long contar() {
        try (Jedis j = RedisConexion.getConexion()) {
            return j.scard(ACTIVAS);
        }
    }

    public void borrarTodas() {
        try (Jedis j = RedisConexion.getConexion()) {
            for (String id : j.smembers(ACTIVAS)) {
                j.del(clave(id));
            }
            j.del(ACTIVAS);
        }
    }

    private String clave(String alertaId) {
        return "alerta:" + alertaId;
    }

    private Map<String, String> aMapa(Alerta a) {
        Map<String, String> m = new HashMap<>();
        agregar(m, "id", a.getId());
        agregar(m, "sensorId", a.getSensorId());
        agregar(m, "envioId", a.getEnvioId());
        agregar(m, "reglaId", a.getReglaId());
        agregar(m, "fechaLectura", a.getFechaLectura());
        agregar(m, "tipo", a.getTipo());
        agregar(m, "valor", a.getValor());
        agregar(m, "umbral", a.getUmbral());
        agregar(m, "severidad", a.getSeveridad());
        agregar(m, "fecha", a.getFecha());
        agregar(m, "estado", a.getEstado());
        agregar(m, "incidenteId", a.getIncidenteId());
        return m;
    }

    private Alerta desdeMapa(Map<String, String> m) {
        Alerta a = new Alerta();
        a.setId(m.get("id"));
        a.setSensorId(entero(m.get("sensorId")));
        a.setEnvioId(entero(m.get("envioId")));
        a.setReglaId(entero(m.get("reglaId")));
        a.setFechaLectura(fecha(m.get("fechaLectura")));
        a.setTipo(m.get("tipo"));
        a.setValor(decimal(m.get("valor")));
        a.setUmbral(decimal(m.get("umbral")));
        a.setSeveridad(m.get("severidad"));
        a.setFecha(fecha(m.get("fecha")));
        a.setEstado(m.get("estado"));
        a.setIncidenteId(m.get("incidenteId"));
        return a;
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

    private LocalDateTime fecha(String valor) {
        return (valor == null) ? null : LocalDateTime.parse(valor);
    }

}
