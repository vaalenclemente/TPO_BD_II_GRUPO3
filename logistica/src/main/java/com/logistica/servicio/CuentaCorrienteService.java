package com.logistica.servicio;

import java.sql.SQLException;
import java.util.List;

import com.logistica.modelo.CuentaCorriente;
import com.logistica.modelo.MovimientoCuenta;
import com.logistica.modelo.Parte;
import com.logistica.repositorio.CuentaCorrienteRepository;
import com.logistica.repositorio.ParteRepository;

// saldo positivo = la parte nos debe; negativo = le debemos.
public class CuentaCorrienteService {

    private final CuentaCorrienteRepository repo = new CuentaCorrienteRepository();
    private final ParteRepository parteRepo = new ParteRepository();

    public CuentaCorriente obtenerCuenta(int parteId) throws SQLException {

        CuentaCorriente cuenta = repo.buscarPorParte(parteId);
        if (cuenta != null) {
            return cuenta;
        }

        Parte p = parteRepo.buscarPorId(parteId);
        if (p == null) {
            throw new IllegalArgumentException("No existe una parte con id " + parteId + ".");
        }

        int id = repo.crear(parteId);
        return repo.buscarPorId(id);
    }

    public List<MovimientoCuenta> listarMovimientos(int parteId) throws SQLException {
        return repo.listarMovimientos(obtenerCuenta(parteId).getId());
    }

    public List<CuentaCorriente> listar() throws SQLException {
        return repo.listar();
    }

}
