---
name: agregar-endpoint
description: Usar cuando haya que crear o modificar un endpoint de la API del backend (Spring Boot) de Café de Finca. Define los pasos por capa, las reglas y los tests.
---

# Agregar un endpoint al backend

Antes de empezar leé `AGENTS.md` (secciones 6 y 7) y `data-base/01-schema.sql`.

## Pasos

1. **Entender la tabla.** Buscá en `01-schema.sql` las tablas que toca el endpoint. No inventes columnas.
2. **Entidad** (`entity/`): si la tabla no tiene entidad, crearla con `@Entity`, `@Table` y `@Column(name = "...")` exactos. Sin setters; los cambios se hacen con métodos de negocio. Gramos y dinero en `BigDecimal`. Si hay una columna calculada por la base (por ejemplo `merma_real_pct`), `insertable = false, updatable = false`.
3. **Repository** (`repository/`): una interfaz que extiende `JpaRepository<Entidad, Integer>`. Si la consulta une varias tablas, usar `JOIN FETCH` para evitar consultas repetidas.
4. **DTO** (`dto/`): un `record` con solo lo que necesita el frontend. Nunca devolver la entidad.
5. **Service** (`service/`): acá van las reglas de negocio. Clase con `@Service`; `@Transactional(readOnly = true)` para lecturas y `@Transactional` para escrituras. La fecha se pide al `Clock` inyectado.
6. **Controller** (`controller/`): solo recibe, llama al service y responde. Ruta con prefijo `/api`, en plural y con guiones. Para datos de entrada usar un DTO con anotaciones de validación (`@NotNull`, `@Positive`) y `@Valid`.
7. **Errores:** lanzar una excepción de negocio (por ejemplo `SolicitudInvalidaException` → 400) y mapearla en `ManejadorGlobalErrores`. Formato de respuesta: `{ "estado": 400, "mensaje": "..." }`. Stock insuficiente → 409.
8. **Tests** (`src/test/java/...`): uno por cada regla de negocio, con un `Clock` fijo si hay fechas. Los bordes importan (stock justo, stock cero, fecha límite).
9. **Documentar:** agregar el endpoint a la tabla de endpoints del `README.md` y, si la IA hizo cambios grandes, registrar el prompt en `docs/uso-de-ia.md`.

## Control antes de terminar

- [ ] Ningún `double` ni `float`.
- [ ] Ninguna regla de negocio en el controller.
- [ ] No se modificó `data-base/`.
- [ ] La respuesta de error es JSON y no un 500.
- [ ] Corrió `./mvnw test`. Si no pudiste correrlo, decilo en la descripción del Pull Request.
