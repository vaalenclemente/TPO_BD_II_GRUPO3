package com.logistica.repositorio;

import static com.logistica.repositorio.MongoConversion.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Sorts.descending;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.logistica.database.MongodbConexion;
import com.logistica.modelo.Incidente;
import com.logistica.modelo.RutaIncidentes;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;

public class IncidenteRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("incidentes");
    }

    private Incidente mapear(Document doc) {
        Document envio = subdocumento(doc, "envio");
        Document tramo = doc.get("tramo", Document.class);

        Incidente i = new Incidente();
        i.setId(aTexto(doc.getObjectId("_id")));
        i.setEnvioId(aInteger(envio.get("id")));
        i.setClienteId(aInteger(envio.get("clienteId")));
        i.setClienteNombre(envio.getString("clienteNombre"));
        i.setEnvioOrigen(envio.getString("origen"));
        i.setEnvioDestino(envio.getString("destino"));
        if (tramo != null) {
            i.setTramoId(aInteger(tramo.get("id")));
            i.setTramoMedio(tramo.getString("medio"));
            i.setTramoOrigen(tramo.getString("origen"));
            i.setTramoDestino(tramo.getString("destino"));
        }
        i.setTipo(doc.getString("tipo"));
        i.setSeveridad(doc.getString("severidad"));
        i.setEstado(doc.getString("estado"));
        i.setDescripcion(doc.getString("descripcion"));
        List<String> alertaIds = new ArrayList<>();
        for (ObjectId id : doc.getList("alertaIds", ObjectId.class, new ArrayList<>())) {
            alertaIds.add(id.toHexString());
        }
        i.setAlertaIds(alertaIds);
        i.setFechaApertura(aLocalDateTime(doc.getDate("fechaApertura")));
        i.setFechaCierre(aLocalDateTime(doc.getDate("fechaCierre")));
        i.setResolucion(doc.getString("resolucion"));
        return i;
    }

    public String guardar(Incidente i) {
        List<ObjectId> alertaIds = new ArrayList<>();
        if (i.getAlertaIds() != null) {
            for (String id : i.getAlertaIds()) {
                alertaIds.add(new ObjectId(id));
            }
        }

        Document doc = new Document()
                .append("envio", new Document("id", i.getEnvioId())
                        .append("clienteId", i.getClienteId())
                        .append("clienteNombre", i.getClienteNombre())
                        .append("origen", i.getEnvioOrigen())
                        .append("destino", i.getEnvioDestino()))
                .append("tramo", (i.getTramoId() == null) ? null
                        : new Document("id", i.getTramoId())
                                .append("medio", i.getTramoMedio())
                                .append("origen", i.getTramoOrigen())
                                .append("destino", i.getTramoDestino()))
                .append("tipo", i.getTipo())
                .append("severidad", i.getSeveridad())
                .append("estado", i.getEstado())
                .append("descripcion", i.getDescripcion())
                .append("alertaIds", alertaIds)
                .append("fechaApertura", aDate(i.getFechaApertura()))
                .append("fechaCierre", aDate(i.getFechaCierre()))
                .append("resolucion", i.getResolucion());

        coleccion().insertOne(doc);
        return aTexto(doc.getObjectId("_id"));
    }

    public Incidente buscarPorId(String id) {
        ObjectId oid = aObjectId(id);
        if (oid == null) {
            return null;
        }
        Document doc = coleccion().find(eq("_id", oid)).first();
        return (doc == null) ? null : mapear(doc);
    }

    public Incidente buscarAbierto(int envioId, String tipo) {
        Document doc = coleccion().find(and(eq("envio.id", envioId), eq("tipo", tipo), eq("estado", "ABIERTO")))
                .first();
        return (doc == null) ? null : mapear(doc);
    }

    // indice { estado: 1 }
    public List<Incidente> listarPorEstado(String estado) {
        return listar(eq("estado", estado));
    }

    public List<Incidente> listarAbiertosPorCliente(int clienteId) {
        return listar(and(eq("estado", "ABIERTO"), eq("envio.clienteId", clienteId)));
    }

    public List<Incidente> listarPorEnvio(int envioId) {
        return listar(eq("envio.id", envioId));
    }

    // indice { tramo.origen: 1, tramo.destino: 1 }
    public long contarPorRuta(String origen, String destino) {
        return coleccion().countDocuments(and(eq("tramo.origen", origen), eq("tramo.destino", destino)));
    }

    public void agregarAlertas(String id, List<String> alertaIds) {
        List<ObjectId> ids = new ArrayList<>();
        for (String a : alertaIds) {
            ids.add(new ObjectId(a));
        }
        coleccion().updateOne(eq("_id", new ObjectId(id)),
                new Document("$addToSet", new Document("alertaIds", new Document("$each", ids))));
    }

    public void quitarAlertas(String id, List<String> alertaIds) {
        List<ObjectId> ids = new ArrayList<>();
        for (String a : alertaIds) {
            ids.add(new ObjectId(a));
        }
        coleccion().updateOne(eq("_id", new ObjectId(id)),
                new Document("$pullAll", new Document("alertaIds", ids)));
    }

    public void cerrar(String id, LocalDateTime fecha, String resolucion) {
        coleccion().updateOne(eq("_id", new ObjectId(id)),
                new Document("$set", new Document("estado", "CERRADO")
                        .append("fechaCierre", aDate(fecha))
                        .append("resolucion", resolucion)));
    }

    public void reabrir(String id) {
        coleccion().updateOne(eq("_id", new ObjectId(id)),
                new Document("$set", new Document("estado", "ABIERTO")
                        .append("fechaCierre", null)
                        .append("resolucion", null)));
    }

    public void borrar(String id) {
        coleccion().deleteOne(eq("_id", new ObjectId(id)));
    }

    // Ranking de rutas (tramo origen → destino) con mas incidentes.
    // $match → solo incidentes que pasaron en un tramo (y del cliente / periodo, si
    // se piden)
    // $group → un resultado por ruta, contando sus incidentes
    // $sort → de mas a menos incidentes
    // clienteId, desde y hasta pueden ser null (= sin filtro).
    public List<RutaIncidentes> rankingRutas(Integer clienteId, LocalDateTime desde, LocalDateTime hasta, int limite) {
        List<Bson> condiciones = new ArrayList<>();
        condiciones.add(ne("tramo", null));
        if (clienteId != null) {
            condiciones.add(eq("envio.clienteId", clienteId));
        }
        if (desde != null) {
            condiciones.add(gte("fechaApertura", aDate(desde)));
        }
        if (hasta != null) {
            condiciones.add(lt("fechaApertura", aDate(hasta)));
        }

        List<Bson> pipeline = new ArrayList<>(Arrays.asList(
                Aggregates.match(and(condiciones)),
                new Document("$group", new Document("_id",
                        new Document("origen", "$tramo.origen").append("destino", "$tramo.destino"))
                        .append("cantidad", new Document("$sum", 1))),
                new Document("$sort", new Document("cantidad", -1))));
        if (limite > 0) {
            pipeline.add(new Document("$limit", limite));
        }

        List<RutaIncidentes> ranking = new ArrayList<>();
        for (Document fila : coleccion().aggregate(pipeline)) {
            Document ruta = fila.get("_id", Document.class);
            ranking.add(new RutaIncidentes(ruta.getString("origen"), ruta.getString("destino"),
                    aInteger(fila.get("cantidad"))));
        }
        return ranking;
    }

    private List<Incidente> listar(Bson filtro) {
        List<Incidente> incidentes = new ArrayList<>();
        for (Document doc : coleccion().find(filtro).sort(descending("fechaApertura"))) {
            incidentes.add(mapear(doc));
        }
        return incidentes;
    }

}
