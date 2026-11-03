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
| **Clientes y ventas** | `cliente`, `canal_venta`, `pedido`, `detalle_pedido`, `plan_membresia`, `membresia_cliente`, `suscripcion`, `detalle_suscripcion`, `hoja_ruta` |
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
- Errores: `400` si falta un dato, la unidad no existe o el origen o el proveedor no existen; `409` con el mensaje "Ya existe un lote verde con el código X." si el `codigoLote` ya está usado.
- Los campos enteros (`idOrigen`, `idProveedor`, `cantBolsas`) no aceptan decimales (`1.5` da `400` en lugar de guardarse como `1`), y `fechaIngreso` solo acepta texto `AAAA-MM-DD`.

### Backend: órdenes de tueste

| Endpoint | Qué hace |
|---|---|
| `GET /api/ordenes-tueste` | Órdenes de la más reciente a la más antigua, cada una con el lote tostado que salió de ella. |
| `POST /api/ordenes-tueste` | Registra un tueste. Responde `201` con la orden creada. |

Cuerpo del `POST /api/ordenes-tueste`:

```json
{ "idLoteVerde": 1, "idPerfilTueste": 2, "pesoVerdeUsado": 5,
  "pesoTostadoObtenido": 4.25, "unidad": "kg", "operadorTostador": "Marta" }
```

Registrar un tueste hace tres cosas **en una sola transacción**: descuenta el café verde, guarda la orden y crea el lote tostado. Si algo falla, no queda nada a medias.

- Los dos pesos llegan con la misma unidad (`g`, `kg` o `lb`) y se convierten a gramos con `ConversorUnidades`. El operador es opcional.
- La fecha del tueste la pone el backend con su reloj; el cliente no la manda.
- **Lote tostado nuevo:** código `LT-xxxx` (el siguiente al mayor usado, desde `LT-1001`), estado `REPOSO` y todo el café disponible.
  - `fecha_hora_fin_tueste` = tueste + `tiempo_tueste_min` del perfil.
  - `fecha_disponible_venta` = fin del tueste + `horas_reposo_minimas` del perfil (0 si el perfil no tiene).
  - `fecha_caducidad` = día del tueste + 30 días (`DIAS_CADUCIDAD`, convención a confirmar con el cliente).
- **Dos tuestes a la vez sobre el mismo lote verde** se hacen de a uno: el lote se bloquea durante la transacción, así el stock nunca se descuenta de más.
- Errores: `409` "Stock insuficiente: hay X g disponibles y se pidieron Y g." si el verde no alcanza; `400` si falta un dato, la unidad no existe, el lote o el perfil no existen, o el tostado pesa más que el verde.

### Backend: pedidos y catálogos de venta

| Endpoint | Qué hace |
|---|---|
| `GET /api/canales-venta` | Devuelve los canales de venta disponibles. |
| `GET /api/medios-pago` | Devuelve los medios de pago disponibles. |
| `POST /api/pedidos` | Crea un pedido, sus detalles y, para clientes B2B, una factura simple. Responde `201`. |

Cuerpo del `POST /api/pedidos`:

```json
{
  "clienteId": 1,
  "canalVentaId": 2,
  "items": [
    {
      "loteTostadoId": 1,
      "presentacionId": 2,
      "tipoMoliendaId": 1,
      "cantidadEnvases": 2,
      "precioUnitario": 12000.00
    }
  ]
}
```

