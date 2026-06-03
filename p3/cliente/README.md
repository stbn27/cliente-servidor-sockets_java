# Cliente de Cine (Consola)

Cliente por consola que se conecta a un servidor de cine usando sockets TCP.

## Requisitos

- Java 11 o superior
- Servidor en ejecucion

## Compilacion

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

## Ejecucion

```bash
java -cp out cliente.Main
```

## Flujo de uso

1. Capture la IP del servidor.
2. Capture el puerto del servidor.
3. El cliente se conecta y espera la primera pantalla automatica del servidor.
4. Se muestran las funciones disponibles del dia en una tabla ASCII.
5. El cliente guiara el flujo segun el estado enviado por el servidor.

Si presiona Enter al capturar IP o puerto, se usan los valores por defecto `127.0.0.1` y `2000`.

## Multiples clientes

Abra varias terminales para simular concurrencia:

- Terminal 1: servidor
- Terminal 2: cliente 1
- Terminal 3: cliente 2
- Terminal 4: cliente 3

Todos los clientes pueden ejecutarse al mismo tiempo mientras apunten a la misma IP y puerto del servidor.
