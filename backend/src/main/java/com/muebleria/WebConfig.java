package com.muebleria;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Configuración web:
 *  1. Protege las rutas del panel (crear, editar, borrar, cambiar clave).
 *  2. Sirve las fotos de la carpeta uploads.
 *  3. Hace que las páginas del front (/catalogo, /panel, etc.) funcionen
 *     aunque se entre directo por la URL o se recargue la página.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Sesiones sesiones;
    private final Imagenes imagenes;

    public WebConfig(Sesiones sesiones, Imagenes imagenes) {
        this.sesiones = sesiones;
        this.imagenes = imagenes;
    }

    // 1. Revisa el token antes de dejar pasar el pedido
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws IOException {
                // Ver productos es público
                if ("GET".equals(request.getMethod())) {
                    return true;
                }
                String usuario = sesiones.usuarioDe(Sesiones.tokenDe(request));
                if (usuario == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Tenés que iniciar sesión.");
                    return false;
                }
                request.setAttribute("usuario", usuario); // lo usa AuthController.cambiarClave
                return true;
            }
        }).addPathPatterns("/api/productos", "/api/productos/**", "/api/cambiar-clave");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 2. Fotos de productos. Como el nombre de cada archivo es único,
        //    el navegador puede guardarlas en caché por un año.
        String ubicacion = imagenes.getCarpeta().toUri().toString();
        if (!ubicacion.endsWith("/")) ubicacion += "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(ubicacion)
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());

        // Archivos JS y CSS que genera Vite. Llevan un código en el nombre que
        // cambia con cada versión, así que también se pueden cachear un año.
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
    }

    // 3. Todas estas rutas devuelven el index.html y Vue se encarga del resto
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        for (String ruta : List.of("/catalogo", "/sobre-nosotros", "/login", "/panel")) {
            registry.addViewController(ruta).setViewName("forward:/index.html");
        }
    }
}
