# Practica distribuida con XML-RPC

## Resumen breve

Este repositorio contiene tres subproyectos Java:

- `servidorPrincipal`: expone tres categorias de servicios XML-RPC.
- `servidorReplica1`: mantiene una replica parcial de la categoria `inventario clinico`.
- `cliente`: cliente por consola con menu persistente.

La categoria replicada es `inventario clinico` porque:

- tiene lecturas sencillas y faciles de verificar en consola;
- su escritura es acotada y permite mostrar consistencia mutua de forma clara;
- facilita demostrar tolerancia a fallas sin introducir coordinacion distribuida compleja.

## Conmutacion por falla en el cliente

El cliente intenta todas las operaciones primero contra el servidor principal.

- Si el principal esta disponible, todas las categorias se atienden desde el principal.
- Si el principal no responde y la operacion pertenece a `InventarioClinico`, el cliente reintenta automaticamente contra la replica.
- Si el principal no responde y la operacion pertenece a `RedesSociales` o `MovilidadUrbana`, el cliente devuelve error porque esas categorias no se replican.

## Reconciliacion automatica del inventario

Para evitar divergencias cuando un nodo estuvo fuera de linea, el inventario
clinico ahora se usa dos mecanismos persistentes:

- cola de pendientes hacia el otro nodo;
- historial de operaciones remotas ya aplicadas para evitar duplicados.

Archivos relevantes:

- `servidorPrincipal/datos/inventario_clinico/operaciones_pendientes_hacia_replica.csv`
- `servidorPrincipal/datos/inventario_clinico/operaciones_aplicadas_desde_replica.csv`
- `servidorReplica1/datos/inventario_clinico/operaciones_pendientes_hacia_principal.csv`
- `servidorReplica1/datos/inventario_clinico/operaciones_aplicadas_desde_principal.csv`

Flujo:

- si el principal escribe y la replica no responde, la operacion queda en la cola del principal;
- si la replica atiende una escritura en contingencia, la operacion queda en la cola de la replica;
- antes de cada operacion de inventario, el nodo intenta reconciliar su cola;
- cuando el principal vuelve a recibir una peticion de inventario, tambien extrae lo que la replica tenga pendiente hacia el principal.

## Codigos de error

- `200_OK`: operacion completada correctamente.
- `207_REPLICA_PENDIENTE`: escritura local exitosa, pero la replica no se actualizo.
- `400_PARAMETRO_INVALIDO`: el usuario envio datos vacios, nulos o invalidos.
- `400_OPERACION_INVALIDA`: operacion de menu o flujo invalido.
- `404_METODO_NO_ENCONTRADO`: llamada RPC inexistente o mal resuelta.
- `404_RECURSO_NO_ENCONTRADO`: pais, usuario, zona, estacion o medicamento no existentes.
- `500_ERROR_EN_EL_SERVIDOR`: error inesperado en la logica del servidor.
- `500_ERROR_PERSISTENCIA`: problema al leer o escribir archivos CSV.
- `503_SERVIDOR_PRINCIPAL_NO_DISPONIBLE`: el cliente no puede alcanzar al principal.
- `503_SERVIDOR_REPLICA_NO_DISPONIBLE`: el principal no puede alcanzar a la replica.
- `207_SINCRONIZACION_PENDIENTE_CON_PRINCIPAL`: la replica aplico el cambio, pero aun no ha podido entregarlo al principal.

## Rutas de datasets

- `servidorPrincipal/datos/redes_sociales`
- `servidorPrincipal/datos/movilidad_urbana`
- `servidorPrincipal/datos/inventario_clinico`
- `servidorReplica1/datos/inventario_clinico`

## Referencia rapida de valores validos

Estos textos ya existen en los datasets actuales y sirven como referencia
para capturar entradas validas durante la demostracion.

### Paises validos para tendencias y publicaciones

- Mexico
- Colombia
- Argentina
- Chile
- Peru
- Espana
- Estados Unidos
- Canada
- Brasil
- Uruguay

### Usuarios validos de redes sociales

- ana_datos
- bruno_dev
- carla_viaja
- diego_rutas
- elena_salud
- fabian_urbano
- gabriela_cafe
- hector_metro
- irene_bicis
- julio_trafico

### Zonas validas para trafico e incidentes

- Centro Historico
- Zona Norte
- Corredor Universitario
- Distrito Financiero
- Anillo Periferico
- Terminal Oriente
- Hospitales Sur
- Parque Industrial
- Azcapotzalco
- Aeropuerto Poniente

### Estaciones validas de eco-bicicletas

- Plaza Mayor
- Universidad Central
- Mercado Norte
- Parque del Lago
- Hospital General
- Terminal Sur
- Museo de Arte
- Distrito Digital
- Jardin Botanico
- Estadio Metropolitano

### Medicamentos validos del inventario clinico

- MED-001 | Paracetamol 500mg
- MED-002 | Ibuprofeno 400mg
- MED-003 | Amoxicilina 500mg
- MED-004 | Suero oral 500ml
- MED-005 | Omeprazol 20mg
- MED-006 | Insulina rapida
- MED-007 | Jeringa 5ml
- MED-008 | Venda elastica
- MED-009 | Alcohol antiseptico
- MED-010 | Cubrebocas quirurgico

## Ejemplos de errores demostrables

- Consulta de pais inexistente: `Redes sociales -> Consultar tendencias -> "Islandia"` devuelve `404_RECURSO_NO_ENCONTRADO`.
- Cantidad invalida: `Inventario clinico -> Registrar entrada -> 0` genera `400_PARAMETRO_INVALIDO`.
- Replica apagada: detiene `servidorReplica1` y luego registra una entrada. El principal responde `207_REPLICA_PENDIENTE`.

