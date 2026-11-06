package com.logistica.servicio;

import java.sql.SQLException;
import java.util.List;

import com.logistica.modelo.Ciudad;
import com.logistica.modelo.Pais;
import com.logistica.repositorio.CiudadRepository;
import com.logistica.repositorio.PaisRepository;

public class UbicacionService {

    private final PaisRepository paisRepo = new PaisRepository();
    private final CiudadRepository ciudadRepo = new CiudadRepository();

    public List<Pais> listarPaises() throws SQLException {
        return paisRepo.listar();
    }

    public int altaPais(String nombre) throws SQLException {
        String limpio = validarNombre(nombre, "del pais");
        if (paisRepo.buscarPorNombre(limpio) != null) {
            throw new IllegalArgumentException("Ya existe el pais " + limpio + ".");
        }
        return paisRepo.guardar(new Pais(null, limpio));
    }

    public void modificarPais(int id, String nombre) throws SQLException {
        buscarPais(id);
        String limpio = validarNombre(nombre, "del pais");
        Pais otro = paisRepo.buscarPorNombre(limpio);
        if (otro != null && otro.getId() != id) {
            throw new IllegalArgumentException("Ya existe el pais " + limpio + ".");
        }
        paisRepo.actualizar(new Pais(id, limpio));
    }

    public void eliminarPais(int id) throws SQLException {
        buscarPais(id);
        if (paisRepo.contarUsos(id) > 0) {
            throw new IllegalArgumentException("El pais tiene ciudades o partes: no se puede eliminar.");
        }
        paisRepo.borrar(id);
    }

    public List<Ciudad> listarCiudades(int paisId) throws SQLException {
        return ciudadRepo.listarPorPais(paisId);
    }

    public int altaCiudad(String nombre, int paisId) throws SQLException {
        buscarPais(paisId);
        String limpio = validarNombre(nombre, "de la ciudad");
        if (ciudadRepo.buscarPorNombre(limpio, paisId) != null) {
            throw new IllegalArgumentException("Ya existe la ciudad " + limpio + " en ese pais.");
        }
        return ciudadRepo.guardar(new Ciudad(null, limpio, paisId));
    }

    public void modificarCiudad(int id, String nombre) throws SQLException {
        Ciudad c = buscarCiudad(id);
        String limpio = validarNombre(nombre, "de la ciudad");
        Ciudad otra = ciudadRepo.buscarPorNombre(limpio, c.getPaisId());
        if (otra != null && otra.getId() != id) {
            throw new IllegalArgumentException("Ya existe la ciudad " + limpio + " en ese pais.");
        }
        c.setNombre(limpio);
        ciudadRepo.actualizar(c);
    }

    public void eliminarCiudad(int id) throws SQLException {
        buscarCiudad(id);
        if (ciudadRepo.contarUsos(id) > 0) {
            throw new IllegalArgumentException("La ciudad esta usada en envios o tramos: no se puede eliminar.");
        }
        ciudadRepo.borrar(id);
    }

    private String validarNombre(String nombre, String de) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre " + de + " es obligatorio.");
        }
        return nombre.trim();
    }

    private Pais buscarPais(int id) throws SQLException {
        Pais p = paisRepo.buscarPorId(id);
        if (p == null) {
            throw new IllegalArgumentException("No existe un pais con id " + id + ".");
        }
        return p;
    }

    private Ciudad buscarCiudad(int id) throws SQLException {
        Ciudad c = ciudadRepo.buscarPorId(id);
        if (c == null) {
            throw new IllegalArgumentException("No existe una ciudad con id " + id + ".");
        }
        return c;
    }

}
