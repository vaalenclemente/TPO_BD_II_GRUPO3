package com.logistica.servicio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.logistica.modelo.TipoReporte;
import com.logistica.repositorio.TipoReporteRepository;

public class TipoReporteService {

    private final TipoReporteRepository repo = new TipoReporteRepository();

    public int alta(TipoReporte t) throws SQLException {

        // alidaciones
        if (t.getCodigo() == null || t.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio.");
        }
        t.setCodigo(t.getCodigo().trim().toUpperCase());

        validarDatos(t);

        if (repo.buscarPorCodigo(t.getCodigo()) != null) {
            throw new IllegalArgumentException("Ya existe un tipo de reporte con ese codigo.");
        }

        // valores del sistema
        t.setActivo(true);

        // guardar
        int id = repo.guardar(t);
        t.setId(id);
        return id;
    }

    public void modificar(TipoReporte t) throws SQLException {
        buscarExistente(t.getId());
        validarDatos(t);
        repo.actualizar(t);
    }

    public void desactivar(int id) throws SQLException {
        buscarExistente(id);
        repo.cambiarActivo(id, false);
    }

    public void activar(int id) throws SQLException {
        buscarExistente(id);
        repo.cambiarActivo(id, true);
    }

    public List<TipoReporte> listar() throws SQLException {
        return repo.listar();
    }

    public List<TipoReporte> listarActivos() throws SQLException {
        return repo.listarActivos();
    }

    public TipoReporte buscarPorId(int id) throws SQLException {
        return repo.buscarPorId(id);
    }

    private void validarDatos(TipoReporte t) {
        if (t.getNombre() == null || t.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (t.getPrecio() == null || t.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
    }

    private TipoReporte buscarExistente(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Falta el id del tipo de reporte.");
        }
        TipoReporte t = repo.buscarPorId(id);
        if (t == null) {
            throw new IllegalArgumentException("No existe un tipo de reporte con id " + id + ".");
        }
        return t;
    }

}
