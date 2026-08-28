# Especificación de Requisitos — Sistema de Inventario Multi-Sucursal
### Prueba Técnica OptiPlant Consultores — Etapa 1: Requisitos, Casos de Uso e Historias de Usuario

> [!info] Convenciones de este documento
> - 🟢 **EXPLÍCITO EN EL PDF** — aparece literalmente en el documento.
> - 🟡 **INFERIDO** — se deduce necesariamente de lo explícito para que el sistema sea coherente.
> - 🔵 **RECOMENDACIÓN** — mejora de calidad, no obligatoria según el PDF.
> - 🔴 **DECISIÓN PENDIENTE** — el documento lo deja abierto; requiere que tú decidas.
> - No se ha diseñado código, entidades de BD, endpoints, frontend ni Docker Compose. Esta etapa es solo especificación.

---

## PARTE 1 — Comprensión del documento

### 1.1 Objetivo
🟢 Diseñar y desarrollar una aplicación robusta para gestión de inventario de **múltiples sucursales** de una misma organización. La evaluación no es solo funcional: también pesan diseño, arquitectura, documentación y uso inteligente de IA. Principio rector: cada decisión debe responder "¿por qué se hizo así?".

### 1.2 Alcance / Problema
🟢 Cada sucursal opera con **autonomía operativa** (transacciones locales independientes) pero debe existir **visibilidad compartida y coherencia** de inventario entre sucursales, con sincronización "tiempo real o near-real-time", consulta cruzada de inventario, y solicitud/recepción de transferencias entre sucursales.

### 1.3 Módulos (sección 3)
🟢 Inventario (CRUD), Compras, Ventas, Transferencias entre sucursales, Tiempos de envío/Logística, Dashboard. Sección 4: al menos **una funcionalidad adicional** de valor real (alertas, predicción de demanda, proveedores, caducidad, auditoría, reportes exportables — ideas orientadoras, no limitantes).

### 1.4 Actores (sección 6.2)
🟢 Administrador general, Gerente de sucursal, Operador de inventario, Sistema externo (opcional).

### 1.5 Restricciones (sección 5 y 8)
🟢 Arquitectura mínima de 3 capas (frontend/backend/BD) separadas; comunicación exclusivamente por API (REST o GraphQL), **sin lógica de negocio en el cliente**; contenedorización total con Docker Compose y **un solo comando**, sin configuración manual; stack tecnológico libre pero justificado; documentación obligatoria de: lenguaje de backend, motor de BD y modelo de datos, estrategia de autenticación/autorización, mecanismo de sincronización entre sucursales, patrones de diseño usados.

### 1.6 Elementos que condicionan directamente la sección 6
🟡 Aunque la sección 6 solo pide "requerimientos, casos de uso, historias de usuario", su contenido real está determinado por:
- Los 6 módulos de la sección 3 (definen el universo de RF).
- La sección 5 (define RNF de arquitectura/portabilidad y restricciones técnicas transversales).
- La sección 7 (exige diagramas de casos de uso, actividades, arquitectura y E-R → los casos de uso de la sección 6.2 deben ser suficientemente completos para alimentar esos diagramas).
- La sección 8 (exige justificar sincronización multi-sucursal → obliga a que los RNF de consistencia/concurrencia sean explícitos desde el levantamiento).
- La sección 9 (uso de IA) no genera RF de negocio, pero sí un requisito de **documentación de proceso** (evidencia de prompts, evaluación crítica) que debe quedar trazado como entregable, no como funcionalidad del sistema.
- La sección 10 (entregables) y 12 (orden de trabajo) confirman que el resultado de la sección 6 es un **insumo de entrada** para las secciones 7 y 8, no un documento aislado.

---

## PARTE 2 — Análisis de la Sección 6

🟢 El documento exige literalmente tres subsecciones: 6.1 Levantamiento de Requerimientos (RF, RNF, restricciones, supuestos/dependencias), 6.2 Casos de Uso (actores + interacciones principales, con la tabla de 4 actores ya dada), 6.3 Historias de Usuario (recomendadas, no obligatorias, con 3 ejemplos dados como ilustración, no como lista cerrada).

🟡 Para que una implementación sea "profesional" en el sentido que pide el documento (sección 1: calidad de diseño + justificación), la sección 6 debe ir más allá de listar viñetas: debe convertir cada capacidad de los módulos 3.1–3.6 en requisitos **atómicos, verificables y trazables**, cubrir los RNF con criterios medibles (no "debe ser seguro"), dejar explícitas las reglas de negocio implícitas en los flujos de transferencia/venta/compra (porque esos flujos son los que más se prestan a errores de concurrencia y consistencia, que es justamente lo que la sección 8.2 pide justificar), y dejar un registro explícito de ambigüedades y decisiones pendientes, de modo que la sección 8 (arquitectura) pueda partir de una base cerrada.

🔵 Recomendación: tratar la sección 6 como el "contrato" del proyecto — todo lo que aparezca en el README, en los diagramas y en el código debe poder rastrearse hasta un ítem de esta sección.

---

## PARTE 3 — Requerimientos Funcionales

> [!note] Notación de tabla
> Fuente = sección del PDF. Tipo: 🟢/🟡/🔵. Prioridad: Crítica/Alta/Media/Baja (ver PARTE 17 para justificación agregada por MoSCoW).

### 3.1 Autenticación y Usuarios
| ID | Requerimiento | Actor(es) | Prioridad | Fuente | Tipo |
|---|---|---|---|---|---|
| RF-001 | El sistema debe permitir el inicio de sesión mediante credenciales (usuario/contraseña) | Todos | Crítica | §8.2 (autenticación) | 🟡 |
| RF-002 | El sistema debe asignar a cada usuario exactamente un rol entre {Administrador, Gerente, Operador} | Administrador | Crítica | §6.2 | 🟡 |
| RF-003 | El sistema debe asociar cada usuario (Gerente/Operador) a una sucursal específica | Administrador | Crítica | §2, §6.2 | 🟡 |
| RF-004 | El Administrador debe poder crear, editar, desactivar y reactivar usuarios | Administrador | Alta | §6.2 (gestión de usuarios) | 🟡 |
| RF-005 | El sistema debe cerrar sesión y expirar tokens/sesiones inactivas | Todos | Alta | §8.2 | 🔵 |
| RF-006 | El sistema debe registrar el usuario responsable en toda operación de escritura (ingreso, retiro, venta, compra, transferencia) | Sistema | Crítica | §3.1 (trazabilidad) | 🟢 |

### 3.2 Sucursales
| ID | Requerimiento | Actor(es) | Prioridad | Fuente | Tipo |
|---|---|---|---|---|---|
| RF-007 | El Administrador debe poder crear, editar y desactivar sucursales | Administrador | Alta | §6.2 | 🟡 |
| RF-008 | El sistema debe permitir consultar el listado de sucursales activas de la red | Todos | Media | §2.1 | 🟡 |
| RF-009 | El sistema debe restringir la visibilidad de operaciones de escritura (venta, ingreso, retiro) a la sucursal del usuario, salvo Administrador | Sistema | Crítica | §2.1, §2 | 🟡 |

### 3.3 Productos e Inventario
| ID | Requerimiento | Actor(es) | Prioridad | Fuente | Tipo |
|---|---|---|---|---|---|
| RF-010 | El sistema debe permitir crear un producto (catálogo) con nombre, descripción, categoría y unidad de medida base | Administrador/Operador¹ | Crítica | §3.1 | 🟢 |
| RF-011 | El sistema debe permitir editar los datos de un producto existente | Administrador/Operador¹ | Alta | §3.1 | 🟢 |
| RF-012 | El sistema debe permitir eliminar (baja lógica) un producto | Administrador | Media | §3.1 (CRUD completo) | 🟡 |
| RF-013 | El sistema debe permitir consultar el catálogo de productos disponibles en la sucursal propia | Todos | Crítica | §3.1 | 🟢 |
| RF-014 | El sistema debe permitir consultar el inventario (existencias) de cualquier otra sucursal de la red | Todos | Crítica | §3.1, §2.1 | 🟢 |
| RF-015 | El sistema debe registrar el ingreso de productos por compra | Operador | Crítica | §3.1 | 🟢 |
| RF-016 | El sistema debe registrar el ingreso de productos por devolución | Operador | Alta | §3.1 | 🟢 |
| RF-017 | El sistema debe registrar el ingreso de productos por ajuste de inventario | Operador/Gerente | Alta | §3.1 | 🟢 |
| RF-018 | El sistema debe registrar el retiro de productos por venta | Operador | Crítica | §3.1 | 🟢 |
| RF-019 | El sistema debe registrar el retiro de productos por merma | Operador | Alta | §3.1 | 🟢 |
| RF-020 | El sistema debe registrar el retiro de productos por ajuste de inventario | Operador/Gerente | Alta | §3.1 | 🟢 |
| RF-021 | El sistema debe permitir definir un stock mínimo por producto y sucursal | Administrador/Gerente | Alta | §3.1 | 🟢 |
| RF-022 | El sistema debe generar una alerta cuando el stock de un producto caiga por debajo de su mínimo definido | Sistema | Alta | §3.1 | 🟢 |
| RF-023 | El sistema debe permitir gestionar múltiples unidades de medida por producto | Administrador | Media | §3.1 | 🟢 |
| RF-024 | Todo ingreso o retiro de inventario debe registrar fecha, responsable, motivo y cantidad | Sistema | Crítica | §3.1 (nota de trazabilidad) | 🟢 |
| RF-025 | El sistema debe impedir que un retiro deje el stock en un valor negativo, salvo que se defina explícitamente lo contrario (ver SUP-013) | Sistema | Crítica | §15 (implícito), inferido de "control de stock" | 🟡 |

