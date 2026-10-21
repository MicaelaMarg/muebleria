package com.muebleria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Punto de entrada de la aplicación.
 * Extiende SpringBootServletInitializer para poder desplegarse como WAR en Tomcat.
 */
@SpringBootApplication
public class MuebleriaApplication extends SpringBootServletInitializer {

    private static final Logger log = LoggerFactory.getLogger(MuebleriaApplication.class);

    // Lo usa Tomcat cuando despliega el WAR
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MuebleriaApplication.class);
    }

    // Lo usamos en desarrollo (mvn spring-boot:run)
    public static void main(String[] args) {
        SpringApplication.run(MuebleriaApplication.class, args);
    }

    // Encripta y compara contraseñas
    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    // La primera vez que arranca, si no hay usuarios, crea al dueño
    // con la clave inicial de application.properties.
    @Bean
    public CommandLineRunner crearDueno(UsuarioRepository usuarios,
                                        BCryptPasswordEncoder encoder,
                                        @Value("${app.dueno.usuario}") String usuario,
                                        @Value("${app.dueno.clave-inicial}") String claveInicial) {
        return args -> {
            if (usuarios.count() == 0) {
                Usuario dueno = new Usuario();
                dueno.setUsuario(usuario);
                dueno.setClaveHash(encoder.encode(claveInicial));
                usuarios.save(dueno);
                log.warn("Se creó el usuario '{}' con la clave inicial. Entrá al panel y cambiala.", usuario);
            }
        };
    }
}
