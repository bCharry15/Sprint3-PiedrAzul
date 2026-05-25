# PiedraAzul - Sistema de Agendamiento Médico

PiedraAzul es un sistema de agendamiento médico desarrollado como proyecto académico para Ingeniería de Software. Permite gestionar pacientes, médicos/terapistas, disponibilidad, citas, historial y notificaciones.

## Tecnologías utilizadas

- Java 17
- JavaFX
- Spring Boot
- MariaDB
- Docker Compose
- Keycloak
- Arquitectura hexagonal en el microservicio de agenda
- Microservicio de notificaciones
- Autenticación y autorización con JWT

## Módulos principales

- `PiedraAzul`: aplicación cliente JavaFX.
- `piedraazul-agenda-service`: microservicio principal de agenda médica.
- `piedraazul-notification-service`: microservicio encargado de registrar y procesar notificaciones.
- `docker-compose.yml`: orquestación de servicios, bases de datos y Keycloak.

## Funcionalidades implementadas

- Registro e inicio de sesión por roles.
- Gestión de médicos y terapistas.
- Gestión de agendadores.
- Configuración de disponibilidad por médico/terapista.
- Agendamiento autónomo por paciente.
- Validación de citas activas para evitar múltiples citas pendientes.
- Validación de días festivos.
- Validación de ventana de agendamiento.
- Validación de horarios e intervalos de atención.
- Cambio de estado de citas por médico.
- Exportación de citas a CSV.
- Historial de citas del paciente.
- Registro de notificaciones.

## Ejecución con Docker

Desde la raíz del proyecto:

```bash
docker compose up --build -d