¹ 🔴 Decisión pendiente: si el Operador puede crear/editar productos del catálogo o solo Administrador/Gerente los define y el Operador solo opera sobre existencias (ver PARTE 15, AMB-004).

### 3.4 Compras
| ID | Requerimiento | Actor(es) | Prioridad | Fuente | Tipo |
|---|---|---|---|---|---|
| RF-026 | El sistema debe permitir crear un proveedor con datos básicos de contacto | Operador/Administrador | Media | §3.2, §4 (gestión de proveedores) | 🟡 |
| RF-027 | El sistema debe permitir crear una orden de compra asociada a un proveedor y una sucursal | Operador | Crítica | §3.2 | 🟢 |
| RF-028 | La orden de compra debe registrar, por línea de producto: cantidad, precio unitario, descuento | Operador | Crítica | §3.2 | 🟢 |
| RF-029 | La orden de compra debe registrar condiciones de pago (plazo) | Operador | Media | §3.2 | 🟢 |
| RF-030 | El sistema debe permitir confirmar la recepción (total o parcial) de una orden de compra | Operador | Crítica | §3.2 | 🟡 |
| RF-031 | Al confirmar la recepción de una compra, el sistema debe actualizar automáticamente el inventario de la sucursal | Sistema | Crítica | §3.2 | 🟢 |
| RF-032 | El sistema debe calcular el costo promedio ponderado del inventario al recibir una compra | Sistema | Crítica | §3.2 | 🟢 |
| RF-033 | El sistema debe permitir consultar el histórico de compras por proveedor | Operador/Gerente | Media | §3.2 | 🟢 |
| RF-034 | El sistema debe permitir consultar el histórico de compras por producto | Operador/Gerente | Media | §3.2 | 🟢 |

### 3.5 Ventas
| ID | Requerimiento | Actor(es) | Prioridad | Fuente | Tipo |
|---|---|---|---|---|---|
| RF-035 | El sistema debe permitir registrar una venta con producto, cantidad y precio | Operador | Crítica | §3.3 | 🟢 |
| RF-036 | El sistema debe asociar cada venta a una sucursal, fecha y responsable | Sistema | Crítica | §3.3 | 🟢 |
| RF-037 | El sistema debe validar la disponibilidad de stock antes de confirmar una venta | Sistema | Crítica | §3.3 | 🟢 |
| RF-038 | El sistema debe permitir aplicar descuentos sobre una venta o línea de venta | Operador | Media | §3.3 | 🟢 |
| RF-039 | El sistema debe permitir gestionar (definir/seleccionar) diferentes listas de precios | Administrador/Gerente | Media | §3.3 | 🟢 |
| RF-040 | El sistema debe generar un comprobante o registro de venta consultable posteriormente | Sistema | Alta | §3.3 | 🟢 |
| RF-041 | Al confirmar una venta, el sistema debe descontar automáticamente el stock de la sucursal | Sistema | Crítica | §3.3 (implícito) | 🟡 |

### 3.6 Transferencias entre Sucursales
| ID | Requerimiento | Actor(es) | Prioridad | Fuente | Tipo |
|---|---|---|---|---|---|
| RF-042 | El sistema debe permitir crear una solicitud de transferencia indicando producto, cantidad, sucursal origen y destino | Operador/Administrador | Crítica | §3.4 (paso 1) | 🟢 |
| RF-043 | El sistema debe permitir indicar un nivel de urgencia/prioridad en la solicitud de transferencia | Operador | Media | §6.3 (HU ejemplo) | 🟢 |
| RF-044 | La sucursal origen debe poder revisar la disponibilidad y confirmar o ajustar la cantidad a enviar | Operador (origen) | Crítica | §3.4 (paso 2) | 🟢 |
| RF-045 | El sistema debe permitir aprobar o rechazar una solicitud de transferencia | Gerente | Crítica | §6.2 (Gerente aprueba transferencias) | 🟢 |
| RF-046 | El sistema debe permitir registrar el despacho de una transferencia con fecha estimada de llegada y transportista | Operador (origen) | Alta | §3.4 (paso 3) | 🟢 |
| RF-047 | El sistema debe permitir confirmar la recepción completa de una transferencia | Operador (destino) | Crítica | §3.4 (paso 4) | 🟢 |
| RF-048 | Al confirmar recepción completa, el sistema debe actualizar automáticamente el inventario de la sucursal destino | Sistema | Crítica | §3.4 (paso 4) | 🟢 |
| RF-049 | El sistema debe permitir confirmar una recepción parcial, registrando la diferencia (faltante) | Operador (destino) | Crítica | §3.4 (paso 5) | 🟢 |
| RF-050 | Ante una recepción parcial, el sistema debe generar una alerta | Sistema | Alta | §3.4 (paso 5) | 🟢 |
| RF-051 | Ante una recepción parcial, el sistema debe permitir definir el tratamiento: reenvío, ajuste o reclamación | Gerente/Operador | Alta | §3.4 (paso 5) | 🟢 |
| RF-052 | El sistema debe permitir consultar el estado de cada transferencia (solicitada, en preparación, aprobada, en tránsito, recibida, con faltantes, cancelada)² | Todos (según sucursal) | Alta | §3.5 (visualizar estado) | 🟡 |
| RF-053 | El sistema debe reservar/comprometer el stock de origen al aprobar la transferencia, evitando su venta antes del despacho³ | Sistema | Alta | Inferido de consistencia multi-sucursal | 🟡 |
| RF-054 | El sistema debe permitir cancelar una solicitud de transferencia mientras no haya sido despachada | Gerente/Administrador | Media | Inferido (flujo crítico B, §14 del prompt) | 🔵 |

² 🔴 El PDF menciona 4 estados explícitos (preparación, tránsito, recibido, con faltantes); "solicitada/aprobada/cancelada" son inferidos para que el flujo sea completo — ver AMB-006.
³ 🔴 Decisión pendiente: si el bloqueo de stock ocurre en la solicitud, en la aprobación o en el despacho — ver AMB-002.

### 3.7 Logística
| ID | Requerimiento | Actor(es) | Prioridad | Fuente | Tipo |
|---|---|---|---|---|---|
| RF-055 | El sistema debe registrar el tiempo estimado de entrega de una transferencia | Operador | Media | §3.5 | 🟢 |
| RF-056 | El sistema debe registrar el tiempo real de entrega al confirmarse la recepción | Sistema | Media | §3.5 | 🟢 |
| RF-057 | El sistema debe permitir clasificar rutas por prioridad, costo o tiempo | Operador/Gerente | Baja | §3.5 | 🟢 |
| RF-058 | El sistema debe generar reportes de cumplimiento logístico por sucursal y por ruta | Gerente/Administrador | Media | §3.5 | 🟢 |

### 3.8 Dashboard
| ID | Requerimiento | Actor(es) | Prioridad | Fuente | Tipo |
|---|---|---|---|---|---|
| RF-059 | El dashboard debe mostrar el volumen de ventas del mes en curso vs. meses anteriores | Gerente/Administrador | Alta | §3.6 | 🟢 |
| RF-060 | El dashboard debe mostrar rotación de inventario y productos de alta/baja demanda | Gerente/Administrador | Alta | §3.6 | 🟢 |
| RF-061 | El dashboard debe mostrar el estado de las transferencias activas y su impacto en inventario | Gerente/Administrador | Alta | §3.6 | 🟢 |
| RF-062 | El dashboard debe mostrar indicadores de productos próximos a agotarse | Gerente/Administrador/Operador | Alta | §3.6 | 🟢 |
| RF-063 | El dashboard debe mostrar comparativa de rendimiento entre sucursales, visible solo para perfiles administrativos | Administrador | Alta | §3.6 | 🟢 |
| RF-064 | El sistema debe restringir el acceso a la comparativa entre sucursales según el rol del usuario | Sistema | Crítica | §3.6 | 🟢 |

### 3.9 Funcionalidad adicional (mínimo una, sección 4)
| ID | Requerimiento | Actor(es) | Prioridad | Fuente | Tipo |
|---|---|---|---|---|---|
| RF-065 | El sistema debe implementar al menos una funcionalidad adicional de valor real, a elegir libremente por el desarrollador entre las sugeridas u otra justificada | Todos | Crítica (obligatoria su existencia; libre su contenido) | §4 | 🟢 |

🟡 Nota: la elección concreta (alertas, predicción de demanda, proveedores, caducidad, auditoría o reportes exportables) es una decisión abierta (ver PARTE 19), no un requisito específico — el PDF las presenta como "ideas orientadoras, no limitantes".

---

## PARTE 4 — Requerimientos No Funcionales

