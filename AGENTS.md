# AGENTS.md — Café de Finca

Guía para agentes de IA (y para personas nuevas) que trabajen en este repositorio. **Leelo completo antes de escribir código.** Si algo de acá contradice lo que ves en el código, avisá en lugar de elegir en silencio.

---

## 1. Qué es este proyecto

Sistema web de gestión para **Café de Finca**, una tostaduría de café de especialidad con un modelo híbrido: ventas mayoristas (**B2B**: cafeterías, hoteles, panaderías) y minoristas (**B2C**: consumidor final). Reemplaza una gestión en Excel que causa tres problemas:

1. **Stock irreal:** se vende café tostado según el stock de grano verde.
2. **Unidades mezcladas:** "12 kg", "8000 g", "6 bolsas" en la misma planilla.
3. **Cobros mezclados:** deudas corporativas a 15 o 30 días junto con pagos inmediatos (mostrador, Instagram, Mercado Pago).

Es el trabajo de **Prácticas Pre-Profesionales** de un equipo de 4 personas, con un cliente simulado. El código se genera con IA y el equipo lo verifica: **cada prompt importante se registra en `docs/uso-de-ia.md`**.

## 2. Estado actual (actualizar al terminar cada tarea)

| Parte                                                                                                                                     | Estado                                                          |
| ----------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------- |
| Base de datos (19 tablas, datos base y de prueba)                                                                                         | Hecha                                                           |
| Backend: 7 endpoints `GET` (clientes, presentaciones, tipos de molienda, lotes tostados, resumen de caducidad, alertas, stock por origen) | Hecho, con tests                                                |
| Backend: producción, pedidos, cobros, suscripciones, hojas de ruta                                                                        | **Pendiente**                                                   |
| Frontend: 6 pantallas (Alertas, Inventario, Tueste, Pedidos, Cobros, Caducidad)                                                           | Hechas con **datos de ejemplo**; todavía **no llaman a la API** |
| Login con roles, configuración, compras de clientes, suscripción con descuento                                                            | **Pendiente** (ver sección 9)                                   |

## 3. Stack

| Capa            | Tecnología                                                                                       |
| --------------- | ------------------------------------------------------------------------------------------------ |
| Backend         | Java 21, Spring Boot (versión en `backend/pom.xml`), Maven (`mvnw`), Spring Data JPA, Validation |
| Base de datos   | PostgreSQL 15 en Docker                                                                          |
| Frontend        | Next.js 16, React 19, TypeScript, Tailwind CSS 4, pnpm                                           |
| Infraestructura | Docker Compose (hoy solo la base de datos)                                                       |

## 4. Estructura del repositorio

```
cafe-de-finca/
├── AGENTS.md                 ← este archivo
├── README.md                 ← manual para personas (cómo levantar, cómo trabajar)
├── skills/                   ← habilidades para IAs (ver sección 10)
├── docs/                     ← documentación: uso de IA, requisitos, diagramas
├── data-base/                ← 01-schema.sql, 02-datos-iniciales.sql, 03-datos-prueba.sql
├── docker-compose.yml        ← PostgreSQL local
├── backend/                  ← Spring Boot (paquete com.cafedefinca.backend)
│   └── src/main/java/.../    controller, service, repository, entity, dto, enums,
│                             exception, config, util
└── frontend/                 ← Next.js (rutas en app/(dashboard)/…)
```

## 5. Comandos

```bash
# Base de datos (desde la raíz)
docker-compose up -d                       # prender
docker-compose down -v && docker-compose up -d   # RECREAR de cero (borra datos; necesario si cambia data-base/)

# Backend (con la base prendida)
cd backend && ./mvnw spring-boot:run       # Windows: .\mvnw.cmd spring-boot:run  → http://localhost:8080
cd backend && ./mvnw test                  # los tests necesitan la base prendida

# Frontend
cd frontend && pnpm install && pnpm dev    # http://localhost:3000
```

Conexión local a la base (solo desarrollo): `localhost:5432`, base `cafedefinca_db`, usuario `admin`, contraseña `adminpassword`.

## 6. Reglas obligatorias

Estas reglas no se negocian. Si una tarea parece exigir romperlas, pará y consultá.

