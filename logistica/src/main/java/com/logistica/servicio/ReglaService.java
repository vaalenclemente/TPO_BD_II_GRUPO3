package com.logistica.servicio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.logistica.modelo.Envio;
import com.logistica.modelo.Regla;
import com.logistica.repositorio.EnvioRepository;
import com.logistica.repositorio.ReglaRepository;

public class ReglaService {

    private final ReglaRepository repo = new ReglaRepository();
    private final EnvioRepository envioRepo = new EnvioRepository();

    // ---------- Alta ----------

    public int alta(Regla r) throws SQLException {

        // a) validaciones
        validarDatos(r);

        if (r.getEnvioId() != null) {
            Envio e = envioRepo.buscarPorId(r.getEnvioId());
            if (e == null) {
                throw new IllegalArgumentException("No existe un envío con id " + r.getEnvioId() + ".");
            }
            if ("ENTREGADO".equals(e.getEstado()) || "CANCELADO".equals(e.getEstado())) {
                throw new IllegalArgumentException("El envío ya está " + e.getEstado() + ".");
            }
        }

        // b) valores del sistema
        r.setActiva(true);

        // c) guardar
        int id = repo.guardar(r);
        r.setId(id);
        return id;
    }

    // ---------- Modificación ----------

    public void modificar(Regla r) throws SQLException {
        buscarExistente(r.getId());
        validarDatos(r);
        repo.actualizar(r);
    }


    public void desactivar(int id) throws SQLException {
        buscarExistente(id);
        repo.cambiarActiva(id, false);
    }

    public void activar(int id) throws SQLException {
        buscarExistente(id);
        repo.cambiarActiva(id, true);
    }


    public List<Regla> listar() throws SQLException {
        return repo.listar();
    }

    public List<Regla> listarAplicables(int envioId) throws SQLException {
        return repo.listarAplicables(envioId);
    }

    public List<Regla> listarGeneralesActivas(String metrica) throws SQLException {
        return repo.listarGeneralesActivas(metrica);
    }

    public Regla buscarPorId(int id) throws SQLException {
        return repo.buscarPorId(id);
    }

    public boolean seIncumple(Regla r, BigDecimal medicion) {
        if (medicion == null) {
            return false;
        }
        int comparacion = medicion.compareTo(r.getValor());
        if ("MAYOR".equals(r.getOperador())) {
            return comparacion > 0;
        }
        return comparacion < 0;
    }

    private void validarDatos(Regla r) {

        if (r.getNombre() == null || r.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la regla es obligatorio.");
        }

        String m = r.getMetrica();
        if (!"TEMPERATURA".equals(m) && !"HUMEDAD".equals(m) && !"VIBRACION".equals(m)
                && !"BATERIA".equals(m) && !"INACTIVIDAD".equals(m)) {
            throw new IllegalArgumentException(
                    "La métrica no es correcta (TEMPERATURA, HUMEDAD, VIBRACION, BATERIA, INACTIVIDAD).");
        }

        if (!"MAYOR".equals(r.getOperador()) && !"MENOR".equals(r.getOperador())) {
            throw new IllegalArgumentException("El operador no es correcto (MAYOR, MENOR).");
        }

        if (r.getValor() == null) {
            throw new IllegalArgumentException("El valor límite es obligatorio.");
        }

        // Batería y humedad son porcentajes; inactividad son minutos.
        if (("BATERIA".equals(m) || "HUMEDAD".equals(m))
                && (r.getValor().compareTo(BigDecimal.ZERO) < 0
                        || r.getValor().compareTo(new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException("Para " + m + " el valor tiene que estar entre 0 y 100.");
        }
        if ("INACTIVIDAD".equals(m)) {
            if (r.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Los minutos de inactividad tienen que ser mayores a 0.");
            }
            if (!"MAYOR".equals(r.getOperador())) {
                throw new IllegalArgumentException("Una regla de inactividad es siempre MAYOR (más de X minutos).");
            }
        }

        if (!"ALTA".equals(r.getSeveridad()) && !"MEDIA".equals(r.getSeveridad())
                && !"BAJA".equals(r.getSeveridad())) {
            throw new IllegalArgumentException("La severidad no es correcta (ALTA, MEDIA, BAJA).");
        }
    }

    private Regla buscarExistente(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Falta el id de la regla.");
        }

        Regla r = repo.buscarPorId(id);
        if (r == null) {
            throw new IllegalArgumentException("No existe una regla con id " + id + ".");
        }
        return r;
    }

}
