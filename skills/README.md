# Habilidades para IAs (skills)

Cada carpeta es una habilidad: un procedimiento corto para una tarea que se repite en este proyecto.
Cada una tiene un `SKILL.md` con un encabezado (`name` y `description`) que indica **cuándo usarla**.

**Cómo las usa una IA:** al leer el repositorio, si la tarea coincide con la `description` de una habilidad, tiene que leer ese `SKILL.md` **antes** de escribir código y seguir sus pasos.
Las reglas generales del proyecto están en [`../AGENTS.md`](../AGENTS.md); las habilidades no las reemplazan, las aplican a una tarea concreta.

| Habilidad | Cuándo usarla |
|---|---|
| [`agregar-endpoint`](agregar-endpoint/SKILL.md) | Crear o modificar un endpoint de la API en el backend |
| [`convertir-unidades`](convertir-unidades/SKILL.md) | Recibir o mostrar pesos (kg, lb, bolsas, gramos) |
| [`descontar-stock`](descontar-stock/SKILL.md) | Cualquier operación que reste stock (pedidos, tueste, generador de suscripciones) |
| [`cambiar-base-de-datos`](cambiar-base-de-datos/SKILL.md) | Necesitar una tabla o columna nueva, o cambiar los datos de prueba |
| [`revisar-pull-request`](revisar-pull-request/SKILL.md) | Revisar el código de un Pull Request |
| [`conectar-pantalla-api`](conectar-pantalla-api/SKILL.md) | Reemplazar datos de ejemplo del frontend por llamadas a la API |

**Compatibilidad:** el formato es el de las skills de Claude Code. Para que se carguen solas en esa herramienta, se pueden copiar las carpetas a `.claude/skills/`.
