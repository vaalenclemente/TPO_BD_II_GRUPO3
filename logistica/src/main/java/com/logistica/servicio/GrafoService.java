package com.logistica.servicio;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.logistica.modelo.ParadaRuta;
import com.logistica.modelo.Parte;
import com.logistica.modelo.RelacionRed;
import com.logistica.repositorio.ContenedorRepository;
import com.logistica.repositorio.EnvioRepository;
import com.logistica.repositorio.GrafoConsultaRepository;
import com.logistica.repositorio.ParteRepository;

public class GrafoService {

    private final GrafoConsultaRepository repo = new GrafoConsultaRepository();
    private final EnvioRepository envioRepo = new EnvioRepository();
    private final ContenedorRepository contenedorRepo = new ContenedorRepository();
    private final ParteRepository parteRepo = new ParteRepository();

    public List<ParadaRuta> rutaEnvio(int envioId) throws SQLException {
        if (envioRepo.buscarPorId(envioId) == null) {
            throw new IllegalArgumentException("No existe un envío con id " + envioId + ".");
        }
        return ruta(envioId);
    }

    public List<ParadaRuta> recorridoContenedor(int contenedorId) throws SQLException {
        if (contenedorRepo.buscarPorId(contenedorId) == null) {
            throw new IllegalArgumentException("No existe un contenedor con id " + contenedorId + ".");
        }

        List<ParadaRuta> recorrido = new ArrayList<>();
        for (Integer envioId : repo.listarEnviosDeContenedor(contenedorId)) {
            recorrido.addAll(ruta(envioId));
        }
        return recorrido;
    }

    public List<RelacionRed> redRelaciones(Integer clienteId, Integer operadorId) throws SQLException {
        validarParte(clienteId, "CLIENTE");
        validarParte(operadorId, "OPERADOR");
        return repo.redRelaciones(clienteId, operadorId);
    }

    private List<ParadaRuta> ruta(int envioId) {
        ParadaRuta origen = repo.buscarOrigen(envioId);
        if (origen == null) {
            throw new IllegalStateException("El envío " + envioId + " no está en Neo4j.");
        }

        List<ParadaRuta> ruta = new ArrayList<>();
        ruta.add(origen);
        ruta.addAll(repo.listarTramos(envioId));
        return ruta;
    }

    private void validarParte(Integer parteId, String tipo) throws SQLException {
        if (parteId == null) {
            return;
        }
        Parte p = parteRepo.buscarPorId(parteId);
        if (p == null || !tipo.equals(p.getTipo())) {
            throw new IllegalArgumentException("La parte " + parteId + " no es un " + tipo + ".");
        }
    }

}
