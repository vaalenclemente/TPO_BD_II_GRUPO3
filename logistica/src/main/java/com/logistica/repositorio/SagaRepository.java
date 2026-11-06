package com.logistica.repositorio;

import java.util.ArrayList;
import java.util.Date;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.logistica.database.MongodbConexion;
import com.mongodb.client.MongoCollection;

import static com.mongodb.client.model.Filters.eq;

public class SagaRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("sagas");
    }

    public String iniciar(String tipo) {
        Document doc = new Document()
                .append("tipo", tipo)
                .append("estado", "INICIADA")
                .append("objetoId", null)
                .append("pasos", new ArrayList<Document>())
                .append("error", null)
                .append("fechaInicio", new Date())
                .append("fechaFin", null);

        coleccion().insertOne(doc);
        return doc.getObjectId("_id").toHexString();
    }

    public void asignarObjeto(String sagaId, Object objetoId) {
        coleccion().updateOne(eq("_id", new ObjectId(sagaId)),
                new Document("$set", new Document("objetoId", objetoId)));
    }

    public void registrarPaso(String sagaId, String paso) {
        Document registro = new Document()
                .append("paso", paso)
                .append("estado", "OK")
                .append("fecha", new Date());

        coleccion().updateOne(eq("_id", new ObjectId(sagaId)),
                new Document("$push", new Document("pasos", registro))
                        .append("$set", new Document("estado", paso)));
    }

    public void completar(String sagaId) {
        finalizar(sagaId, "COMPLETADA", null);
    }

    public void compensada(String sagaId, String error) {
        finalizar(sagaId, "COMPENSADA", error);
    }

    public void error(String sagaId, String error) {
        finalizar(sagaId, "ERROR", error);
    }

    private void finalizar(String sagaId, String estado, String error) {
        coleccion().updateOne(eq("_id", new ObjectId(sagaId)),
                new Document("$set", new Document("estado", estado)
                        .append("error", error)
                        .append("fechaFin", new Date())));
    }

}
