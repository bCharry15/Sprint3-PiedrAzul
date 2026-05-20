\# Sistema Piedra Azul - Segundo Corte



Proyecto refactorizado hacia una arquitectura distribuida basada en microservicios y comunicación REST.



\## Estructura del proyecto



\- `SistemaPiedraAzul-main/PiedraAzul`: aplicación principal JavaFX.

\- `piedraazul-agenda-service`: microservicio de agenda, citas, médicos, disponibilidad y autenticación.

\- `piedraazul-notification-service`: microservicio de notificaciones.



\## Funcionalidades principales



\- RF1: listar citas por médico y fecha.

\- RF2: crear citas desde el rol agendador.

\- RF3: agendamiento autónomo desde el rol paciente.

\- RF4: configuración de disponibilidad médica desde el rol administrador.



\## Arquitectura



El sistema separa la interfaz gráfica de los servicios de negocio mediante APIs REST.  

`PiedraAzul` consume `agenda-service`, y `agenda-service` se comunica con `notification-service` para registrar notificaciones de citas.



\## Patrones GoF implementados



\- Builder

\- Factory

\- Facade

\- Adapter

\- Strategy

\- Observer



\## Servicios



\- Agenda Service: `http://localhost:8081`

\- Notification Service: `http://localhost:8082`

