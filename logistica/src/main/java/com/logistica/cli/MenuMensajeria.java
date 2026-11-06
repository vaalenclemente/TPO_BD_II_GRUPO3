package com.logistica.cli;

import com.logistica.modelo.Grupo;
import com.logistica.servicio.GrupoService;
import com.logistica.servicio.MensajeService;
import com.logistica.servicio.UsuarioActual;
import com.logistica.servicio.UsuarioService;

public class MenuMensajeria {

    private final MensajeService mensajes = new MensajeService();
    private final GrupoService grupos = new GrupoService();
    private final UsuarioService usuarios = new UsuarioService();

    public void mostrar() {
        while (true) {
            int opcion = Consola.menu("Mensajeria",
                    "Ver la conversacion con una parte", "Escribir a una parte", "Mis grupos", "Todos los grupos",
                    "Crear grupo", "Ver la conversacion de un grupo", "Escribir en un grupo",
                    "Agregar integrante a un grupo", "Quitar integrante de un grupo", "Dar de baja un grupo");
            int yo = UsuarioActual.get();
            switch (opcion) {
                case 0 -> {
                    return;
                }
                case 1 -> Consola.ejecutar(() -> Consola.listar(mensajes.conversacionConParte(Selector.cualquierParte(), yo)));
                case 2 -> Consola.ejecutar(() -> {
                    int parteId = Selector.cualquierParte();
                    mensajes.enviarAParte(parteId, yo, Consola.texto("Mensaje"));
                    Consola.mostrar("Enviado.");
                });
                case 3 -> Consola.ejecutar(() -> Consola.listar(grupos.listarPorUsuario(yo)));
                case 4 -> Consola.ejecutar(() -> Consola.listar(grupos.listar()));
                case 5 -> Consola.ejecutar(() -> {
                    String nombre = Consola.texto("Nombre del grupo");
                    String descripcion = Consola.textoOpcional("Descripcion");
                    Consola.listar(usuarios.listar());
                    var integrantes = Consola.enteros("Ids de los integrantes (usted se agrega solo)");
                    String id = grupos.crear(new Grupo(nombre, descripcion, integrantes, yo));
                    Consola.mostrar("Grupo creado con id " + id + ".");
                });
                case 6 -> Consola.ejecutar(() -> Consola.listar(mensajes.conversacionDeGrupo(elegirGrupo(yo), yo)));
                case 7 -> Consola.ejecutar(() -> {
                    String grupoId = elegirGrupo(yo);
                    mensajes.enviarAGrupo(grupoId, yo, Consola.texto("Mensaje"));
                    Consola.mostrar("Enviado.");
                });
                case 8 -> Consola.ejecutar(() -> {
                    String grupoId = elegirGrupo(yo);
                    grupos.agregarIntegrante(grupoId, Selector.usuario());
                    Consola.mostrar("Integrante agregado.");
                });
                case 9 -> Consola.ejecutar(() -> {
                    String grupoId = elegirGrupo(yo);
                    grupos.quitarIntegrante(grupoId, Selector.usuario());
                    Consola.mostrar("Integrante quitado.");
                });
                case 10 -> Consola.ejecutar(() -> {
                    grupos.darDeBaja(elegirGrupo(yo));
                    Consola.mostrar("Grupo dado de baja.");
                });
            }
        }
    }

    private String elegirGrupo(int usuarioId) {
        Consola.listar(grupos.listarPorUsuario(usuarioId));
        return Consola.texto("Id del grupo");
    }

}
