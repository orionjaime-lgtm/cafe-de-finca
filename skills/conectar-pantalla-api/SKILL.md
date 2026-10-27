---
name: conectar-pantalla-api
description: Usar cuando haya que reemplazar los datos de ejemplo de una pantalla del frontend (Next.js) de Café de Finca por llamadas reales a la API del backend.
---

# Conectar una pantalla del frontend a la API

Antes de empezar leé `frontend/AGENTS.md` (esta versión de Next.js tiene diferencias; consultá la documentación en `node_modules/next/dist/docs/`) y la tabla de endpoints del `README.md`.

## Pasos

1. **Configurar la URL.** Crear `frontend/.env.local` con `NEXT_PUBLIC_API_URL=http://localhost:8080`. No subir ese archivo; dejar un `.env.example` con la misma variable.
2. **Cliente de API.** Un solo archivo (`frontend/lib/api.ts`) con una función que arme la URL, llame a `fetch`, y lance un error con el mensaje de la API cuando la respuesta no es correcta. Los errores del backend tienen la forma `{ "estado": 400, "mensaje": "..." }`.
3. **Tipos.** Definir los tipos TypeScript según la respuesta real del endpoint. Los pesos llegan en **gramos**.
4. **Reemplazar los datos de ejemplo** (arreglos escritos a mano, como `const lots = [...]`, o el proveedor de demo en `roastery-demo-provider.tsx`) por la llamada a la API, de **una pantalla por vez**.
5. **Tres estados obligatorios** en cada pantalla: cargando, error (mostrar el mensaje de la API) y sin datos.
6. **Mostrar kilos** dividiendo por 1000 solo al dibujar. Para mandar datos, usar la unidad que pide el endpoint.
7. **Probar con el backend y la base prendidos**, con los datos de prueba (`03-datos-prueba.sql`). Si el navegador bloquea el pedido (error de CORS), el origen del frontend tiene que estar permitido en `config/WebConfig` del backend.

## Orden recomendado

1. Caducidad (`GET /api/lotes-tostados`, `GET /api/caducidad/resumen`).
2. Alertas y límites de stock (`GET /api/alertas`, `GET /api/origenes/stock`).
3. Desplegables de Pedidos (`/api/clientes`, `/api/presentaciones`, `/api/tipos-molienda`).
4. El resto, a medida que el backend sume endpoints.

## Nunca

- No escribir la URL de la API dentro de los componentes.
- No dejar la pantalla a medias con datos reales y de ejemplo mezclados.
- No hacer cuentas de stock o de dinero en el navegador: eso lo calcula el backend.
