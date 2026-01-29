## Todo Management System – Vaadin + Spring Boot

Aplicación web full-stack desarrollada con **Java, Spring Boot y Vaadin Flow**, como parte de un trabajo práctico académico, con el objetivo de aplicar arquitectura por capas, JPA, servicios y UI empresarial moderna.

Este proyecto demuestra mi aprendizaje en **desarrollo backend con Java, integración con frontend en Vaadin, arquitectura limpia y persistencia con JPA/Hibernate**.


# Tecnologías utilizadas

- **Java 21**
- **Spring Boot 3**
- **Vaadin Flow 24**
- **Spring Data JPA**
- **Hibernate ORM**
- **Base de datos H2 (en memoria)**
- **Maven**
- **Arquitectura en capas (Domain, Service, UI)**

---

# Funcionalidades implementadas

## Gestión de Tareas (Tasks)
- Listado de tareas
- Creación, edición y eliminación
- Persistencia con JPA
- Relación con Persona (Many-to-One)

## Gestión de Personas
- Listado de personas
- Alta, baja y modificación
- Relación One-to-Many con tareas

## UI Empresarial con Vaadin
- Navegación lateral con `SideNav`
- Vistas separadas (`TaskListView`, `PersonaView`)
- Layout principal con `AppLayout`
- Formularios interactivos
- Grid con edición y selección

---

##  Arquitectura del proyecto

El proyecto sigue una arquitectura en capas:

```text
com.prueba.todotp
 ├── base
 │   ├── domain        # Entidades base y clases comunes
 │   └── ui             # Layout principal, navegación y componentes UI compartidos
 │
 ├── taskmanagement
 │   ├── domain          # Entidades JPA (Task, Persona)
 │   ├── repository      # Repositorios Spring Data JPA
 │   ├── service          # Lógica de negocio (TaskService, PersonaService)
 │   └── ui.view           # Vistas Vaadin (TaskListView, PersonaView)
 │
 └── Application.java      # Clase principal Spring Boot

**Principio clave aplicado:**  
👉 *Las vistas NO acceden directamente a los repositorios, sino a servicios (clean architecture).*


## ▶ Cómo ejecutar el proyecto

## Requisitos
- Java 21
- Maven

## Pasos

```bash
git clone https://github.com/PabloHepp/Vaadin-task-management-app.git
cd Vaadin-task-management-app
mvn spring-boot:run