| ID | Categoría | Requerimiento (verificable) | Fuente | Tipo |
|---|---|---|---|---|
| RNF-001 | Rendimiento | Las consultas de inventario propio deben responder en ≤ 1 s bajo carga normal (a definir: nº usuarios concurrentes) | §6.1 (implícito) | 🔵 |
| RNF-002 | Rendimiento | La carga del dashboard no debe superar 2–3 s con datos de una sucursal | §6.1 | 🔵 |
| RNF-003 | Rendimiento | El sistema debe soportar operaciones concurrentes de venta/ingreso sin pérdida de actualizaciones (sin "lost update") | §8.2 (sincronización) | 🟡 |
| RNF-004 | Rendimiento | La sincronización de inventario entre sucursales debe reflejarse en un plazo definido y documentado (ver AMB-001) | §2.1 ("tiempo real o near-real-time") | 🟢/🔴 |
| RNF-005 | Seguridad | Todo acceso al sistema debe requerir autenticación | §8.2 | 🟡 |
| RNF-006 | Seguridad | El sistema debe implementar autorización basada en roles (RBAC) para las operaciones descritas en la matriz de permisos (PARTE 9) | §8.2 | 🟡 |
| RNF-007 | Seguridad | El acceso a datos de una sucursal debe estar restringido según el rol y la sucursal del usuario | §2.1, §6.2 | 🟡 |
| RNF-008 | Seguridad | Las contraseñas deben almacenarse cifradas (hash con salt), nunca en texto plano | Estándar de industria | 🔵 |
| RNF-009 | Seguridad | Las sesiones/tokens deben tener expiración y mecanismo de renovación | §8.2 | 🔵 |
| RNF-010 | Seguridad | Toda entrada de usuario debe validarse en backend (no confiar en validación de frontend) | §5 (no lógica de negocio en cliente) | 🟢 |
| RNF-011 | Seguridad | La API debe protegerse contra accesos no autorizados (autenticación en cada endpoint) | §8.1 | 🟡 |
| RNF-012 | Escalabilidad | El modelo de datos debe soportar el alta de nuevas sucursales sin cambios estructurales | §2 | 🟡 |
| RNF-013 | Escalabilidad | El modelo de datos debe soportar crecimiento del catálogo de productos y del volumen de movimientos sin degradar el diseño | Inferido | 🔵 |
| RNF-014 | Disponibilidad | El sistema debe manejar errores de comunicación entre módulos/servicios sin pérdida de datos (transacciones atómicas en operaciones críticas) | §14 del prompt / §8.2 | 🟡 |
| RNF-015 | Disponibilidad | Ante fallo de un servicio (p. ej. BD caída), el sistema debe fallar de forma controlada y comunicar el error al usuario | Buenas prácticas | 🔵 |
| RNF-016 | Consistencia | El descuento de stock en origen y el incremento en destino durante una transferencia deben ser consistentes (no debe perderse ni duplicarse cantidad) | §3.4, §8.2 | 🟡 |
| RNF-017 | Consistencia | Dos operaciones concurrentes sobre el mismo producto/sucursal no deben producir un estado de inventario inconsistente | §8.2 (sincronización) | 🟡 |
| RNF-018 | Consistencia | El costo promedio ponderado debe recalcularse de forma consistente ante ingresos concurrentes | §3.2 | 🟡 |
| RNF-019 | Usabilidad | La interfaz debe ser responsiva (adaptable a distintos tamaños de pantalla) | §8.1 | 🟢 |
| RNF-020 | Usabilidad | El sistema debe dar retroalimentación clara ante errores de validación (stock insuficiente, campos obligatorios, etc.) | Buenas prácticas | 🔵 |
| RNF-021 | Usabilidad | La navegación entre módulos debe ser consistente y accesible desde un menú principal | Buenas prácticas | 🔵 |
| RNF-022 | Mantenibilidad | El backend debe separar responsabilidades (capas de presentación de API, lógica de negocio, acceso a datos) | §8.1, §8.2 (patrones) | 🟢 |
| RNF-023 | Mantenibilidad | El código debe estar organizado, comentado y libre de archivos innecesarios (.env, node_modules) en el repositorio | §10 | 🟢 |
| RNF-024 | Mantenibilidad | El proyecto debe incluir pruebas automatizadas para los módulos críticos (compras, ventas, transferencias) | §9.1 (generación de tests) | 🟡 |
| RNF-025 | Observabilidad | El sistema debe mantener trazabilidad completa de movimientos de inventario (fecha, responsable, motivo, cantidad) | §3.1 (nota) | 🟢 |
| RNF-026 | Observabilidad | Se recomienda registrar logs de operaciones críticas (compras, ventas, transferencias, cambios de stock) | Inferido / §4 (auditoría sugerida) | 🔵 |
| RNF-027 | Observabilidad | Se recomienda exponer métricas básicas de uso/errores para monitoreo | Buenas prácticas | 🔵 |
| RNF-028 | Portabilidad/Despliegue | Todo el sistema debe poder levantarse con un único comando (`docker compose up`) sin configuración manual adicional | §5, §8.1 | 🟢 |
| RNF-029 | Portabilidad/Despliegue | Cada capa (frontend, backend, BD) debe ejecutarse en un contenedor/servicio aislado dentro de Docker Compose | §8.1 | 🟢 |
| RNF-030 | Portabilidad/Despliegue | El repositorio debe incluir instrucciones de configuración inicial (README) | §10 | 🟢 |

---

## PARTE 5 — Restricciones

### 5.1 Restricciones técnicas 🟢
- Arquitectura mínima de 3 capas separadas: frontend, backend, base de datos (§5, §8.1).
- Comunicación frontend↔backend exclusivamente por API (REST o GraphQL); **prohibida** la lógica de negocio en el cliente (§5).
- Ejecución completa con un único comando vía Docker Compose; sin configuración manual en el entorno local (§5).
- Stack tecnológico libre, pero cada elección debe estar justificada (§5, §8.2).
- BD relacional o NoSQL, decisión libre pero justificada (§8.1).

### 5.2 Restricciones de negocio 🟢
- Multi-sucursal: cada sucursal tiene autonomía operativa en sus transacciones locales.
- Visibilidad compartida: cualquier sucursal puede consultar el inventario de cualquier otra.
- Trazabilidad obligatoria de todo movimiento de inventario.
- El flujo de transferencias debe cubrir el ciclo completo: solicitud → preparación → despacho → recepción (completa o parcial).

### 5.3 Restricciones de evaluación 🟢
- README completo (descripción, instalación, arquitectura, módulos, decisiones de diseño).
- Diagramas obligatorios: casos de uso, actividades/flujo (transferencia y venta), arquitectura, entidad-relación.
- Evidencia documentada de uso de IA: herramientas usadas, prompts y resultados, evaluación crítica, % estimado de contenido generado con IA.
- Repositorio Git público con historial de commits representativo del proceso.
- Justificación explícita de: lenguaje de backend, motor de BD y modelo de datos, autenticación/autorización, sincronización entre sucursales, patrones de diseño.

---

## PARTE 6 — Supuestos

| ID | Supuesto | Razón | Impacto | ¿Confirmar antes de desarrollar? |
|---|---|---|---|---|
| SUP-001 | Existe una única organización (no se modela multi-tenant entre organizaciones distintas) | El PDF habla de "una misma organización" | Simplifica el modelo de datos (no requiere tabla de "organización" con aislamiento estricto) | Sí, aunque de bajo riesgo |
| SUP-002 | Un usuario pertenece exactamente a una sucursal (excepto Administrador, que ve todas) | §6.2 no menciona usuarios multi-sucursal | Simplifica autorización; si es falso, cambia el modelo de permisos | Sí |
| SUP-003 | El catálogo de productos es global (mismo producto puede existir con distinto stock por sucursal); el stock sí es por sucursal | §3.1 pide "catálogo de productos disponibles en la sucursal propia" pero también consulta cruzada — coherente con catálogo compartido + stock local | Afecta el modelo E-R (producto global, tabla de existencias por sucursal) | Sí — es una decisión estructural clave |
| SUP-004 | El stock de origen se descuenta en el momento del **despacho**, no en la solicitud ni en la aprobación | El PDF no lo define; es la interpretación más consistente con "preparación del envío: revisa disponibilidad" | Afecta lógica de reservas y concurrencia | Sí (ver AMB-002) |
| SUP-005 | El stock de destino aumenta al confirmar la **recepción** (completa o parcial, según cantidad recibida) | Explícito en §3.4 paso 4 | Ninguno — ya está definido | No |
| SUP-006 | Durante el tránsito, el producto no aparece como stock disponible en ninguna de las dos sucursales (está "en tránsito") | Inferido para evitar doble conteo | Requiere un estado/columna de cantidad "en tránsito" | Sí |
| SUP-007 | El Gerente aprueba transferencias que llegan a **o salen de** su propia sucursal, no ambas necesariamente | §6.2 dice "aprueba transferencias" sin especificar dirección | Afecta reglas de autorización | Sí (ver AMB-003) |
| SUP-008 | Sí puede existir recepción parcial, con registro de faltante y definición de tratamiento | Explícito en §3.4 paso 5 | Ninguno | No |
| SUP-009 | El costo promedio ponderado se recalcula con cada recepción de compra: `nuevo_costo = (stock_actual × costo_actual + cantidad_ingresada × precio_compra) / (stock_actual + cantidad_ingresada)` | Fórmula estándar de costeo; el PDF solo dice "calcular el costo promedio ponderado" sin fórmula | Afecta cálculo financiero mostrado en dashboard y reportes | Sí — confirmar fórmula exacta |
| SUP-010 | Las ventas **no** pueden generar stock negativo (se bloquean si no hay stock suficiente) | §3.3 "validar disponibilidad de stock antes de confirmar la venta" | Afecta RF-025, RF-037 | No, ya está explícito |
| SUP-011 | Ante modificación simultánea del mismo inventario por dos usuarios, se requiere control de concurrencia (p. ej. bloqueo optimista o transacciones a nivel de BD) | El PDF exige "sincronización" en §8.2 sin especificar mecanismo | Decisión de arquitectura, no solo de requisitos | Sí (se resuelve en la etapa de arquitectura, pero debe quedar como RNF) |
| SUP-012 | Pueden coexistir varias unidades de medida por producto, con una unidad "base" para el stock y factores de conversión | §3.1 "gestionar múltiples unidades de medida por producto" | Afecta modelo de datos de producto | Sí |
| SUP-013 | Las listas de precios pueden ser globales o por sucursal — se asume que pueden variar por sucursal salvo que se decida lo contrario | §3.3 "gestionar diferentes listas de precios" sin especificar alcance | Afecta modelo de datos de precios | Sí (ver AMB-007) |
| SUP-014 | "Tiempo real o near-real-time" se interpreta como sincronización síncrona dentro de la misma transacción de BD (no un pipeline asíncrono/eventual), dado que no se exige una arquitectura de eventos | El PDF usa el término sin definirlo técnicamente | Afecta directamente la arquitectura (más simple: consulta directa a BD compartida; más compleja: mensajería) | Sí — es la ambigüedad más importante del documento (ver AMB-001) |

