package com.logistica.database;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Jedis;

public class RedisConexion {

    private static final String REDIS_Host = "localhost";
    private static final int REDIS_Puerto = 6379;

    private static final JedisPool REDIS_POOL = new JedisPool(REDIS_Host, REDIS_Puerto);

    public static Jedis getConexion() {
        return REDIS_POOL.getResource();
    }

}