1. **Todo peso se guarda y calcula en GRAMOS.** La conversión de unidades (kg, lb, bolsas) vive en **una sola clase**: `util/ConversorUnidades`. No convertir en otros lugares.
2. **`BigDecimal` para gramos y dinero. Nunca `double` ni `float`.**
3. **El esquema lo manejan los archivos `data-base/*.sql`.** `spring.jpa.hibernate.ddl-auto=validate`, nunca `update` ni `create`. **No modifiques las tablas ni inventes columnas.** Si falta algo, pedilo: los cambios de esquema los hace el responsable de la base y se avisa al equipo para recrear la base.
4. **`merma_real_pct` es una columna calculada por la base:** en la entidad va `@Column(insertable = false, updatable = false)`.
5. **Todo lo que descuenta stock o dinero va dentro de una transacción** (`@Transactional`) y valida antes de escribir.
6. **Errores de negocio devuelven JSON claro**, nunca un error 500 ni una traza de Java. Formato actual: `{ "estado": 400, "mensaje": "..." }` (`ErrorDto`, manejado en `ManejadorGlobalErrores`). Stock insuficiente debe responder **409**.
7. **Las entidades no se exponen por la API:** los controllers devuelven DTOs (records de Java).
8. **Las entidades no llevan setters.** Los cambios de estado se hacen con métodos de negocio (por ejemplo, `descontarStock(gramos)`, que lanza una excepción si no alcanza).
9. **La fecha actual se pide al `Clock` inyectado** (`config/TiempoConfig`), no a `LocalDate.now()`. Zona horaria: `America/Argentina/Buenos_Aires`.
10. **No subir secretos ni archivos locales** (`.env`, certificados, `target/`, `node_modules/`).

## 7. Convenciones de código

- Capas del backend: `controller` (solo recibe y responde) → `service` (reglas de negocio) → `repository` (acceso a datos). Las reglas **nunca** van en el controller.
- Nombres de clases, métodos y variables en **español**, como el resto del código.
- Indentación con **tabs** en Java. Cada clase se divide con comentarios de sección (`// VARIABLES`, `// CONSTRUCTOR`, cada método) y las constantes de negocio van **al principio**.
- Cada regla de negocio nueva lleva su **test** (`backend/src/test/...`). Las fechas se prueban con un `Clock` fijo.
- Rutas de la API: prefijo `/api`, sustantivos en plural y minúsculas con guiones (`/api/lotes-tostados`).
- Frontend: antes de escribir código de Next.js leé `frontend/AGENTS.md` (aviso generado por Next: esta versión tiene cambios que no están en tu entrenamiento; consultá la documentación incluida en `node_modules/next/dist/docs/`).

## 8. Reglas de dominio

**Producción**

- Café verde → **orden de tueste** → **lote tostado**. Un lote tostado nace de **una sola** orden (relación 1 a 1).
- Merma = `(peso verde usado − peso tostado obtenido) / peso verde usado`. El tostado nunca pesa más que el verde.
- El lote tostado tiene **reposo** (`fecha_disponible_venta`, según `perfil_tueste.horas_reposo_minimas`) y **caducidad** (`fecha_caducidad`; en los datos de prueba, 30 días desde el tueste: convención a confirmar con el cliente).
- Estados de `lote_cafe_tostado.estado_disponibilidad`: `REPOSO`, `DISPONIBLE`, `AGOTADO`, `VENCIDO`.
- Un lote se puede vender si: terminó el reposo, tiene stock disponible, no está vencido y no está `AGOTADO`. Se vende el más antiguo primero (**FIFO**).
- Frescura: `VENCIDO` si la fecha pasó; `POR_VENCER` si quedan 7 días o menos; `OPTIMO` en el resto.

**Ventas y cobros**

- Todo pedido tiene cliente. Para ventas de mostrador sin datos existe el cliente **"Consumidor Final"** (B2C).
- Un detalle de pedido exige **presentación** (envase con su peso en gramos), **tipo de molienda** y **lote tostado**. Gramos del renglón = `cantidad_envases × presentacion.peso_gramos`.
- `subtotal = cantidad_envases × precio_unitario`. Hoy **no hay lista de precios**: el precio viaja en el pedido.
- **B2B**: cuenta corriente con plazo (`cliente.plazo_cta_cte_dias`, 15 o 30), con factura (`factura`, 1 por pedido, con vencimiento). **B2C**: pago inmediato. Un pedido puede tener varios pagos.
- `estado_cobro`: `PENDIENTE`, `PARCIAL`, `PAGADO`, `REEMBOLSADO`. `estado_preparacion`: `PENDIENTE`, `EN_PREPARACION`, `LISTO`, `ENTREGADO`, `CANCELADO`.
- La integración con AFIP queda **fuera del MVP** (Hito 2); la factura solo lleva tipo y número simples.