---

## PARTE 7 — Dependencias

### 7.1 Obligatorias 🟢/🟡
- Motor de base de datos (relacional o NoSQL) — persistencia de todo el sistema.
- Backend con API REST o GraphQL — capa de negocio obligatoria.
- Frontend web — capa de presentación obligatoria.
- Docker y Docker Compose — despliegue obligatorio con un solo comando.
- Mecanismo de autenticación/autorización — obligatorio aunque la tecnología es libre.

### 7.2 Opcionales / recomendadas 🔵
- Servicio de correo — solo si se implementa notificación de alertas por correo (funcionalidad adicional).
- Integración con ERP/sistema de punto de venta externo — actor "Sistema externo" es explícitamente opcional.
- Servicio de notificaciones push/in-app — si se elige alertas inteligentes como funcionalidad adicional.
- Motor de predicción de demanda (librería estadística) — solo si se elige esa funcionalidad adicional.
- Proveedor logístico externo — no exigido; el módulo de logística es interno (registro de tiempos/rutas, no integración real con transportistas).

---

## PARTE 8 — Actores

### 🟢 Administrador general
- **Objetivo:** gobernar la red completa de sucursales.
- **Responsabilidades:** configuración del sistema, gestión de usuarios, gestión de sucursales, visibilidad total.
- **Permisos:** acceso de lectura/escritura sobre todos los módulos y todas las sucursales.
- **Acciones permitidas:** todo lo permitido a Gerente y Operador, más gestión de usuarios/sucursales y comparativa entre sucursales en el dashboard.
- **Acciones prohibidas:** ninguna explícita en el PDF (rol de máximo privilegio).
- **Módulos utilizados:** todos.
- **Información consultable:** inventario, ventas, compras, transferencias y dashboard de todas las sucursales.
- **Relación con otros actores:** crea y administra cuentas de Gerente y Operador.

### 🟢 Gerente de sucursal
- **Objetivo:** supervisar la operación de su propia sucursal.
- **Responsabilidades:** supervisión de operaciones locales, aprobación de transferencias, consulta de reportes.
- **Permisos:** lectura/escritura restringida a su sucursal (más lectura de inventario de otras sucursales, permitida a todos).
- **Acciones permitidas:** aprobar/rechazar transferencias, consultar dashboard de su sucursal, consultar reportes logísticos y de ventas de su sucursal.
- **Acciones prohibidas:** 🔴 gestionar usuarios o sucursales (no mencionado como su responsabilidad); 🔴 ver comparativa entre sucursales (el PDF dice "visible para perfiles administrativos", ambiguo si incluye Gerente — ver AMB-008).
- **Módulos utilizados:** inventario, compras, ventas, transferencias (aprobación), logística, dashboard (su sucursal).
- **Relación con otros actores:** recibe solicitudes de transferencia generadas por Operadores; puede ser también quien registra ventas/compras si el negocio lo permite (🔴 no aclarado si el Gerente opera o solo supervisa).

### 🟢 Operador de inventario
- **Objetivo:** ejecutar la operación diaria de inventario, compras, ventas y transferencias de su sucursal.
- **Responsabilidades:** ingresos, retiros, solicitud de transferencias, registro de ventas/compras.
- **Permisos:** lectura/escritura sobre inventario, compras y ventas de su propia sucursal; lectura del inventario de otras sucursales; creación de solicitudes de transferencia.
- **Acciones permitidas:** RF-010 a RF-058 en lo operativo (excluyendo aprobación de transferencias y gestión de usuarios/sucursales).
- **Acciones prohibidas:** aprobar transferencias (rol del Gerente); gestionar usuarios/sucursales; ver comparativa entre sucursales.
- **Módulos utilizados:** inventario, compras, ventas, transferencias (solicitud/despacho/recepción), logística (registro), dashboard (indicadores operativos de su sucursal).
- **Relación con otros actores:** solicita transferencias que el Gerente aprueba; opera bajo la supervisión del Gerente de su sucursal.

### 🟢 Sistema externo (opcional)
- **Objetivo:** integrar el inventario con un ERP o punto de venta existente.
- **Responsabilidades:** consumir/enviar datos vía API.
- **Permisos:** 🔴 no definidos — se asume acceso de API limitado y autenticado (p. ej. API key/token de servicio), sin acceso a la interfaz web.
- **Módulos utilizados:** 🔴 no definido — probablemente ventas e inventario, dado que son los módulos típicos de integración con POS/ERP.
- **Nota:** al ser explícitamente opcional, no debería tratarse como requisito obligatorio (ver PARTE 17, OUT OF SCOPE).

### 🔵 Actor recomendado, no explícitamente definido en el PDF: "Sistema" (actor automático)
No es un usuario humano, pero varios requisitos (RF-022 alertas de stock mínimo, RF-041 descuento automático de stock, RF-048 actualización automática de inventario) son ejecutados por el propio sistema como reacción a eventos, no por un actor humano. Se separa como actor técnico para que los casos de uso y diagramas puedan representar correctamente estas reacciones automáticas, sin inventar un rol de negocio nuevo.

---

## PARTE 9 — Matriz de Permisos (preliminar)

| Funcionalidad | Administrador | Gerente | Operador |
|---|---|---|---|
| Gestionar usuarios | ✅ | ❌ | ❌ |
| Gestionar sucursales | ✅ | ❌ | ❌ |
| CRUD de productos (catálogo) | ✅ | 🔴 POR DEFINIR | 🔴 POR DEFINIR |
| Consultar inventario propio | ✅ | ✅ | ✅ |
| Consultar inventario de otras sucursales | ✅ | ✅ | ✅ |
| Registrar ingreso/retiro de inventario | ✅ | 🔴 POR DEFINIR | ✅ |
| Crear orden de compra | ✅ | 🔴 POR DEFINIR | ✅ |
| Confirmar recepción de compra | ✅ | 🔴 POR DEFINIR | ✅ |
| Registrar venta | ✅ | 🔴 POR DEFINIR | ✅ |
| Solicitar transferencia | ✅ | ✅ | ✅ |
| Aprobar/rechazar transferencia | ✅ | ✅ | ❌ |
| Registrar despacho de transferencia | ✅ | 🔴 POR DEFINIR | ✅ |
| Confirmar recepción (completa/parcial) | ✅ | 🔴 POR DEFINIR | ✅ |
| Ver dashboard de su sucursal | ✅ | ✅ | ✅ (indicadores operativos) |
| Ver comparativa entre sucursales | ✅ | 🔴 POR DEFINIR | ❌ |
| Ver reportes logísticos | ✅ | ✅ | 🔴 POR DEFINIR |

🔴 Recomendación: dado que el PDF describe al Gerente como quien "supervisa" (no necesariamente "opera") y al Operador como quien "realiza" las operaciones diarias, la interpretación más consistente es que el **Gerente puede hacer todo lo que hace el Operador en su sucursal, más aprobar transferencias y ver reportes**, y que la comparativa entre sucursales es exclusiva del Administrador (lectura estricta de "perfiles administrativos" = Administrador). Esta es una recomendación, no un hecho del PDF — debe confirmarse antes de codificar la autorización.

---

## PARTE 10 — Casos de Uso

