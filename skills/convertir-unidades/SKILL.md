---
name: convertir-unidades
description: Usar cuando el código reciba, guarde, calcule o muestre pesos de café (kilos, libras, bolsas, envases o gramos) en el backend o el frontend de Café de Finca.
---

# Manejo de unidades: todo en gramos

**Regla de oro:** el sistema guarda y calcula **siempre en gramos**, con `BigDecimal`. Esto evita los descuadres de stock que tenía el Excel del cliente ("12 kg", "8000 g", "6 bolsas").

## Equivalencias

| Unidad | Gramos |
|---|---|
| 1 g | 1 |
| 1 kg | 1000 |
| 1 lb | 453.59237 |
| 1 bolsa / envase | `presentacion.peso_gramos` (por ejemplo, Bolsa 5kg = 5000) |

## Backend

1. La conversión vive en **una sola clase**: `util/ConversorUnidades`. No convertir en controllers, services ni entidades por separado.
2. La entrada con unidad se traduce a gramos en el service **antes** de validar o guardar:
   - `ConversorUnidades.aGramos(cantidad, UnidadPeso.LB)` o con el texto del frontend: `ConversorUnidades.aGramos(cantidad, "lb")`. Unidades: `g`, `kg`, `lb` (enum `UnidadPeso`). Rechaza cantidades de cero o negativas y unidades desconocidas con un error 400.
3. Para envases: `ConversorUnidades.envasesAGramos(cantidadEnvases, presentacion.getPesoGramos())` (el peso sale de la tabla, no se escribe a mano).
   Para mostrar kilos: `ConversorUnidades.gramosAKilos(gramos)`.
4. Nunca usar `double`. Usar `BigDecimal` y definir el redondeo de forma explícita: gramos con 3 decimales (`NUMERIC(12,3)` en la base).
5. La API **devuelve gramos**. No devolver kilos ya convertidos, salvo en textos de mensajes.

## Frontend

- Mostrar kilos dividiendo los gramos que llegan de la API por 1000. La conversión es solo de presentación.
- Si un formulario permite elegir la unidad, enviar a la API la cantidad y la unidad tal cual; el backend convierte.

## Tests que tiene que tener

- 1 kg → 1000 g y 3 lb → 1360.777 g (3 decimales).
- Cantidad cero o negativa → error 400.
- Unidad desconocida → error 400.
- Ida y vuelta: gramos → kilos → gramos sin perder precisión.
