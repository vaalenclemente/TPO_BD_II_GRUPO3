package com.logistica.servicio;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.logistica.modelo.Parte;
import com.logistica.modelo.Sesion;
import com.logistica.modelo.Usuario;
import com.logistica.repositorio.ParteRepository;
import com.logistica.repositorio.SesionRepository;
import com.logistica.repositorio.UsuarioRepository;

public class SesionService {

    private static final int MINUTOS_EXPIRACION = 30;

    private final SesionRepository repo = new SesionRepository();
    private final UsuarioRepository usuarioRepo = new UsuarioRepository();
    private final ParteRepository parteRepo = new ParteRepository();
    private final UsuarioService usuarioService = new UsuarioService();

    // ---------- Login ----------

    public Sesion iniciarSesion(String email, String password) throws SQLException {

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("El email y la contraseña son obligatorios.");
        }

        Usuario u = usuarioRepo.buscarPorEmail(email);

        if (u == null || !u.getPasswordHash().equals(usuarioService.hashear(password))) {
            throw new IllegalArgumentException("Email o contraseña incorrectos.");
        }

        if (!"ACTIVO".equals(u.getEstado())) {
            throw new IllegalArgumentException("El usuario está dado de baja.");
        }

        if ("EXTERNO".equals(u.getTipoUsuario())) {
            Parte p = parteRepo.buscarPorId(u.getParteId());
            if (p == null || !"ACTIVO".equals(p.getEstado())) {
                throw new IllegalArgumentException("La empresa del usuario está dada de baja.");
            }
        }

        LocalDateTime ahora = LocalDateTime.now();

        Sesion s = new Sesion(u.getId(), UUID.randomUUID().toString());
        s.setFechaInicio(ahora);
        s.setFechaUltimaActividad(ahora);
        s.setEstado("ACTIVA");

        int id = repo.guardar(s);
        s.setId(id);
        return s;
    }

    // ---------- Uso de la sesión ----------

    public void validar(String token) throws SQLException {

        Sesion s = repo.buscarPorToken(token);

        if (s == null || !"ACTIVA".equals(s.getEstado())) {
            throw new IllegalArgumentException("La sesión no está activa. Vuelva a iniciar sesión.");
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (s.getFechaUltimaActividad().isBefore(ahora.minusMinutes(MINUTOS_EXPIRACION))) {
            repo.finalizar(s.getId(), "EXPIRADA", ahora);
            throw new IllegalArgumentException("La sesión expiró por inactividad. Vuelva a iniciar sesión.");
        }

        repo.actualizarActividad(s.getId(), ahora);
    }

    // ---------- Logout ----------

    public void cerrarSesion(String token) throws SQLException {

        Sesion s = repo.buscarPorToken(token);

        if (s != null && "ACTIVA".equals(s.getEstado())) {
            repo.finalizar(s.getId(), "CERRADA", LocalDateTime.now());
        }
    }

    public List<Sesion> listarActivas() throws SQLException {
        expirarInactivas();
        return repo.listarActivas();
    }

    public void forzarCierre(int id) throws SQLException {

        Sesion s = repo.buscarPorId(id);

        if (s == null) {
            throw new IllegalArgumentException("No existe una sesión con id " + id + ".");
        }
        if (!"ACTIVA".equals(s.getEstado())) {
            throw new IllegalArgumentException("La sesión ya no está activa.");
        }

        repo.finalizar(id, "CERRADA", LocalDateTime.now());
    }

    public int expirarInactivas() throws SQLException {
        LocalDateTime ahora = LocalDateTime.now();
        return repo.expirarInactivas(ahora.minusMinutes(MINUTOS_EXPIRACION), ahora);
    }

}
