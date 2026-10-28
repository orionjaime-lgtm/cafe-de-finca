---
name: descontar-stock
description: Usar cuando una operación reste o mueva stock de café verde o tostado (pedidos, órdenes de tueste, generación de pedidos de suscripción) en el backend de Café de Finca.
---

# Descontar stock de forma segura

El problema central del cliente es **vender café que no existe**. Toda operación que reste stock tiene que ser exacta y completa o no hacerse.

## Reglas

1. **Una transacción por operación.** El método del service lleva `@Transactional`. Si algo falla, no queda nada a medias (por ejemplo, un pedido guardado sin descontar el stock).
2. **Validar antes de escribir.** Revisar todas las condiciones y recién después modificar datos.
3. **El descuento es un método de la entidad**, no un setter. Ejemplo: `lote.descontarStock(gramos)` lanza una excepción si `gramos` es mayor que el stock disponible o no es positivo.
4. **Stock insuficiente → HTTP 409** con `{ "estado": 409, "mensaje": "Stock insuficiente..." }`. Lanzar `StockInsuficienteException` (ya existe en `exception/`; el `ManejadorGlobalErrores` la convierte en 409). Para que el mensaje lleve los gramos: `new StockInsuficienteException(gramosDisponibles, gramosPedidos)`.
5. **Concurrencia:** si dos personas piden el mismo lote a la vez, no deben venderse los mismos gramos. Leer el lote con bloqueo (`@Lock(LockModeType.PESSIMISTIC_WRITE)` en el repository) dentro de la transacción.
6. **Estado del lote:** si el stock llega a 0, marcar `AGOTADO`.

## Qué se valida en cada caso

**Pedido (stock tostado).** El lote tiene que estar: fuera de reposo (`fecha_disponible_venta` ≤ ahora), con stock disponible suficiente, no vencido y no `AGOTADO`. Se elige el lote más antiguo primero (FIFO). Los gramos del renglón son `cantidad_envases × presentacion.peso_gramos`. Después se descuenta `stock_disponible_gramos`.

**Orden de tueste (stock verde).** El lote verde tiene que tener `stock_actual_gramos` ≥ `peso_verde_usado_g`. En **una** transacción se descuenta el verde, se crea la orden y se crea el lote tostado. Se cumplen los `CHECK` de la base: el tostado no puede pesar más que el verde.

**Generador de suscripciones.** Por cada suscripción `ACTIVA` con `proxima_fecha_generacion` vencida, se crea el pedido y se descuenta el stock en la misma transacción. Si no hay stock, **no se genera ese pedido** y se deja constancia (sin frenar las demás suscripciones). Nunca debe duplicarse el pedido de una fecha.

## Tests obligatorios

- Descuento exacto (queda el stock correcto).
- Stock insuficiente → 409 y el stock **no cambia**.
- Lote vencido o en reposo → rechazado.
- Si falla a mitad de camino, no queda ningún cambio guardado.
