package com.muebleria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Consultas a la tabla productos. Spring arma el SQL a partir del nombre del método.
 * Ordenamos por id descendente para que los más nuevos aparezcan primero.
 */
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findAllByOrderByIdDesc();

    List<Producto> findByCategoriaOrderByIdDesc(String categoria);

    // Los últimos 6 cargados, para la página de inicio
    List<Producto> findTop6ByOrderByIdDesc();
}
