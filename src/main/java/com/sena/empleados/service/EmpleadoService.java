package com.sena.empleados.service;

import com.sena.empleados.dao.EmpleadoDAO;
import com.sena.empleados.model.Empleado;
import java.sql.SQLException;
import java.util.List;

public final class EmpleadoService {
    private final EmpleadoDAO empleadoDAO;

    public EmpleadoService(EmpleadoDAO empleadoDAO) {
        this.empleadoDAO = empleadoDAO;
    }

    public List<Empleado> consultar(String filtro) throws SQLException {
        return empleadoDAO.consultar(filtro == null ? "" : filtro.trim());
    }

    public Empleado buscarPorId(int id) throws SQLException {
        validarId(id);
        return empleadoDAO.buscarPorId(id);
    }

    public int registrar(String nombre, String documento, String correo, String telefono) throws SQLException {
        return empleadoDAO.registrar(validarDatos(0, nombre, documento, correo, telefono));
    }

    public boolean actualizar(int id, String nombre, String documento, String correo, String telefono)
            throws SQLException {
        validarId(id);
        return empleadoDAO.actualizar(validarDatos(id, nombre, documento, correo, telefono));
    }

    public boolean eliminar(int id) throws SQLException {
        validarId(id);
        return empleadoDAO.eliminar(id);
    }

    private static Empleado validarDatos(int id, String nombre, String documento, String correo, String telefono) {
        String nombreLimpio = limpiar(nombre);
        String documentoLimpio = limpiar(documento);
        String correoLimpio = limpiar(correo).toLowerCase();
        String telefonoLimpio = limpiar(telefono);

        if (nombreLimpio.length() < 2 || nombreLimpio.length() > 100) {
            throw new IllegalArgumentException("El nombre es obligatorio y debe tener entre 2 y 100 caracteres.");
        }
        if (documentoLimpio.isEmpty() || documentoLimpio.length() > 30) {
            throw new IllegalArgumentException("El documento es obligatorio y admite hasta 30 caracteres.");
        }
        if (correoLimpio.length() > 150 || !correoLimpio.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Ingresa un correo valido (maximo 150 caracteres).");
        }
        if (telefonoLimpio.isEmpty() || telefonoLimpio.length() > 30) {
            throw new IllegalArgumentException("El telefono es obligatorio y admite hasta 30 caracteres.");
        }
        return new Empleado(id, nombreLimpio, documentoLimpio, correoLimpio, telefonoLimpio);
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static void validarId(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("El identificador del empleado no es valido.");
        }
    }
}
