package com.logistica.repositorio;

import static com.logistica.repositorio.MongoConversion.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Sorts.descending;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.logistica.database.MongodbConexion;
import com.logistica.modelo.Reporte;
import com.mongodb.client.MongoCollection;

public class ReporteRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("reportes");
    }

    private Reporte mapear(Document doc) {
        Document parametros = subdocumento(doc, "parametros");

        Reporte r = new Reporte();
        r.setId(aTexto(doc.getObjectId("_id")));
        r.setParteId(aInteger(doc.get("parteId")));
        r.setNombreSolicitante(doc.getString("nombreSolicitante"));
        r.setTipoReporteId(aInteger(doc.get("tipoReporteId")));
        r.setCodigo(doc.getString("codigo"));
        r.setDesde(aLocalDate(parametros.getDate("desde")));
        r.setHasta(aLocalDate(parametros.getDate("hasta")));
        r.setEstado(doc.getString("estado"));
        r.setFechaSolicitud(aLocalDateTime(doc.getDate("fechaSolicitud")));
        r.setFechaGeneracion(aLocalDateTime(doc.getDate("fechaGeneracion")));
        r.setContenido(doc.getString("contenido"));
        r.setCosto(aBigDecimal(doc.get("costo")));
        r.setFacturaId(aInteger(doc.get("facturaId")));
        r.setError(doc.getString("error"));
        return r;
    }

    public String guardar(Reporte r) {
        Document doc = new Document()
                .append("parteId", r.getParteId())
                .append("nombreSolicitante", r.getNombreSolicitante())
                .append("tipoReporteId", r.getTipoReporteId())
                .append("codigo", r.getCodigo())
                .append("parametros", new Document("desde", aDate(r.getDesde()))
                        .append("hasta", aDate(r.getHasta())))
                .append("estado", r.getEstado())
                .append("fechaSolicitud", aDate(r.getFechaSolicitud()))
                .append("fechaGeneracion", aDate(r.getFechaGeneracion()))
                .append("contenido", r.getContenido())
                .append("costo", aDecimal(r.getCosto()))
                .append("facturaId", r.getFacturaId())
                .append("error", r.getError());

        coleccion().insertOne(doc);
        return aTexto(doc.getObjectId("_id"));
    }

    public Reporte buscarPorId(String id) {
        ObjectId oid = aObjectId(id);
        if (oid == null) {
            return null;
        }
        Document doc = coleccion().find(eq("_id", oid)).first();
        return (doc == null) ? null : mapear(doc);
    }

    public void guardarResultado(String id, String estado, String contenido, LocalDateTime fechaGeneracion,
            String error) {
        coleccion().updateOne(eq("_id", new ObjectId(id)),
                new Document("$set", new Document("estado", estado)
                        .append("contenido", contenido)
                        .append("fechaGeneracion", aDate(fechaGeneracion))
                        .append("error", error)));
    }

    public void asignarFactura(String id, Integer facturaId) {
        coleccion().updateOne(eq("_id", new ObjectId(id)),
                new Document("$set", new Document("facturaId", facturaId)));
    }

    public List<Reporte> listarPorParte(int parteId) {
        return listar(eq("parteId", parteId));
    }

    public List<Reporte> listarGeneradosEntre(LocalDateTime desde, LocalDateTime hasta) {
        return listar(and(eq("estado", "GENERADO"), gte("fechaGeneracion", aDate(desde)),
                lt("fechaGeneracion", aDate(hasta))));
    }

    private List<Reporte> listar(Bson filtro) {
        List<Reporte> reportes = new ArrayList<>();
        for (Document doc : coleccion().find(filtro).sort(descending("fechaSolicitud"))) {
            reportes.add(mapear(doc));
        }
        return reportes;
    }

}
