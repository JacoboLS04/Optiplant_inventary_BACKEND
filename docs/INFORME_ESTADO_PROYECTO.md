# INFORME DE ESTADO GLOBAL — PROYECTO OPTIPLANT (Inventario Multi-Sucursal)

Generado: 2026-08-29 · Última verificación: fases A–F completadas y verificadas E2E.
Propósito: dar a un agente (Claude) contexto completo para retomar o continuar el trabajo.

---

## 1. Qué es el proyecto

Sistema de **inventario multi-sucursal** para Optiplant (agroquímicos/insumos).
- **Backend**: Java 17+ / Spring Boot 3.5.14, JPA/Hibernate, Spring Security + JWT, Flyway, PostgreSQL. APP rol `inventario`, version `0.1.0-SNAPSHOT`, jar `target/inventario-0.1.0-SNAPSHOT.jar`, puerto **8080**, context-path `/`.
- **Frontend**: React 19 + Vite + TypeScript 6, TanStack Query 5, react-router 7, axios, Tailwind + shadcn/ui, Vitest 4.
- **Ramas/Git**: rama `main`. Último commit `db4204b`. **Fases A–F están SIN commitear** (trabajo en curso): backend 14 modificados + 7 nuevos; frontend 26 modificados + 20 nuevos.

Especificación: `docs/Especificacion_Requisitos_Inventario_MultiSucursal.md` (referencias RF-###).

---

## 2. Credenciales / datos de entorno (crítico)

- **Admin**: `admin2@optiplant.com` / `Admin456!` → rol **ADMINISTRADOR**, usuarioId=5.
- **Operador (dev)**: `operador1@x.com` / **`Operador123!`** → rol **OPERADOR**, `sucursal_id=NULL`.
  > ⚠️ La contraseña de operador1 se **cambió** durante las pruebas de Fase E (no se pudo revertir al hash original). Tener en cuenta.
- Otros dev: `t@t.com` (BODEGUERO, sucursal null).
- **DBs**: `optiplant_dev` y `optiplant_test` — usuario/password `postgres/postgres`. Acceso: `PGPASSWORD=postgres psql -U postgres -d optiplant_dev`.
- **Migraciones Flyway**: hasta **V17** (aplicadas en dev y test). `.mvn` no usa; comandos:
  - Compilar: `mvn -q compile`
  - Tests: `mvn test` → **8/8 verdes** (UsuarioServiceTest=5, InventarioApplicationTests=1, VentaServiceTest=2)
  - Empaquetar: `mvn -q package -DskipTests`
- **Config** (`application.yml`): `spring.jpa.hibernate.ddl-auto: validate`, Flyway en DB, JWT `expiration=86400000` (24h), Swagger en `/api/v1/swagger-ui.html` y `/api/v1/docs`.

---

## 3. Frontend — utilidades y convenciones

- **Patrón de ids**: backend usa ids `Long`; la **capa API** los convierte a `string` antes de que lleguen a la UI. Getters de lectura: `getId()`, `String(value)`.
- **PageEnvelope** (backend): `{content, page, size, totalElements, totalPages}`.
- **apiClient** (`src/api/client.ts`): pone Bearer token; en **401** borra token de localStorage y redirige a `/login`.
- **AuthContext** (`src/features/auth/context/AuthContext.tsx`): provee `user`, `isAuthenticated`, `login`, `logout`; **persiste `usuarioId`**; `useAuth()` lanza error si se usa fuera de proveedor (importante para tests).
- **Roles**: `normalizarRol(rol)` en `src/lib/roles`.
- **Validación frontend**: `npx tsc -b`, `npm run build`, `npx vitest run`. **Vitest solo tiene 2 tests** en `src/App.test.tsx` que **mockean `@/features/dashboard/api/dashboard.api`** (deben incluir `fetchRotacion` y `fetchVentasMensuales`) y **mockean `@/features/auth/context/AuthContext`** (debe devolver un usuario ADMINISTRADOR para que `BranchNetworkCard` se renderice).
- **Format utils** (`src/lib/format.ts`): `formatCurrency`, `formatCompactCurrency`, `formatNumber`, `formatPercent`, `formatRelativeTime`, `formatDateTime`, `formatDate`, `formatTime`.

---

## 4. Cómo levantar / reiniciar el backend

```bash
# Matar el proceso actual
OLD=$(pgrep -f "inventario-0.1.0-SNAPSHOT.jar" | head -1); [ -n "$OLD" ] && kill "$OLD"
# Arrancar nuevo en segundo plano y registrar log
cd /home/jacobo/Imágenes/Proyecto_Optiplant_back
setsid bash -c 'exec java -jar target/inventario-0.1.0-SNAPSHOT.jar' > /tmp/optiplant_backend.log 2>&1 < /dev/null & disown
# Esperar readiness con poll a login (HTTP 200, ~5–7 intentos de 2s)
for i in $(seq 1 40); do
  code=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" -d '{"email":"admin2@optiplant.com","password":"Admin456!"}')
  [ "$code" = "200" ] && echo "listo tras $i" && break
  sleep 2
done
```
**Importante para pruebas E2E por API**: logins consecutivos del **mismo usuario en el mismo milisegundo producen JWTs idénticos** (mismo `iat`). Por eso, al probar revocación (logout), usa tokens separados por un `sleep(1100)` o aísla la prueba de logout al final. En producción esto no es un problema real.

**E2E por script**: copiar `/tmp/*.mjs` a `.../front` y `node`, luego borrar el `.tmp.mjs`. La limpieza de datos de prueba se hace por psql directo.

---

## 5. Datos actuales en `optiplant_dev` (después de limpieza)

- **Sucursales**: solo 2 — Central (id=1), Norte (id=2). (En fase anterior se creó/borró una tercera.)
- **Existencias producto 1** ("Fertilizante NPK", SKU `PRD-BVC1QUNW`): suc1 stock=**79**, suc2 stock=**15**.
- **Compras** (`orden_compra`): 4 órdenes (PO-001…PO-004), todas del proveedor 1 y todas con producto 1.
- **Ventas**: 0 (ftp vacío; el flujo de venta se probó y limpió).
- **Movimientos**: sin datos de prueba residuales (movimientos "RF009 test" y "E2E…" borrados).

---

## 6. Estado por fase — README DE TRABAJO (fases de cierre A–F)

### ✅ Fase A — UI Transferencias
Flujo E2E completo: crear → aprobar (ORIGEN + DESTINO) → preparar → despachar → recibir. Limpiado (0 transferencias resto).

### ✅ Fase B — Historial Ventas
`toVenta`/`fetchVentas`/`fetchVenta`, `DetalleVentaDialog`, `HistorialVentas`, toggle en `Ventas.tsx`. Venta de prueba creada y luego eliminada (incluido su movimiento de retiro y reversa de stock).

### ✅ Fase C — RF-012 + RF-007
- **RF-012 baja lógica de producto**: `ProductoService.inactivar()` + `PATCH /productos/{id}/estado`; `search` filtra solo `activo`; `ProductoResponse` ya traía `estado`. Frontend: `activo` en type, `inactivarProducto`, `useInactivarProducto`, `BajaProductoDialog`, dropdown "Dar de baja" en Inventario.
- **RF-007 CRUD sucursales**: `SucursalRequest`, `SucursalService` (create/update/inactivar + nombre único), `SucursalController` (GET/GET{id}/POST/PUT/PATCH). Frontend: feature nueva `src/features/sucursales/` (página `/administracion/sucursales` + nav `Store` + ruta lazy).

### ✅ Fase D — RF-060 rotación de inventario
`RotacionResponse` + `DashboardService.rotacion()` (30 días; unidades retiro por producto, stock físico actual, altaDemanda top-5, bajaDemanda = stock>0 sin retiros) + `GET /dashboard/rotacion`. Frontend: `RotacionData`/`ProductoRotacion`, `fetchRotacion`, `useRotacion`, `RotacionCard.tsx`, render en Dashboard; **mock de `fetchRotacion` añadido a App.test.tsx**.

### ✅ Fase E — Seguridad por sucursal/rol (RF-009, RF-063/064)
- `common/security/UsuarioActualService.java` (nuevo): `actual()` (resuelve Usuario del SecurityContext por email), `esAdministrador()`, `validarAccesoSucursal(Long)` → `AccessDeniedException("Solo puedes operar sobre tu propia sucursal (RF-009)")` si no-admin y sucursal no coincide o es null.
- `VentaService.registrar` y `MovimientoInventarioService.registrar`: llaman `validarAccesoSucursal(sucursal.getId())` tras resolver la sucursal.
- `SecurityConfig`: `/api/v1/dashboard/red` → `hasRole("ADMINISTRADOR")`.
- `Dashboard.tsx`: renderiza `BranchNetworkCard` solo si `normalizarRol(user?.role) === "ADMINISTRADOR"`.
- Verificado E2E: operador en sucursal propia→201, ajena→403; admin exento→201; `/dashboard/red` admin→200, operador→403; `/dashboard/resumen` operador→200.
- **Nota RF-009**: usuario no-admin **sin sucursal (null)** → cualquier escritura le da 403 (negación por defecto; en dev `operador1` quedó con sucursal NULL).

### ✅ Fase F — Parciales menores (los 4: RF-005, RF-019, RF-034, RF-059)
Verificado E2E y limpiado.

#### RF-005 — logout + expiración de tokens
- Backend: `common/security/TokenRevocationService.java` (nuevo; blacklist in-memory `ConcurrentHashMap`), `POST /api/v1/auth/logout`, y `JwtAuthenticationFilter` descarta tokens revocados. `SecurityConfig` permite `/api/v1/auth/logout` sin auth.
- Frontend: ya existía botón "Cerrar sesión" en `AppLayout` → `logout()` de AuthContext (borra localStorage). Token sigue expirando a las 24h (`jwt.expiration`).
- E2E: logout→200; el token revocado→**401**; re-login→**200**. (Nota: la blacklist se pierde al reiniciar el backend.)

#### RF-019 — retiro por merma
- Backend: `MovimientoInventarioService.registrar` acepta `"merma"` (se trata como salida con chequeo de stock). **Migración nueva `V17__add_merma_to_movimiento_tipo.sql`** (rebaja y recrea el CHECK permitiendo `'merma'`).
- Frontend: `TipoAjuste = "entrada" | "salida" | "merma"`, opción "Registrar merma" en el dropdown de Inventario, `AjusteStockDialog` con textos/motivo de merma, mapeo en `inventario.api.ts` (entrada→`ingreso`, merma→`merma`, resto→`retiro`).
- E2E: 201, stock 79→77.
- ⚠️ Efectos secundarios a tener en cuenta: `DashboardService.toMovementResponse` clasifica cualquier no-`ingreso` como `salida`; `rotacion()` solo cuenta `retiro` (la merma no cuenta como demanda — intencional).

#### RF-034 — histórico de compras por producto
- Backend: `GET /ordenes-compra` acepta `productoId` (parámetro opcional); en `OrdenCompraService.buscar` se hace `join("lineas")` con `query.distinct(true)` y predicado sobre `lineas.producto.id`.
- Frontend: `FiltrosComprasValue.productoId`, selector de producto en `FiltrosCompras` (productos derivados de las órdenes cargadas), filtrado en `Compras.tsx`.
- E2E: producto 1→4 órdenes; producto inexistente→0.

#### RF-059 — ventas del mes en curso vs meses anteriores
- Backend: `VentaRepository.sumTotalPorMesDesde` (agrupa `SUM(total)` por año/mes), DTO `VentasMensualesResponse`, `DashboardService.ventasMensuales()` (últimos 4 meses incluido el actual, rellena ceros), `GET /dashboard/ventas-mensuales`.
- Frontend: types `VentasMensualesData`/`MesVentas`, `fetchVentasMensuales`, `useVentasMensuales`, nuevo componente `VentasMensualesCard.tsx` (gráfico de barras, resalta el mes actual, badge de variación vs anteriores, enlace al historial `/ventas`), render en `Dashboard.tsx`. **Mock de `fetchVentasMensuales` añadido a App.test.tsx**.
- E2E: 200 con 4 meses; mes actual reflejó la venta de prueba (total=30) antes de la limpieza.

---

## 7. Archivos relevantes (cambios de fases A–F)

### Backend (nuevos)
- `common/security/TokenRevocationService.java` (RF-005)
- `common/security/UsuarioActualService.java` (RF-009)
- `catalogo/dto/SucursalRequest.java`, `catalogo/service/SucursalService.java` (RF-007)
- `dashboard/dto/RotacionResponse.java` (RF-060), `dashboard/dto/VentasMensualesResponse.java` (RF-059)
- `db/migration/V17__add_merma_to_movimiento_tipo.sql` (RF-019)

### Backend (modificados)
- `common/security/JwtAuthenticationFilter.java`, `SecurityConfig.java`, `identidad/controller/AuthController.java` (RF-005)
- `inventario/service/MovimientoInventarioService.java` (RF-009/RF-019)
- `ventas/service/VentaService.java` (RF-009), `ventas/repository/VentaRepository.java` (RF-059)
- `compras/controller/OrdenCompraController.java`, `compras/service/OrdenCompraService.java` (RF-034)
- `dashboard/controller/DashboardController.java`, `dashboard/service/DashboardService.java` (RF-059/RF-060/RF-063)
- `catalogo/controller/ProductoController.java`, `catalogo/service/ProductoService.java`, `inventario/controller/SucursalController.java` (RF-007/RF-012)
- Test: `ventas/service/VentaServiceTest.java` (añadido `@Mock UsuarioActualService` para RF-009)

### Frontend (feature/componet clave)
- `src/api/client.ts` — interceptor 401.
- `src/features/auth/context/AuthContext.tsx` — persiste `usuarioId`; `logout` borra localStorage.
- `src/features/dashboard/` — `VentasMensualesCard.tsx` (nuevo), `RotacionCard.tsx`, `Dashboard.tsx` (gates por rol), `types.ts`, `api/dashboard.api.ts`, `hooks/useDashboardQueries.ts`.
- `src/features/inventario/` — merma (dropdown/dialog/api), baja lógica (dialog/api/hook).
- `src/features/sucursales/` — feature CRUD nueva.
- `src/features/compras/` — filtro por producto.
- `src/features/ventas/`, `src/features/transferencias/` — historial/toggle y flujo completo.
- `src/routes/index.tsx` — rutas lazy (incl. `/administracion/sucursales`, `/ventas`).
- `src/App.test.tsx` — mocks de dashboard api (con `fetchRotacion` y `fetchVentasMensuales`) y de AuthContext (usuario admin).
- `docs/CHANGELOG.md` — actualizado.

---

## 8. Endpoints backend relevantes

- `POST /api/v1/auth/login` (permitAll), `POST /api/v1/auth/logout` (permitAll + revoca token)
- `GET/POST/PUT/PATCH /api/v1/productos` (+ `PATCH /productos/{id}/estado`, `GET /api/v1/existencias`, `/api/v1/precios`, `/api/v1/unidades-medida`)
- `/api/v1/usuarios/**` → solo ADMINISTRADOR
- `GET/POST/PUT/PATCH /api/v1/sucursales`
- `POST /api/v1/movimientos-inventario` (tipos: `ingreso`/`retiro`/`merma`)
- `GET/POST /api/v1/ordenes-compra` (+ `{id}/estado`, `{id}/recepcion`) — `GET` filtra por `productoId`
- `POST /api/v1/ventas`, `GET /api/v1/ventas` (paginado), `GET /api/v1/ventas/{id}`, `GET /api/v1/ventas/catalogo`
- `/api/v1/transferencias` (flujo multi-item, V15)
- **Dashboard**: `GET /api/v1/dashboard/resumen`, `/movimientos`, `/rotacion`, `/ventas-mensuales`, `/red` (solo ADMINISTRADOR)
- Excepciones: `BusinessRuleException`→400, `AccessDeniedException`→403, `ResourceNotFoundException`→404 (`GlobalExceptionHandler`).

---

## 9. Pendientes / observaciones

1. **Commitear fases A–F** (todo el trabajo está sin commitear en rama `main`). Revisar `git status` en backend y frontend antes de commitear.
2. **Contraseña de `operador1@x.com`** quedó en `Operador123!` (cambio durante pruebas; no se revirtió). Decidir si dejarla o resetear.
3. **Debounce/validación de negocio en merma**: la merma exige un motivo no vacío (ya validado); considerar si debe requerir aprobación o un rol específico (hoy la hace cualquier rol con acceso a la sucursal).
4. **Seguridad**: la blacklist de tokens es en memoria (se pierde al reiniciar). Si se requiere revocación persistente, habría que persistirla (sobre-ingeniería para demo).
5. **RF-009 en dev**: `operador1` tiene sucursal NULL → no puede registrar movimientos (403). Para demostrarlo en demo, asignarle una sucursal.
6. **Tests**: solo hay tests de `UsuarioService`, `VentaService` y `Application` en backend (8 en total); frontend solo 2 tests de Dashboard. Considerar ampliar cobertura (p. ej. pruebas de merma, filtro por producto, ventas mensuales) si se quiere más robustez.

---

## 10. Fases de cierre (checklist global)

- [x] Fase A — UI Transferencias
- [x] Fase B — Historial Ventas
- [x] Fase C — Baja lógica producto (RF-012) + CRUD sucursales (RF-007)
- [x] Fase D — Rotación de inventario (RF-060)
- [x] Fase E — Seguridad por sucursal/rol (RF-009, RF-063/064)
- [x] Fase F — Parciales menores (RF-005, RF-019, RF-034, RF-059)
- [ ] Commitear el trabajo (fases A–F en backend y frontend)
