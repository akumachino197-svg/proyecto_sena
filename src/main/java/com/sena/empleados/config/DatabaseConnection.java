package com.sena.empleados.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private final String url;
    private final String user;
    private final String password;

    public DatabaseConnection() {
        this.url = setting("DB_URL", "jdbc:mysql://localhost:3306/sena_empleados?serverTimezone=UTC");
        this.user = setting("DB_USER", "root");
        this.password = setting("DB_PASSWORD", "");
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void verifyConnection() throws SQLException {
        try (Connection connection = getConnection()) {
            if (!connection.isValid(3)) {
                throw new SQLException("El servidor JDBC no valido la conexion.");
            }
        }
    }

    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null ? defaultValue : value;
    }
}
