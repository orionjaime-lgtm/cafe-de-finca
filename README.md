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
- [ ] Esqueleto del backend (Spring Boot conectado a la base)
- [ ] Conversor a gramos con tests
- [ ] Módulo de producción y stock
- [ ] Módulo de ventas y finanzas
- [ ] Esqueleto del frontend y pantallas
- [ ] Alertas de stock y panel de caducidad
- [ ] Pruebas finales y pase a producción