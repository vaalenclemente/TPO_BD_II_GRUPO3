package com.logistica.repositorio;

import static com.logistica.repositorio.MongoConversion.*;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.logistica.database.MongodbConexion;
import com.logistica.modelo.CierreCuenta;
import com.logistica.modelo.MovimientoCuenta;
import com.mongodb.client.MongoCollection;

public class CierreCuentaRepository {

    private MongoCollection<Document> coleccion() {
        return MongodbConexion.getDatabase().getCollection("cierres_cuenta");
    }

    private CierreCuenta mapear(Document doc) {
        CierreCuenta c = new CierreCuenta();
        c.setId(aTexto(doc.getObjectId("_id")));
        c.setCuentaId(aInteger(doc.get("cuentaId")));
        c.setParteId(aInteger(doc.get("parteId")));
        c.setPeriodo(doc.getString("periodo"));
        c.setSaldoInicial(aBigDecimal(doc.get("saldoInicial")));
        c.setTotalDebitos(aBigDecimal(doc.get("totalDebitos")));
        c.setTotalCreditos(aBigDecimal(doc.get("totalCreditos")));
        c.setSaldoFinal(aBigDecimal(doc.get("saldoFinal")));

        List<MovimientoCuenta> movimientos = new ArrayList<>();
        for (Document m : doc.getList("movimientos", Document.class, new ArrayList<>())) {
            MovimientoCuenta mov = new MovimientoCuenta();
            mov.setId(aInteger(m.get("movimientoId")));
            mov.setCuentaId(c.getCuentaId());
            mov.setFecha(aLocalDateTime(m.getDate("fecha")));
            mov.setTipo(m.getString("tipo"));
            mov.setImporte(aBigDecimal(m.get("importe")));
            mov.setConcepto(m.getString("concepto"));
            mov.setFacturaId(aInteger(m.get("facturaId")));
            mov.setPagoId(m.getString("pagoId"));
            movimientos.add(mov);
        }
        c.setMovimientos(movimientos);
        c.setFechaCierre(aLocalDateTime(doc.getDate("fechaCierre")));
        return c;
    }

    public String guardar(CierreCuenta c) {
        List<Document> movimientos = new ArrayList<>();
        for (MovimientoCuenta m : c.getMovimientos()) {
            movimientos.add(new Document("movimientoId", m.getId())
                    .append("fecha", aDate(m.getFecha()))
                    .append("tipo", m.getTipo())
                    .append("importe", aDecimal(m.getImporte()))
                    .append("concepto", m.getConcepto())
                    .append("facturaId", m.getFacturaId())
                    .append("pagoId", m.getPagoId()));
        }

        Document doc = new Document()
                .append("cuentaId", c.getCuentaId())
                .append("parteId", c.getParteId())
                .append("periodo", c.getPeriodo())
                .append("saldoInicial", aDecimal(c.getSaldoInicial()))
                .append("totalDebitos", aDecimal(c.getTotalDebitos()))
                .append("totalCreditos", aDecimal(c.getTotalCreditos()))
                .append("saldoFinal", aDecimal(c.getSaldoFinal()))
                .append("movimientos", movimientos)
                .append("fechaCierre", aDate(c.getFechaCierre()));

        coleccion().insertOne(doc);
        return aTexto(doc.getObjectId("_id"));
    }

    // indice { cuentaid: 1, periodo: 1 }
    public boolean existe(int cuentaId, String periodo) {
        return coleccion().countDocuments(and(eq("cuentaId", cuentaId), eq("periodo", periodo))) > 0;
    }

    public CierreCuenta buscar(int cuentaId, String periodo) {
        Document doc = coleccion().find(and(eq("cuentaId", cuentaId), eq("periodo", periodo))).first();
        return (doc == null) ? null : mapear(doc);
    }

    public List<CierreCuenta> listarPorCuenta(int cuentaId) {
        List<CierreCuenta> cierres = new ArrayList<>();
        for (Document doc : coleccion().find(eq("cuentaId", cuentaId)).sort(ascending("periodo"))) {
            cierres.add(mapear(doc));
        }
        return cierres;
    }

    public List<CierreCuenta> listarPorPeriodo(String periodo) {
        List<CierreCuenta> cierres = new ArrayList<>();
        for (Document doc : coleccion().find(eq("periodo", periodo)).sort(ascending("cuentaId"))) {
            cierres.add(mapear(doc));
        }
        return cierres;
    }

}
