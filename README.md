# OptiPlant Inventario — Backend

API REST del sistema de inventario multi-sucursal de OptiPlant. Implementada
con Spring Boot y expuesta bajo el prefijo `/api/v1`.

## Requisitos

- Java 21
- Maven 3.9+
- PostgreSQL 16

## Puesta en marcha

### Con Docker Compose

El `docker-compose.yml` levanta la base de datos, el backend y el frontend
completos. Consulta el archivo para los detalles de configuración.

```bash
docker compose up -d --build
```

La API queda disponible en `http://localhost:8080`.

### En desarrollo

```bash
# Base de datos (ajusta credenciales en application-dev.yml o variables)
mvn spring-boot:run
```

Por defecto se activa el perfil `dev`, que usa las credenciales de
`application-dev.yml` (variables `DB_*`). Para producción usa el perfil `prod`,
que exige definir `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME` y
`DB_PASSWORD`.

### Pruebas

```bash
mvn test
```

## Tecnologías y stack

- Spring Boot 3 (Java 21)
- Spring Security con autenticación JWT
- Spring Data JPA con Hibernate
- Flyway para migraciones de esquema
- PostgreSQL
- MapStruct y Lombok
- Springdoc OpenAPI para documentación

## Autenticación y roles

El acceso se protege con tokens JWT. Tras iniciar sesión, se debe enviar el
token en el encabezado `Authorization: Bearer <token>`.

Existen tres roles:

- `ADMINISTRADOR`: acceso total, gestión de usuarios y sucursales, y
  comparativas entre sucursales.
- `GERENTE`: opera sobre su sucursal y aprueba o rechaza transferencias.
- `OPERADOR`: opera sobre su sucursal (ventas, entradas/salidas,
  transferencias) sin poder aprobarlas.

## Módulos

| Módulo            | Paquete      | Descripción                                    |
|-------------------|--------------|------------------------------------------------|
| Identidad         | `identidad`  | Usuarios y autenticación (JWT, roles).         |
| Catálogo          | `catalogo`   | Sucursales, productos, categorías, proveedores.|
| Inventario        | `inventario` | Existencias y movimientos de stock.            |
| Compras           | `compras`    | Órdenes de compra y recepción.                 |
| Ventas            | `ventas`     | Registro de ventas y catálogo de venta.        |
| Transferencias    | `transferencias` | Traslados entre sucursales con aprobaciones.|
| Dashboard         | `dashboard`  | Reportes, resumen y comparativa de sucursales. |

## Documentación de la API

Con la aplicación en ejecución, la documentación OpenAPI (Swagger UI) está en:

```
http://localhost:8080/api/v1/swagger-ui.html
```

El contrato JSON/YAML se publica en `api/v1/docs`.
