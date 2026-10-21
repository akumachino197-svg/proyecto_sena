package com.sena.empleados;

import com.sena.empleados.config.DatabaseConnection;
import com.sena.empleados.dao.EmpleadoDAO;
import com.sena.empleados.service.EmpleadoService;
import com.sena.empleados.web.EmpleadoHttpServer;
import java.sql.SQLException;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        try {
            databaseConnection.verifyConnection();
            System.out.println("Conexion JDBC con MySQL establecida.");
        } catch (SQLException exception) {
            System.err.println("No se pudo conectar a MySQL: " + exception.getMessage());
            System.err.println("Verifica que MySQL este iniciado, que exista sena_empleados y que DB_URL, DB_USER y DB_PASSWORD sean correctos.");
        }

        int port = readPort();
        EmpleadoService empleadoService = new EmpleadoService(new EmpleadoDAO(databaseConnection));
        EmpleadoHttpServer server = new EmpleadoHttpServer(empleadoService, port);
        server.start();
        System.out.println("Aplicacion disponible en http://localhost:" + port);
    }

    private static int readPort() {
        String configuredPort = System.getenv().getOrDefault("APP_PORT", "8080");
        try {
            int port = Integer.parseInt(configuredPort);
            if (port < 1 || port > 65535) {
                throw new NumberFormatException("Fuera de rango");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("APP_PORT debe ser un puerto entre 1 y 65535.", exception);
        }
    }
}
