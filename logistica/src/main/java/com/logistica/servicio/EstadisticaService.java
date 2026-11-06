package com.logistica.servicio;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.logistica.modelo.DesempenioMensual;
import com.logistica.modelo.EnvioDemorado;
import com.logistica.modelo.Parte;
import com.logistica.modelo.ResultadoAgrupado;
import com.logistica.repositorio.EstadisticaRepository;
import com.logistica.repositorio.ParteRepository;

public class EstadisticaService {

    private static final LocalDate PRIMER_DIA = LocalDate.of(2000, 1, 1);
    private static final LocalDate ULTIMO_DIA = LocalDate.of(2100, 1, 1);

    private final EstadisticaRepository repo = new EstadisticaRepository();
    private final ParteRepository parteRepo = new ParteRepository();

    public List<ResultadoAgrupado> enviosPorPais(Integer clienteId, LocalDate desde, LocalDate hasta)
            throws SQLException {
        validarCliente(clienteId);
        return repo.enviosPorPais(clienteId, inicio(desde, hasta), fin(hasta));
    }

    public List<ResultadoAgrupado> enviosPorEstado(Integer clienteId, LocalDate desde, LocalDate hasta)
            throws SQLException {
        validarCliente(clienteId);
        return repo.enviosPorEstado(clienteId, inicio(desde, hasta), fin(hasta));
    }

    public List<ResultadoAgrupado> enviosPorCliente(LocalDate desde, LocalDate hasta) throws SQLException {
        return repo.enviosPorCliente(inicio(desde, hasta), fin(hasta));
    }

    public List<ResultadoAgrupado> rankingClientesPorVolumen(LocalDate desde, LocalDate hasta, int limite)
            throws SQLException {
        if (limite < 1) {
            throw new IllegalArgumentException("La cantidad del ranking tiene que ser 1 o más.");
        }
        return repo.rankingClientesPorVolumen(inicio(desde, hasta), fin(hasta), limite);
    }

    public List<EnvioDemorado> enviosDemorados(Integer clienteId, LocalDate desde, LocalDate hasta)
            throws SQLException {
        validarCliente(clienteId);
        return repo.enviosDemorados(clienteId, inicio(desde, hasta), fin(hasta), LocalDateTime.now());
    }

    public List<ResultadoAgrupado> tiemposEntregaPorCliente(Integer clienteId, LocalDate desde, LocalDate hasta)
            throws SQLException {
        validarCliente(clienteId);
        return repo.tiemposEntregaPorCliente(clienteId, inicio(desde, hasta), fin(hasta));
    }

    public ResultadoAgrupado tiempoPromedioEntrega(Integer clienteId, LocalDate desde, LocalDate hasta)
            throws SQLException {
        validarCliente(clienteId);
        return repo.tiempoPromedioEntrega(clienteId, inicio(desde, hasta), fin(hasta));
    }

    public List<ResultadoAgrupado> promedioIncidentesPorPais(Integer clienteId, LocalDate desde, LocalDate hasta)
            throws SQLException {
        validarCliente(clienteId);
        return repo.promedioIncidentesPorPais(clienteId, inicio(desde, hasta), fin(hasta));
    }

    public List<ResultadoAgrupado> promedioIncidentesPorEstado(Integer clienteId, LocalDate desde, LocalDate hasta)
            throws SQLException {
        validarCliente(clienteId);
        return repo.promedioIncidentesPorEstado(clienteId, inicio(desde, hasta), fin(hasta));
    }

    public List<ResultadoAgrupado> promedioIncidentesPorCliente(LocalDate desde, LocalDate hasta)
            throws SQLException {
        return repo.promedioIncidentesPorCliente(inicio(desde, hasta), fin(hasta));
    }

    public List<ResultadoAgrupado> costosPorTransporte(Integer clienteId, LocalDate desde, LocalDate hasta)
            throws SQLException {
        validarCliente(clienteId);
        return repo.costosPorTransporte(clienteId, inicio(desde, hasta), fin(hasta));
    }

    public List<DesempenioMensual> desempenioMensual(Integer clienteId, LocalDate desde, LocalDate hasta)
            throws SQLException {
        validarCliente(clienteId);
        return repo.desempenioMensual(clienteId, inicio(desde, hasta), fin(hasta));
    }

    private LocalDateTime inicio(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha desde no puede ser posterior a la fecha hasta.");
        }
        return ((desde == null) ? PRIMER_DIA : desde).atStartOfDay();
    }

    private LocalDateTime fin(LocalDate hasta) {
        return ((hasta == null) ? ULTIMO_DIA : hasta.plusDays(1)).atStartOfDay();
    }

    private void validarCliente(Integer clienteId) throws SQLException {
        if (clienteId == null) {
            return;
        }
        Parte p = parteRepo.buscarPorId(clienteId);
        if (p == null || !"CLIENTE".equals(p.getTipo())) {
            throw new IllegalArgumentException("La parte " + clienteId + " no es un cliente.");
        }
    }

}
