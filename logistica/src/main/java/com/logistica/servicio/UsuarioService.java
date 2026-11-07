package com.logistica.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

import com.logistica.modelo.Usuario;
import com.logistica.repositorio.RolRepository;
import com.logistica.repositorio.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository repo = new UsuarioRepository();
    private final RolRepository rolRepo = new RolRepository();

    public int alta(Usuario u, String password) throws SQLException {

        // validaciones
        validarDatos(u);

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }

        if (repo.buscarPorEmail(u.getEmail()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email.");
        }

        // valores del sistema
        u.setPasswordHash(hashear(password));
        u.setEstado("ACTIVO");
        u.setFechaRegistro(LocalDateTime.now());

        // guardar
        int id = repo.guardar(u);
        u.setId(id);
        return id;
    }

    public void modificar(Usuario u) throws SQLException {

        buscarExistente(u.getId());
        validarDatos(u);

        Usuario mismoEmail = repo.buscarPorEmail(u.getEmail());
        if (mismoEmail != null && !mismoEmail.getId().equals(u.getId())) {
            throw new IllegalArgumentException("Ya existe otro usuario con ese email.");
        }

        repo.actualizar(u);
    }

    public void darDeBaja(int id) throws SQLException {
        buscarExistente(id);
        repo.cambiarEstado(id, "INACTIVO");
    }

    public void reactivar(int id) throws SQLException {
        buscarExistente(id);
        repo.cambiarEstado(id, "ACTIVO");
    }

    public List<Usuario> listar() throws SQLException {
        return repo.listar();
    }

    public Usuario buscarPorId(int id) throws SQLException {
        return repo.buscarPorId(id);
    }

    private void validarDatos(Usuario u) throws SQLException {

        if (u.getNombre() == null || u.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        if (u.getApellido() == null || u.getApellido().isBlank()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }

        if (u.getEmail() == null || u.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio.");
        }

        if (u.getRolId() == null) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }

        if (rolRepo.buscarPorId(u.getRolId()) == null) {
            throw new IllegalArgumentException("El rol elegido no existe.");
        }
    }

    private Usuario buscarExistente(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("Falta el id del usuario.");
        }

        Usuario u = repo.buscarPorId(id);
        if (u == null) {
            throw new IllegalArgumentException("No existe un usuario con id " + id + ".");
        }
        return u;
    }

    public String hashear(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no esta disponible.", e);
        }
    }

}