**Suscripciones (abono recurrente B2B) y reparto**

- Una suscripción genera pedidos automáticos con frecuencia `SEMANAL` o `MENSUAL`. `dia_entrega`: 1 a 7 (semanal) o 1 a 28 (mensual).
- `proxima_fecha_generacion` es la **fecha de entrega del próximo pedido**. Al generarlo: +7 días (semanal) o el mismo `dia_entrega` del mes siguiente (mensual). Solo las suscripciones `ACTIVA` generan pedidos.
- No puede haber dos pedidos de la misma suscripción para la misma fecha de entrega (índice único en la base).
- Los pedidos se agrupan en una **hoja de ruta** (una sola por fecha) y se ordenan con `pedido.orden_visita`. Los clientes B2B tienen domicilio obligatorio.

**Alertas**

- No se guardan: se calculan en cada consulta a partir del stock y el mínimo (`origen_cafe.stock_minimo_alerta_gramos`). Alerta predictiva: consumo de los últimos 30 días ÷ 30. Si el mínimo es 0 no hay alerta.

## 9. Alcance del MVP y pendientes

**Incluye:** conversor a gramos, producción con merma, punto de venta con molienda y cobros separados (B2B y B2C), control de caducidad, alertas de stock mínimo y abonos recurrentes con hojas de ruta.

**Fuera del MVP (Hito 2):** AFIP, reportes de rentabilidad, app móvil nativa, e-commerce externo.

**Pedidos recientes del profesor — supuestos, a confirmar antes de construir:**

- Suscripción de clientes con porcentaje de descuento (membresía simple, B2B y B2C), **distinta** del abono recurrente.
- Sección de compras para clientes (probablemente reutilizando Pedidos, filtrada por cliente).
- Inicio de sesión con roles: administrador (todo), tueste, vendedor y cliente (solo compras).
- Pantalla de configuración (modo oscuro, idioma).

Estos cambios requieren tablas nuevas (`usuario`, planes de suscripción): **no las crees por tu cuenta**; pedí el cambio de esquema.

## 10. Habilidades para IAs

La carpeta [`skills/`](skills/) contiene procedimientos paso a paso para las tareas más repetidas. **Si tu tarea coincide con alguna, leé su `SKILL.md` antes de empezar.** Índice en [`skills/README.md`](skills/README.md).

## 11. Flujo de trabajo con Git

- **Nadie sube a `main`** (está protegida). Una rama por tarea: `feature/nombre-corto`.
- Todo cambio entra por **Pull Request** y lo revisa otra persona.
- Pull Requests chicos: uno por funcionalidad.
- Si el cambio toca `data-base/`, todo el equipo tiene que recrear su base.

## 12. Responsabilidades del equipo

| Persona | Área                                                             |
| ------- | ---------------------------------------------------------------- |
| Orión   | Base de datos, revisión de Pull Requests, documentación, pruebas |
| Thiago  | Backend: producción, suscripciones y hojas de ruta               |
| Ignacio | Backend: pedidos, cobros y pagos                                 |
| Agustín | Frontend                                                         |

Evitá tocar archivos del área de otra persona sin avisarle.

## 13. Qué NO hacer

- No modificar `data-base/` ni inventar columnas o tablas.
- No usar `double` para gramos o dinero, ni convertir unidades fuera de `ConversorUnidades`.
- No devolver entidades en la API, ni poner reglas de negocio en los controllers.
- No agregar dependencias grandes ni cambiar versiones de Java, Spring Boot o Next.js sin consultar.
- No sobrescribir el `README.md` ni este archivo: si hay que actualizarlos, solo agregar o corregir secciones.
- No dar por probado lo que no se ejecutó: si no pudiste correr los tests, decilo.
- Cuando se suben ramas automaticamente nunca mencionar a la inteligencia artificial utilizada, solo al integrante del grupo.
- No inventar cosas, si no conseguis el resultado de busqueda o encontras la respuesta indicada, avisarlo. Nunca inventarlo.
- Siempre verificar primero usando los archivos dentro del proyecto (Indicaciones del trabajo practico, mensajes del equipo, etc) antes de verificar en internet.
