package com.logistica.repositorio;

import static com.logistica.repositorio.MongoConversion.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.logistica.database.MongodbConexion;
import com.logistica.modelo.RegistroAuditoria;
import com.mongodb.client.MongoCollection;

public class AuditoriaRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("auditoria");
    }

    private RegistroAuditoria mapear(Document doc) {
        Document detalle = doc.get("detalle", Document.class);

        RegistroAuditoria r = new RegistroAuditoria();
        r.setId(aTexto(doc.getObjectId("_id")));
        r.setFecha(aLocalDateTime(doc.getDate("fecha")));
        r.setUsuarioId(aInteger(doc.get("usuarioId")));
        r.setAccion(doc.getString("accion"));
        r.setEntidad(doc.getString("entidad"));
        r.setEntidadId(doc.getString("entidadId"));
        if (detalle != null) {
            r.setCampo(detalle.getString("campo"));
            r.setAntes(detalle.getString("antes"));
            r.setDespues(detalle.getString("despues"));
        }
        r.setResultado(doc.getString("resultado"));
        ObjectId sagaId = doc.getObjectId("sagaId");
        r.setSagaId(aTexto(sagaId));
        return r;
    }

    public String guardar(RegistroAuditoria r) {
        Document doc = new Document()
                .append("fecha", aDate(r.getFecha()))
                .append("usuarioId", r.getUsuarioId())
                .append("accion", r.getAccion())
                .append("entidad", r.getEntidad())
                .append("entidadId", r.getEntidadId());

        if (r.getCampo() != null) {
            doc.append("detalle", new Document("campo", r.getCampo())
                    .append("antes", r.getAntes())
                    .append("despues", r.getDespues()));
        }

        doc.append("resultado", r.getResultado())
                .append("sagaId", aObjectId(r.getSagaId()));

        coleccion().insertOne(doc);
        return aTexto(doc.getObjectId("_id"));
    }

    public List<RegistroAuditoria> listarPorEntidad(String entidad, String entidadId) {
        return listar(and(eq("entidad", entidad), eq("entidadId", entidadId)), 0);
    }

    public List<RegistroAuditoria> listarPorUsuario(int usuarioId) {
        return listar(eq("usuarioId", usuarioId), 0);
    }

    public List<RegistroAuditoria> listarUltimos(int cantidad) {
        return listar(new Document(), cantidad);
    }

    private List<RegistroAuditoria> listar(Bson filtro, int limite) {
        List<RegistroAuditoria> lista = new ArrayList<>();
        for (Document doc : coleccion().find(filtro).sort(descending("fecha")).limit(limite)) {
            lista.add(mapear(doc));
        }
        return lista;
    }

}
