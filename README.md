# Café de Finca ☕

> **Sistema de gestión integral para una tostaduría de café de especialidad.**
> Control de producción, stock en gramos, ventas omnicanal (B2B y B2C) y cobros, en una aplicación web usable desde el celular del galpón y la PC del mostrador.

---

## 📌 1. Acerca del proyecto

**Café de Finca** es una tostaduría que combina manufactura (importación de grano verde, tueste y molienda) con ventas mayoristas (B2B) y minoristas (B2C). Hoy gestiona todo en planillas de Excel, lo que genera tres problemas:

| Problema | Consecuencia |
|---|---|
| **Stock irreal.** Se vende café tostado según el inventario de grano verde. | Se prometen entregas de un producto que todavía no existe. |
| **Unidades mezcladas.** Se registran valores como "12 kg", "8000 g" y "6 bolsas". | Es imposible descontar stock con exactitud. |
| **Cobros mezclados.** Deudas corporativas a 15 o 30 días conviven con pagos inmediatos de mostrador o Instagram. | No se sabe quién debe, cuánto ni desde cuándo. |

Este sistema reemplaza esa gestión con reglas que se hacen cumplir desde la base de datos y el backend.

---

## 🎯 2. Alcance del MVP 1

### Incluye

- **Conversor automático de unidades.** Todo lo que se ingresa (kilos, libras, bolsas) se convierte y se guarda **siempre en gramos**.
- **Producción y merma.** Las órdenes de tueste descuentan el café verde, registran la pérdida de peso y suman el café tostado al inventario.
- **Punto de venta (POS).** Toma de pedidos con tipo de molienda obligatorio, descuento de stock y separación de cobros: cuenta corriente para B2B, pago inmediato para B2C.
- **Control de caducidad.** Panel para ver la frescura de los lotes tostados y evitar vender producto rancio.
- **Alertas de stock mínimo.** Aviso cuando el café verde o tostado llega a un umbral crítico.

### Queda para el Hito 2

- Integración con AFIP (facturación electrónica oficial).
- Reportería financiera avanzada y gráficos de rentabilidad.
- App móvil nativa y e-commerce externo.

---

## 🏗️ 3. Arquitectura

```
┌───────────────────┐      HTTP / JSON      ┌─────────────────────┐      SQL      ┌──────────────────┐
│  Frontend         │ ────────────────────▶ │  Backend            │ ────────────▶ │  Base de datos   │
│  React (SPA)      │ ◀──────────────────── │  Spring Boot (Java) │ ◀──────────── │  PostgreSQL 15   │
└───────────────────┘                       └─────────────────────┘               └──────────────────┘
        └──────────────────────── todo corre en contenedores Docker ────────────────────────┘
```

| Capa | Tecnología | Qué hace |
|---|---|---|
| **Frontend** | React | Pantallas para el operario (celular) y el vendedor (PC). |
| **Backend** | Spring Boot, Java 21, Maven | Reglas de negocio: conversión a gramos, merma, descuento de stock, cobros. |
| **Base de datos** | PostgreSQL 15 | Almacenamiento con integridad referencial y restricciones (`CHECK`, `UNIQUE`, `NOT NULL`). |
| **Infraestructura** | Docker y Docker Compose | Mismo entorno para todo el equipo. |

### Convención del backend

El backend se organiza en capas, cada una con una sola responsabilidad:

```
controller  →  recibe las peticiones HTTP
service     →  reglas de negocio (aquí vive la lógica)
repository  →  acceso a la base de datos
entity      →  clases que representan las tablas
dto         →  datos que viajan entre backend y frontend
```

---

## 🗄️ 4. Modelo de datos

El esquema está en [`data-base/01-schema.sql`](data-base/01-schema.sql) y los datos base en [`data-base/02-datos-iniciales.sql`](data-base/02-datos-iniciales.sql).

