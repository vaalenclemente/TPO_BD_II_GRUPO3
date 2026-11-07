package com.logistica.servicio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.logistica.modelo.Permiso;
import com.logistica.repositorio.PermisoRepository;

public class PermisoService {

    private final PermisoRepository repo = new PermisoRepository();

    public boolean puede(String codigo) throws SQLException {
        Integer usuarioId = UsuarioActual.get();
        if (usuarioId == null) {
            return false;
        }
        return repo.usuarioTiene(usuarioId, codigo);
    }

    public List<String> delUsuarioActual() throws SQLException {
        List<String> codigos = new ArrayList<>();
        Integer usuarioId = UsuarioActual.get();
        if (usuarioId == null) {
            return codigos;
        }
        for (Permiso p : repo.listarDeUsuario(usuarioId)) {
            codigos.add(p.getCodigo());
        }
        return codigos;
    }

}