- El peso de cada detalle es `cantidadEnvases × presentacion.peso_gramos`; el subtotal es `cantidadEnvases × precioUnitario`. Los importes se calculan con `BigDecimal`.
- Se utiliza el `loteTostadoId` enviado en cada ítem; no se sustituye por otro lote. El lote tiene que haber terminado el reposo, no estar vencido y tener stock disponible. Si se piden más gramos que los disponibles, responde `409` con "Stock insuficiente" y no guarda el pedido ni modifica el stock.
- El descuento de stock, el pedido, sus detalles y la factura se procesan en una única transacción. Los lotes solicitados se bloquean durante la operación para evitar descuentos concurrentes.
- Para clientes B2B se genera una factura simple de tipo `Factura A`, con número correlativo de cuatro dígitos y vencimiento según `plazo_cta_cte_dias`. La numeración se protege frente a pedidos simultáneos; no es una integración con AFIP.
- Si el cliente tiene una membresía activa, se aplica su porcentaje al total del pedido. El descuento se redondea a dos decimales; la respuesta informa `porcentajeDescuento` y `montoDescuento`. Los pagos se registran por separado.
- La respuesta contiene el pedido, sus detalles y la factura (`null` para B2C).

### Backend: cobros y pagos

| Endpoint | Qué hace |
|---|---|
| `GET /api/cobros/b2b` | Facturas B2B pendientes o parciales con deuda, datos de factura, vencimiento y estado. |
| `GET /api/cobros/b2c` | Pagos B2C ordenados del más reciente al más antiguo. |
| `POST /api/pagos` | Registra un pago y actualiza el estado de cobro del pedido. Responde `201`. |

Cuerpo del `POST /api/pagos`:

```json
{ "pedidoId": 1, "medioPagoId": 2, "monto": 12000.00 }
```

- `PAGADO` se asigna al cubrir el total; en otro caso queda `PARCIAL`. Se rechazan pagos superiores al saldo pendiente (`409`).
- El pedido se bloquea al registrar el pago para evitar sobrepagos concurrentes.
- Los cobros B2B muestran `VENCIDO` cuando el vencimiento ya pasó, `POR_VENCER` si vence hoy o dentro de los próximos 7 días, y `AL_DIA` si falta más tiempo o no tiene fecha de vencimiento.

### Backend: listado y estado de pedidos

| Endpoint | Qué hace |
|---|---|
| `GET /api/pedidos` | Lista pedidos con cliente, detalles y factura. |
| `PATCH /api/pedidos/{id}` | Actualiza el estado de preparación. |

Cuerpo del `PATCH /api/pedidos/{id}`: `{ "estadoPreparacion": "EN_PREPARACION" }`. Los estados admitidos son `PENDIENTE`, `EN_PREPARACION`, `LISTO`, `ENTREGADO` y `CANCELADO`.

### Backend: planes de suscripción con descuento

Estos planes (`Plan Básico`, `Plan Plus` y `Plan Premium`) son membresías con descuento, distintas de los abonos recurrentes descritos más abajo.

| Endpoint | Qué hace |
|---|---|
| `GET /api/planes-suscripcion` | Lista los planes activos y sus porcentajes de descuento. |
| `POST /api/membresias` | Activa una membresía para un cliente en un plan. Responde `201`. |
| `DELETE /api/membresias/{id}` | Cancela una membresía activa. |
| `POST /api/pedidos/cotizar` | Devuelve total, descuento actual y ahorro estimado por cada plan activo. |

Alta de membresía:

```json
{ "clienteId": 1, "planId": 2 }
```

Cotización (usa `clienteId` e `items`, con los mismos campos de cada ítem del `POST /api/pedidos`):

```json
{ "clienteId": 1, "items": [
  { "loteTostadoId": 1, "presentacionId": 2, "tipoMoliendaId": 1,
    "cantidadEnvases": 2, "precioUnitario": 12000.00 }
] }
```

La respuesta de cotización incluye `montoTotal`, `porcentajeDescuentoAplicado`, `montoDescuentoAplicado` y `ahorroPorPlan` (plan, porcentaje y ahorro en dinero). La cotización no reserva ni descuenta stock. Cada cliente puede tener como máximo una membresía activa.

### Backend: abonos recurrentes (suscripciones)