| Módulo | Tablas |
|---|---|
| **Abastecimiento** | `origen_cafe`, `proveedor`, `lote_cafe_verde` |
| **Producción** | `perfil_tueste`, `orden_tueste`, `lote_cafe_tostado` |
| **Catálogo de venta** | `presentacion`, `tipo_molienda` |
| **Clientes y ventas** | `cliente`, `canal_venta`, `pedido`, `detalle_pedido`, `suscripcion`, `detalle_suscripcion`, `hoja_ruta` |
| **Finanzas** | `factura`, `medio_pago`, `pago` |

### Reglas que la base hace cumplir

- Los stocks nunca pueden ser negativos.
- El café tostado obtenido no puede pesar más que el verde usado.
- `merma_real_pct` se calcula sola a partir de los pesos.
- Disponible + reservado no puede superar el peso inicial del lote tostado.
- Cada orden de tueste tiene como máximo un lote tostado, y cada pedido como máximo una factura.

---

## 🚀 5. Puesta en marcha local

### Requisitos

| Herramienta | Para qué |
|---|---|
| [Git](https://git-scm.com/) | Control de versiones |
| [Docker Desktop](https://www.docker.com/products/docker-desktop/) (con WSL 2 en Windows) | Levantar la base de datos |
| [VS Code](https://code.visualstudio.com/) | Editor |
| JDK 21 y la extensión *Extension Pack for Java* | Solo backend |
| [Node.js](https://nodejs.org/) | Solo frontend |

### Levantar la base de datos

```bash
git clone https://github.com/orionjaime-lgtm/cafe-de-finca.git
cd cafe-de-finca
docker-compose up -d
```

La primera vez, PostgreSQL ejecuta automáticamente los archivos de `data-base/` en orden alfabético y deja las tablas creadas con los datos base.

### Datos de conexión (solo desarrollo local)

| Campo | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `5432` |
| Base | `cafedefinca_db` |
| Usuario | `admin` |
| Contraseña | `adminpassword` |

Estas credenciales son **solo para desarrollo local**. No deben usarse en producción.

Para conectarte desde VS Code, instalá las extensiones **SQLTools** y **SQLTools PostgreSQL/Cockroach Driver** y creá una conexión con los datos de arriba.

### Comandos útiles

| Acción | Comando |
|---|---|
| Prender la base | `docker-compose up -d` |
| Apagar la base (conserva los datos) | `docker-compose down` |
| Ver los registros (logs) | `docker-compose logs db` |
| **Recrear la base desde cero** (borra los datos) | `docker-compose down -v` y luego `docker-compose up -d` |

> ⚠️ Los scripts de `data-base/` solo se ejecutan cuando la base se crea de cero. Si alguien cambia el esquema, **todos tienen que recrear su base** con `docker-compose down -v`.

### Levantar el backend

Con la base de datos prendida:

```bash
cd backend
./mvnw spring-boot:run        # en Windows: .\mvnw.cmd spring-boot:run
```

La API queda en `http://localhost:8080/api/...`. Si el puerto 8080 está ocupado:

```bash
./mvnw spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

Para correr los tests (necesitan la base prendida): `./mvnw test`.

---

## 🔌 Backend: endpoints del grupo A

Estos son los primeros endpoints del backend. Todos son de **solo lectura** (`GET`) y alimentan el formulario de pedidos, el panel de caducidad y el panel de alertas.

| Endpoint | Para qué pantalla | Qué devuelve |
|---|---|---|
| `GET /api/clientes` | Desplegable del formulario de pedidos | Clientes ordenados por nombre. Acepta `?tipo=B2B` o `?tipo=B2C`. |
| `GET /api/presentaciones` | Desplegable del formulario de pedidos | Envases (Bolsa 250g, 1kg…) con su peso en gramos, del más chico al más grande. |
| `GET /api/tipos-molienda` | Desplegable del formulario de pedidos | Grano Entero, Espresso, Italiana, Filtro. |
| `GET /api/lotes-tostados` | Panel de caducidad y "Café disponible" | Lotes tostados con stock, del más antiguo al más nuevo (FIFO), con días restantes y estado de frescura. Con `?soloDisponibles=true` devuelve solo los que hoy se pueden vender. |
| `GET /api/caducidad/resumen` | Los 3 números de arriba del panel de caducidad | `{ activos, porVencer, vencidos }` |
| `GET /api/alertas` | Panel "Alertas Activas" | Alertas de stock de café verde: primero las `CRITICO`, después las `ADVERTENCIA`. |
| `GET /api/origenes/stock` | Tabla "Límites de Stock Mínimo" | Cada origen con su stock de café verde, su mínimo y su estado (`OK`, `BAJO`, `CRITICO`). |

Si se manda un dato inválido (por ejemplo `?tipo=XYZ`), la API responde `400` con un JSON claro: `{ "estado": 400, "mensaje": "..." }`.

### Backend: catálogos de producción y lotes verdes

Alimentan los desplegables de ingreso de café verde y, más adelante, de órdenes de tueste.

| Endpoint | Qué hace |
|---|---|
| `GET /api/proveedores` | Proveedores ordenados por razón social. |
| `GET /api/origenes` | Orígenes (país, región, variedad, mínimo de alerta). El stock por origen sigue en `GET /api/origenes/stock`. |
| `GET /api/perfiles-tueste` | Perfiles Claro, Medio y Oscuro, con su merma esperada y horas de reposo. |
| `GET /api/lotes-verdes` | Lotes de café verde del más antiguo al más nuevo (FIFO), con su stock en gramos. |
| `POST /api/lotes-verdes` | Ingresa un lote. Responde `201` con el lote creado. |

Cuerpo del `POST /api/lotes-verdes`:

```json
{ "idOrigen": 1, "idProveedor": 2, "codigoLote": "LV-HUI-002",
  "cantidad": 150, "unidad": "kg", "cantBolsas": 5, "fechaIngreso": "2026-10-28" }
```

- La cantidad llega con su unidad (`g`, `kg` o `lb`) y se convierte a gramos con `ConversorUnidades`. El stock actual arranca igual al peso inicial.
- `fechaIngreso` es opcional (por defecto, hoy) y no puede ser futura.
- `cantBolsas` es obligatorio porque la tabla lo exige: el peso de una bolsa de café verde no es fijo, por eso no se calcula a partir de la unidad.
- Errores: `400` si falta un dato, la unidad no existe o el origen o el proveedor no existen; `409` si el `codigoLote` ya está usado.

> El backend se generó con IA y fue verificado por el equipo. Los prompts utilizados están en [`docs/uso-de-ia.md`](docs/uso-de-ia.md).

### Cómo está organizado el código

Siguiendo la convención del punto 3, cada endpoint pasa por las mismas capas:

```
backend/src/main/java/com/cafedefinca/backend/
├── controller/   ClienteController, CatalogoController, LoteTostadoController,
│                 CaducidadController, AlertaController, OrigenController
├── service/      ClienteService, CatalogoService, LoteTostadoService,
│                 CaducidadService, AlertaService, OrigenService
├── repository/   una interfaz por tabla (Spring arma el SQL)
├── entity/       una clase por tabla usada: cliente, presentacion, tipo_molienda,
│                 origen_cafe, lote_cafe_verde, perfil_tueste, orden_tueste, lote_cafe_tostado
├── dto/          lo que viaja al frontend (records de Java)
├── enums/        EstadoFrescura, EstadoStock, NivelAlerta
├── exception/    errores en JSON claro (ManejadorGlobalErrores)
├── config/       zona horaria (TiempoConfig) y permiso CORS para React (WebConfig)
└── util/         ConversorUnidades: la única clase que convierte gramos a kilos
```

Cada clase está dividida con comentarios por secciones (VARIABLES, CONSTRUCTOR, cada método) y las constantes de negocio están al principio de cada clase, para poder cambiarlas en un solo lugar.

### Reglas de negocio que se implementaron

**Frescura (`CaducidadService`)**
- `diasRestantes` = días entre hoy y `fecha_caducidad`.
- `VENCIDO` si la fecha ya pasó **o** si el lote fue marcado `VENCIDO` en la base (por ejemplo, al descartarlo).
- `POR_VENCER` si quedan 7 días o menos (constante `DIAS_AVISO_VENCIMIENTO`). El día de la caducidad todavía cuenta como por vencer.
- `OPTIMO` en cualquier otro caso.
- En el resumen, los tres números son **excluyentes**: `activos` son solo los lotes `OPTIMO`, y los `porVencer` y `vencidos` se cuentan aparte (cada lote suma en uno solo). Solo se cuentan lotes que tienen stock.

**¿Se puede vender un lote hoy? (`LoteTostadoService`)** Tienen que cumplirse las 4 condiciones:
1. Terminó el reposo (`fecha_disponible_venta` ya llegó).
2. Le queda stock disponible (no reservado).
3. No está vencido.
4. No está marcado `AGOTADO` en la base.

Se calcula con las fechas y no solo con `estado_disponibilidad`, porque todavía no hay ningún proceso que actualice ese estado solo (por ejemplo, pasar de `REPOSO` a `DISPONIBLE`).

**Stock por origen (`OrigenService`)**
- Stock de un origen = suma de `stock_actual_gramos` de todos sus lotes de café verde.
- `OK` si alcanza el mínimo, `BAJO` si no lo alcanza, `CRITICO` si además está en 0. Un origen con mínimo 0 nunca entra en alerta.

**Alertas (`AlertaService`)**: no se guardan en la base, se calculan en cada consulta.
- `CRITICO`: el origen está `BAJO` o `CRITICO`.
- `ADVERTENCIA` (predictiva): el stock todavía está bien, pero al ritmo de consumo llega al mínimo en 7 días o menos.
  - Consumo diario = gramos de verde usados en tuestes completados de los últimos 30 días ÷ 30.
  - Días hasta el mínimo = (stock − mínimo) ÷ consumo diario, redondeado hacia arriba.
  - Ejemplo con los datos de prueba: Brasil Santos tiene 12 kg y mínimo 10 kg; se tostaron 25 kg en 30 días (0.8 kg/día) → *"Reabastecer en 3 días"*.
- Las constantes `DIAS_VENTANA_CONSUMO = 30` y `DIAS_AVISO_REABASTECIMIENTO = 7` están al principio de la clase.

### Decisiones técnicas

- **Gramos y dinero en `BigDecimal`**, nunca `double` (regla 2). El JSON devuelve gramos y el frontend decide cómo mostrarlos.
- **Las entidades solo tienen getters** porque por ahora no se escribe nada. Cuando se agreguen los `POST`, se suman los setters o constructores que hagan falta.
- **`merma_real_pct`** se mapeó con `insertable = false, updatable = false` (regla 4).
- **Reloj inyectable (`Clock`)**: los services piden la fecha a `TiempoConfig` en lugar de usar `LocalDate.now()`. Así los tests pueden fijar "hoy" y probar las fechas.
- **Zona horaria**: Windows con región Argentina le da a Java la zona vieja `America/Buenos_Aires`, que PostgreSQL 15 rechaza al conectarse (`invalid value for parameter "TimeZone"`). Por eso `BackendApplication` y los tests (`pom.xml`) fijan `America/Argentina/Buenos_Aires`.
- **CORS**: `WebConfig` deja que React llame a la API desde `localhost:5173` (Vite) y `localhost:3000`.
- **Consultas sin N+1**: `/api/lotes-tostados` trae el lote, la orden de tueste, el origen y el perfil en **una sola consulta** (`JOIN FETCH`).

### Tests

| Clase | Qué prueba |
|---|---|
| `BackendApplicationTests` | Que la app arranque y que **cada entidad coincida con el esquema SQL** (`ddl-auto=validate`). |
| `CaducidadServiceTest` | Los bordes de la frescura (−1, 0, 7 y 8 días) y el lote marcado vencido a mano. |
| `OrigenServiceTest` | Los bordes de `OK` / `BAJO` / `CRITICO`. |
| `AlertaServiceTest` | Alertas críticas, la predictiva de Brasil Santos (3 días), que el consumo lento no avise y el orden de las alertas. |

---

## 📂 6. Estructura del repositorio

```text
cafe-de-finca/
├── backend/            # Spring Boot (Java)
├── frontend/           # React
├── data-base/          # Scripts SQL: esquema y datos iniciales
│   ├── 01-schema.sql
│   └── 02-datos-iniciales.sql
├── docs/               # Documentación: decisiones, contrato de la API
├── docker-compose.yml  # Base de datos local
├── .gitignore
└── README.md
```

> `backend/`, `frontend/` y `docs/` se agregan a medida que cada parte del equipo sube su trabajo.

---

## 🧭 7. Reglas técnicas

Estas reglas existen para evitar errores de precisión y de esquema. Aplican a todo el backend.

1. **Todo peso se guarda en gramos.** Cualquier conversión (kg, lb, bolsas) se hace en una única clase utilitaria y no se repite en otros lugares.
2. **`BigDecimal` para gramos y dinero. Nunca `double` ni `float`.**
3. **El esquema lo manejan los archivos `.sql`.** En `application.properties` usar `spring.jpa.hibernate.ddl-auto=validate`, nunca `update` ni `create`.
4. **`merma_real_pct` es una columna calculada por la base.** En la entidad Java se declara con `@Column(insertable = false, updatable = false)`.
5. **El stock se descuenta dentro de una transacción** (`@Transactional`), para que nunca quede a medias.
6. **Los errores de negocio devuelven un JSON claro** (por ejemplo, "Stock insuficiente") y no un error 500.

---

## 🤝 8. Cómo trabajamos

### Reglas de oro

- **Nadie sube directo a `main`.** La rama está protegida.
- **Una rama por tarea**, con nombre `feature/nombre-corto` (por ejemplo, `feature/lote-verde`).
- **Todo cambio entra por Pull Request** y lo revisa otra persona antes de sumarse a `main`.
- **Tareas chicas.** Cada tarea es un Issue en el tablero del proyecto, con una persona asignada.

### Paso a paso para cada tarea

```bash
# 1. Traer lo último de main
git checkout main
git pull origin main

# 2. Crear tu rama
git checkout -b feature/nombre-de-la-tarea

# 3. Trabajar y guardar cambios
git add .
git commit -m "Descripción corta de lo que hiciste"

# 4. Subir tu rama
git push -u origin feature/nombre-de-la-tarea
```

Después, en GitHub, apretá **Compare & pull request**, describí qué hiciste y pedí revisión.

### Antes de abrir un Pull Request

- [ ] Mi rama está actualizada con `main`.
- [ ] El proyecto compila y corre en mi máquina.
- [ ] Probé lo que cambié.
- [ ] Si toqué una regla de stock, conversión o dinero, agregué un test.
- [ ] No subí contraseñas, archivos `.env` ni datos personales.

### Rutina de equipo

- Dos reuniones cortas por semana (15 minutos): qué hice, qué voy a hacer, qué me traba.
- Los Pull Requests se revisan en un día.

---

## 👥 9. Equipo

| Persona | Rol |
|---|---|
| **Orión** | base de datos, tests |
| **Agustín** | fronted react |
| **Ignacio** | backend springboot |
| **Thiago** | backend springboot |

---

## 📅 10. Plan de trabajo

El proyecto avanza de adentro hacia afuera (*Data-First*): primero los datos, después la lógica y por último las pantallas.

| Etapa | Semanas | Foco |
|---|---|---|
| **Sprints 1 y 2** | 1 a 4 | Docker, base de datos PostgreSQL y API en Spring Boot (conversión a gramos, merma, stock) |
| **Sprint 3** | 5 y 6 | Pantallas en React conectadas a la API |
| **Buffer** | 7 | Pruebas, corrección de errores y pase a producción |

### Estado actual

- [x] Entorno Docker con PostgreSQL
- [x] Esquema de base de datos y datos iniciales
- [x] Repositorio con colaboradores y `main` protegida
- [x] Esqueleto del backend (Spring Boot conectado a la base)
- [x] Endpoints de lectura del grupo A (catálogos, lotes tostados, caducidad, alertas y stock por origen)
- [ ] Conversor a gramos con tests
- [ ] Módulo de producción y stock
- [ ] Módulo de ventas y finanzas
- [ ] Esqueleto del frontend y pantallas
- [ ] Alertas de stock y panel de caducidad
- [ ] Pruebas finales y pase a producción