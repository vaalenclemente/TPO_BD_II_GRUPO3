package com.logistica.servicio;

import java.time.LocalDateTime;
import java.util.List;

import com.logistica.modelo.RegistroAuditoria;
import com.logistica.repositorio.AuditoriaRepository;

public class AuditoriaService {

    private final AuditoriaRepository repo = new AuditoriaRepository();

    public void registrar(String accion, String entidad, Object entidadId, String resultado) {
        registrarCambio(accion, entidad, entidadId, null, null, null, resultado, null);
    }

    public void registrarCambio(String accion, String entidad, Object entidadId, String campo,
            Object antes, Object despues, String resultado, String sagaId) {

        RegistroAuditoria r = new RegistroAuditoria(UsuarioActual.get(), accion, entidad,
                (entidadId == null) ? null : entidadId.toString(), resultado);
        r.setFecha(LocalDateTime.now());
        r.setCampo(campo);
        r.setAntes((antes == null) ? null : antes.toString());
        r.setDespues((despues == null) ? null : despues.toString());
        r.setSagaId(sagaId);

        repo.guardar(r);
    }

    public List<RegistroAuditoria> listarPorEntidad(String entidad, Object entidadId) {
        return repo.listarPorEntidad(entidad, entidadId.toString());
    }

    public List<RegistroAuditoria> listarPorUsuario(int usuarioId) {
        return repo.listarPorUsuario(usuarioId);
    }

    public List<RegistroAuditoria> listarUltimos(int cantidad) {
        return repo.listarUltimos(cantidad);
    }

}
