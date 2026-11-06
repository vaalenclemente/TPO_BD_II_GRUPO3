package com.logistica.servicio;

import redis.clients.jedis.exceptions.JedisException;

class RedisOpcional {

    interface Escritura {
        void ejecutar();
    }

    interface Lectura<T> {
        T ejecutar();
    }

    static void escribir(String que, Escritura escritura) {
        try {
            escritura.ejecutar();
        } catch (JedisException e) {
            System.out.println("Aviso: Redis no esta disponible, no se actualizo " + que + ".");
        }
    }

    static <T> T leer(Lectura<T> lectura) {
        try {
            return lectura.ejecutar();
        } catch (JedisException e) {
            System.out.println("Aviso: Redis no esta disponible, se consulta MongoDB.");
            return null;
        }
    }

}
