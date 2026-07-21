# Tolerancia a fallos

## Alcance

La práctica implementa detección, mensajes y recuperación manual ante fallos esperables de una aplicación RMI. No pretende ofrecer alta disponibilidad: si el único servidor o su base están fuera de servicio, el cliente no puede completar operaciones hasta que se recuperen.

## Fallos detectados por el cliente

| Fallo | Señal habitual | Reacción |
|---|---|---|
| Host incorrecto o no resoluble | `RemoteException`/causa de red | Mensaje de conexión y conservación de los campos. |
| Puerto cerrado o firewall | conexión rechazada o timeout | Mensaje que sugiere revisar dirección, puertos y firewall. |
| Registry ausente | fallo de `lookup` | El diálogo permanece disponible para reintentar. |
| Nombre no registrado | `NotBoundException` | Mensaje específico de servicio no registrado. |
| Servidor no saludable | `EstadoServidor.disponible=false` | La conexión no se acepta como activa. |
| Desconexión durante operación | `RemoteException` | La ventana no se cierra; estado desconectado y reconexión manual. |
| Datos inválidos | `ValidacionException` | Mensaje de campo; el usuario conserva el formulario. |
| Token ausente, inválido o vencido | `AutenticacionException` | Se descarta sesión local y se solicita autenticar otra vez. |
| Noticia de otro autor | `AutorizacionException` | Se informa que la acción no está permitida. |
| Noticia eliminada | `NoticiaNoEncontradaException` | Se actualiza el listado. |
| Versión obsoleta | `ConflictoEdicionException` | Mensaje especial y petición de actualizar antes de reintentar. |
| Persistencia no disponible | `ServicioNoDisponibleException` | Mensaje neutro; no se muestran detalles SQL. |

Las operaciones remotas se ejecutan mediante `SwingWorker`. Una demora no congela el EDT y `done()` centraliza la actualización de botones, tabla y mensajes.

## Reconexión

La reconexión es explícita. El usuario vuelve al diálogo con el host, puerto y servicio anteriores. Una conexión se considera válida solo después de localizar el stub e invocar `verificarEstado()`.

No se reintentan automáticamente publicaciones, ediciones ni eliminaciones. Ante la pérdida de una respuesta no siempre puede saberse si el servidor confirmó la transacción; repetir silenciosamente podría duplicar una publicación o aplicar una intención sobre un estado nuevo. El usuario actualiza primero y decide.

## Reacción del servidor

Cada llamada valida referencias nulas, identificadores, límites y versión. Las escrituras se ejecutan dentro de una transacción. Si JDBC falla:

1. se intenta rollback;
2. se cierran statements, resultados y conexión;
3. se registra el diagnóstico técnico en el servidor;
4. se traduce a una excepción remota de dominio con un mensaje no sensible;
5. otros hilos RMI continúan atendiendo solicitudes.

No se utiliza un bloque global, por lo que el fallo de una petición no deja bloqueadas todas las demás. Una excepción de una operación tampoco termina el proceso servidor.

## Rollback

La inicialización del esquema y datos de ejemplo es transaccional. Publicar, editar y eliminar también deben confirmar únicamente al terminar todos sus pasos. Si la operación falla antes del commit, rollback conserva el estado previo.

El rollback es una acción de recuperación secundaria y también puede fallar. Ese fallo se registra; nunca se usa un `catch` vacío para fingir éxito. En todos los casos el cliente recibe que la operación no pudo confirmarse.

## Gestión de recursos

- `try-with-resources` para conexiones, statements y resultados.
- Una conexión JDBC por operación.
- `DatabaseManager.close()` solicita `SHUTDOWN` de H2.
- Un shutdown hook ejecuta el cierre al detener normalmente el proceso.
- `DB_CLOSE_ON_EXIT=FALSE` evita depender del hook implícito de H2.
- El objeto remoto se exporta en un puerto definido para facilitar diagnóstico de red.

Un cierre abrupto del sistema operativo no permite ejecutar hooks; H2 aporta recuperación de su archivo, pero deben existir respaldos en un uso real.

## Logs

Se registran inicio y cierre, disponibilidad de H2, creación o reutilización del Registry, registro del servicio, autenticaciones exitosas y fallidas, publicaciones, ediciones, eliminaciones, conflictos y errores técnicos.

Nunca se registran:

- contraseñas;
- hashes BCrypt;
- tokens completos;
- contenido íntegro de noticias salvo necesidad explícita de diagnóstico;
- trazas SQL enviadas al cliente.

Los mensajes remotos se diseñan para el usuario. Los logs conservan el detalle útil para el operador, incluida la excepción original cuando corresponde.

## Fallos de sesión

Las sesiones viven en memoria y tienen vencimiento configurable. Reiniciar el servidor invalida todos los tokens, comportamiento aceptado para la práctica. La validación del token se realiza en cada escritura; que el cliente muestre todavía el nombre de un redactor no prueba que su sesión siga vigente.

Después de una expiración, el cliente debe limpiar su estado de redactor, deshabilitar acciones protegidas y permitir iniciar sesión nuevamente. Cerrar una sesión ya expirada es seguro e idempotente.

## Base no disponible o ruta inválida

Si la ruta no puede crearse o H2 no abre, el servidor falla durante inicialización en vez de publicar un objeto aparentemente sano. Si la base deja de responder durante ejecución, la operación afectada se revierte cuando es posible y se traduce a `ServicioNoDisponibleException`. `verificarEstado()` permite representar por separado disponibilidad del proceso y de la base.

## Limitaciones reales

- No hay réplica del servidor o H2.
- No hay circuit breaker ni reconexión automática.
- No existe persistencia de sesiones.
- No existe TLS ni autenticación del servidor.
- No hay protocolo de idempotencia para escrituras ambiguas.
- Los timeouts finales dependen en parte de la implementación y propiedades de red de RMI/JVM.
- Un firewall debe permitir tanto el puerto del Registry como el del objeto remoto.

Estas limitaciones se documentan para no confundir tratamiento de errores con disponibilidad continua.
