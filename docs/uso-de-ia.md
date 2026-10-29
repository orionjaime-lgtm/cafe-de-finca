# Uso de inteligencia artificial en el backend

El backend de Café de Finca se generó con una IA (Claude Code) y fue verificado por el equipo. Este documento deja registrados los dos prompts que se usaron: uno para **generar** el código y otro para **verificarlo**.

| Etapa           | Rol dado a la IA                          | Qué se pidió                                                                                                 |
| --------------- | ----------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| 1. Generación   | Programador full stack, líder del backend | Escribir los primeros endpoints del backend en Java con Spring Boot.                                         |
| 2. Verificación | Tester                                    | Revisar el código sin modificarlo e indicar qué está mal, qué es mejorable y qué requiere un cambio urgente. |
| 3. Verificación del punto 1 de Prioridad 1 | Tester | Verificar que el arreglo de alertas y caducidad cumple los objetivos, sin modificar nada. |
| 4. Corrección y entrega del punto 1 | Programador senior | Resolver las decisiones abiertas, corregir el comentario del test y subir la rama. |
| 5. Implementación del punto 2 | Programador senior, luego QA | Decidir las ramas, implementar catálogos y lotes verdes, y después revisar el resultado. |
| 6. Verificación con la base real | QA tester | Probar la base con datos reales, revisar lo implementado y subir la rama solo si todo está correcto. |
| 7. Implementación de pedidos | Programador backend | Agregar entidades y catálogos de venta, registrar pedidos transaccionales, facturas B2B y pruebas de stock/reposo/caducidad. |

---

## Prompt 1: generación del backend

```text
[Rol]

sos un programador full stack especializado en backend, y tu rol en el proyecto es de liderar el backend y administrarlo correctamente

[Objetivos]

Tenes que empezar con el codigo de backend, cumpliendo con los endpoints indicados dentro de la imagen agregada al mensaje

[Contexto]

tengo que hacer un proyecto grupal de programacion pero el codigo tiene que ser utilizando inteligencia artificial y nosotros verificando que hiciste, mi rol en este proyecto es de generar los primeros pasos dentro del backend

[Material de referencia]

dentro de la imagen adjuntada (WhatsApp Image 2026-10-21 at 19.05.49.jpeg) son los objetivos principales que tenes que cumplir por ahora, los llamados endpoints. Luego te voy a mandar el archivo pdf para que veas que era la propuesta y la ideologia que teniamos en mente para el proyecto, se llama (Sin titulo (1).pdf)

[Formato]

tiene que estar bien identado, todo separado con comentarios para una simple legibilidad del programador, ademas de en lo posible poner las variables al principio para entender paso por paso el codigo generado.

[Restricciones]

Luego de generar todo el codigo quiero que me hagas una breve explicacion de que hiciste para yo entenderlo al leerlo, dentro del readme generar la misma justificacion creada a mi

[Tecnologias]

para el backend vamos a utilizar:  java con springboot
```

**Material adjunto al prompt:**

- Una imagen con los endpoints a cumplir (los 7 `GET` del grupo A).
- El PDF de la propuesta del proyecto (`Sin título (1).pdf`).

**Resultado:** los endpoints y la explicación quedaron en la sección _"Backend: endpoints del grupo A"_ del [README](../README.md).

---

## Prompt 2: verificación del código

```text
[Rol]

sos un tester y venis a verificar el codigo backend del proyecto

[Objetivo]

verificar que se cumplen todos los objetivos indicados en el anterior prompt, indicar cuales no, no modificar nada solo indicar

[Restricciones]

no modificar nada, indicar que esta medianamente mal, que esta mejorable y que necesita un cambio urgente
```

**Resultado:** un informe que clasifica los hallazgos en tres niveles (cambio urgente, medianamente mal y mejorable). Los hallazgos quedan como tareas para los próximos pasos del equipo.

---

## Prompt 3: verificación del punto 1 de Prioridad 1

```text
[Rol]

sos un tester y venis a verificar el codigo backend del proyecto

[Objetivo]

verificar que se cumplen todos los objetivos indicados en Prioridad 1

[Restricciones]

no modificar nada, indicar que esta medianamente mal, que esta mejorable y que necesita un cambio urgente
```

**Resultado:** el informe confirmó que el arreglo de alertas con mínimo 0 y el conteo de `activos` estaban implementados y sin commitear. Además marcó como urgente que los puntos 2 y 3 no existían todavía. Los hallazgos quedaron como tareas en los próximos pasos.

---

## Prompt 4: corrección y entrega del punto 1

```text
[Rol]

Programador senior fullstack orientado en backend

resolve las preguntas antes mencionadas y corregi los comentarios del test y hacer comit y push del punto 1 para abrir el pr.

[Restricciones]

No comiences los puntos 2 y 3 solo hace un pequenio escaneo de la situacion e indica como se deberian resolver
```

**Resultado:** se corrigió el comentario del test (los datos decían 12 kg y 15 días, y eran 5 kg y 6 días), se subió la rama del punto 1 y se dejó el punto 2 y el punto 3 solo como propuesta, sin implementar.

---

## Prompt 5: implementación del punto 2

```text
[Rol]

seguis con el rol de desarrollador senior

[Objetivo]

volver a leer los puntos 2 y 3 de la Prioridad 1 del enunciado, considerar si deberian ir en dos ramas aparte o en una misma junta, como resolver los puntos indicados, empezando por el 2, el 3 lo vemos despues. Y luego de todo eso resolverlos y explicar que hiciste y como llegaste a esa resolucion

[Restricciones]

tenes que seguir la etica logica y de codigo que mantienen mis companieros, a su vez de indicar si modificaste algo ya creado y luego de crear todo automaticamente tu rol pasa a ser de un QA employee senior y probaras si los cambios creados estan bien hechos y si convergen bien con todo lo demas del codigo
```

**Resultado:** el punto 2 se implementó en una rama propia, sin tocar el punto 3. Se modificaron tres archivos existentes (`LoteCafeVerde`, `LoteCafeVerdeRepository` y el README), y se revisó que conviva con el resto del código. Durante la revisión se corrigieron dos casos borde (código de más de 50 caracteres y cantidad mayor al máximo de la columna).

---

## Prompt 6: verificación con la base real y subida de la rama

```text
ahi encendi el docker, quiero que chequees todo y si esta todo correcto, subas la nueva rama

acordarte que tu rol ahora es de QA tester
```

**Resultado:** con la base encendida, la suite completa pasó (62 de 62 pruebas) y los endpoints respondieron como se esperaba. Se probaron 16 casos del `POST`, incluidos los de error. La rama se subió solo después de verificarla.

---

## Prompt 7: pedidos, facturas y catálogos de venta

```text
Implementar las entidades Pedido, DetallePedido, Factura, Pago, MedioPago y CanalVenta,
los GET /api/canales-venta y GET /api/medios-pago, el POST /api/pedidos con descuento
transaccional del stock, factura B2B con vencimiento y tests para stock justo, insuficiente,
lote vencido y lote en reposo. No modificar el esquema SQL.
```

**Decisiones confirmadas:** el pedido usa exactamente el `loteTostadoId` que llega en cada ítem, sin reasignarlo a otro lote; las facturas se crean como `Factura A` con numeración correlativa interna.
