# Uso de inteligencia artificial en el backend

El backend de Café de Finca se generó con una IA (Claude Code) y fue verificado por el equipo. Este documento deja registrados los dos prompts que se usaron: uno para **generar** el código y otro para **verificarlo**.

| Etapa | Rol dado a la IA | Qué se pidió |
|---|---|---|
| 1. Generación | Programador full stack, líder del backend | Escribir los primeros endpoints del backend en Java con Spring Boot. |
| 2. Verificación | Tester | Revisar el código sin modificarlo e indicar qué está mal, qué es mejorable y qué requiere un cambio urgente. |

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

**Resultado:** los endpoints y la explicación quedaron en la sección *"Backend: endpoints del grupo A"* del [README](../README.md).

---

## Prompt 2: verificación del código

```text
[Rol]

sos un testes y venis a verificar el codigo backend del proyecto

[Objetivo]

verificar que se cumplen todos los objetivos indicados en el anterior prompt, indicar cuales no, no modificar nada solo indicar

[Restricciones]

no modificar nada, indicar que esta medianamente mal, que esta mejorable y que necesita un cambio urgente
```

**Resultado:** un informe que clasifica los hallazgos en tres niveles (cambio urgente, medianamente mal y mejorable). Los hallazgos quedan como tareas para los próximos pasos del equipo.