> [!info] Se detallan en profundidad los 14 solicitados. CU-001 a CU-009 (núcleo transaccional) llevan flujo alternativo y excepciones completos; CU-010 a CU-014 (gestión/consulta) se documentan de forma más compacta por ser de menor complejidad transaccional.

### CU-001 — Registrar ingreso de inventario
- **Actor principal:** Operador de inventario. **Secundarios:** Sistema (actualización de stock).
- **Objetivo:** incorporar unidades de un producto al stock de la sucursal.
- **Precondiciones:** el producto existe en el catálogo; el usuario está autenticado y pertenece a la sucursal.
- **Disparador:** el operador inicia el registro de un ingreso (compra recibida, devolución o ajuste positivo).
- **Flujo principal:** 1) Operador selecciona producto y motivo (compra/devolución/ajuste). 2) Ingresa cantidad y, si aplica, precio de compra. 3) Sistema valida datos. 4) Sistema incrementa el stock de la sucursal y recalcula costo promedio si aplica. 5) Sistema registra el movimiento con fecha, responsable, motivo y cantidad.
- **Flujos alternativos:** el ingreso proviene de una compra (ver CU-010) o de una recepción de transferencia (ver CU-007/CU-008), en cuyo caso el registro es automático, no manual.
- **Excepciones:** cantidad ≤ 0 → sistema rechaza; producto inexistente → sistema rechaza.
- **Postcondiciones:** stock actualizado; movimiento trazado.
- **RN relacionadas:** RN-002, RN-009. **RF relacionados:** RF-015 a RF-017, RF-024.

### CU-002 — Registrar salida/venta
- **Actor principal:** Operador. **Secundarios:** Sistema.
- **Objetivo:** registrar la venta de un producto y descontar stock.
- **Precondiciones:** usuario autenticado; producto con stock en la sucursal.
- **Disparador:** el operador atiende una venta.
- **Flujo principal:** 1) Operador selecciona producto(s), cantidad y lista de precios. 2) Sistema valida stock disponible. 3) Operador aplica descuento si corresponde. 4) Operador confirma la venta. 5) Sistema descuenta stock, registra venta con fecha/sucursal/responsable y genera comprobante.
- **Flujos alternativos:** venta de múltiples productos en una sola transacción (carrito).
- **Excepciones:** stock insuficiente → sistema rechaza la confirmación y notifica al operador (RF-037); dos ventas concurrentes sobre el último ítem de stock → el sistema debe garantizar que solo una tenga éxito (control de concurrencia, RNF-017).
- **Postcondiciones:** stock descontado; venta y comprobante registrados.
- **RN relacionadas:** RN-001, RN-002. **RF relacionados:** RF-035 a RF-041.

### CU-003 — Consultar inventario de otra sucursal
- **Actor principal:** cualquier usuario autenticado.
- **Objetivo:** ver existencias de un producto en otra sucursal (p. ej. antes de solicitar una transferencia).
- **Precondiciones:** usuario autenticado.
- **Disparador:** el usuario navega al inventario de red / busca un producto.
- **Flujo principal:** 1) Usuario selecciona sucursal a consultar. 2) Sistema retorna el stock por producto de esa sucursal (solo lectura). 3) Usuario puede iniciar una solicitud de transferencia desde esta vista.
- **Excepciones:** sucursal inexistente o inactiva → sistema informa error.
- **Postcondiciones:** ninguna (operación de solo lectura).
- **RF relacionados:** RF-014.

### CU-004 — Solicitar transferencia
- **Actor principal:** Operador (o Administrador). **Secundario:** Gerente (receptor de la solicitud).
- **Objetivo:** iniciar el traslado de producto entre sucursales.
- **Precondiciones:** el producto existe; hay indicios de stock en la sucursal origen (visible por CU-003).
- **Disparador:** necesidad de reabastecimiento detectada por el operador o por una alerta de stock mínimo (RF-022).
- **Flujo principal:** 1) Operador indica producto, cantidad, origen, destino y urgencia. 2) Sistema crea la solicitud en estado "solicitada". 3) Sistema notifica al Gerente correspondiente.
- **Flujos alternativos:** la solicitud nace directamente de una alerta de stock mínimo (enlace desde el dashboard).
- **Excepciones:** origen y destino iguales → sistema rechaza.
- **Postcondiciones:** solicitud creada, pendiente de aprobación.
- **RN relacionadas:** RN-006, RN-007. **RF relacionados:** RF-042, RF-043.

### CU-005 — Aprobar/preparar transferencia
- **Actor principal:** Gerente. **Secundario:** Operador de la sucursal origen.
- **Objetivo:** validar y autorizar el envío solicitado.
- **Precondiciones:** existe una solicitud en estado "solicitada".
- **Disparador:** el Gerente revisa solicitudes pendientes.
- **Flujo principal:** 1) Gerente revisa la solicitud. 2) Gerente aprueba o rechaza. 3) Si aprueba, el Operador de origen revisa disponibilidad real y confirma o ajusta la cantidad. 4) Sistema pasa el estado a "en preparación".
- **Flujos alternativos:** rechazo por parte del Gerente → estado "rechazada", fin del caso de uso.
- **Excepciones:** stock insuficiente al momento de preparar → el Operador ajusta la cantidad o cancela.
- **Postcondiciones:** transferencia aprobada y en preparación, o rechazada.
- **RN relacionadas:** RN-007, RN-008. **RF relacionados:** RF-044, RF-045.

### CU-006 — Registrar despacho
- **Actor principal:** Operador (origen).
- **Objetivo:** formalizar la salida física de la mercancía.
- **Precondiciones:** transferencia en estado "en preparación".
- **Disparador:** la mercancía está lista para salir.
- **Flujo principal:** 1) Operador registra fecha estimada de llegada y transportista. 2) Sistema descuenta el stock de origen (según SUP-004) y cambia el estado a "en tránsito".
- **Excepciones:** intento de despachar sin aprobación previa → sistema rechaza.
- **Postcondiciones:** stock de origen descontado; transferencia "en tránsito".
- **RN relacionadas:** RN-006. **RF relacionados:** RF-046, RF-053.

### CU-007 — Confirmar recepción completa
- **Actor principal:** Operador (destino).
- **Objetivo:** cerrar el ciclo de transferencia cuando llega la cantidad completa.
- **Precondiciones:** transferencia "en tránsito".
- **Disparador:** llega la mercancía a destino.
- **Flujo principal:** 1) Operador confirma que la cantidad recibida coincide con la despachada. 2) Sistema incrementa el stock de destino. 3) Sistema registra tiempo real de entrega. 4) Estado pasa a "recibida".
- **Postcondiciones:** stock de destino actualizado; transferencia cerrada.
- **RN relacionadas:** RN-006. **RF relacionados:** RF-047, RF-048, RF-056.

### CU-008 — Confirmar recepción parcial
- **Actor principal:** Operador (destino). **Secundario:** Gerente (define tratamiento del faltante).
- **Objetivo:** registrar una entrega incompleta y disparar su gestión.
- **Precondiciones:** transferencia "en tránsito".
- **Disparador:** llega menos cantidad de la despachada.
- **Flujo principal:** 1) Operador registra la cantidad efectivamente recibida. 2) Sistema calcula el faltante y actualiza stock de destino solo con lo recibido. 3) Sistema genera alerta de faltante. 4) Estado pasa a "con faltantes". 5) Gerente/Operador define tratamiento (ver CU-009).
- **Postcondiciones:** stock de destino parcialmente actualizado; alerta y faltante registrados.
- **RN relacionadas:** RN-006, RN-009. **RF relacionados:** RF-049, RF-050.

### CU-009 — Gestionar faltantes
- **Actor principal:** Gerente (o Administrador). **Secundario:** Operador.
- **Objetivo:** resolver un faltante detectado en una recepción parcial.
- **Precondiciones:** transferencia en estado "con faltantes".
- **Disparador:** alerta generada por CU-008.
- **Flujo principal:** 1) Gerente revisa el faltante. 2) Gerente decide entre reenvío (crea nueva transferencia parcial), ajuste (se acepta la pérdida y se registra como merma/ajuste) o reclamación (se deja registrado para gestión externa, sin efecto automático en stock). 3) Sistema registra la decisión y cierra o deriva el caso.
- **Excepciones:** ninguna decisión tomada en un plazo → 🔴 no definido si debe existir escalamiento automático.
- **Postcondiciones:** faltante resuelto o derivado.
- **RN relacionadas:** RN-009. **RF relacionados:** RF-051.

### CU-010 — Gestionar compras
- **Actor principal:** Operador. **Objetivo:** ciclo completo orden→recepción→actualización de inventario→costo promedio.
- **Flujo resumido:** crear orden (RF-027–029) → confirmar recepción total o parcial (RF-030) → actualizar stock (RF-031) → recalcular costo promedio (RF-032) → quedar disponible en histórico (RF-033–034).
- **Excepciones:** recepción con cantidad distinta a lo ordenado → 🔴 no definido si se trata igual que faltantes de transferencia (ver AMB-009).
- **RF relacionados:** RF-026 a RF-034.

