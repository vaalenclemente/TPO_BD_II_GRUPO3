package com.logistica.repositorio;

import static com.logistica.repositorio.MongoConversion.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Updates.addToSet;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.pull;
import static com.mongodb.client.model.Updates.set;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.logistica.database.MongodbConexion;
import com.logistica.modelo.Grupo;
import com.mongodb.client.MongoCollection;

public class GrupoRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("grupos");
    }

    private Grupo mapear(Document doc) {
        Grupo g = new Grupo();
        g.setId(aTexto(doc.getObjectId("_id")));
        g.setNombre(doc.getString("nombre"));
        g.setDescripcion(doc.getString("descripcion"));
        g.setIntegrantes(doc.getList("integrantes", Integer.class, new ArrayList<>()));
        g.setCreadoPor(aInteger(doc.get("creadoPor")));
        g.setFechaCreacion(aLocalDateTime(doc.getDate("fechaCreacion")));
        g.setEstado(doc.getString("estado"));
        return g;
    }

    public String guardar(Grupo g) {
        Document doc = new Document()
                .append("nombre", g.getNombre())
                .append("descripcion", g.getDescripcion())
                .append("integrantes", g.getIntegrantes())
                .append("creadoPor", g.getCreadoPor())
                .append("fechaCreacion", aDate(g.getFechaCreacion()))
                .append("estado", g.getEstado());

        coleccion().insertOne(doc);
        return aTexto(doc.getObjectId("_id"));
    }

    public Grupo buscarPorId(String id) {
        ObjectId oid = aObjectId(id);
        if (oid == null) {
            return null;
        }
        Document doc = coleccion().find(eq("_id", oid)).first();
        return (doc == null) ? null : mapear(doc);
    }

    public List<Grupo> listar() {
        return listar(new Document());
    }

    public List<Grupo> listarPorIntegrante(int usuarioId) {
        return listar(eq("integrantes", usuarioId));
    }

    public void actualizar(Grupo g) {
        coleccion().updateOne(eq("_id", new ObjectId(g.getId())),
                combine(set("nombre", g.getNombre()), set("descripcion", g.getDescripcion())));
    }

    // $addtoset no agrega si ya esta; $pull lo saca.
    public void agregarIntegrante(String id, int usuarioId) {
        coleccion().updateOne(eq("_id", new ObjectId(id)), addToSet("integrantes", usuarioId));
    }

    public void quitarIntegrante(String id, int usuarioId) {
        coleccion().updateOne(eq("_id", new ObjectId(id)), pull("integrantes", usuarioId));
    }

    public void cambiarEstado(String id, String estado) {
        coleccion().updateOne(eq("_id", new ObjectId(id)), set("estado", estado));
    }

    private List<Grupo> listar(Bson filtro) {
        List<Grupo> grupos = new ArrayList<>();
        for (Document doc : coleccion().find(filtro).sort(ascending("nombre"))) {
            grupos.add(mapear(doc));
        }
        return grupos;
    }

}
