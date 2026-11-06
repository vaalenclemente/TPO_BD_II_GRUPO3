package com.logistica.repositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

// Conversiones entre los tipos de Java que usan los modelos y los que guarda Mongo.
//   LocalDateTime / LocalDate  <->  Date (ISODate)
//   BigDecimal (importes)      <->  Decimal128 (decimal exacto, como DECIMAL de MySQL)
//   String (id del modelo)     <->  ObjectId (_id)
class MongoConversion {

    private static final ZoneId ZONA = ZoneId.systemDefault();

    static Date aDate(LocalDateTime fecha) {
        return (fecha == null) ? null : Date.from(fecha.atZone(ZONA).toInstant());
    }

    static Date aDate(LocalDate fecha) {
        return (fecha == null) ? null : aDate(fecha.atStartOfDay());
    }

    static LocalDateTime aLocalDateTime(Date fecha) {
        return (fecha == null) ? null : LocalDateTime.ofInstant(fecha.toInstant(), ZONA);
    }

    static LocalDate aLocalDate(Date fecha) {
        return (fecha == null) ? null : aLocalDateTime(fecha).toLocalDate();
    }

    static Decimal128 aDecimal(BigDecimal importe) {
        return (importe == null) ? null : new Decimal128(importe);
    }

    static BigDecimal aBigDecimal(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Decimal128 d) {
            return d.bigDecimalValue();
        }
        return new BigDecimal(valor.toString());
    }

    // Lee un numero aunque se haya guardado como int o double.
    static Double aDouble(Object valor) {
        return (valor == null) ? null : ((Number) valor).doubleValue();
    }

    static Integer aInteger(Object valor) {
        return (valor == null) ? null : ((Number) valor).intValue();
    }

    static String aTexto(ObjectId id) {
        return (id == null) ? null : id.toHexString();
    }

    // Un id mal escrito no es un error de base: simplemente no existe.
    static ObjectId aObjectId(String id) {
        if (id == null || !ObjectId.isValid(id)) {
            return null;
        }
        return new ObjectId(id);
    }

    static Document subdocumento(Document doc, String campo) {
        Document sub = doc.get(campo, Document.class);
        return (sub == null) ? new Document() : sub;
    }

}
