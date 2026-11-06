package com.logistica.repositorio;

import static com.logistica.repositorio.MongoConversion.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.set;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.logistica.database.MongodbConexion;
import com.logistica.modelo.Alerta;
import com.mongodb.client.MongoCollection;

public class AlertaRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("alertas");
    }

    private Alerta mapear(Document doc) {
        Alerta a = new Alerta();
        a.setId(aTexto(doc.getObjectId("_id")));
        a.setSensorId(aInteger(doc.get("sensorId")));
        a.setEnvioId(aInteger(doc.get("envioId")));
        a.setReglaId(aInteger(doc.get("reglaId")));
        a.setFechaLectura(aLocalDateTime(doc.getDate("fechaLectura")));
        a.setTipo(doc.getString("tipo"));
        a.setValor(aDouble(doc.get("valor")));
        a.setUmbral(aDouble(doc.get("umbral")));
        a.setSeveridad(doc.getString("severidad"));
        a.setFecha(aLocalDateTime(doc.getDate("fecha")));
        a.setEstado(doc.getString("estado"));
        a.setIncidenteId(doc.getString("incidenteId"));
        return a;
    }

    public String guardar(Alerta a) {
        Document doc = new Document()
                .append("sensorId", a.getSensorId())
                .append("envioId", a.getEnvioId())
                .append("reglaId", a.getReglaId())
                .append("fechaLectura", aDate(a.getFechaLectura()))
                .append("tipo", a.getTipo())
                .append("valor", a.getValor())
                .append("umbral", a.getUmbral())
                .append("severidad", a.getSeveridad())
                .append("fecha", aDate(a.getFecha()))
                .append("estado", a.getEstado())
                .append("incidenteId", a.getIncidenteId());

        coleccion().insertOne(doc);
        return aTexto(doc.getObjectId("_id"));
    }

    public Alerta buscarPorId(String id) {
        ObjectId oid = aObjectId(id);
        if (oid == null) {
            return null;
        }
        Document doc = coleccion().find(eq("_id", oid)).first();
        return (doc == null) ? null : mapear(doc);
    }

    public Alerta buscarAbierta(int sensorId, int reglaId) {
        Document doc = coleccion().find(and(eq("sensorId", sensorId), eq("reglaId", reglaId),
                ne("estado", "CERRADA"))).first();
        return (doc == null) ? null : mapear(doc);
    }

    // indice { estado: 1, fecha: -1 }
    public List<Alerta> listarPorEstado(String estado) {
        return listar(eq("estado", estado));
    }

    public List<Alerta> listarNoCerradas() {
        return listar(ne("estado", "CERRADA"));
    }

    public List<Alerta> listarAbiertasPorSensor(int sensorId, String tipo) {
        return listar(and(eq("sensorId", sensorId), eq("tipo", tipo), ne("estado", "CERRADA")));
    }

    public List<Alerta> listarPorEnvio(int envioId) {
        return listar(eq("envioId", envioId));
    }

    public long contarPorEnvio(int envioId) {
        return coleccion().countDocuments(eq("envioId", envioId));
    }

    public void cambiarEstado(String id, String estado) {
        coleccion().updateOne(eq("_id", new ObjectId(id)), set("estado", estado));
    }

    public void asignarIncidente(List<String> alertaIds, String incidenteId) {
        List<ObjectId> ids = new ArrayList<>();
        for (String id : alertaIds) {
            ids.add(new ObjectId(id));
        }
        coleccion().updateMany(in("_id", ids), set("incidenteId", incidenteId));
    }

    public void borrar(String id) {
        coleccion().deleteOne(eq("_id", new ObjectId(id)));
    }

    private List<Alerta> listar(Bson filtro) {
        List<Alerta> alertas = new ArrayList<>();
        for (Document doc : coleccion().find(filtro).sort(descending("fecha"))) {
            alertas.add(mapear(doc));
        }
        return alertas;
    }

}
