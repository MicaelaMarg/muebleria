package com.muebleria;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sesiones abiertas del panel, guardadas en memoria.
 *
 * Al hacer login se genera un token al azar que el front manda en cada pedido
 * (header "Authorization: Bearer <token>"). El token vence a las 8 horas.
 * Como hay un solo usuario, guardarlas en memoria alcanza: si se reinicia
 * el servidor, el dueño vuelve a iniciar sesión y listo.
 */
@Component
public class Sesiones {

    private static final Duration DURACION = Duration.ofHours(8);

    private record Sesion(String usuario, Instant vence) {}

    private final Map<String, Sesion> abiertas = new ConcurrentHashMap<>();

    public String crear(String usuario) {
        String token = UUID.randomUUID().toString();
        abiertas.put(token, new Sesion(usuario, Instant.now().plus(DURACION)));
        return token;
    }

    // Devuelve el usuario dueño del token, o null si el token no existe o venció
    public String usuarioDe(String token) {
        if (token == null) return null;

        Sesion sesion = abiertas.get(token);
        if (sesion == null) return null;

        if (sesion.vence().isBefore(Instant.now())) {
            abiertas.remove(token);
            return null;
        }
        return sesion.usuario();
    }

    public void cerrar(String token) {
        if (token != null) abiertas.remove(token);
    }

    // Saca el token del header Authorization
    public static String tokenDe(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
