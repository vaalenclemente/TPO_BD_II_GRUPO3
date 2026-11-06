package com.logistica.repositorio;

import static com.logistica.repositorio.MongoConversion.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.logistica.database.MongodbConexion;
import com.logistica.modelo.LecturaSensor;
import com.mongodb.client.MongoCollection;

public class LecturaSensorRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("lecturas_sensor");
    }

    private Document aDocumento(LecturaSensor l) {
        Document doc = new Document()
                .append("fechaHora", aDate(l.getFechaHora()))
                .append("sensor", new Document("sensorId", l.getSensorId())
                        .append("tipo", l.getSensorTipo())
                        .append("fabricante", l.getSensorFabricante()))
                .append("ids", new Document("contenedorId", l.getContenedorId())
                        .append("envioId", l.getEnvioId()))
                .append("temperatura", l.getTemperatura())
                .append("humedad", l.getHumedad())
                .append("vibracion", l.getVibracion())
                .append("nivelBateria", l.getNivelBateria())
                .append("posicion", new Document("latitud", l.getLatitud())
                        .append("longitud", l.getLongitud()));

        if (l.getAnomaliaReglaId() != null) {
            doc.append("anomalia", new Document("reglaId", l.getAnomaliaReglaId())
                    .append("metrica", l.getAnomaliaMetrica())
                    .append("limite", l.getAnomaliaLimite())
                    .append("exceso", l.getAnomaliaExceso()));
        }
        return doc;
    }

    private LecturaSensor mapear(Document doc) {
        Document sensor = subdocumento(doc, "sensor");
        Document ids = subdocumento(doc, "ids");
        Document posicion = subdocumento(doc, "posicion");
        Document anomalia = doc.get("anomalia", Document.class);

        LecturaSensor l = new LecturaSensor();
        l.setId(aTexto(doc.getObjectId("_id")));
        l.setFechaHora(aLocalDateTime(doc.getDate("fechaHora")));
        l.setSensorId(aInteger(sensor.get("sensorId")));
        l.setSensorTipo(sensor.getString("tipo"));
        l.setSensorFabricante(sensor.getString("fabricante"));
        l.setContenedorId(aInteger(ids.get("contenedorId")));
        l.setEnvioId(aInteger(ids.get("envioId")));
        l.setTemperatura(aDouble(doc.get("temperatura")));
        l.setHumedad(aDouble(doc.get("humedad")));
        l.setVibracion(aDouble(doc.get("vibracion")));
        l.setNivelBateria(aDouble(doc.get("nivelBateria")));
        l.setLatitud(aDouble(posicion.get("latitud")));
        l.setLongitud(aDouble(posicion.get("longitud")));
        if (anomalia != null) {
            l.setAnomaliaReglaId(aInteger(anomalia.get("reglaId")));
            l.setAnomaliaMetrica(anomalia.getString("metrica"));
            l.setAnomaliaLimite(aDouble(anomalia.get("limite")));
            l.setAnomaliaExceso(aDouble(anomalia.get("exceso")));
        }
        return l;
    }

    public String guardar(LecturaSensor l) {
        Document doc = aDocumento(l);
        coleccion().insertOne(doc);
        return aTexto(doc.getObjectId("_id"));
    }

    public List<LecturaSensor> listarPorSensor(int sensorId, LocalDateTime desde, LocalDateTime hasta) {
        Bson filtro = and(eq("sensor.sensorId", sensorId), entre(desde, hasta));
        return listar(filtro, descending("fechaHora"), 0);
    }

    public List<LecturaSensor> listarPorContenedor(int contenedorId, LocalDateTime desde, LocalDateTime hasta) {
        Bson filtro = and(eq("ids.contenedorId", contenedorId), entre(desde, hasta));
        return listar(filtro, ascending("fechaHora"), 0);
    }

    public List<LecturaSensor> listarUltimasPorSensor(int sensorId, int cantidad) {
        return listar(eq("sensor.sensorId", sensorId), descending("fechaHora"), cantidad);
    }

    public LecturaSensor buscarUltimaPorSensor(int sensorId) {
        return primera(eq("sensor.sensorId", sensorId));
    }

    public LecturaSensor buscarUltimaPorContenedor(int contenedorId) {
        return primera(eq("ids.contenedorId", contenedorId));
    }

    public LecturaSensor buscarUltimaConPosicion(int envioId) {
        return primera(and(eq("ids.envioId", envioId), ne("posicion.latitud", null)));
    }

    private LecturaSensor primera(Bson filtro) {
        Document doc = coleccion().find(filtro).sort(descending("fechaHora")).first();
        return (doc == null) ? null : mapear(doc);
    }

    public List<LecturaSensor> listarAnomalias(String metrica, List<Integer> envioIds, LocalDateTime desde,
            LocalDateTime hasta) {
        Bson filtro = and(exists("anomalia.reglaId"), entre(desde, hasta));
        if (metrica != null) {
            filtro = and(filtro, eq("anomalia.metrica", metrica));
        }
        if (envioIds != null) {
            filtro = and(filtro, in("ids.envioId", envioIds));
        }
        return listar(filtro, descending("fechaHora"), 0);
    }

    public long contarAnomaliasPorEnvio(int envioId, LocalDateTime desde) {
        return coleccion().countDocuments(and(eq("ids.envioId", envioId), exists("anomalia.reglaId"),
                gte("fechaHora", aDate(desde))));
    }

    private List<LecturaSensor> listar(Bson filtro, Bson orden, int limite) {
        List<LecturaSensor> lecturas = new ArrayList<>();
        for (Document doc : coleccion().find(filtro).sort(orden).limit(limite)) {
            lecturas.add(mapear(doc));
        }
        return lecturas;
    }

    private Bson entre(LocalDateTime desde, LocalDateTime hasta) {
        List<Bson> condiciones = new ArrayList<>();
        if (desde != null) {
            condiciones.add(gte("fechaHora", aDate(desde)));
        }
        if (hasta != null) {
            condiciones.add(lt("fechaHora", aDate(hasta)));
        }
        return condiciones.isEmpty() ? new Document() : and(condiciones);
    }

}
