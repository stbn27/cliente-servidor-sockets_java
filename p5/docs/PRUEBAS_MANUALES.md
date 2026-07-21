# Pruebas manuales

Este documento registra pruebas de aceptación que requieren interacción gráfica, procesos separados o condiciones de red/fallo. “Pendiente” significa que el escenario todavía no se ha ejecutado; no equivale a un resultado satisfactorio.

Antes de cada escenario debe anotarse fecha, versión del JAR, sistema operativo, Java utilizado y equipos participantes. Las credenciales mencionadas son exclusivamente las semillas académicas.

## 1. Servidor local y tres clientes

**Objetivo:** verificar que un servidor atiende a un redactor y dos lectores en el mismo equipo.

**Precondiciones:** servidor y tres procesos cliente disponibles; puertos 1099 y 1100 libres; base inicializada.

**Pasos:**

1. Iniciar el servidor con valores predeterminados.
2. Abrir tres clientes y conectar cada uno a `localhost:1099/TableroNoticias`.
3. Autenticar uno como `redactor1`; dejar dos como lectores.
4. Pulsar Actualizar y abrir una noticia en cada cliente.

**Resultado esperado:** los tres clientes conectan sin bloquearse, muestran al menos tres noticias y solo el autenticado presenta acciones de redactor.

**Resultado obtenido:** Pendiente.

## 2. Servidor usando una IP de red local

**Objetivo:** validar comunicación RMI entre equipos y el hostname anunciado por el stub.

**Precondiciones:** equipos en una red local; IP fija o conocida del servidor; `rmi.host` configurado con esa IP; firewall permite los puertos del Registry y objeto.

**Pasos:**

1. Iniciar el servidor con su IP accesible en `rmi.host`.
2. Desde otro equipo, abrir el cliente e indicar IP, 1099 y `TableroNoticias`.
3. Listar y abrir una noticia.
4. Autenticar y publicar una noticia breve.
5. Actualizar desde un tercer equipo.

**Resultado esperado:** el stub dirige al puerto de objeto correcto, la lectura y publicación remotas funcionan y el tercer equipo ve la noticia confirmada.

**Resultado obtenido:** Pendiente.

## 3. Dos lectores consultando simultáneamente

**Objetivo:** comprobar que las lecturas concurrentes son independientes.

**Precondiciones:** servidor activo y dos clientes lectores conectados.

**Pasos:**

1. Seleccionar categorías diferentes en ambos clientes.
2. Pulsar Buscar o Actualizar en ambos con mínima separación.
3. Abrir noticias distintas mediante doble clic.
4. Repetir varias consultas y limpiar filtros.

**Resultado esperado:** no aparecen errores de concurrencia, cada cliente conserva su filtro y ambos reciben resultados completos y ordenados.

**Resultado obtenido:** Pendiente.

## 4. Un redactor publicando mientras los lectores actualizan

**Objetivo:** comprobar lecturas durante una escritura transaccional.

**Precondiciones:** un cliente autenticado como redactor y dos lectores conectados.

**Pasos:**

1. Preparar una noticia válida en el diálogo de publicación.
2. Mientras se guarda, pulsar Actualizar repetidamente en los lectores.
3. Tras el mensaje de éxito, actualizar de nuevo ambos lectores.
4. Abrir la noticia y comparar todos sus campos.

**Resultado esperado:** ningún lector observa una fila parcial; antes del commit puede ver el estado anterior y después ve la noticia completa con ID, autor, fechas y versión 1.

**Resultado obtenido:** Pendiente.

## 5. Edición de una noticia propia

**Objetivo:** validar el camino autorizado de edición y el incremento de versión.

**Precondiciones:** sesión de `redactor1`; al menos una noticia cuyo autor sea Ana Torres.

**Pasos:**

1. Seleccionar la noticia propia y pulsar Editar.
2. Cambiar título, categoría y contenido con valores válidos.
3. Guardar y actualizar el listado.
4. Abrir de nuevo la noticia.

**Resultado esperado:** la edición se confirma, la modificación cambia, la creación se conserva y la versión aumenta exactamente en uno.

**Resultado obtenido:** Pendiente.

## 6. Intento de edición de una noticia ajena

**Objetivo:** confirmar autorización en interfaz y servidor.

**Precondiciones:** sesión de `redactor1`; noticia perteneciente a Luis Mendoza.

**Pasos:**

1. Seleccionar la noticia ajena.
2. Verificar el estado del botón Editar.
3. Si la interfaz permite enviar la acción, intentar guardar un cambio; alternativamente usar una prueba de integración para invocar el método con ese ID.
4. Actualizar y abrir la noticia original.

**Resultado esperado:** la GUI no habilita la edición ajena y el servidor rechaza cualquier invocación manipulada con `AutorizacionException`; la noticia no cambia.

**Resultado obtenido:** Pendiente.

## 7. Eliminación

**Objetivo:** validar confirmación, autorización y eliminación persistente.

**Precondiciones:** redactor autenticado con una noticia propia prescindible.

**Pasos:**

1. Seleccionar la noticia propia y pulsar Eliminar.
2. Cancelar una primera confirmación y comprobar que sigue presente.
3. Repetir y confirmar.
4. Actualizar desde el redactor y desde un lector.
5. Reiniciar el servidor y consultar otra vez.

**Resultado esperado:** cancelar no modifica datos; confirmar elimina una sola noticia; no aparece después de actualizar ni después del reinicio.

**Resultado obtenido:** Pendiente.

## 8. Búsqueda por palabra

**Objetivo:** verificar normalización y búsqueda insensible a mayúsculas en título y contenido.

