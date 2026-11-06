package com.logistica.servicio;

import java.util.List;

import com.logistica.modelo.Alerta;
import com.logistica.repositorio.AlertaRedisRepository;
import com.logistica.repositorio.AlertaRepository;

public class AlertaService {

    private final AlertaRepository repo = new AlertaRepository();
    private final AlertaRedisRepository redis = new AlertaRedisRepository();

    public List<Alerta> listarNoCerradas() {
        List<Alerta> alertas = RedisOpcional.leer(() -> redis.listarActivas());
        return (alertas != null) ? alertas : repo.listarNoCerradas();
    }

    public List<Alerta> listarPorEstado(String estado) {
        if (!"ACTIVA".equals(estado) && !"RECONOCIDA".equals(estado) && !"CERRADA".equals(estado)) {
            throw new IllegalArgumentException("El estado no es correcto (ACTIVA, RECONOCIDA, CERRADA).");
        }
        return repo.listarPorEstado(estado);
    }

    public List<Alerta> listarPorEnvio(int envioId) {
        return repo.listarPorEnvio(envioId);
    }

    public Alerta buscarPorId(String id) {
        return repo.buscarPorId(id);
    }

    public void reconocer(String id) {
        Alerta a = buscarExistente(id);
        if (!"ACTIVA".equals(a.getEstado())) {
            throw new IllegalArgumentException("Solo se reconoce una alerta ACTIVA (esta está " + a.getEstado() + ").");
        }
        repo.cambiarEstado(id, "RECONOCIDA");
        RedisOpcional.escribir("las alertas activas", () -> redis.guardar(repo.buscarPorId(id)));
    }

    public void cerrar(String id) {
        Alerta a = buscarExistente(id);
        if ("CERRADA".equals(a.getEstado())) {
            throw new IllegalArgumentException("La alerta ya está cerrada.");
        }
        repo.cambiarEstado(id, "CERRADA");
        RedisOpcional.escribir("las alertas activas", () -> redis.quitar(id));
    }

    private Alerta buscarExistente(String id) {
        Alerta a = repo.buscarPorId(id);
        if (a == null) {
            throw new IllegalArgumentException("No existe una alerta con id " + id + ".");
        }
        return a;
    }

}
