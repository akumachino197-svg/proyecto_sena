# Sistema de Gestion de Empleados

Prototipo web local en Java para registrar, consultar, actualizar y eliminar empleados con JDBC y MySQL/MariaDB de XAMPP.

## Requisitos

- JDK 25 (o Java 17 o superior).
- XAMPP con MySQL/MariaDB.
- MySQL Connector/J. El proyecto busca el controlador en `lib/`.
- Git para versionar el proyecto.

No se requiere Maven ni un servidor web separado. La aplicacion inicia un servidor HTTP local en `http://localhost:8080`.

## Preparar la base de datos

1. Inicia **MySQL** desde el panel de control de XAMPP.
2. Abre phpMyAdmin o la consola MySQL.
3. Ejecuta el script [`database/schema.sql`](database/schema.sql).
4. El usuario predeterminado de XAMPP suele ser `root` sin contrasena. Si tu instalacion usa otras credenciales, configuralas en las variables de entorno antes de iniciar la aplicacion.

El script crea la base `sena_empleados` y la tabla `empleados`. No inserta registros de ejemplo.

## Configuracion del conector

El archivo `lib/mysql-connector-j-26.7.0.jar` debe estar presente. Si no esta incluido en tu copia del proyecto, copia el JAR de MySQL Connector/J a la carpeta `lib`.

## Ejecutar en Windows

Abre PowerShell en la carpeta del proyecto y ejecuta:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/sena_empleados?serverTimezone=UTC"
$env:DB_USER = "root"
$env:DB_PASSWORD = ""
$env:APP_PORT = "8080"
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1
```

El permiso para ejecutar scripts se limita a ese proceso de PowerShell; no cambia la politica permanente del equipo. Si el usuario de MySQL tiene contrasena, asignala solo en la variable local `DB_PASSWORD`; no la guardes en archivos ni la publiques en GitHub. El script compila el codigo y arranca la aplicacion.

Visita `http://localhost:8080`. Para detenerla, usa `Ctrl+C` en la terminal.

## Funcionalidades

- Crear empleados con nombre, documento, correo y telefono.
- Consultar todos los empleados o filtrar por nombre, documento o correo.
- Actualizar los datos desde la tabla.
- Eliminar un registro con confirmacion del navegador.
- Validar campos y mostrar mensajes de resultado.
- Usar `PreparedStatement` en las consultas JDBC.

## Estructura

```text
src/main/java/com/sena/empleados/
  config/DatabaseConnection.java
  dao/EmpleadoDAO.java
  model/Empleado.java
  service/EmpleadoService.java
  web/EmpleadoHttpServer.java
  Main.java
database/schema.sql
lib/mysql-connector-j-26.7.0.jar
run.ps1
```

## Artefactos de diseno usados por este prototipo

No se encontraron diagramas o historias de usuario previos en la carpeta entregada. Para mantener el prototipo consistente, su alcance se limita al CRUD de empleados:

- **Caso de uso principal:** el usuario registra, consulta, filtra, edita o elimina empleados.
- **Historia de usuario:** como usuario del sistema, quiero administrar empleados para mantener sus datos actualizados en la base de datos.
- **Clases:** `Empleado` representa los datos; `EmpleadoDAO` ejecuta SQL; `EmpleadoService` valida reglas; `DatabaseConnection` configura JDBC; `EmpleadoHttpServer` presenta la interfaz web.

Si existen diagramas o diseños aprobados anteriormente, deben revisarse e incorporarse antes de la entrega final.
El detalle de casos de uso, historias y clases esta en [`docs/artefactos.md`](docs/artefactos.md).

## Verificacion realizada

Se inicio MySQL/MariaDB de XAMPP (MariaDB 10.4.32, puerto 3306), se ejecuto el esquema y se comprobo desde la pagina el alta, busqueda por documento, actualizacion y eliminacion de un empleado temporal. Se verifico cada cambio directamente en MySQL y se elimino el registro de prueba al finalizar. No se cargaron datos de muestra permanentes.

## Git

Los commits locales se configuran con el nombre y correo del aprendiz. Para publicar el proyecto, crea un repositorio en GitHub y agrega su URL al archivo `repositorio.txt`. No publiques contrasenas ni archivos de configuracion local.