**Precondiciones:** datos de ejemplo cargados.

**Pasos:**

1. Buscar `rmi`.
2. Buscar `  RMI  ` con espacios exteriores y mayúsculas.
3. Buscar una palabra que solo aparezca en contenido.
4. Buscar un término inexistente.

**Resultado esperado:** las dos primeras búsquedas devuelven el mismo conjunto; título y contenido participan; un término ausente muestra una lista vacía y un mensaje comprensible.

**Resultado obtenido:** Pendiente.

## 9. Filtro por categoría

**Objetivo:** comprobar categorías legibles y correspondencia con valores persistidos.

**Precondiciones:** noticias en Tecnología, Ciencia y Cultura.

**Pasos:**

1. Elegir Tecnología y verificar cada fila.
2. Repetir con Ciencia y Cultura.
3. Elegir una categoría sin resultados.
4. Pulsar Limpiar.

**Resultado esperado:** solo se muestran noticias de la categoría elegida, las etiquetas tienen acentos legibles, una categoría vacía no causa error y Limpiar recupera el listado general.

**Resultado obtenido:** Pendiente.

## 10. Apagado del servidor con clientes abiertos

**Objetivo:** verificar que una pérdida de comunicación no termina la interfaz.

**Precondiciones:** servidor activo; lector y redactor conectados.

**Pasos:**

1. Detener el servidor de forma normal.
2. Pulsar Actualizar en el lector.
3. Intentar una publicación en el redactor.
4. Observar estado, ventanas y datos mostrados.

**Resultado esperado:** ambas operaciones muestran un fallo de conexión; ninguna ventana se cierra ni queda congelada; la publicación no se afirma como exitosa y se ofrece reconexión.

**Resultado obtenido:** Pendiente.

## 11. Reinicio y reconexión

**Objetivo:** validar recuperación manual y persistencia después de una caída.

**Precondiciones:** escenario anterior completado; al menos una noticia creada antes del apagado.

**Pasos:**

1. Reiniciar el servidor con la misma ruta H2.
2. Abrir Reconectar en un cliente y conservar los datos anteriores.
3. Conectar y actualizar.
4. Intentar una escritura con la sesión anterior y luego iniciar sesión otra vez.

**Resultado esperado:** las noticias persisten; el token anterior ya no es válido; después de autenticarse nuevamente las acciones del redactor funcionan.

**Resultado obtenido:** Pendiente.

## 12. Conflicto de edición entre dos clientes

**Objetivo:** demostrar control optimista y ausencia de actualización perdida.

**Precondiciones:** dos clientes autenticados con el mismo redactor, una noticia propia y versión conocida.

**Pasos:**

1. Abrir la misma noticia para editar en ambos clientes antes de guardar.
2. Guardar un cambio desde el primer cliente.
3. Sin actualizar el segundo, guardar un cambio diferente.
4. Actualizar ambos y abrir la noticia.

**Resultado esperado:** el primero tiene éxito; el segundo recibe el mensaje de noticia modificada y debe actualizar; la base contiene exclusivamente el primer cambio y la versión aumentó una vez.

**Resultado obtenido:** Pendiente.

## 13. Credenciales incorrectas

**Objetivo:** validar rechazo seguro sin filtrar la existencia de usuarios ni registrar secretos.

**Precondiciones:** servidor y cliente conectados.

**Pasos:**

1. Probar `redactor1` con contraseña incorrecta.
2. Probar un usuario inexistente.
3. Dejar usuario o contraseña vacío.
4. Revisar el log del servidor.

**Resultado esperado:** no se crea sesión; la GUI muestra un mensaje breve; los campos vacíos se rechazan; el log registra intentos fallidos sin contraseña, hash ni token.

**Resultado obtenido:** Pendiente.

## 14. Sesión expirada

**Objetivo:** comprobar expiración autoritativa y recuperación de la interfaz.

**Precondiciones:** servidor configurado temporalmente con un timeout corto y redactor autenticado.

**Pasos:**

1. Confirmar que una publicación inicial funciona.
2. Esperar hasta superar el vencimiento configurado sin reiniciar procesos.
3. Intentar editar o publicar.
4. Iniciar sesión de nuevo y repetir la acción.

**Resultado esperado:** la primera acción posterior al vencimiento recibe `AutenticacionException`; la GUI limpia el estado de redactor; una sesión nueva permite continuar. La consulta pública sigue disponible.

**Resultado obtenido:** Pendiente.

## 15. Base de datos no disponible o ruta inválida

**Objetivo:** comprobar arranque seguro, rollback y mensajes de persistencia.

**Precondiciones:** ruta de prueba sin permisos o inválida; no usar ni alterar la base académica real.

**Pasos:**

1. Configurar `database.path` con la ruta de prueba inaccesible.
2. Intentar iniciar el servidor.
3. Restaurar una ruta válida e iniciar nuevamente.
4. Para probar fallo durante operación, usar únicamente un entorno desechable y provocar indisponibilidad controlada.
5. Revisar logs y consultar el estado final.

**Resultado esperado:** el servidor no publica un servicio si H2 no inicializa; registra la causa sin exponer credenciales; tras restaurar la ruta inicia normalmente; una escritura fallida no deja datos parciales.

**Resultado obtenido:** Pendiente.

## Registro de cierre

Al completar las pruebas debe agregarse un resumen con número de escenarios aprobados, fallidos y bloqueados, además de enlaces o nombres de evidencias. Un fallo debe conservar pasos de reproducción y mensaje exacto; no debe reemplazarse “Pendiente” por “Correcto” sin ejecución observable.
