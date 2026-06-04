# Cliente de Cine (Consola)

Cliente por consola que se conecta a un servidor de cine usando sockets TCP.

## Requisitos

- Java 11 o superior
- Servidor en ejecucion


## Flujo de uso

1. El cliente se conecta y espera la primera pantalla automatica del servidor.
2. Se muestran las funciones disponibles del dia en una tabla ASCII.
3. El cliente guiara el flujo segun el estado enviado por el servidor.


## Multiples clientes

Abra varias terminales para simular concurrencia:

- Terminal 1: servidor
- Terminal 2: cliente 1
- Terminal 3: cliente 2
- Terminal 4: cliente 3

Todos los clientes pueden ejecutarse al mismo tiempo mientras apunten a la misma IP y puerto del servidor.
