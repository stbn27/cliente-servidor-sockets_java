# Decisiones técnicas

## Maven sobre Ant

Maven aporta un modelo declarativo estándar para los tres módulos, resolución reproducible de dependencias, ciclo de compilación/pruebas/empaquetado y plugins conocidos para JUnit y JAR ejecutables. La relación `server/client -> common` queda expresada como dependencia, no como rutas manuales a clases.

Ant permitiría controlar cada tarea, pero exigiría definir explícitamente compilación, classpaths, pruebas y ensamblado. Esa flexibilidad no aporta valor a una práctica pequeña y haría más difícil reproducir el resultado en otro equipo.

## H2 sobre archivos JSON

H2 proporciona transacciones, claves foráneas, restricciones, índices, identidad y actualizaciones condicionales. Estas capacidades son centrales para demostrar consistencia y control optimista. También permite una base persistente en el servidor y bases en memoria aisladas para pruebas.

Un archivo JSON requeriría implementar bloqueo, escritura atómica, índices, relaciones, recuperación ante archivos truncados y coordinación concurrente. Ese código desviaría la práctica de RMI y sería menos robusto. H2 sigue siendo embebida, por lo que no agrega un servicio externo.

## JDBC sin ORM

El esquema tiene dos entidades de negocio y consultas directas. JDBC con `PreparedStatement` hace visibles transacciones, número de filas actualizado y condición de versión. Un ORM añadiría configuración y comportamiento implícito sin reducir de manera importante el código. Además, Hibernate está fuera del alcance tecnológico solicitado.

## Swing sobre aplicación web

Swing está incluido en Java y permite que cliente y servidor compartan modelos y contrato RMI sin introducir HTTP, JavaScript ni un servidor web. `SwingWorker` proporciona el límite necesario entre llamadas de red y el Event Dispatch Thread.

Una interfaz web exigiría otra capa de transporte porque el navegador no consume RMI directamente. Eso ocultaría el objetivo distribuido bajo REST u otra tecnología y ampliaría innecesariamente el despliegue.

## Un único cliente con roles

Lectores y redactores necesitan la misma conexión, tabla, búsqueda, filtro y vista de detalle. Un solo módulo evita duplicar interfaz y manejo de errores. Tras autenticar, la ventana habilita publicar y limita editar/eliminar a noticias del autor.

La seguridad no depende de esa visibilidad. El servidor comprueba token y propietario en cada escritura. El lector es anónimo, por lo que consultar no requiere una sesión artificial; cualquier escritura sin sesión válida falla.

## Control optimista sobre bloqueo global

Una persona puede mantener abierto un formulario durante minutos. Bloquear una fila o todo el servicio mientras edita consumiría recursos y afectaría lectores u otros redactores. El control optimista supone que el conflicto es poco frecuente y lo detecta al confirmar mediante el campo `version`.

El resultado es mayor concurrencia y una semántica explícita: un cliente obsoleto recibe conflicto. No se sobrescribe silenciosamente y tampoco se necesita mantener estado de edición en el servidor.

## Registry creado por el servidor

El proceso intenta crear el RMI Registry mediante `LocateRegistry.createRegistry`. Si ya existe uno válido, lo reutiliza. Así la práctica no depende de ejecutar `rmiregistry` con un classpath correcto y reduce los pasos externos que suelen causar errores académicos.

El nombre del servicio y el puerto siguen siendo configurables. Crear el Registry en proceso no elimina la necesidad de exponer su puerto en el firewall.

## Puerto fijo para el objeto remoto

Registry y objeto remoto usan puertos configurables separados, predeterminadamente 1099 y 1100. Permitir que el objeto use un puerto efímero funciona localmente, pero complica redes entre equipos. Un puerto fijo permite reglas de firewall pequeñas y diagnósticos reproducibles.

## Arquitectura modular sin frameworks

`common` contiene únicamente la frontera compartida. `server` separa bootstrap, configuración, base, repositorios, servicios y fachada remota. `client` separa conexión, modelos Swing y diálogos. Esta estructura distribuye responsabilidades sin crear una interfaz por cada clase ni un contenedor de inyección.

Las dependencias se construyen explícitamente en el bootstrap. Para el tamaño actual, esto es fácil de leer y probar. Incorporar Spring Boot, Jakarta EE o un framework de DI ampliaría el producto sin resolver una necesidad real.

## DTO inmutables

Los objetos que cruzan RMI se construyen completos y no exponen setters. Esto reduce cambios accidentales, facilita razonar sobre qué versión vio el cliente y evita confundir un DTO local modificado con un cambio persistido. La base sigue siendo la fuente autoritativa.

`Autor` compartido no contiene `passwordHash`. El servidor conserva un tipo interno para autenticación. `Sesion` expone token, identidad visible, rol y expiración, pero ningún secreto derivado de contraseña.

## Excepciones checked de dominio

La interfaz declara errores recuperables que el cliente debe presentar: autenticación, autorización, validación, noticia ausente, conflicto y servicio no disponible. `RemoteException` se reserva para transporte. Esta separación produce mensajes precisos y obliga al cliente a considerar fallos distribuidos.

Las causas SQL se registran localmente y no se adjuntan a una excepción enviada si pueden revelar detalles internos.

## BCrypt

BCrypt incorpora sal y un costo de trabajo configurable, y evita diseñar criptografía propia. Las semillas académicas se transforman antes de insertarse y la comparación se hace contra el hash almacenado. Las credenciales documentadas no son apropiadas para producción.

## Sesiones en memoria

Un `ConcurrentHashMap` es suficiente para un solo servidor y demuestra estado compartido seguro. Los tokens son aleatorios y expiran. Persistir o distribuir sesiones complicaría la práctica; la consecuencia aceptada es que un reinicio obliga a volver a iniciar sesión.

## Actualización manual sin callbacks

El botón Actualizar conserva un flujo predecible y evita que un callback modifique la tabla mientras existe un formulario de edición. También reduce contratos remotos bidireccionales, puertos adicionales y gestión de clientes desconectados. En una evolución podrían añadirse notificaciones, pero no son necesarias para demostrar RMI básico.

## Configuración por propiedades, entorno y argumentos

Los predeterminados viven en `server.properties`; variables y argumentos `--clave=valor` permiten adaptar la red sin recompilar. La implementación usa `Properties` y Java estándar. Se valida host, puertos, ruta y duración antes de arrancar.

## Logging con SLF4J y Logback

La fachada SLF4J evita usar `System.out` como mecanismo principal y Logback proporciona formato y niveles sin un servidor adicional. Los datos sensibles se excluyen deliberadamente. En producción se preferirían logs estructurados y centralizados.
