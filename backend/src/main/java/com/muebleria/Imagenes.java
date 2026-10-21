package com.muebleria;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Guarda las fotos de los productos en la carpeta uploads.
 *
 * De cada foto se guardan dos versiones en JPG:
 *   - abc123.jpg        hasta 1200 px de ancho (para pantallas grandes)
 *   - abc123_chica.jpg  hasta 600 px de ancho (para las tarjetas del catálogo)
 *
 * Así el catálogo carga rápido aunque se suban fotos pesadas.
 * Se usa solo Java estándar (ImageIO), sin librerías extra.
 */
@Component
public class Imagenes {

    private static final int ANCHO_GRANDE = 1200;
    private static final int ANCHO_CHICA = 600;
    private static final float CALIDAD_JPG = 0.82f; // 0 a 1. 0.82 se ve bien y pesa poco

    private final Path carpeta;

    public Imagenes(@Value("${app.carpeta-uploads}") String carpeta) throws IOException {
        this.carpeta = Paths.get(carpeta).toAbsolutePath().normalize();
        Files.createDirectories(this.carpeta); // si no existe, la crea
    }

    public Path getCarpeta() {
        return carpeta;
    }

    // Guarda la foto (en sus dos tamaños) y devuelve el nombre del archivo
    public String guardar(MultipartFile archivo) throws IOException {
        BufferedImage original;
        try (InputStream entrada = archivo.getInputStream()) {
            original = ImageIO.read(entrada);
        } catch (IOException e) {
            original = null;
        }
        // ImageIO devuelve null si el archivo no es una imagen que pueda leer
        if (original == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se pudo leer la foto. Usá un archivo JPG o PNG.");
        }

        // Nombre al azar: nunca se repite y no depende del nombre que mandó el usuario
        String nombre = UUID.randomUUID() + ".jpg";
        escribirJpg(achicar(original, ANCHO_GRANDE), carpeta.resolve(nombre));
        escribirJpg(achicar(original, ANCHO_CHICA), carpeta.resolve(versionChica(nombre)));
        return nombre;
    }

    // Borra las dos versiones de una foto
    public void borrar(String nombre) {
        if (nombre == null || nombre.isBlank()) return;
        try {
            Files.deleteIfExists(carpeta.resolve(nombre));
            Files.deleteIfExists(carpeta.resolve(versionChica(nombre)));
        } catch (IOException e) {
            // Si no se puede borrar no es grave: queda un archivo de más en la carpeta
        }
    }

    private static String versionChica(String nombre) {
        return nombre.replace(".jpg", "_chica.jpg");
    }

    // Achica la imagen a un ancho máximo manteniendo la proporción.
    // Se achica de a mitades para que el resultado no quede pixelado.
    private static BufferedImage achicar(BufferedImage imagen, int anchoMax) {
        int anchoFinal = Math.min(imagen.getWidth(), anchoMax);
        int altoFinal = Math.max(1, Math.round(imagen.getHeight() * (anchoFinal / (float) imagen.getWidth())));

        BufferedImage actual = imagen;
        while (actual.getWidth() / 2 >= anchoFinal) {
            actual = redibujar(actual, actual.getWidth() / 2, Math.max(1, actual.getHeight() / 2));
        }
        return redibujar(actual, anchoFinal, altoFinal);
    }

    // Dibuja la imagen en un lienzo nuevo, sin transparencia (fondo blanco para los PNG)
    private static BufferedImage redibujar(BufferedImage origen, int ancho, int alto) {
        BufferedImage destino = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = destino.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ancho, alto);
        g.drawImage(origen, 0, 0, ancho, alto, null);
        g.dispose();
        return destino;
    }

    // Guarda como JPG progresivo (se ve borroso y se va definiendo mientras carga)
    private static void escribirJpg(BufferedImage imagen, Path destino) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam parametros = writer.getDefaultWriteParam();
        parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        parametros.setCompressionQuality(CALIDAD_JPG);
        parametros.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);

        try (ImageOutputStream salida = ImageIO.createImageOutputStream(destino.toFile())) {
            writer.setOutput(salida);
            writer.write(null, new IIOImage(imagen, null, null), parametros);
        } finally {
            writer.dispose();
        }
    }
}
