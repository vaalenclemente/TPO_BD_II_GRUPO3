package com.logistica.repositorio;

import static com.logistica.repositorio.MongoConversion.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.logistica.database.MongodbConexion;
import com.logistica.modelo.PrediccionRiesgo;
import com.mongodb.client.MongoCollection;

public class PrediccionRiesgoRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("predicciones_riesgo");
    }

    private PrediccionRiesgo mapear(Document doc) {
        Document factores = subdocumento(doc, "factores");

        PrediccionRiesgo p = new PrediccionRiesgo();
        p.setId(aTexto(doc.getObjectId("_id")));
        p.setEnvioId(aInteger(doc.get("envioId")));
        p.setFechaCalculo(aLocalDateTime(doc.getDate("fechaCalculo")));
        p.setProbabilidadDemora(aDouble(doc.get("probabilidadDemora")));
        p.setRiesgoClimatico(aDouble(doc.get("riesgoClimatico")));
        p.setRiesgoOperativo(aDouble(doc.get("riesgoOperativo")));
        p.setNivelRiesgo(doc.getString("nivelRiesgo"));
        p.setDemoraActualHs(aDouble(factores.get("demoraActualHs")));
        p.setIncidentesRuta(aInteger(factores.get("incidentesRuta")));
        p.setAnomaliasSensor(aInteger(factores.get("anomaliasSensor")));
        return p;
    }

    public String guardar(PrediccionRiesgo p) {
        Document doc = new Document()
                .append("envioId", p.getEnvioId())
                .append("fechaCalculo", aDate(p.getFechaCalculo()))
                .append("probabilidadDemora", p.getProbabilidadDemora())
                .append("riesgoClimatico", p.getRiesgoClimatico())
                .append("riesgoOperativo", p.getRiesgoOperativo())
                .append("nivelRiesgo", p.getNivelRiesgo())
                .append("factores", new Document("demoraActualHs", p.getDemoraActualHs())
                        .append("incidentesRuta", p.getIncidentesRuta())
                        .append("anomaliasSensor", p.getAnomaliasSensor()));

        coleccion().insertOne(doc);
        return aTexto(doc.getObjectId("_id"));
    }

    public List<PrediccionRiesgo> listarPorEnvio(int envioId) {
        List<PrediccionRiesgo> lista = new ArrayList<>();
        for (Document doc : coleccion().find(eq("envioId", envioId)).sort(descending("fechaCalculo"))) {
            lista.add(mapear(doc));
        }
        return lista;
    }

    public List<PrediccionRiesgo> listarUltimas(String nivelRiesgo) {
        Document ultima = coleccion().find().sort(descending("fechaCalculo")).first();
        List<PrediccionRiesgo> lista = new ArrayList<>();
        if (ultima == null) {
            return lista;
        }

        Bson filtro = eq("fechaCalculo", ultima.getDate("fechaCalculo"));
        if (nivelRiesgo != null) {
            filtro = and(filtro, eq("nivelRiesgo", nivelRiesgo));
        }
        for (Document doc : coleccion().find(filtro).sort(descending("probabilidadDemora"))) {
            lista.add(mapear(doc));
        }
        return lista;
    }

}
