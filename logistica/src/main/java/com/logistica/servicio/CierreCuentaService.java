package com.logistica.servicio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.logistica.modelo.CierreCuenta;
import com.logistica.modelo.CuentaCorriente;
import com.logistica.modelo.MovimientoCuenta;
import com.logistica.repositorio.CierreCuentaRepository;
import com.logistica.repositorio.CuentaCorrienteRepository;

public class CierreCuentaService {

    private final CierreCuentaRepository repo = new CierreCuentaRepository();
    private final CuentaCorrienteRepository cuentaRepo = new CuentaCorrienteRepository();

    public List<CierreCuenta> cerrarMes(String periodo) throws SQLException {

        YearMonth mes = validarPeriodo(periodo);
        if (mes.isAfter(YearMonth.now())) {
            throw new IllegalArgumentException("No se puede cerrar un mes futuro.");
        }
        LocalDateTime desde = mes.atDay(1).atStartOfDay();
        LocalDateTime hasta = mes.plusMonths(1).atDay(1).atStartOfDay();

        List<CierreCuenta> cierres = new ArrayList<>();

        for (CuentaCorriente cuenta : cuentaRepo.listar()) {

            if (repo.existe(cuenta.getId(), mes.toString())) {
                continue;
            }

            BigDecimal saldoInicial = cuentaRepo.saldoAntesDe(cuenta.getId(), desde);
            List<MovimientoCuenta> movimientos = cuentaRepo.listarMovimientosEntre(cuenta.getId(), desde, hasta);

            if (movimientos.isEmpty() && saldoInicial.signum() == 0) {
                continue;
            }

            BigDecimal debitos = BigDecimal.ZERO;
            BigDecimal creditos = BigDecimal.ZERO;
            for (MovimientoCuenta m : movimientos) {
                if ("DEBITO".equals(m.getTipo())) {
                    debitos = debitos.add(m.getImporte());
                } else {
                    creditos = creditos.add(m.getImporte());
                }
            }

            CierreCuenta c = new CierreCuenta();
            c.setCuentaId(cuenta.getId());
            c.setParteId(cuenta.getParteId());
            c.setPeriodo(mes.toString());
            c.setSaldoInicial(saldoInicial);
            c.setTotalDebitos(debitos);
            c.setTotalCreditos(creditos);
            c.setSaldoFinal(saldoInicial.add(debitos).subtract(creditos));
            c.setMovimientos(movimientos);
            c.setFechaCierre(LocalDateTime.now());

            c.setId(repo.guardar(c));
            cierres.add(c);
        }

        return cierres;
    }

    public List<CierreCuenta> listarPorParte(int parteId) throws SQLException {
        CuentaCorriente cuenta = cuentaRepo.buscarPorParte(parteId);
        if (cuenta == null) {
            return new ArrayList<>();
        }
        return repo.listarPorCuenta(cuenta.getId());
    }

    public List<CierreCuenta> listarPorPeriodo(String periodo) {
        return repo.listarPorPeriodo(validarPeriodo(periodo).toString());
    }

    private YearMonth validarPeriodo(String periodo) {
        try {
            return YearMonth.parse(periodo);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new IllegalArgumentException("El período tiene que tener el formato AAAA-MM (ej. 2026-10).");
        }
    }

}
