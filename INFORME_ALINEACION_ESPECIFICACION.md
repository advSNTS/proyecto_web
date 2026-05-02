# Informe de alineación del backend con la especificación multiempresa / BPMN

## 1. Problemas detectados en el análisis inicial

- **Proceso** no tenía `empresa` ni `pool` ni `estado` enum; usaba solo `borrador`/`activo` sin vínculo explícito a empresa.
- **Aislamiento multiempresa**: no había filtro sistemático por empresa en consultas de proceso; `EmpresaXProceso` cubría parte del modelo pero no sustituía `empresaId` en `Proceso`.
- **Pool por defecto**: no existía entidad `Pool` ni creación automática al registrar empresa.
- **Roles de sistema** (`ADMIN`/`EDITOR`/`READER`): no estaban modelados; solo existían roles funcionales (`Rol`) y `RolXEmpleado`.
- **Compartición** tipo `ProcesoCompartido(proceso, pool, permiso)` no existía como entidad dedicada (convivía con `EmpresaXProceso`).
- **Lanes, RolPool, UsuarioRolPool, mensajes throw/catch, mensaje externo, tarea integración**: ausentes.
- **Eliminación lógica**: procesos pasan a `INACTIVO`; faltaba `eliminado` en nodos y arcos; `NodoService` hacía borrado físico; `ArcoService` borraba físicamente.
- **Arcos al eliminar actividad/gateway**: no se marcaban como eliminados.
- **Rol funcional**: al eliminar se desactivaban `Requiere` en cascada; la especificación exige **rechazar** si hay asignaciones activas.
- **Seguridad**: no había Spring Security ni JWT; no había contexto de empresa en petición.
- **Herencia JPA `ElementoProceso`**: el código usaba `Nodo` + `Actividad`/`Gateway` 1:1 (equivalente funcional al diagrama); no se reestructuró a `@Inheritance` para no romper datos y tests masivamente.

## 2. Cambios realizados (resumen)

### Entidades nuevas

- `Pool`, `Lane`, `RolPool`, `UsuarioRolPool`, `ProcesoCompartido`, `MensajeThrow`, `MensajeCatch`, `MensajeExterno`, `TareaIntegracion`, `EmpleadoRolSistema`.

### Entidades modificadas

- **`Proceso`**: `EstadoProceso` (`BORRADOR`, `PUBLICADO`, `INACTIVO`), `Empresa empresa`, `Pool pool`.
- **`Empleado`**: `adminGlobal`, y en alta se crea **`EmpleadoRolSistema`** con `READER` por defecto.
- **`Rol`**: `descripcion` (rol de proceso / funcional).
- **`Nodo`**: `eliminado`.
- **`Arco`**: `eliminado`.
- **`Actividad`**: `tipoActividad`, `Lane lane` (opcional).

### Repositorios y servicios

- Repositorios Spring Data para todas las entidades nuevas; consultas de proceso por empresa y estado no `INACTIVO`.
- **`EmpresaService`**: tras crear empresa, persiste **pool por defecto**.
- **`ProcesoService`**: exige `nitEmpresa`, asigna pool (por defecto o `poolId`), `eliminar` → `INACTIVO`, historial con JSON como antes.
- **`NodoService` / `ArcoService`**: validan pertenencia al proceso vía empresa; **borrado lógico** de arcos y nodos donde aplica.
- **`ActividadService` / `GatewayService`**: al eliminar, **marcan arcos** conectados como `eliminado` y el **nodo** como eliminado (gateway también desactiva el registro gateway).
- **`RolService`**: eliminación bloqueada si existe `Requiere` activo (`409 CONFLICT`).
- **`ProcesoCompartidoService`**: compartir con pool y permiso; valida **`TipoRolSistema.ADMIN`** para la empresa; auditoría en `HistorialProceso` con `tipoAccion=COMPARTIR`.
- Servicios CRUD: **`PoolService`**, **`LaneService`**, **`MensajeThrowService`**, **`MensajeCatchService`**, **`MensajeExternoService`**, **`TareaIntegracionService`**.

### Seguridad

- Dependencias: `spring-boot-starter-security`, JJWT 0.12.6.
- **`SecurityConfig`**: JWT stateless si `app.security.enabled=true` (por defecto en `application.properties`); en **tests** `app.security.enabled=false` (todo permitido).
- **`JwtAuthenticationFilter`**, **`JwtService`**, **`UsuarioPrincipal`** (claims: `nit`, `adminGlobal`, `authorities`).
- **`EmpleadoService.login`**: devuelve `token` cuando la seguridad está habilitada.
- **`SecurityUtils`**: resolución de `nitEmpresa` desde query param o JWT.

