package com.logistica.database;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

public class MongoInicializacion {

    public static void inicializar() {

        MongoDatabase db = MongodbConexion.getDatabase();

        // indices

        db.getCollection("lecturas_sensor").createIndex(new Document("sensor.sensorId", 1).append("fechaHora", -1));

        db.getCollection("lecturas_sensor").createIndex(new Document("ids.contenedorId", 1).append("fechaHora", -1));

        db.getCollection("lecturas_sensor").createIndex(new Document("anomalia.reglaId", 1));

        db.getCollection("alertas").createIndex(new Document("estado", 1).append("fecha", -1));

        db.getCollection("alertas").createIndex(new Document("sensorId", 1).append("reglaId", 1));

        db.getCollection("eventos").createIndex(new Document("envioId", 1).append("fechaHora", 1));

        db.getCollection("eventos").createIndex(new Document("contenedorIds", 1).append("fechaHora", 1));

        db.getCollection("incidentes").createIndex(new Document("estado", 1));

        db.getCollection("incidentes").createIndex(new Document("tramo.origen", 1).append("tramo.destino", 1));

        db.getCollection("incidentes").createIndex(new Document("envio.id", 1).append("estado", 1));

        db.getCollection("grupos").createIndex(new Document("integrantes", 1));

        db.getCollection("mensajes").createIndex(new Document("parteId", 1).append("fechaHora", 1));
        db.getCollection("mensajes").createIndex(
                new Document("parteId", 1).append("remitenteId", 1).append("fechaHora", 1));

        db.getCollection("mensajes").createIndex(new Document("grupoId", 1).append("fechaHora", 1));

        db.getCollection("cierres_cuenta").createIndex(new Document("cuentaId", 1).append("periodo", 1));

        db.getCollection("predicciones_riesgo").createIndex(new Document("envioId", 1).append("fechaCalculo", -1));

        db.getCollection("predicciones_riesgo").createIndex(new Document("fechaCalculo", -1));
    }
}