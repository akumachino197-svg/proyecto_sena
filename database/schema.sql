CREATE DATABASE IF NOT EXISTS sena_empleados
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE sena_empleados;

CREATE TABLE IF NOT EXISTS empleados (
  id INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  documento VARCHAR(30) NOT NULL,
  correo VARCHAR(150) NOT NULL,
  telefono VARCHAR(30) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_empleados_documento (documento),
  UNIQUE KEY uq_empleados_correo (correo)
) ENGINE=InnoDB;
