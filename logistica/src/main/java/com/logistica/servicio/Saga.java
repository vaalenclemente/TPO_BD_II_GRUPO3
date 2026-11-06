package com.logistica.servicio;

import java.util.ArrayDeque;
import java.util.Deque;

import com.logistica.repositorio.SagaRepository;

public class Saga {

    public interface Accion {
        void ejecutar() throws Exception;
    }

    private final SagaRepository repo = new SagaRepository();
    private final AuditoriaService auditoria = new AuditoriaService();
    private final String id;
    private final String tipo;
    private final String entidad;
    private Object objetoId;
    private final Deque<Accion> compensaciones = new ArrayDeque<>();

    // tipo = qué operación es (CREAR_ENVIO); entidad = sobre qué (envio), para la auditoría.
    public Saga(String tipo, String entidad) {
        this.tipo = tipo;
        this.entidad = entidad;
        this.id = repo.iniciar(tipo);
    }

    public void objeto(Object objetoId) {
        this.objetoId = objetoId;
        repo.asignarObjeto(id, objetoId);
    }

    public void paso(String nombre, Accion compensacion) {
        if (compensacion != null) {
            compensaciones.push(compensacion);
        }
        repo.registrarPaso(id, nombre);
    }

    public void completar() {
        repo.completar(id);
        auditar("OK");
    }

    public IllegalStateException fallar(Exception causa) {
        String mensaje = causa.getMessage();

        try {
            while (!compensaciones.isEmpty()) {
                compensaciones.pop().ejecutar();
            }
            repo.compensada(id, mensaje);
            auditar("COMPENSADA");
        } catch (Exception errorAlDeshacer) {
            try {
                repo.error(id, mensaje + " | al compensar: " + errorAlDeshacer.getMessage());
            } catch (Exception ignorada) {

            }
            auditar("ERROR");
        }

        return new IllegalStateException("No se pudo completar la operación: " + mensaje, causa);
    }

    // Toda saga queda en la auditoría con su resultado. Si la auditoría falla, la operación
    // ya está hecha (o deshecha): no se la hace fallar por eso.
    private void auditar(String resultado) {
        try {
            auditoria.registrarCambio(tipo, entidad, objetoId, null, null, null, resultado, id);
        } catch (Exception ignorada) {

        }
    }

}
