package com.logistica.cli;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Consola {

    private static final Scanner ENTRADA = new Scanner(System.in);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public interface Accion {
        void ejecutar() throws Exception;
    }

    public static int menu(String titulo, String... opciones) {
        System.out.println();
        System.out.println("=== " + titulo + " ===");
        for (int i = 0; i < opciones.length; i++) {
            System.out.println((i + 1) + ". " + opciones[i]);
        }
        System.out.println("0. Volver");
        while (true) {
            Integer opcion = enteroOpcional("Opción");
            if (opcion != null && opcion >= 0 && opcion <= opciones.length) {
                return opcion;
            }
            System.out.println("Opción inválida.");
        }
    }

    public static void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("No se pudo: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error de MySQL: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
        }
    }

    public static String leer(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return ENTRADA.nextLine().trim();
    }

    public static String texto(String etiqueta) {
        while (true) {
            String valor = leer(etiqueta);
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("Es obligatorio.");
        }
    }

    public static String textoOpcional(String etiqueta) {
        String valor = leer(etiqueta + " (Enter para omitir)");
        return valor.isEmpty() ? null : valor;
    }

    public static String texto(String etiqueta, String actual) {
        String valor = leer(etiqueta + " [" + actual + "]");
        return valor.isEmpty() ? actual : valor;
    }

    public static int entero(String etiqueta) {
        while (true) {
            Integer valor = enteroOpcional(etiqueta);
            if (valor != null) {
                return valor;
            }
            System.out.println("Ingrese un número entero.");
        }
    }

    public static Integer enteroOpcional(String etiqueta) {
        String valor = leer(etiqueta);
        if (valor.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static List<Integer> enteros(String etiqueta) {
        while (true) {
            String valor = leer(etiqueta + " (separados por coma)");
            List<Integer> numeros = new ArrayList<>();
            try {
                for (String parte : valor.split(",")) {
                    if (!parte.isBlank()) {
                        numeros.add(Integer.valueOf(parte.trim()));
                    }
                }
                return numeros;
            } catch (NumberFormatException e) {
                System.out.println("Ingrese números separados por coma.");
            }
        }
    }

    public static List<String> textos(String etiqueta) {
        String valor = leer(etiqueta + " (separados por coma, Enter para ninguno)");
        List<String> lista = new ArrayList<>();
        for (String parte : valor.split(",")) {
            if (!parte.isBlank()) {
                lista.add(parte.trim());
            }
        }
        return lista;
    }

    public static BigDecimal decimal(String etiqueta) {
        while (true) {
            BigDecimal valor = decimalOpcional(etiqueta);
            if (valor != null) {
                return valor;
            }
            System.out.println("Ingrese un número (con punto decimal).");
        }
    }

    public static BigDecimal decimalOpcional(String etiqueta) {
        String valor = leer(etiqueta);
        if (valor.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(valor.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static BigDecimal decimal(String etiqueta, BigDecimal actual) {
        BigDecimal valor = decimalOpcional(etiqueta + " [" + actual + "]");
        return (valor == null) ? actual : valor;
    }

    public static Double medicion(String etiqueta) {
        BigDecimal valor = decimalOpcional(etiqueta + " (Enter si no la mide)");
        return (valor == null) ? null : valor.doubleValue();
    }

    public static LocalDate fecha(String etiqueta) {
        while (true) {
            LocalDate valor = fechaOpcional(etiqueta);
            if (valor != null) {
                return valor;
            }
            System.out.println("Ingrese una fecha dd/MM/aaaa.");
        }
    }

    public static LocalDate fechaOpcional(String etiqueta) {
        String valor = leer(etiqueta + " (dd/MM/aaaa)");
        if (valor.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(valor, FECHA);
        } catch (DateTimeParseException e) {
            System.out.println("Fecha inválida, se omite.");
            return null;
        }
    }

    public static LocalDateTime fechaHora(String etiqueta) {
        while (true) {
            String valor = leer(etiqueta + " (dd/MM/aaaa HH:mm)");
            try {
                return LocalDateTime.parse(valor, FECHA_HORA);
            } catch (DateTimeParseException e) {
                System.out.println("Ingrese fecha y hora dd/MM/aaaa HH:mm.");
            }
        }
    }

    public static LocalDateTime fechaHoraOAhora(String etiqueta) {
        String valor = leer(etiqueta + " (dd/MM/aaaa HH:mm, Enter = ahora)");
        if (valor.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(valor, FECHA_HORA);
        } catch (DateTimeParseException e) {
            System.out.println("Fecha inválida, se usa ahora.");
            return LocalDateTime.now();
        }
    }

    public static LocalDateTime inicioDelDia(LocalDate fecha) {
        return (fecha == null) ? null : fecha.atStartOfDay();
    }

    public static LocalDateTime finDelDia(LocalDate fecha) {
        return (fecha == null) ? null : fecha.plusDays(1).atStartOfDay();
    }

    public static String elegir(String etiqueta, String... opciones) {
        System.out.println(etiqueta + ":");
        for (int i = 0; i < opciones.length; i++) {
            System.out.println("  " + (i + 1) + ". " + opciones[i]);
        }
        while (true) {
            Integer opcion = enteroOpcional("Opción");
            if (opcion != null && opcion >= 1 && opcion <= opciones.length) {
                return opciones[opcion - 1];
            }
            System.out.println("Opción inválida.");
        }
    }

    public static boolean confirmar(String pregunta) {
        return leer(pregunta + " (s/n)").equalsIgnoreCase("s");
    }

    public static void listar(List<?> lista) {
        if (lista == null || lista.isEmpty()) {
            System.out.println("(sin resultados)");
            return;
        }
        for (Object o : lista) {
            System.out.println(o);
        }
    }

    public static void mostrar(String texto) {
        System.out.println(texto);
    }

}
