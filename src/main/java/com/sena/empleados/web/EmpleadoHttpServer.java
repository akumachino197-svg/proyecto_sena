package com.sena.empleados.web;

import com.sena.empleados.model.Empleado;
import com.sena.empleados.service.EmpleadoService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public final class EmpleadoHttpServer {
    private final EmpleadoService empleadoService;
    private final int port;
    private HttpServer server;

    public EmpleadoHttpServer(EmpleadoService empleadoService, int port) {
        this.empleadoService = empleadoService;
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", this::manejarSolicitud);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void manejarSolicitud(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())
                    && ("/".equals(exchange.getRequestURI().getPath())
                    || "/empleados".equals(exchange.getRequestURI().getPath()))) {
                mostrarPagina(exchange, 200, mensajeDesdeEstado(exchange), "");
                return;
            }
            if (!"POST".equals(exchange.getRequestMethod())) {
                responder(exchange, 404, paginaError("No se encontro la pagina solicitada."));
                return;
            }

            Map<String, String> datos = leerFormulario(exchange);
            String ruta = exchange.getRequestURI().getPath();
            switch (ruta) {
                case "/empleados/crear" -> {
                    empleadoService.registrar(datos.get("nombre"), datos.get("documento"),
                            datos.get("correo"), datos.get("telefono"));
                    redirigir(exchange, "creado");
                }
                case "/empleados/actualizar" -> {
                    int id = leerId(datos.get("id"));
                    if (!empleadoService.actualizar(id, datos.get("nombre"), datos.get("documento"),
                            datos.get("correo"), datos.get("telefono"))) {
                        throw new IllegalArgumentException("El empleado ya no existe; actualiza la lista.");
                    }
                    redirigir(exchange, "actualizado");
                }
                case "/empleados/eliminar" -> {
                    if (!"si".equals(datos.get("confirmacion"))) {
                        throw new IllegalArgumentException("Confirma la eliminacion antes de continuar.");
                    }
                    if (!empleadoService.eliminar(leerId(datos.get("id")))) {
                        throw new IllegalArgumentException("El empleado ya no existe; actualiza la lista.");
                    }
                    redirigir(exchange, "eliminado");
                }
                default -> responder(exchange, 404, paginaError("No se encontro la pagina solicitada."));
            }
        } catch (IllegalArgumentException exception) {
            mostrarPagina(exchange, 400, exception.getMessage(), "");
        } catch (SQLException exception) {
            System.err.println("Error en la operacion JDBC: " + exception.getMessage());
            mostrarPagina(exchange, 503,
                    "No se pudo completar la operacion en la base de datos. Revisa MySQL, las credenciales y los datos unicos.",
                    "");
        } finally {
            exchange.close();
        }
    }

    private void mostrarPagina(HttpExchange exchange, int status, String mensaje, String filtro) throws IOException {
        List<Empleado> empleados = List.of();
        String errorConsulta = "";
        try {
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null && filtro.isEmpty()) {
                filtro = parametros(query).getOrDefault("q", "");
            }
            empleados = empleadoService.consultar(filtro);
        } catch (SQLException exception) {
            System.err.println("Error consultando empleados: " + exception.getMessage());
            errorConsulta = "No se pudo consultar la base de datos. Inicia MySQL y verifica su configuracion.";
            status = 503;
        }
        if (!errorConsulta.isEmpty()) {
            mensaje = mensaje.isEmpty() ? errorConsulta : mensaje + " " + errorConsulta;
        }
        responder(exchange, status, renderizar(empleados, filtro, mensaje));
    }

    private static String renderizar(List<Empleado> empleados, String filtro, String mensaje) {
        StringBuilder filas = new StringBuilder();
        for (Empleado empleado : empleados) {
            filas.append("""
                    <tr>
                      <td>%d</td>
                      <td colspan="5">
                        <form class="edit-form" method="post" action="/empleados/actualizar">
                          <input type="hidden" name="id" value="%d">
                          <input name="nombre" aria-label="Nombre" required maxlength="100" value="%s">
                          <input name="documento" aria-label="Documento" required maxlength="30" value="%s">
                          <input type="email" name="correo" aria-label="Correo" required maxlength="150" value="%s">
                          <input name="telefono" aria-label="Telefono" required maxlength="30" value="%s">
                          <button class="secondary" type="submit">Guardar</button>
                        </form>
                        <form class="delete-form" method="post" action="/empleados/eliminar">
                          <input type="hidden" name="id" value="%d">
                          <label><input type="checkbox" name="confirmacion" value="si" required> Confirmar eliminacion</label>
                          <button class="danger" type="submit">Eliminar</button>
                        </form>
                      </td>
                    </tr>
                    """.formatted(empleado.id(), empleado.id(), escapar(empleado.nombre()),
                    escapar(empleado.documento()), escapar(empleado.correo()), escapar(empleado.telefono()),
                    empleado.id()));
        }
        if (empleados.isEmpty()) {
            filas.append("<tr><td colspan=\"6\" class=\"empty\">No hay empleados para mostrar.</td></tr>");
        }

        String aviso = mensaje.isEmpty() ? "" : "<div class=\"notice\" role=\"status\">" + escapar(mensaje) + "</div>";
        return """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Gestion de empleados</title>
                  <style>
                    :root{font-family:Arial,sans-serif;color:#172b4d;background:#f4f7fb}
                    *{box-sizing:border-box}body{margin:0}header{background:#123b66;color:#fff;padding:28px max(24px,calc((100%% - 1120px)/2))}
                    header p{margin:8px 0 0;color:#d6e5f5}.container{max-width:1120px;margin:28px auto;padding:0 20px}
                    .card{background:#fff;border:1px solid #dce4ee;border-radius:12px;padding:22px;margin-bottom:20px;box-shadow:0 3px 12px #19395b0b}
                    h1{margin:0;font-size:25px}h2{margin:0 0 16px;font-size:19px}.grid{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:12px}
                    label{display:block;font-size:13px;font-weight:bold;margin-bottom:6px}input{width:100%%;padding:10px;border:1px solid #c7d2df;border-radius:6px;font:inherit}
                    button{border:0;border-radius:6px;padding:10px 14px;background:#1769aa;color:white;font-weight:bold;cursor:pointer}
                    button:hover{filter:brightness(.92)}.primary{margin-top:14px}.secondary{background:#1769aa}.danger{background:#a83434}
                    .notice{padding:12px 14px;border-radius:7px;background:#e7f4eb;color:#175b2c;margin-bottom:18px}
                    .toolbar{display:flex;gap:10px;margin-bottom:14px}.toolbar input{max-width:360px}.table-wrap{overflow:auto}
                    table{border-collapse:collapse;width:100%%;min-width:900px}th,td{text-align:left;padding:10px 8px;border-bottom:1px solid #e4eaf1;vertical-align:top}
                    th{font-size:13px;color:#4d6078}.edit-form{display:grid;grid-template-columns:1fr 1fr 1.4fr 1fr auto;gap:7px}
                    .delete-form{display:flex;align-items:center;gap:8px;margin-top:7px}.delete-form label{margin:0}
                    .delete-form input[type=checkbox]{width:auto}.empty{color:#62748a;text-align:center;padding:24px}.hint{font-size:13px;color:#62748a}
                    @media(max-width:760px){.grid{grid-template-columns:1fr 1fr}.container{margin-top:16px}.card{padding:16px}}
                    @media(max-width:480px){.grid{grid-template-columns:1fr}.toolbar{flex-direction:column}.toolbar input{max-width:none}}
                  </style>
                </head>
                <body>
                  <header><h1>Sistema de Gestion de Empleados</h1><p>Modulo CRUD conectado a MySQL mediante JDBC</p></header>
                  <main class="container">
                    %s
                    <section class="card">
                      <h2>Registrar empleado</h2>
                      <form method="post" action="/empleados/crear">
                        <div class="grid">
                          <div><label for="nombre">Nombre completo</label><input id="nombre" name="nombre" required minlength="2" maxlength="100"></div>
                          <div><label for="documento">Documento</label><input id="documento" name="documento" required maxlength="30"></div>
                          <div><label for="correo">Correo electronico</label><input id="correo" type="email" name="correo" required maxlength="150"></div>
                          <div><label for="telefono">Telefono</label><input id="telefono" name="telefono" required maxlength="30"></div>
                        </div>
                        <button class="primary" type="submit">Registrar empleado</button>
                      </form>
                    </section>
                    <section class="card">
                      <h2>Empleados registrados</h2>
                      <form class="toolbar" method="get" action="/">
                        <input name="q" aria-label="Buscar" placeholder="Buscar por nombre, documento o correo" value="%s">
                        <button type="submit">Buscar</button>
                        <a href="/" style="align-self:center">Limpiar</a>
                      </form>
                      <p class="hint">Edita los campos de una fila y selecciona Guardar para actualizarla.</p>
                      <div class="table-wrap"><table>
                        <thead><tr><th>ID</th><th colspan="5">Datos y acciones</th></tr></thead>
                        <tbody>%s</tbody>
                      </table></div>
                    </section>
                  </main>
                </body>
                </html>
                """.formatted(aviso, escapar(filtro), filas);
    }

    private static Map<String, String> leerFormulario(HttpExchange exchange) throws IOException {
        byte[] body = exchange.getRequestBody().readNBytes(16_385);
        if (body.length > 16_384) {
            throw new IllegalArgumentException("El formulario supera el tamano permitido.");
        }
        return parametros(new String(body, StandardCharsets.UTF_8));
    }

    private static Map<String, String> parametros(String contenido) {
        Map<String, String> parametros = new HashMap<>();
        if (contenido == null || contenido.isEmpty()) {
            return parametros;
        }
        for (String par : contenido.split("&")) {
            String[] partes = par.split("=", 2);
            String clave = decodificar(partes[0]);
            String valor = partes.length > 1 ? decodificar(partes[1]) : "";
            parametros.put(clave, valor);
        }
        return parametros;
    }

    private static String decodificar(String valor) {
        return URLDecoder.decode(valor, StandardCharsets.UTF_8);
    }

    private static int leerId(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException | NullPointerException exception) {
            throw new IllegalArgumentException("Selecciona un empleado valido.");
        }
    }

    private static String mensajeDesdeEstado(HttpExchange exchange) {
        String estado = parametros(exchange.getRequestURI().getRawQuery()).get("estado");
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "creado" -> "Empleado registrado correctamente.";
            case "actualizado" -> "Empleado actualizado correctamente.";
            case "eliminado" -> "Empleado eliminado correctamente.";
            default -> "";
        };
    }

    private static void redirigir(HttpExchange exchange, String estado) throws IOException {
        exchange.getResponseHeaders().set("Location", "/?estado=" + estado);
        exchange.sendResponseHeaders(303, -1);
    }

    private static String escapar(String valor) {
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String paginaError(String mensaje) {
        return "<!doctype html><html lang=\"es\"><meta charset=\"utf-8\"><title>Error</title>"
                + "<body><h1>" + escapar(mensaje) + "</h1><a href=\"/\">Volver</a></body></html>";
    }

    private static void responder(HttpExchange exchange, int status, String contenido) throws IOException {
        byte[] cuerpo = contenido.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Content-Security-Policy",
                "default-src 'none'; style-src 'unsafe-inline'; form-action 'self'; base-uri 'none'; frame-ancestors 'none'");
        exchange.sendResponseHeaders(status, cuerpo.length);
        exchange.getResponseBody().write(cuerpo);
    }
}