### CU-011 — Consultar dashboard
- **Actor principal:** Gerente/Administrador/Operador (según nivel de detalle permitido).
- **Flujo resumido:** el usuario accede al dashboard de su sucursal (o comparativo, si es Administrador) y visualiza ventas, rotación, transferencias activas, reabastecimiento y (solo Administrador) comparativa entre sucursales.
- **RF relacionados:** RF-059 a RF-064.

### CU-012 — Gestionar productos
- **Actor principal:** Administrador (y posiblemente Operador, ver AMB-004).
- **Flujo resumido:** crear, editar, dar de baja productos del catálogo; definir unidades de medida y stock mínimo por producto/sucursal.
- **RF relacionados:** RF-010 a RF-013, RF-021, RF-023.

### CU-013 — Gestionar usuarios/sucursales
- **Actor principal:** Administrador.
- **Flujo resumido:** crear/editar/desactivar sucursales y usuarios, asignar rol y sucursal a cada usuario.
- **RF relacionados:** RF-004, RF-007.

### CU-014 — Consultar trazabilidad
- **Actor principal:** Administrador/Gerente (auditoría). **Objetivo:** revisar el historial de movimientos de un producto o de una sucursal (quién, cuándo, motivo, cantidad).
- **Flujo resumido:** el usuario filtra por producto, sucursal y/o rango de fechas; el sistema retorna el histórico de movimientos registrados por RF-024.
- **RF relacionados:** RF-024, RNF-025.

---

## PARTE 11 — Reglas de Negocio

| ID | Regla | Estado |
|---|---|---|
| RN-001 | No se puede vender más stock del disponible en la sucursal al momento de confirmar la venta | 🟢 Explícita |
| RN-002 | Todo movimiento de inventario (ingreso o retiro) debe registrar fecha, responsable, motivo y cantidad | 🟢 Explícita |
| RN-003 | El costo promedio ponderado se recalcula en cada recepción de compra según la fórmula estándar de costeo (SUP-009) | 🔵 Regla propuesta |
| RN-004 | Las devoluciones incrementan el stock de la sucursal que las registra, con motivo "devolución" | 🟡 Inferida de RF-016 |
| RN-005 | Los ajustes de inventario (positivos o negativos) requieren un motivo explícito y quedan trazados igual que cualquier otro movimiento | 🟡 Inferida |
| RN-006 | Una transferencia solo puede despacharse tras ser aprobada por el Gerente correspondiente | 🟢 Explícita (flujo §3.4) |
| RN-007 | Solo el Gerente puede aprobar o rechazar solicitudes de transferencia; el Operador solo puede solicitarlas o prepararlas | 🟢 Explícita (§6.2) |
| RN-008 | 🔴 Regla propuesta: un Gerente no debería poder aprobar una transferencia en la que su propia sucursal es simultáneamente origen y destino (caso degenerado ya cubierto por CU-004, pero se deja explícito) | 🔵 Regla propuesta |
| RN-009 | Todo faltante detectado en una recepción parcial debe resolverse mediante reenvío, ajuste o reclamación; no puede quedar sin tratamiento definido | 🟢 Explícita (§3.4 paso 5) |
| RN-010 | Las operaciones de escritura sobre inventario, compras, ventas y transferencias solo pueden ser realizadas por usuarios de la sucursal involucrada, salvo el Administrador | 🟡 Inferida |
| RN-011 | Todo movimiento crítico (venta, compra, transferencia, ajuste) requiere auditoría (usuario y timestamp) | 🔵 Regla propuesta, alineada con §3.1 y con la funcionalidad adicional de auditoría sugerida en §4 |

---

## PARTE 12 — Historias de Usuario (por épicas)

> [!note] Las tres historias del PDF (§6.3) están marcadas 🟢 e integradas en su épica correspondiente. El resto son 🟡/🔵 según se indica.

**Épica 1 — Autenticación y usuarios**
- HU-001 🟡 Como usuario, quiero iniciar sesión con mis credenciales, para acceder a las funciones de mi rol.
- HU-002 🟡 Como Administrador, quiero crear usuarios y asignarles rol y sucursal, para controlar quién opera en el sistema.

**Épica 2 — Sucursales**
- HU-003 🟡 Como Administrador, quiero registrar nuevas sucursales, para incorporarlas a la red de inventario.

**Épica 3 — Productos**
- HU-004 🟡 Como Administrador, quiero mantener un catálogo único de productos, para que todas las sucursales trabajen sobre la misma referencia.
- HU-005 🟡 Como Gerente, quiero definir un stock mínimo por producto en mi sucursal, para anticipar quiebres de inventario.

**Épica 4 — Inventario**
- HU-006 🟢 Como operador de inventario, quiero registrar el ingreso de productos con su precio de compra, para mantener el costo promedio del inventario actualizado y generar órdenes de pago a proveedores. *(HU literal del PDF)*
- HU-007 🟡 Como operador, quiero consultar el inventario de otra sucursal, para decidir si conviene solicitar una transferencia en vez de una compra.
- HU-008 🟡 Como operador, quiero recibir una alerta cuando un producto llegue a su stock mínimo, para reabastecerlo a tiempo.

**Épica 5 — Compras**
- HU-009 🟡 Como operador, quiero crear una orden de compra a un proveedor, para reabastecer mi sucursal.
- HU-010 🟡 Como operador, quiero confirmar la recepción de una compra, para que el inventario y el costo promedio se actualicen automáticamente.

**Épica 6 — Ventas**
- HU-011 🟡 Como operador, quiero registrar una venta validando el stock disponible, para evitar vender productos que no existen físicamente.
- HU-012 🟡 Como operador, quiero aplicar descuentos y listas de precios distintas, para atender condiciones comerciales específicas.

**Épica 7 — Transferencias**
- HU-013 🟢 Como operador de inventario, quiero solicitar la transferencia de un producto desde otra sucursal con indicación de urgencia, para que la sucursal origen pueda priorizar el despacho según disponibilidad. *(HU literal del PDF)*
- HU-014 🟡 Como Gerente, quiero aprobar o rechazar solicitudes de transferencia, para controlar el movimiento de inventario de mi sucursal.
- HU-015 🟡 Como operador (destino), quiero registrar una recepción parcial y su faltante, para que quede documentado y gestionado.

**Épica 8 — Logística**
- HU-016 🟡 Como Gerente, quiero ver el cumplimiento de tiempos estimados vs. reales por ruta, para evaluar el desempeño logístico.

**Épica 9 — Dashboard**
- HU-017 🟢 Como gerente de sucursal, quiero ver en un dashboard la comparativa de ventas entre el mes actual y los tres meses anteriores, para identificar tendencias y tomar decisiones de compra anticipadas. *(HU literal del PDF)*
- HU-018 🟡 Como Administrador, quiero comparar el rendimiento entre sucursales, para identificar cuáles requieren atención.

**Épica 10 — Auditoría**
- HU-019 🔵 Como Administrador, quiero consultar quién realizó cada movimiento de inventario y cuándo, para poder auditar el sistema. *(Épica no contemplada explícitamente en el PDF más allá de la nota de trazabilidad de §3.1; se propone porque §4 sugiere auditoría como funcionalidad adicional)*

**Épica 11 — Funcionalidad adicional**
- HU-020 🔵 Como [actor según funcionalidad elegida], quiero [funcionalidad adicional elegida], para [beneficio específico] — *pendiente de concretar según la decisión de PARTE 19.*

---

## PARTE 13 — Criterios de Aceptación (historias clave)

**HU-006 / RF-015, RF-031, RF-032**
- Dado un producto existente y una cantidad y precio de compra válidos, cuando el operador registra el ingreso, entonces el sistema incrementa el stock de la sucursal y recalcula el costo promedio ponderado.
- Dado un precio de compra no numérico o cantidad ≤ 0, cuando el operador intenta registrar el ingreso, entonces el sistema rechaza la operación y muestra un error.

**HU-011 / RF-037, RF-041**
- Dado que existe stock suficiente, cuando el operador registra una venta, entonces el sistema valida disponibilidad y actualiza el inventario. *(criterio dado como ejemplo por el usuario, incorporado literalmente)*
- Dado que el stock disponible es menor a la cantidad solicitada, cuando el operador intenta confirmar la venta, entonces el sistema rechaza la venta y no descuenta stock.

**HU-013 / RF-042, RF-043**
- Dado un producto con stock visible en otra sucursal, cuando el operador solicita una transferencia indicando urgencia, entonces el sistema crea la solicitud en estado "solicitada" y la asocia a la sucursal origen indicada.
- Dado que origen y destino son la misma sucursal, cuando el operador intenta crear la solicitud, entonces el sistema la rechaza.

**HU-014 / RF-045**
- Dado que existe una solicitud de transferencia pendiente para su sucursal, cuando el Gerente la aprueba, entonces el sistema cambia el estado a "en preparación" y notifica a la sucursal origen.
- Dado que el Gerente rechaza la solicitud, cuando confirma el rechazo, entonces el sistema cambia el estado a "rechazada" y no se descuenta stock.

**HU-015 / RF-049, RF-050, RF-051**
- Dado que llega una cantidad menor a la despachada, cuando el operador de destino confirma la recepción, entonces el sistema registra el faltante, genera una alerta y deja la transferencia en estado "con faltantes" hasta que se defina tratamiento.

