---
name: revisar-pull-request
description: Usar cuando haya que revisar el código de un Pull Request del proyecto Café de Finca (backend, frontend o base de datos) y decidir si se puede hacer merge.
---

# Revisar un Pull Request

Revisá en este orden. Si algo falla, pedí el cambio con una frase concreta y no hagas merge.

## 1. Alcance
- [ ] El Pull Request hace **una** cosa. Si mezcla varias, pedir que se separe.
- [ ] No toca archivos de otra área sin aviso (`data-base/` solo lo cambia el responsable de la base).
- [ ] La descripción explica qué hace y cómo se probó.

## 2. Archivos que no deben estar
- [ ] No hay `.env`, certificados (`.jks`, `.p12`), `target/`, `node_modules/`, `.class` ni contraseñas nuevas.
- [ ] El `README.md` y `AGENTS.md` solo tienen líneas agregadas o correcciones, no fueron reemplazados.

## 3. Backend
- [ ] Gramos y dinero en `BigDecimal`, sin `double` ni `float`.
- [ ] La conversión de unidades solo está en `ConversorUnidades`.
- [ ] Los descuentos de stock están dentro de `@Transactional` y validan antes de escribir.
- [ ] Los controllers devuelven DTOs y no tienen reglas de negocio.
- [ ] Los errores de negocio devuelven JSON (400 o 409), no 500.
- [ ] Las entidades no tienen setters nuevos y las columnas existen en `01-schema.sql`.
- [ ] `ddl-auto` sigue en `validate`.
- [ ] Hay tests de cada regla nueva, con los bordes.

## 4. Frontend
- [ ] La URL de la API sale de `NEXT_PUBLIC_API_URL`, no está escrita en el código.
- [ ] La pantalla maneja los estados de **carga**, **error** y **sin datos**.
- [ ] Los pesos se muestran en kg dividiendo los gramos por 1000.
- [ ] No quedan datos de ejemplo mezclados con datos reales.

## 5. Base de datos
- [ ] Recrear la base (`docker-compose down -v && docker-compose up -d`) no da errores.
- [ ] Los datos de prueba cumplen los `CHECK`.
- [ ] El Pull Request avisa que todos tienen que recrear su base.

## 6. Prueba
- [ ] Si es posible, bajar la rama, correr `./mvnw test` y probar el endpoint o la pantalla.
- [ ] Si no se pudo ejecutar, dejarlo escrito en lugar de dar el Pull Request por probado.

## 7. Cierre
Aprobar cuando todo esté en orden. Hacer merge con "Merge pull request" y avisar si el cambio exige recrear la base.
