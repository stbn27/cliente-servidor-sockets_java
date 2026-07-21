# Ejecucion de JAR en Windows y Linux

Este documento explica como ejecutar los JAR generados del servidor y del cliente desde una terminal.

## Requisito

Instalar Java 17 o una version LTS compatible.

Verificacion:

```bash
java -version
```

Debe mostrarse una version 17 o superior compatible.

## Archivos esperados

Despues de compilar el proyecto con Maven, los JAR ejecutables quedan en:

```text
server/target/tablero-noticias-server.jar
client/target/tablero-noticias-client.jar
```

Para ejecutar el servidor en otra computadora basta copiar el JAR del servidor.
Para ejecutar el cliente en otra computadora basta copiar el JAR del cliente.

## Ejecutar en Linux

Si la terminal esta ubicada en el directorio donde se encuentra el JAR:

```bash
java -jar tablero-noticias-server.jar
```

Para el cliente:

```bash
java -jar tablero-noticias-client.jar
```

Si el servidor sera usado por clientes en otras maquinas, conviene iniciar el servidor indicando la IP real de la maquina servidor:

```bash
java -jar tablero-noticias-server.jar --rmi.host=192.168.1.50
```

Tambien pueden indicarse los puertos:

```bash
java -jar tablero-noticias-server.jar --rmi.host=192.168.1.50 --rmi.port=1099 --rmi.object.port=1100 --rmi.service=TableroNoticias
```

En Linux, el cliente Swing requiere entorno grafico. Si se ejecuta en una terminal sin pantalla, por ejemplo por SSH sin X11 o en un servidor sin escritorio, aparecera `HeadlessException`.

## Ejecutar en Windows

Abrir PowerShell o CMD en la carpeta donde se encuentra el JAR.

Servidor:

```powershell
java -jar tablero-noticias-server.jar
```

Cliente:

```powershell
java -jar tablero-noticias-client.jar
```

Si el servidor recibira conexiones desde otras computadoras, usar la IP real de la maquina servidor:

```powershell
java -jar tablero-noticias-server.jar --rmi.host=192.168.1.50
```

Con puertos explicitos:

```powershell
java -jar tablero-noticias-server.jar --rmi.host=192.168.1.50 --rmi.port=1099 --rmi.object.port=1100 --rmi.service=TableroNoticias
```

## Conexion desde el cliente

Al abrir el cliente aparece el dialogo de conexion.

Valores locales:

```text
Servidor: localhost
Puerto: 1099
Servicio: TableroNoticias
```

Si el servidor esta en otra maquina, en `Servidor` se debe escribir la IP de esa maquina, por ejemplo:

```text
Servidor: 192.168.1.50
Puerto: 1099
Servicio: TableroNoticias
```

## Firewall y red

Para usar maquinas distintas, la maquina servidor debe permitir conexiones entrantes a:

```text
1099  Registry RMI
1100  Objeto remoto RMI
```

Si se cambian los puertos con `--rmi.port` o `--rmi.object.port`, deben abrirse esos puertos en el firewall.

## Base de datos

La base H2 se crea unicamente en la maquina servidor.

Por defecto se guarda en:

```text
./data/tablero-noticias
```

Se puede cambiar al iniciar el servidor:

```bash
java -jar tablero-noticias-server.jar --database.path=./data/tablero-noticias
```

En Windows tambien puede usarse una ruta propia:

```powershell
java -jar tablero-noticias-server.jar --database.path=C:\tablero-noticias\data\tablero-noticias
```

## Credenciales de demostracion

```text
redactor1 / redactor123
redactor2 / noticias123
```

Estas credenciales son solo para la practica academica.