| Endpoint | Qué hace |
|---|---|
| `GET /api/suscripciones` | Todos los abonos con su cliente y sus renglones. |
| `POST /api/suscripciones` | Crea un abono. Responde `201`. Solo clientes **B2B**. |
| `PATCH /api/suscripciones/{id}` | Pausa, reactiva o cancela. |

Cuerpo del `POST /api/suscripciones`:

```json
{ "clienteId": 1, "frecuencia": "SEMANAL", "diaEntrega": 1, "fechaInicio": "2026-11-02", "fechaFin": null,
  "items": [ { "presentacionId": 3, "tipoMoliendaId": 4, "cantidadEnvases": 2, "precioUnitario": 120000.00 } ] }
```

- `frecuencia` es `SEMANAL` (`diaEntrega` de 1 = lunes a 7 = domingo) o `MENSUAL` (`diaEntrega` de 1 a 28, así existe en todos los meses). `fechaInicio` y `fechaFin` son opcionales.
- **Primera entrega:** el primer día de entrega que cae desde la fecha de inicio, o desde hoy si la fecha de inicio quedó en el pasado. Es `proximaFechaGeneracion`, la fecha de entrega del próximo pedido. El cálculo vive en el enum `FrecuenciaSuscripcion`: semanal suma 7 días; mensual repite el mismo día del mes siguiente.
- El renglón guarda el **precio pactado**; el lote se elige recién al generar cada pedido, según el stock de ese día.
- **Cambio de estado**, con el cuerpo `{ "estado": "PAUSADA" }`:

| Desde | Hacia | Resultado |
|---|---|---|
| `ACTIVA` | `PAUSADA` | Deja de generar pedidos. |
| `PAUSADA` | `ACTIVA` | Reactiva; si su próxima entrega quedó en el pasado, se recalcula para no generar de golpe todo lo atrasado. |
| `ACTIVA` o `PAUSADA` | `CANCELADA` | Definitivo: una cancelada no vuelve. |

- Errores: `400` si falta un dato, el cliente no existe o **no es B2B**, la frecuencia o el día no son válidos, o la fecha de fin es anterior a la primera entrega; `409` si el cambio de estado no está permitido (por ejemplo, reactivar una cancelada).

### Backend: generador de pedidos de los abonos

| Qué | Cómo se dispara |
|---|---|
| Tarea programada | Todos los días a las **06:00** (hora de Buenos Aires). |
| `POST /api/suscripciones/generar` | A mano. Responde `200` con lo generado y lo omitido. |

A cada abono **ACTIVO** con la entrega vencida (`proxima_fecha_generacion` de hoy o anterior, sin haber pasado su `fecha_fin`) le genera su pedido:

1. **Reutiliza `PedidoService.crear`** con los precios pactados del abono: descuenta el stock y emite la factura B2B. El pedido sale por el canal *Mayorista (B2B)*.
2. **Elige el lote de cada renglón**: el más antiguo (FIFO) que esté vendible (terminó el reposo, tiene stock, no está vencido ni agotado) y alcance para todo el renglón. Dentro del mismo pedido no ofrece dos veces el mismo café.
3. **Lo asigna a la hoja de ruta de su fecha de entrega** (la crea si no existe) con el siguiente `orden_visita`.
4. **Avanza la próxima fecha**: +7 días (semanal) o el mismo día del mes siguiente (mensual).

Reglas:
- **No duplica.** Cada abono se procesa en su propia transacción y bloqueado: si otra corrida ya lo generó, ya no le toca. Además la base tiene un índice único por abono y fecha de entrega. Correrlo dos veces, o varias a la vez, genera cada pedido una sola vez.
- **No genera** para abonos `PAUSADOS` o `CANCELADOS`.
- **Sin stock**: ese abono se omite, no se crea el pedido y **su fecha no avanza** (se reintenta en la próxima corrida). El motivo queda en el log y en la respuesta, y los demás abonos siguen.
- Si hay varios períodos atrasados, genera uno por corrida (no crea entregas con fechas pasadas de golpe).