### API

- Prefijo **`/api/...`** en todos los controladores.
- Roles de proceso: **`/api/roles-proceso`** (antes `/roles`).
- Nuevos: **`/api/pools`**, **`/api/lanes`**, **`/api/mensajes-throw`**, **`/api/mensajes-catch`**, **`/api/mensajes-externos`**, **`/api/tareas-integracion`**.
- **`POST /api/procesos/{id}/compartir`**, **`GET /api/procesos/{id}/compartidos`**.
- Procesos y recursos multiempresa: query **`nitEmpresa`** cuando no hay JWT (o para forzar tenant en pruebas).

### Excepciones

- **`BusinessException`** con `HttpStatus`; **`GlobalExceptionHandler`** respeta el código; `RuntimeException` delega si es `BusinessException`.

### Base de datos

- Script de referencia para DBA: `src/main/resources/db/migracion_postgresql_especificacion.sql` (backfill y tablas nuevas; revisar `NOT NULL` antes de aplicar en producción).

## 3. Endpoints nuevos o modificados

| Método | Ruta | Notas |
|--------|------|--------|
| * | `/api/*` | Prefijo común |
| GET | `/api/procesos?nitEmpresa=` | Lista por empresa |
| GET/PUT/DELETE | `/api/procesos/...?nitEmpresa=` | Filtrado por empresa |
| POST | `/api/procesos/{id}/compartir` | Body: `poolId`, `permiso`; requiere rol sistema ADMIN |
| GET | `/api/procesos/{id}/compartidos` | |
| GET | `/api/pools`, `/api/pools/{id}` | |
| CRUD | `/api/lanes` | |
| CRUD | `/api/mensajes-throw`, `/api/mensajes-catch` | |
| CRUD | `/api/mensajes-externos` | Sin filtro por empresa (configuración global) |
| CRUD | `/api/tareas-integracion` | |
| POST | `/api/empleados/login` | Público; opcionalmente devuelve JWT |

## 4. Pruebas

- Actualizados tests que crean procesos/nodos/arcos para incluir **empresa + `nitEmpresa`**.
- Nuevo: **`RolServiceEliminacionConflictTest`** (eliminación de rol con `Requiere` activo → conflicto).
- **`mvn test`**: en verde (107 tests en la última ejecución).

## 5. Riesgos y TODOs

- **Reajuste inteligente de arcos** (reenrutar en lugar de solo marcar `eliminado`): solo se implementó **marcado lógico** de arcos incidentes al borrar actividad/gateway/nodo.
- **Correlación real** entre `MensajeThrow` y `MensajeCatch` / sistemas externos: solo persistencia y API; motor de ejecución **no** incluido.
- **`ElementoProceso` con `@Inheritance`**: no aplicado; se mantiene `Nodo` como elemento de diagrama.
- **`EmpresaXProceso`**: se conserva; convive con `ProcesoCompartido` (enfoque distinto: empresa vs pool).
- **Administrador global**: flag `adminGlobal` en `Empleado`; la mayoría de endpoints siguen exigiendo `nitEmpresa` explícito (ampliar rutas “admin” si se desea listar todo sin tenant).
- **Sonar**: revisar cobertura nueva en servicios añadidos si el umbral del proyecto es estricto.

## 6. Cómo probar manualmente

1. **Perfil local**: `app.security.enabled=false` o usar `application-test` solo en tests.
2. Crear empresa: `POST /api/empresas` → debe existir pool por defecto (`GET /api/pools?nitEmpresa=NIT`).
3. Crear empleado: `POST /api/empleados`; opcionalmente insertar filas `empleado_rol_sistema` con `ADMIN` para probar compartir.
4. Con seguridad **true**: `POST /api/empleados/login` → usar `Authorization: Bearer <token>` y omitir `nitEmpresa` en algunos GET si el token lleva `nit`.
5. `POST /api/procesos` con `nitEmpresa` y campos del proceso; crear nodos con `nitEmpresa` en body; arcos igual.
6. Probar `POST /api/procesos/{id}/compartir` con usuario que tenga rol sistema `ADMIN` en esa empresa.
