package com.logistica.repositorio;

import static com.logistica.repositorio.MongoConversion.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.logistica.database.MongodbConexion;
import com.logistica.modelo.Pago;
import com.mongodb.client.MongoCollection;

public class PagoRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("pagos");
    }

    private Document aDocumento(Pago.CambioEstado c) {
        return new Document("estado", c.getEstado())
                .append("fecha", aDate(c.getFecha()))
                .append("usuarioId", c.getUsuarioId());
    }

    private Pago mapear(Document doc) {
        Pago p = new Pago();
        p.setId(aTexto(doc.getObjectId("_id")));
        p.setTipo(doc.getString("tipo"));
        p.setFacturaId(aInteger(doc.get("facturaId")));
        p.setParteId(aInteger(doc.get("parteId")));
        p.setMonto(aBigDecimal(doc.get("monto")));
        p.setMedioPago(doc.getString("medioPago"));
        p.setFecha(aLocalDateTime(doc.getDate("fecha")));
        p.setEstado(doc.getString("estado"));
        p.setMovimientoId(aInteger(doc.get("movimientoId")));

        List<Pago.CambioEstado> historial = new ArrayList<>();
        for (Document c : doc.getList("historialEstados", Document.class, new ArrayList<>())) {
            historial.add(new Pago.CambioEstado(c.getString("estado"), aLocalDateTime(c.getDate("fecha")),
                    aInteger(c.get("usuarioId"))));
        }
        p.setHistorialEstados(historial);
        return p;
    }

    public String guardar(Pago p) {
        List<Document> historial = new ArrayList<>();
        for (Pago.CambioEstado c : p.getHistorialEstados()) {
            historial.add(aDocumento(c));
        }

        Document doc = new Document()
                .append("tipo", p.getTipo())
                .append("facturaId", p.getFacturaId())
                .append("parteId", p.getParteId())
                .append("monto", aDecimal(p.getMonto()))
                .append("medioPago", p.getMedioPago())
                .append("fecha", aDate(p.getFecha()))
                .append("estado", p.getEstado())
                .append("movimientoId", p.getMovimientoId())
                .append("historialEstados", historial);

        coleccion().insertOne(doc);
        return aTexto(doc.getObjectId("_id"));
    }

    public Pago buscarPorId(String id) {
        ObjectId oid = aObjectId(id);
        if (oid == null) {
            return null;
        }
        Document doc = coleccion().find(eq("_id", oid)).first();
        return (doc == null) ? null : mapear(doc);
    }

    public List<Pago> listarPorFactura(int facturaId) {
        return listar(eq("facturaId", facturaId));
    }

    public List<Pago> listarPorParte(int parteId) {
        return listar(eq("parteId", parteId));
    }

    public List<Pago> listarPorEstado(String estado) {
        return listar(eq("estado", estado));
    }

    public void cambiarEstado(String id, Pago.CambioEstado cambio) {
        coleccion().updateOne(eq("_id", new ObjectId(id)),
                new Document("$set", new Document("estado", cambio.getEstado()))
                        .append("$push", new Document("historialEstados", aDocumento(cambio))));
    }

    public void deshacerCambio(String id, String estadoAnterior) {
        coleccion().updateOne(eq("_id", new ObjectId(id)),
                new Document("$set", new Document("estado", estadoAnterior))
                        .append("$pop", new Document("historialEstados", 1)));
    }

    public void asignarMovimiento(String id, Integer movimientoId) {
        coleccion().updateOne(eq("_id", new ObjectId(id)),
                new Document("$set", new Document("movimientoId", movimientoId)));
    }

    public void borrar(String id) {
        coleccion().deleteOne(eq("_id", new ObjectId(id)));
    }

    private List<Pago> listar(Bson filtro) {
        List<Pago> pagos = new ArrayList<>();
        for (Document doc : coleccion().find(filtro).sort(descending("fecha"))) {
            pagos.add(mapear(doc));
        }
        return pagos;
    }

}