**HU-017 / RF-059**
- Dado que existen ventas registradas en los últimos 4 meses, cuando el Gerente abre el dashboard, entonces el sistema muestra el volumen de ventas del mes actual comparado con los tres meses anteriores.

---

## PARTE 14 — Matriz de Trazabilidad (resumen)

> [!note] Matriz condensada por módulo; cada fila representa el enlace Necesidad → RF/RNF → CU → HU → Criterio. La lista completa ítem-a-ítem se deriva directamente de las tablas de las PARTES 3, 10, 12 y 13 (mismos IDs referenciados de forma cruzada).

| Necesidad | RF/RNF | CU | HU | Criterio de aceptación |
|---|---|---|---|---|
| Ingresar productos con costeo | RF-015, RF-031, RF-032 | CU-001 | HU-006 | Ver PARTE 13 |
| Vender con validación de stock | RF-035–041 | CU-002 | HU-011 | Ver PARTE 13 |
| Ver inventario de otra sucursal | RF-014 | CU-003 | HU-007 | Cobertura: sin CA explícito → **hueco detectado (ver más abajo)** |
| Solicitar transferencia con urgencia | RF-042, RF-043 | CU-004 | HU-013 | Ver PARTE 13 |
| Aprobar/preparar transferencia | RF-044, RF-045 | CU-005 | HU-014 | Ver PARTE 13 |
| Despachar transferencia | RF-046, RF-053 | CU-006 | — | **Hueco: sin HU dedicada** |
| Recibir transferencia completa | RF-047, RF-048 | CU-007 | — | **Hueco: sin HU dedicada** |
| Recibir transferencia parcial / faltantes | RF-049–051 | CU-008, CU-009 | HU-015 | Ver PARTE 13 |
| Gestionar compras (ciclo completo) | RF-026–034 | CU-010 | HU-009, HU-010 | Parcial (falta CA para recepción parcial de compra) |
| Consultar dashboard | RF-059–064 | CU-011 | HU-017, HU-018 | Ver PARTE 13 (parcial, falta CA para HU-018) |
| Gestionar productos | RF-010–013, RF-021, RF-023 | CU-012 | HU-004, HU-005 | **Hueco: sin CA definido** |
| Gestionar usuarios/sucursales | RF-004, RF-007 | CU-013 | HU-002, HU-003 | **Hueco: sin CA definido** |
| Consultar trazabilidad | RF-024 | CU-014 | HU-019 | **Hueco: sin CA definido** |
| Sincronización multi-sucursal (transversal) | RNF-004, RNF-016, RNF-017 | (transversal a CU-001, 002, 006–008) | — | **Hueco: requisito no funcional sin HU ni CA propios — es correcto que no la tenga, pero debe verificarse en pruebas de integración, no en historias de usuario** |

**Duplicados/inconsistencias detectados:**
- RF-041 (descuento automático de stock en venta) se solapa parcialmente con RF-037 (validación de stock); se mantienen separados porque uno es validación y otro es efecto, pero deben implementarse en la misma transacción atómica.
- RF-052 (estados de transferencia) es transversal a CU-004 a CU-009; no se referencia como CU propio para evitar duplicar el mismo flujo.

---

## PARTE 15 — Ambigüedades, decisiones pendientes y riesgos

| ID | Ambigüedad | Qué dice el documento | Interpretaciones posibles | Recomendación | Impacto |
|---|---|---|---|---|---|
| AMB-001 | "Tiempo real o near-real-time" (§2.1) | Frase textual, sin definición técnica | (a) Consulta directa a BD compartida entre sucursales (consistencia fuerte, arquitectura simple); (b) réplica/eventual consistency vía mensajería (más escalable, más compleja) | (a) para el alcance de una prueba técnica: más simple de justificar y de demostrar en la demo, cumple igualmente "near-real-time" | Backend (arquitectura), BD (una sola base compartida vs. réplicas), documentación (justificación en §8.2) |
| AMB-002 | Momento del descuento de stock de origen en una transferencia | No se especifica si se descuenta en la solicitud, en la aprobación o en el despacho | (a) En el despacho (SUP-004); (b) reserva en la aprobación y descuento físico en el despacho | (b) es más robusto: evita vender un producto ya comprometido en una transferencia aprobada, sin bloquear stock desde la sola solicitud | Modelo de datos (columna "reservado"), lógica de ventas (debe descontar de stock disponible = stock físico − reservado) |
| AMB-003 | Quién aprueba una transferencia: ¿Gerente de origen, de destino, o ambos? | "Gerente de sucursal... aprueba transferencias" sin especificar dirección | (a) Gerente de destino (quien la solicitó/necesita); (b) Gerente de origen (quien debe ceder stock); (c) ambos | (b): quien cede el recurso es quien debe autorizar su salida — más alineado con "autonomía operativa" de cada sucursal | Autorización, notificaciones, CU-005 |
| AMB-004 | ¿Quién puede crear/editar productos del catálogo? | §3.1 no indica actor; §6.2 no lo asigna a ningún rol específico | (a) Solo Administrador (catálogo centralizado y controlado); (b) Administrador y Gerente; (c) también Operador | (a): un catálogo único y compartido entre sucursales requiere control centralizado para evitar duplicados/inconsistencias | Matriz de permisos, RF-010/011 |
| AMB-005 | Devoluciones: ¿de cliente a sucursal, o de sucursal a proveedor, o ambas? | §3.1 menciona "devoluciones" como motivo de ingreso, sin distinguir tipo | (a) Solo devoluciones de cliente (ingreso); (b) también devoluciones a proveedor (que sería un retiro, no un ingreso) | Tratar como devolución de cliente (ingreso) por ser la lectura literal del texto (aparece en la lista de motivos de *ingreso*); devolución a proveedor se deja como ajuste/retiro si se necesita | Modelo de motivos de movimiento |
| AMB-006 | Estados formales de una transferencia | El PDF menciona 4 estados de forma dispersa (preparación, tránsito, recibido, con faltantes) sin listar el ciclo completo | Se infieren "solicitada", "aprobada/rechazada" y "cancelada" para cerrar el ciclo | Adoptar: solicitada → aprobada/rechazada → en preparación → en tránsito → recibida / con faltantes → (reenvío/ajuste/reclamación) | Máquina de estados en backend, diagrama de actividades (§7) |
| AMB-007 | Alcance de las listas de precios: ¿globales o por sucursal? | §3.3 solo dice "gestionar diferentes listas de precios" | (a) Globales (misma lista para toda la red); (b) por sucursal (autonomía operativa) | (b) es más coherente con el principio de autonomía operativa por sucursal (§2) | Modelo de datos de precios, módulo de ventas |
| AMB-008 | ¿La comparativa entre sucursales del dashboard es solo para Administrador, o también para Gerente? | "visible para perfiles administrativos" (plural, ambiguo) | (a) Solo Administrador; (b) Administrador y Gerente (ambos "administran" algo) | (a), lectura más estricta y más segura por defecto (menor superficie de exposición de datos entre sucursales) | RF-063, RF-064, matriz de permisos |
| AMB-009 | Recepción de compra con cantidad distinta a la ordenada | El PDF no lo menciona (a diferencia de transferencias, donde sí define recepción parcial) | (a) Tratar igual que faltante de transferencia (alerta + tratamiento); (b) simplemente registrar lo recibido, sin flujo de faltantes | (b) para el alcance de esta prueba (compras es un módulo más simple que transferencias en el PDF); documentar como decisión, no como funcionalidad completa de reclamos a proveedor | RF-030, CU-010 |
| AMB-010 | Rutas y transportistas: ¿son entidades gestionables o solo campos de texto libre? | §3.4 y §3.5 mencionan "transportista" y "rutas" sin definir si son catálogos | (a) Campos de texto libre (más simple); (b) catálogos con su propia gestión (más "empresarial" pero fuera del foco del ciclo de vida principal) | (a) — no hay evidencia de que el PDF pida gestión de transportistas como entidad propia | Modelo de datos, alcance de RF-046, RF-057, RF-058 |
| AMB-011 | Integración con "Sistema externo": ¿se debe implementar realmente o basta con dejar la API preparada? | El actor está marcado explícitamente "(opcional)" | (a) Solo diseñar la API para que sea integrable (no implementar un consumidor real); (b) construir un mock/adapter de integración | (a): es la lectura más segura de "opcional", y evita gastar tiempo de la prueba en algo no evaluado como obligatorio | Alcance (PARTE 17: OUT OF SCOPE u opcional), backend |

---

## PARTE 16 — Requisitos complementarios recomendados

