package com.sena.empleados.dao;

import com.sena.empleados.config.DatabaseConnection;
import com.sena.empleados.model.Empleado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class EmpleadoDAO {
    private final DatabaseConnection databaseConnection;

    public EmpleadoDAO(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public List<Empleado> consultar(String filtro) throws SQLException {
        String sql = """
                SELECT id, nombre, documento, correo, telefono
                FROM empleados
                WHERE nombre LIKE ? OR documento LIKE ? OR correo LIKE ?
                ORDER BY id DESC
                """;
        String patron = "%" + filtro + "%";
        List<Empleado> empleados = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patron);
            statement.setString(2, patron);
            statement.setString(3, patron);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    empleados.add(mapear(resultSet));
                }
            }
        }
        return empleados;
    }

    public Empleado buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, documento, correo, telefono FROM empleados WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapear(resultSet) : null;
            }
        }
    }

    public int registrar(Empleado empleado) throws SQLException {
        String sql = "INSERT INTO empleados (nombre, documento, correo, telefono) VALUES (?, ?, ?, ?)";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(statement, empleado);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se recibio el identificador del empleado creado.");
                }
                return keys.getInt(1);
            }
        }
    }

    public boolean actualizar(Empleado empleado) throws SQLException {
        String sql = "UPDATE empleados SET nombre = ?, documento = ?, correo = ?, telefono = ? WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            asignarDatos(statement, empleado);
            statement.setInt(5, empleado.id());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        String sql = "DELETE FROM empleados WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    private static void asignarDatos(PreparedStatement statement, Empleado empleado) throws SQLException {
        statement.setString(1, empleado.nombre());
        statement.setString(2, empleado.documento());
        statement.setString(3, empleado.correo());
        statement.setString(4, empleado.telefono());
    }

    private static Empleado mapear(ResultSet resultSet) throws SQLException {
        return new Empleado(
                resultSet.getInt("id"),
                resultSet.getString("nombre"),
                resultSet.getString("documento"),
                resultSet.getString("correo"),
                resultSet.getString("telefono"));
    }
}