Ejemplo de respuesta:

```json
{ "fecha": "2026-11-02",
  "generados": [ { "suscripcionId": 1, "pedidoId": 6, "fechaEntrega": "2026-11-02", "montoTotal": 125000.00, "hojaRutaId": 4, "ordenVisita": 1 } ],
  "omitidas":  [ { "suscripcionId": 5, "motivo": "No hay un lote vendible con 5000000 g libres para 1000 x Bolsa 5kg (Grano Entero)." } ] }
```

Configuración (opcional, como propiedad o variable de entorno):

| Propiedad | Variable de entorno | Qué hace |
|---|---|---|
| `cafe.suscripciones.cron` | `CAFE_SUSCRIPCIONES_CRON` | Cambia el horario (por defecto `0 0 6 * * *`). |
| `cafe.suscripciones.tarea-activa` | `CAFE_SUSCRIPCIONES_TAREA_ACTIVA` | Con `false` apaga la tarea diaria; el endpoint manual sigue funcionando. |

> Los datos de prueba traen abonos con la entrega de "el día en que se creó la base". La primera vez que corra la tarea (o el `POST`) generará esos pedidos y descontará stock.

### Backend: hojas de ruta

| Endpoint | Qué hace |
|---|---|
| `GET /api/hojas-ruta` | Todas las hojas, de la más reciente a la más antigua, cada una con sus paradas. |
| `GET /api/hojas-ruta?fecha=2026-11-09` | Solo la hoja de ese día. Si ese día no hay reparto devuelve una lista vacía. |
| `GET /api/hojas-ruta/{id}` | Una hoja con sus paradas. |

Solo consultan: las hojas y el orden de visita los arma el generador de pedidos de los abonos. Cada parada es un pedido a entregar, en orden de visita:

```json
{ "idHojaRuta": 4, "fechaReparto": "2026-11-02", "estadoRuta": "PENDIENTE",
  "paradas": [ { "ordenVisita": 1, "pedidoId": 6, "clienteId": 2, "cliente": "Café Cortázar S.A.S.",
                 "domicilio": "Av. Hipólito Yrigoyen 4120", "localidad": "Lanús",
                 "montoTotal": 125000.00, "estadoPreparacion": "PENDIENTE" } ] }
```

- `fecha` va en formato `AAAA-MM-DD`; con otro formato responde `400`. Un id que no existe responde `400` con "No existe la hoja de ruta X.".
- El `orden_visita` se asigna en el orden en que se generan los pedidos del día (1, 2, 3...). Todavía no se ordena por zona o localidad.

> El backend se generó con IA y fue verificado por el equipo. Los prompts utilizados están en [`docs/uso-de-ia.md`](docs/uso-de-ia.md).

### Cómo está organizado el código

Siguiendo la convención del punto 3, cada endpoint pasa por las mismas capas:

```
backend/src/main/java/com/cafedefinca/backend/
├── controller/   ClienteController, CatalogoController, LoteTostadoController,
│                 CaducidadController, AlertaController, OrigenController, PedidoController
├── service/      ClienteService, CatalogoService, LoteTostadoService,
│                 CaducidadService, AlertaService, OrigenService, PedidoService
├── repository/   una interfaz por tabla (Spring arma el SQL)
├── entity/       una clase por tabla usada: cliente, presentacion, tipo_molienda,
│                 origen_cafe, lote_cafe_verde, perfil_tueste, orden_tueste, lote_cafe_tostado,
│                 canal_venta, medio_pago, pedido, detalle_pedido, factura, pago
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
- [x] Conversor a gramos con tests
- [x] Módulo de producción y stock
- [ ] Módulo de ventas y finanzas
- [x] Esqueleto del frontend y pantallas
- [x] Alertas de stock y panel de caducidad
- [ ] Pruebas finales y pase a producción