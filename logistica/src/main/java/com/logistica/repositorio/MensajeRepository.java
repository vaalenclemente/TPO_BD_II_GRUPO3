package com.logistica.repositorio;

import static com.logistica.repositorio.MongoConversion.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.logistica.database.MongodbConexion;
import com.logistica.modelo.Mensaje;
import com.mongodb.client.MongoCollection;

public class MensajeRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("mensajes");
    }

    private Mensaje mapear(Document doc) {
        Mensaje m = new Mensaje();
        m.setId(aTexto(doc.getObjectId("_id")));
        m.setTipoConversacion(doc.getString("tipoConversacion"));
        m.setParteId(aInteger(doc.get("parteId")));
        m.setParteTipo(doc.getString("parteTipo"));
        m.setDireccion(doc.getString("direccion"));
        m.setGrupoId(doc.getString("grupoId"));
        m.setRemitenteId(aInteger(doc.get("remitenteId")));
        m.setContenido(doc.getString("contenido"));
        m.setFechaHora(aLocalDateTime(doc.getDate("fechaHora")));
        m.setTipo(doc.getString("tipo"));
        return m;
    }

    public String guardar(Mensaje m) {
        Document doc = new Document("tipoConversacion", m.getTipoConversacion());

        if ("PARTE".equals(m.getTipoConversacion())) {
            doc.append("parteId", m.getParteId())
                    .append("parteTipo", m.getParteTipo())
                    .append("direccion", m.getDireccion());
        } else {
            doc.append("grupoId", m.getGrupoId());
        }

        doc.append("remitenteId", m.getRemitenteId())
                .append("contenido", m.getContenido())
                .append("fechaHora", aDate(m.getFechaHora()))
                .append("tipo", m.getTipo());

        coleccion().insertOne(doc);
        return aTexto(doc.getObjectId("_id"));
    }

    // indice { parteId: 1, fechaHora: 1 }
    public List<Mensaje> listarPorParte(int parteId) {
        return listar(and(eq("tipoConversacion", "PARTE"), eq("parteId", parteId)));
    }

    // indice { grupoId: 1, fechaHora: 1 }
    public List<Mensaje> listarPorGrupo(String grupoId) {
        return listar(and(eq("tipoConversacion", "GRUPO"), eq("grupoId", grupoId)));
    }

    private List<Mensaje> listar(Bson filtro) {
        List<Mensaje> mensajes = new ArrayList<>();
        for (Document doc : coleccion().find(filtro).sort(ascending("fechaHora"))) {
            mensajes.add(mapear(doc));
        }
        return mensajes;
    }

}
