package com.logistica.repositorio;

import static com.logistica.repositorio.MongoConversion.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.logistica.database.MongodbConexion;
import com.logistica.modelo.Evento;
import com.mongodb.client.MongoCollection;

public class EventoRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("eventos");
    }

    private Evento mapear(Document doc) {
        Document ubicacion = doc.get("ubicacion", Document.class);

        Evento e = new Evento();
        e.setId(aTexto(doc.getObjectId("_id")));
        e.setEnvioId(aInteger(doc.get("envioId")));
        e.setContenedorIds(doc.getList("contenedorIds", Integer.class, new ArrayList<>()));
        e.setTipo(doc.getString("tipo"));
        e.setFechaHora(aLocalDateTime(doc.getDate("fechaHora")));
        if (ubicacion != null) {
            e.setCiudadId(aInteger(ubicacion.get("ciudadId")));
            e.setCiudad(ubicacion.getString("ciudad"));
            e.setPais(ubicacion.getString("pais"));
        }
        e.setDescripcion(doc.getString("descripcion"));
        e.setUsuarioId(aInteger(doc.get("usuarioId")));
        return e;
    }

    public String guardar(Evento e) {
        Document doc = new Document()
                .append("envioId", e.getEnvioId())
                .append("contenedorIds", e.getContenedorIds())
                .append("tipo", e.getTipo())
                .append("fechaHora", aDate(e.getFechaHora()));

        if (e.getCiudadId() != null) {
            doc.append("ubicacion", new Document("ciudadId", e.getCiudadId())
                    .append("ciudad", e.getCiudad())
                    .append("pais", e.getPais()));
        }

        doc.append("descripcion", e.getDescripcion())
                .append("usuarioId", e.getUsuarioId());

        coleccion().insertOne(doc);
        return aTexto(doc.getObjectId("_id"));
    }

    // indice { envioid: 1, fechahora: 1 }
    public List<Evento> listarPorEnvio(int envioId) {
        return listar(eq("envioId", envioId));
    }

    // indice { contenedorids: 1, fechahora: 1 }
    public List<Evento> listarPorContenedor(int contenedorId) {
        return listar(eq("contenedorIds", contenedorId));
    }

    public void borrar(String id) {
        coleccion().deleteOne(eq("_id", new ObjectId(id)));
    }

    private List<Evento> listar(Bson filtro) {
        List<Evento> eventos = new ArrayList<>();
        for (Document doc : coleccion().find(filtro).sort(ascending("fechaHora"))) {
            eventos.add(mapear(doc));
        }
        return eventos;
    }

}
