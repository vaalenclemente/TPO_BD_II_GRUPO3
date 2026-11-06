package com.logistica.servicio;

import java.util.function.Supplier;

import redis.clients.jedis.exceptions.JedisException;

class RedisOpcional {

    static void escribir(String que, Runnable accion) {
        try {
            accion.run();
        } catch (JedisException e) {
            System.out.println("Aviso: Redis no está disponible, no se actualizó " + que + ".");
        }
    }

    static <T> T leer(Supplier<T> consulta) {
        try {
            return consulta.get();
        } catch (JedisException e) {
            System.out.println("Aviso: Redis no está disponible, se consulta MongoDB.");
            return null;
        }
    }

}
