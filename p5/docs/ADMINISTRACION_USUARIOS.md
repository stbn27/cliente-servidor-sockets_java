# Administracion de usuarios redactores

La interfaz Swing del cliente no incluye administracion de usuarios. Para mantener el sistema academico y pequeno, los redactores se administran desde el lado del servidor.

## Opcion recomendada: herramienta del servidor

El JAR del servidor incluye una utilidad de terminal para listar, crear, activar y desactivar usuarios. Esta utilidad genera el hash BCrypt automaticamente.

Debe ejecutarse en la maquina del servidor, usando la misma ruta de base de datos que usa el servidor RMI.

Importante: detener el servidor antes de ejecutar esta herramienta si la base esta en modo archivo. H2 embebido puede bloquear el archivo de base de datos mientras el servidor esta abierto.

## Listar usuarios

Desde la carpeta donde esta el JAR:

```bash
java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool listar
```

En Windows PowerShell:

```powershell
java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool listar
```

Si la base esta en otra ruta:

```bash
java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool listar --database.path=./data/tablero-noticias
```

## Crear un redactor

Linux:

```bash
java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool crear redactor3 clave123 "Mariana Lopez"
```

Windows PowerShell:

```powershell
java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool crear redactor3 clave123 "Mariana Lopez"
```

Con ruta explicita de base de datos:

```bash
java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool crear redactor3 clave123 "Mariana Lopez" --database.path=./data/tablero-noticias
```

El usuario queda creado con rol `REDACTOR` y `activo=TRUE`.

## Desactivar o activar usuarios

Desactivar:

```bash
java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool desactivar redactor3
```

Activar:

```bash
java -cp tablero-noticias-server.jar server.tools.UsuarioAdminTool activar redactor3
```

## Ver datos con H2 Console

H2 permite abrir una consola web en el navegador. Como H2 esta incluido dentro del JAR del servidor, se puede iniciar asi:

```bash
java -cp tablero-noticias-server.jar org.h2.tools.Console
```

En la pantalla de conexion usar:

```text
JDBC URL: jdbc:h2:file:/ruta/absoluta/data/tablero-noticias;DB_CLOSE_ON_EXIT=FALSE
User Name: sa
Password:
```

La contrasena esta vacia.

Ejemplo de consulta para ver usuarios:

```sql
SELECT id, usuario, nombre, rol, activo
FROM autores
ORDER BY id;
```

Ejemplo para ver noticias con autor:

```sql
SELECT n.id, n.titulo, n.categoria, a.usuario, a.nombre, n.version
FROM noticias n
JOIN autores a ON a.id = n.autor_id
ORDER BY n.fecha_modificacion DESC;
```

No se recomienda crear usuarios manualmente desde H2 Console, porque `password_hash` debe contener un hash BCrypt valido. Si se inserta texto plano, el inicio de sesion fallara y ademas se rompe el requisito de seguridad del proyecto.

## Insertar usuario manualmente solo si ya tienes un hash BCrypt

Si por alguna razon se genera un hash BCrypt valido con otra herramienta, el SQL seria:

```sql
INSERT INTO autores(usuario, password_hash, nombre, rol, activo)
VALUES ('redactor4', '<hash-bcrypt>', 'Nombre Visible', 'REDACTOR', TRUE);
```

Para el uso normal del proyecto, usar `server.tools.UsuarioAdminTool crear`.
