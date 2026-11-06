package com.logistica;

import java.sql.Connection;
import java.sql.SQLException;
import com.logistica.database.MySQLConexion;
import com.logistica.database.MySQLInicializacion;

import org.bson.Document;
import com.mongodb.MongoException;
import com.logistica.database.MongoInicializacion;
import com.logistica.database.MongodbConexion;
import org.neo4j.driver.exceptions.Neo4jException;
import com.logistica.database.Neo4jInicializacion;
import com.logistica.database.Neo4jConexion;
import com.logistica.database.Neo4jInicializacion;
import com.logistica.database.RedisConexion;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import com.logistica.servicio.MonitoreoService;
import com.logistica.cli.MenuPrincipal;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== SISTEMA LOGISTICO CLI ===");

        // verifico coneccion con mysql
        try (Connection c = MySQLConexion.getConexion()) {
            System.out.println("Conexión a MySQL exitosa.");
        } catch (SQLException e) {
            System.out.println("Falló la conexión MySQL: " + e.getMessage());
            return;
        }

        // verifico coneccion con mongo
        try {
            MongodbConexion.getDatabase().runCommand(new Document("ping", 1));
            System.out.println("Conexión a MongoDB exitosa.");
        } catch (MongoException e) {
            System.out.println("Falló la conexión MongoDB: " + e.getMessage());
            return;
        }

        // verifico coneccion con neo4j
        try {
            Neo4jConexion.getDriver().verifyConnectivity();
            System.out.println("Conexión a neo4j exitosa.");
        } catch (Neo4jException e) {
            System.out.println("Falló la conexión neo4j: " + e.getMessage());
            return;
        }

        // verifico coneccion con redis
        try (Jedis j = RedisConexion.getConexion()) {
            j.ping();
            System.out.println("Conexión a Redis exitosa.");
        } catch (JedisException e) {
            System.out.println("Falló la conexión Redis: " + e.getMessage());
        }

        // creo estructuras en db
        try {
            MySQLInicializacion.inicializar();
            System.out.println("Estructura MySQL lista.");
        } catch (SQLException e) {
            System.out.println("Falló la creación de tablas MySQL: " + e.getMessage());
            return;
        }

        try {
            MongoInicializacion.inicializar();
            System.out.println("Estructura Mongo lista.");
        } catch (MongoException e) {
            System.out.println("Falló la creación de índices Mongo: " + e.getMessage());
            return;
        }

        try {
            Neo4jInicializacion.inicializar();
            System.out.println("Estructura Neo4j lista.");
        } catch (Neo4jException e) {
            System.out.println("Falló la creación de índices Neo4j: " + e.getMessage());
            return;
        }

        try {
            MonitoreoService monitoreo = new MonitoreoService();
            if (monitoreo.redisVacio()) {
                System.out.println(monitoreo.reconstruir());
            }
        } catch (JedisException | SQLException e) {
            System.out.println("Aviso: no se pudo reconstruir Redis: " + e.getMessage());
        }

        new MenuPrincipal().iniciar();
    }
}