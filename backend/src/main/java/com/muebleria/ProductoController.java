package com.muebleria;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * API de productos.
 *
 * Públicas (cualquiera las puede ver):
 *   GET  /api/productos              todos, o filtrados con ?categoria=sofas
 *   GET  /api/productos/ultimos      los 6 más nuevos
 *
 * Solo el dueño (las protege el interceptor de WebConfig):
 *   POST   /api/productos            crear
 *   POST   /api/productos/{id}       editar
 *   DELETE /api/productos/{id}       borrar
 *
 * Para editar usamos POST y no PUT porque algunos servidores no leen
 * formularios con archivos cuando el método es PUT.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    // Categorías válidas. Si agregás una, sumala también en frontend/src/tienda.js
    private static final List<String> CATEGORIAS = List.of("sofas", "sillones", "sillas");

    private final ProductoRepository productos;
    private final Imagenes imagenes;

    public ProductoController(ProductoRepository productos, Imagenes imagenes) {
        this.productos = productos;
        this.imagenes = imagenes;
    }

    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return productos.findAllByOrderByIdDesc();
        }
        return productos.findByCategoriaOrderByIdDesc(categoria);
    }

    @GetMapping("/ultimos")
    public List<Producto> ultimos() {
        return productos.findTop6ByOrderByIdDesc();
    }

    @PostMapping
    public Producto crear(@RequestParam String nombre,
                          @RequestParam String categoria,
                          @RequestParam BigDecimal precio,
                          @RequestParam(required = false) String descripcion,
                          @RequestParam(required = false) MultipartFile imagen) throws IOException {
        Producto producto = new Producto();
        completarDatos(producto, nombre, categoria, precio, descripcion);

        if (imagen != null && !imagen.isEmpty()) {
            producto.setImagen(imagenes.guardar(imagen));
        }
        return productos.save(producto);
    }

    @PostMapping("/{id}")
    public Producto editar(@PathVariable Long id,
                           @RequestParam String nombre,
                           @RequestParam String categoria,
                           @RequestParam BigDecimal precio,
                           @RequestParam(required = false) String descripcion,
                           @RequestParam(required = false) MultipartFile imagen) throws IOException {
        Producto producto = buscar(id);
        completarDatos(producto, nombre, categoria, precio, descripcion);

        // La foto es opcional al editar: si no mandan una nueva, queda la que estaba
        if (imagen != null && !imagen.isEmpty()) {
            String anterior = producto.getImagen();
            producto.setImagen(imagenes.guardar(imagen));
            imagenes.borrar(anterior);
        }
        return productos.save(producto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void borrar(@PathVariable Long id) {
        Producto producto = buscar(id);
        productos.delete(producto);
        imagenes.borrar(producto.getImagen());
    }

    // ----- Auxiliares -----

    private Producto buscar(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese producto."));
    }

    // Valida los datos del formulario y los copia al producto
    private void completarDatos(Producto producto, String nombre, String categoria,
                                BigDecimal precio, String descripcion) {
        nombre = nombre.trim();
        if (nombre.isEmpty()) {
            throw error("Falta el nombre.");
        }
        if (nombre.length() > 120) {
            throw error("El nombre es muy largo (máximo 120 letras).");
        }
        if (!CATEGORIAS.contains(categoria)) {
            throw error("La categoría no es válida.");
        }
        if (precio.signum() < 0) {
            throw error("El precio no puede ser negativo.");
        }

        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecio(precio);
        producto.setDescripcion(descripcion == null ? "" : descripcion.trim());
    }

    private ResponseStatusException error(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}
