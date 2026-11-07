package com.logistica.cli;

import com.logistica.modelo.Parte;
import com.logistica.servicio.ParteService;

public class MenuPartes {

    private final ParteService partes = new ParteService();

    public void mostrar() {
        String tipo = Consola.elegir("¿Que partes?", "CLIENTE", "PROVEEDOR", "OPERADOR");
        while (true) {
            int opcion = Consola.menu("ABM " + tipo, "Listar", "Alta", "Modificar", "Dar de baja", "Reactivar");
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> Vista.listar(partes.listarPorTipo(tipo)));
                case 2 -> Consola.ejecutar(() -> alta(tipo));
                case 3 -> Consola.ejecutar(() -> modificar(tipo));
                case 4 -> Consola.ejecutar(() -> {
                    partes.darDeBaja(Selector.parte(tipo));
                    Consola.mostrar("Dado de baja.");
                });
                case 5 -> Consola.ejecutar(() -> {
                    partes.reactivar(Selector.parte(tipo));
                    Consola.mostrar("Reactivado.");
                });
            }
        }
    }

    private void alta(String tipo) throws Exception {
        String razonSocial = Consola.texto("Razon social");
        String cuit = Consola.texto("CUIT");
        String email = Consola.texto("Email");
        String telefono = Consola.textoOpcional("Telefono");
        String direccion = Consola.textoOpcional("Direccion");
        Consola.mostrar("Pais:");
        int paisId = Selector.pais();
        String rubro = "PROVEEDOR".equals(tipo) ? Consola.textoOpcional("Rubro") : null;

        int id = partes.alta(new Parte(tipo, razonSocial, cuit, email, telefono, direccion, paisId, rubro));
        Consola.mostrar("Alta con id " + id + ".");
    }

    private void modificar(String tipo) throws Exception {
        Parte p = partes.buscarPorId(Selector.parte(tipo));
        if (p == null) {
            throw new IllegalArgumentException("No existe esa parte.");
        }
        p.setRazonSocial(Consola.texto("Razon social", p.getRazonSocial()));
        p.setEmail(Consola.texto("Email", p.getEmail()));
        p.setTelefono(Consola.texto("Telefono", p.getTelefono()));
        p.setDireccion(Consola.texto("Direccion", p.getDireccion()));
        if (Consola.confirmar("¿Cambiar el pais?")) {
            p.setPaisId(Selector.pais());
        }
        if ("PROVEEDOR".equals(tipo)) {
            p.setRubro(Consola.texto("Rubro", p.getRubro()));
        }
        partes.modificar(p);
        Consola.mostrar("Modificado.");
    }

}
