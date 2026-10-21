-- =========================================================
--  Base de datos de la mueblería (MySQL 8)
--  Correr una sola vez:  mysql -u root -p < muebleria.sql
-- =========================================================

CREATE DATABASE IF NOT EXISTS muebleria
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE muebleria;

-- Productos del catálogo
CREATE TABLE IF NOT EXISTS productos (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(120)  NOT NULL,
  categoria   VARCHAR(20)   NOT NULL,              -- sofas, sillones o sillas
  precio      DECIMAL(12,2) NOT NULL,
  descripcion TEXT,
  imagen      VARCHAR(100),                        -- nombre del archivo en uploads/
  creado_en   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_categoria (categoria)
);

-- Usuarios del panel (solo el dueño).
-- No hace falta cargarlo a mano: la aplicación lo crea sola la primera vez
-- con los datos de application.properties (app.dueno.usuario / app.dueno.clave-inicial).
CREATE TABLE IF NOT EXISTS usuarios (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  usuario    VARCHAR(50)  NOT NULL,
  clave_hash VARCHAR(100) NOT NULL,                -- hash BCrypt, nunca la clave real
  PRIMARY KEY (id),
  UNIQUE KEY uq_usuario (usuario)
);

-- Usuario de MySQL que usa la aplicación (solo puede leer y escribir datos).
-- Cambiá la contraseña y poné la misma en application.properties.
CREATE USER IF NOT EXISTS 'muebleria'@'localhost' IDENTIFIED BY 'cambiar-esta-clave';
GRANT SELECT, INSERT, UPDATE, DELETE ON muebleria.* TO 'muebleria'@'localhost';
FLUSH PRIVILEGES;


-- ---------------------------------------------------------
--  Opcional: algunos productos de prueba (sin foto) para ver
--  cómo queda la página. Después se pueden borrar desde el panel.
-- ---------------------------------------------------------
-- INSERT INTO productos (nombre, categoria, precio, descripcion) VALUES
-- ('Sofá Lisboa 3 cuerpos', 'sofas',    850000, 'Estructura de madera maciza, almohadones de espuma de alta densidad. Tela antimanchas en gris, beige o verde.'),
-- ('Sofá Nórdico 2 cuerpos', 'sofas',   620000, 'Patas de paraíso, tapizado en lino. Mide 1,60 m de ancho.'),
-- ('Sillón Berger',          'sillones', 390000, 'El clásico de respaldo alto. Lo hacemos en la tela que elijas.'),
-- ('Sillón Mecedor',         'sillones', 310000, 'Madera de eucalipto y asiento tapizado. Ideal para el living o el cuarto.'),
-- ('Silla de chapa',         'sillas',    85000, 'Chapa de acero pintada. Se puede apilar. Varios colores.'),
-- ('Silla de madera curvada','sillas',   110000, 'Asiento de esterilla. Para comedor.');
