# Servidor de cine (sockets + hilos)

## Requisitos
- Java 17 (segun `nbproject/project.properties`).

## Compilacion y ejecucion
Desde `servidor/`:

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
```

```bash
java -cp out servidor.ServidorCine
```

Prueba local sin sockets:

```bash
java -cp out servidor.PruebaLocal
```

## Protocolo (texto con |)
- `LISTAR_FUNCIONES`
- `VER_ASIENTOS|F001`
- `BLOQUEAR_ASIENTO|F001|A1|Juan Perez`
- `CONFIRMAR_COMPRA|F001|A1|Juan Perez`
- `CANCELAR_COMPRA|F001|A1|Juan Perez`
- `SALIR`

Respuestas:
- OK: `OK|...`
- Error: `ERR|CODIGO_ERROR|Mensaje descriptivo`

## Orden de mensajes (escenarios)
1. Consultar funciones: cliente envia `LISTAR_FUNCIONES`, servidor responde `OK|FUNCIONES|...`.
2. Consultar asientos: cliente envia `VER_ASIENTOS|F001`, servidor responde `OK|ASIENTOS|...`.
3. Bloquear asiento: cliente envia `BLOQUEAR_ASIENTO|F001|A1|Juan Perez`, servidor responde `OK|ASIENTO_BLOQUEADO|F001|A1|60`.
4. Confirmar compra: cliente envia `CONFIRMAR_COMPRA|F001|A1|Juan Perez`, servidor responde `OK|BOLETO_GENERADO|...`.
5. Dos clientes mismo asiento: el primero bloquea con `BLOQUEAR_ASIENTO|F001|A1|...`, el segundo recibe `ERR|ERR_ASIENTO_BLOQUEADO|...`.
6. Bloqueo expira: si pasan 60s sin confirmar, el servidor libera y responde `ERR|ERR_BLOQUEO_EXPIRADO|...` al confirmar, y el asiento vuelve a `DISPONIBLE`.

## Concurrencia y consistencia
- Cada cliente se atiende en un `ClienteHandler` (hilo independiente).
- La clase `CineService` sincroniza los metodos criticos (`synchronized`).
- Antes de cada operacion se limpian bloqueos expirados.
- El estado del asiento se actualiza en memoria y se persiste en `data/asientos.txt`.
- La confirmacion de compra valida bloqueo vigente, cliente propietario y estado del asiento, evitando venta duplicada.

## Archivos de datos
Ubicados en `servidor/data/`:
- `peliculas.txt`
- `funciones.txt`
- `asientos.txt`
- `boletos.txt`

Si no existen, el servidor los crea con datos iniciales.
