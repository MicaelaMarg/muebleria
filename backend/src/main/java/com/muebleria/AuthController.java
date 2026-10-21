package com.muebleria;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Login del dueño. No hay registro: el usuario se crea solo la primera vez
 * (ver MuebleriaApplication).
 *
 *   POST /api/login           { usuario, clave }  ->  { token }
 *   POST /api/logout          cierra la sesión
 *   POST /api/cambiar-clave   { actual, nueva }   (requiere sesión)
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    // Lo que manda el front en el cuerpo del pedido
    public record DatosLogin(String usuario, String clave) {}
    public record DatosClave(String actual, String nueva) {}

    private final UsuarioRepository usuarios;
    private final BCryptPasswordEncoder encoder;
    private final Sesiones sesiones;

    public AuthController(UsuarioRepository usuarios, BCryptPasswordEncoder encoder, Sesiones sesiones) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.sesiones = sesiones;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody DatosLogin datos) throws InterruptedException {
        Usuario usuario = datos.usuario() == null ? null
                : usuarios.findByUsuario(datos.usuario().trim()).orElse(null);

        boolean claveCorrecta = usuario != null
                && datos.clave() != null
                && encoder.matches(datos.clave(), usuario.getClaveHash());

        if (!claveCorrecta) {
            Thread.sleep(1000); // demora a propósito: frena a quien prueba contraseñas al azar
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos.");
        }
        return Map.of("token", sesiones.crear(usuario.getUsuario()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        sesiones.cerrar(Sesiones.tokenDe(request));
    }

    // El atributo "usuario" lo pone el interceptor de WebConfig después de validar el token
    @PostMapping("/cambiar-clave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarClave(@RequestBody DatosClave datos, @RequestAttribute("usuario") String nombreUsuario) {
        Usuario usuario = usuarios.findByUsuario(nombreUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        if (datos.actual() == null || !encoder.matches(datos.actual(), usuario.getClaveHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta.");
        }
        if (datos.nueva() == null || datos.nueva().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña tiene que tener al menos 8 caracteres.");
        }

        usuario.setClaveHash(encoder.encode(datos.nueva()));
        usuarios.save(usuario);
    }
}
