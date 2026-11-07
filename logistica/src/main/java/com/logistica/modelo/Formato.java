package com.logistica.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String fecha(LocalDateTime f) {
        return (f == null) ? "-" : f.format(FECHA_HORA);
    }

    public static String fecha(LocalDate f) {
        return (f == null) ? "-" : f.format(FECHA);
    }

    public static String dinero(BigDecimal v) {
        return (v == null) ? "-" : String.format("$ %,.2f", v);
    }

    public static String kg(BigDecimal v) {
        return (v == null) ? "-" : String.format("%,.0f kg", v);
    }

}