| Recomendación | Valor | Complejidad | Prioridad | Justificación |
|---|---|---|---|---|
| Recuperación de contraseña | Bajo (no evaluado explícitamente) | Baja | COULD | Mejora percepción de completitud, pero no aporta a los criterios de evaluación del PDF (arquitectura, requisitos, IA) |
| Bloqueo de usuarios inactivos/sospechosos | Bajo | Baja | COULD | Buena práctica de seguridad, no exigida |
| Paginación en listados (inventario, ventas, histórico) | Alto | Baja | SHOULD | El PDF exige histórico y trazabilidad; sin paginación la demo con datos de prueba puede verse pobre o la app puede degradar con volumen |
| Filtros y búsqueda (por producto, sucursal, fecha) | Alto | Baja-Media | SHOULD | Necesario para que dashboard, trazabilidad y reportes logísticos sean realmente usables, tal como pide §3.6 y §3.5 |
| Exportación de reportes (PDF/Excel) | Medio-Alto | Media | COULD (o MUST si se elige como la funcionalidad adicional obligatoria de §4) | Está explícitamente sugerida en §4 como una de las ideas orientadoras |
| Notificaciones (in-app o correo) de alertas de stock/faltantes | Medio-Alto | Media | COULD (o MUST si se elige como funcionalidad adicional) | También sugerida explícitamente en §4 |
| Idempotencia en confirmaciones críticas (venta, recepción) | Medio | Media | SHOULD | Reduce riesgo de doble venta/doble recepción por doble clic o reintento de red — relevante para RNF-017 |
| Control de concurrencia optimista (versión de registro) | Alto | Media | MUST | Es la forma más directa de cumplir la exigencia de "sincronización" y "consistencia" del §8.2 sin necesitar arquitectura distribuida compleja |
| Soft delete en productos y sucursales | Medio | Baja | SHOULD | Evita romper el histórico/trazabilidad (RF-024) si se "elimina" un producto con movimientos asociados |
| Versionado de API | Bajo | Baja | COULD | No exigido; útil solo si se prevé evolución post-entrega, lo cual excede el alcance de la prueba |

🔵 No se recomienda agregar funcionalidades fuera de esta lista solo para "hacer el proyecto más grande": el documento explícitamente valora justificación y calidad sobre cantidad de features.

---

## PARTE 17 — Priorización (MoSCoW)

**MUST** (obligatorio para cumplir la prueba): RF-001 a RF-064 en su totalidad (son los módulos 3.1–3.6, explícitamente obligatorios); RNF-005 a RNF-011 (seguridad básica, exigida indirectamente por §8.2); RNF-022, RNF-028, RNF-029 (arquitectura de 3 capas y Docker, restricción sin excepción de §5); RF-065 (al menos una funcionalidad adicional, obligatoria en su existencia); control de concurrencia optimista (recomendación de PARTE 16, porque sin ella no se puede justificar §8.2 "sincronización").

**SHOULD** (muy recomendable para demostrar calidad): RNF-024 (tests para módulos críticos, explícitamente valorado en §9.1); RNF-025 a RNF-027 (observabilidad/trazabilidad, ya parcialmente MUST vía RF-024 pero logs/métricas adicionales son SHOULD); paginación y filtros (PARTE 16); idempotencia en operaciones críticas.

**COULD** (valor agregado si queda tiempo): recuperación de contraseña, bloqueo de usuarios, exportación avanzada de reportes (más allá de lo mínimo si no se eligió como funcionalidad adicional obligatoria), versionado de API, predicción de demanda (si no se elige como la funcionalidad adicional obligatoria, queda como valor agregado extra).

**OUT OF SCOPE** (no debería implementarse en esta etapa): integración real con un sistema externo/ERP (el actor es explícitamente opcional — basta con que la API esté preparada para integrarse, ver AMB-011); gestión de transportistas como entidad completa (AMB-010); multi-organización/multi-tenant (SUP-001); notificaciones push nativas (excede el foco de una prueba técnica, salvo que se elija como la funcionalidad adicional).

---

## PARTE 18 — Auditoría como evaluador

**¿Qué está bien de este levantamiento?** Cubre los 6 módulos obligatorios con requisitos atómicos y verificables; distingue explícitamente lo obligatorio de lo interpretado; deja una lista cerrada de ambigüedades con recomendación justificada para cada una, lo cual es exactamente lo que el principio rector del documento ("¿por qué se hizo así?") exige poder responder.

**¿Qué falta / qué podría causar pérdida de puntos si no se resuelve antes de programar?**
- Confirmar AMB-001 (significado técnico de "tiempo real/near-real-time"): es la decisión que más condiciona la arquitectura completa (§8.2 exige justificar el "mecanismo de sincronización").
- Confirmar AMB-002/AMB-003 (momento de descuento de stock y quién aprueba): errores aquí producen bugs de inventario visibles en cualquier demo.
- La matriz de permisos tiene varias celdas 🔴 POR DEFINIR (PARTE 9): dejar esto abierto hasta la fase de desarrollo genera riesgo de tener que rehacer autorización a mitad de camino.
- No se ha elegido aún la funcionalidad adicional obligatoria (RF-065): el PDF la exige explícitamente y su ausencia sería una omisión crítica el día de la entrega.

**Aspectos que probablemente se revisarán en una demostración:** flujo completo de transferencia con recepción parcial (es el flujo más citado y más rico del documento); validación de stock en venta bajo condiciones límite (stock exacto, stock insuficiente); consulta cruzada de inventario entre sucursales; dashboard con datos reales de al menos 4 meses para poder mostrar la comparativa (HU-017); ejecución con un solo comando Docker Compose desde cero.

**Aspectos que deberían aparecer en el README:** justificación de AMB-001 a AMB-011 resueltas (no solo mencionadas); las 5 justificaciones explícitas exigidas en §8.2 (lenguaje backend, motor de BD/modelo de datos, autenticación/autorización, sincronización multi-sucursal, patrones de diseño); la funcionalidad adicional elegida y su valor.

**Aspectos que deberían demostrarse mediante la aplicación (no solo documentarse):** ciclo de transferencia completo con al menos un caso de recepción parcial; alerta de stock mínimo funcionando; costo promedio ponderado recalculado correctamente tras una compra.

**Aspectos que deberían aparecer en diagramas (§7):** el caso de uso de transferencia debe reflejar los 4 actores y el actor técnico "Sistema" (PARTE 8) para las actualizaciones automáticas; el diagrama de actividades de transferencia debe incluir explícitamente la rama de recepción parcial (no solo el camino feliz); el diagrama E-R debe reflejar la separación producto (global) vs. existencia (por sucursal) decidida en SUP-003.

---

## PARTE 19 — Decisiones que debes tomar antes de pasar a arquitectura y desarrollo

1. **AMB-001** — Definir el significado técnico de "tiempo real/near-real-time": ¿BD compartida con lectura directa, o mecanismo de replicación/mensajería?
2. **AMB-002** — Definir el momento exacto de descuento/reserva de stock en una transferencia (solicitud, aprobación o despacho).
3. **AMB-003** — Definir si aprueba el Gerente de origen, de destino, o ambos.
4. **AMB-004** — Definir quién puede crear/editar productos del catálogo (¿solo Administrador?).
5. **AMB-007** — Definir si las listas de precios son globales o por sucursal.
6. **AMB-008** — Definir si la comparativa entre sucursales es exclusiva de Administrador o también de Gerente.
7. **AMB-011** — Definir si se implementa algo concreto para el actor "Sistema externo" o solo se deja la API preparada.
8. **PARTE 17 / RF-065** — Elegir la funcionalidad adicional obligatoria entre las sugeridas en §4 (o proponer una propia con justificación de valor).
9. **SUP-009** — Confirmar la fórmula exacta de costo promedio ponderado a implementar.
10. **SUP-012 / SUP-013** — Confirmar el modelo de unidades de medida múltiples y el alcance (global/por sucursal) de las listas de precios (relacionado con AMB-007).
11. Completar las celdas 🔴 POR DEFINIR de la matriz de permisos (PARTE 9) con una decisión explícita por celda.
12. Decidir el mecanismo concreto de control de concurrencia (bloqueo optimista con número de versión es la recomendación de PARTE 16, pero debe confirmarse como decisión de diseño formal para documentar en §8.2).

---

## SIGUIENTE PASO RECOMENDADO

Una vez cierres las 12 decisiones de la PARTE 19 (idealmente registrándolas como respuestas explícitas dentro de este mismo documento, para mantener la trazabilidad), el siguiente paso —que aún no se debe desarrollar en esta etapa— es:

1. Usar este documento como entrada única de verdad para diseñar el **modelo de datos** (entidades: organización implícita, sucursal, usuario, producto, existencia por sucursal, movimiento de inventario, orden de compra, proveedor, venta, transferencia con su máquina de estados, y los campos de auditoría/trazabilidad).
2. Diseñar la **arquitectura técnica** (capas, elección de lenguaje de backend, motor de BD, estrategia de autenticación/autorización, mecanismo de sincronización) apoyándose directamente en las secciones 5, 7 y 8 de las decisiones ya tomadas aquí.
3. Producir los **diagramas obligatorios** (casos de uso, actividades de venta y de transferencia —incluyendo la rama de recepción parcial—, arquitectura, entidad-relación), derivándolos directamente de las PARTES 8, 10 y de la SUP-003.
4. Definir el **stack tecnológico concreto**, justificando cada elección contra los requisitos no funcionales de la PARTE 4 (especialmente consistencia, seguridad y portabilidad).
5. Solo después de (1)-(4): iniciar la implementación siguiendo el orden sugerido por el propio documento (§12): Docker/estructura base → backend por orden de dependencia de módulos → frontend → funcionalidad adicional → documentación final y evidencia de uso de IA.
