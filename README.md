# PiedraAzul - Sistema de Agendamiento Médico

PiedraAzul es un sistema de gestión y agendamiento de citas médicas desarrollado como proyecto académico para Ingeniería de Software. El sistema está orientado a consultorios, centros médicos pequeños y terapeutas que requieren administrar pacientes, médicos, disponibilidad, citas, historial de cambios y notificaciones de forma organizada, segura y trazable.

El proyecto integra una aplicación cliente de escritorio desarrollada en JavaFX con microservicios construidos en Spring Boot, autenticación con Keycloak, persistencia en MariaDB y despliegue local mediante Docker Compose.

## Documentación del proyecto

* Documento: [Google Docs - PiedraAzul](https://docs.google.com/document/d/1sPHt0zLF7bGqXfEkfuj3O59Gnp1NG1h-SVsOveMMYvk/edit?usp=sharing)

## Problema que resuelve

En muchos consultorios y centros médicos, el proceso de agendamiento se realiza mediante llamadas, mensajes, hojas de cálculo o registros manuales. Esto puede generar:

* Cruces de horarios.
* Duplicidad de información.
* Pérdida de trazabilidad.
* Dificultad para controlar disponibilidad por médico o terapeuta.
* Riesgos de acceso no autorizado a información sensible.
* Mayor carga operativa para el personal administrativo.

PiedraAzul busca transformar este proceso manual en una gestión digital más segura, organizada, trazable y preparada para crecer.

## Propuesta de valor

PiedraAzul no solo permite crear citas médicas. También permite controlar el proceso completo de agendamiento mediante roles, disponibilidad configurable, validaciones de negocio, historial de cambios y reportes exportables.

El sistema aporta valor en tres aspectos principales:

* **Seguridad:** autenticación con Keycloak, control de acceso por roles y restricción del registro administrativo.
* **Usabilidad:** interfaces separadas por rol, formularios claros, selección de horas en formato AM/PM y búsqueda de pacientes por documento.
* **Modificabilidad:** separación por capas, uso de arquitectura hexagonal en el microservicio de agenda y desacoplamiento entre controladores, casos de uso, puertos y adaptadores.

## Tecnologías utilizadas

* Java 17
* JavaFX
* Spring Boot
* MariaDB
* Docker Compose
* Keycloak
* JWT
* Maven
* Arquitectura hexagonal
* Microservicios
* Patrón Factory
* Patrón Observer
* Patrón Builder
* Patrón Adapter

## Arquitectura general

El proyecto está organizado en una arquitectura basada en cliente de escritorio y microservicios.

```text
PiedraAzul/
├── PiedraAzul/                         # Aplicación cliente JavaFX
├── piedraazul-agenda-service/          # Microservicio principal de agenda médica
├── piedraazul-notification-service/    # Microservicio de notificaciones
├── keycloak/                           # Configuración de autenticación
├── docs/                               # Evidencias y documentación
└── docker-compose.yml                  # Orquestación de servicios
```

## Módulos principales

### 1. Aplicación cliente JavaFX

Módulo encargado de la interfaz gráfica del sistema. Permite el acceso diferenciado según rol:

* Administrador
* Agendador
* Médico
* Paciente

Cada usuario visualiza únicamente las funcionalidades correspondientes a su perfil.

### 2. Agenda Service

Microservicio principal encargado de la lógica de negocio relacionada con:

* Médicos
* Pacientes
* Disponibilidad
* Citas
* Reagendamientos
* Historial
* Cambios de estado
* Exportación de información

Este microservicio aplica arquitectura hexagonal en los flujos críticos, separando controladores, casos de uso, puertos de entrada, puertos de salida y adaptadores de infraestructura.

### 3. Notification Service

Microservicio encargado de registrar y procesar eventos de notificación relacionados con la creación y gestión de citas.

### 4. Keycloak

Servicio utilizado para autenticación y autorización. Permite validar credenciales, emitir tokens JWT y controlar el acceso según el rol del usuario.

## Requisitos funcionales implementados

* Registro e inicio de sesión por roles.
* Registro de pacientes.
* Registro y gestión de médicos/terapistas.
* Gestión de agendadores.
* Configuración de disponibilidad por médico o terapeuta.
* Agendamiento autónomo por paciente.
* Creación de citas por agendador.
* Búsqueda de pacientes por documento.
* Consulta de citas por médico y fecha.
* Exportación de citas a formato CSV.
* Consulta de historial de citas del paciente.
* Cambio de estado de citas por parte del médico.
* Reagendamiento de citas.
* Historial de reagendamientos.
* Registro de notificaciones asociadas a citas.

## Requisitos no funcionales considerados

### Seguridad

* Autenticación mediante Keycloak.
* Manejo de tokens JWT.
* Acceso diferenciado por roles.
* Restricción de funcionalidades según perfil.
* Validación adicional para registro de administradores mediante clave de autorización.

### Usabilidad

* Interfaces separadas según el rol del usuario.
* Formularios organizados por secciones.
* Búsqueda de pacientes por número de documento.
* Selección de horarios en formato AM/PM para evitar errores de escritura.
* Mensajes de confirmación, validación y error.
* Flujo visual orientado a usuarios administrativos y médicos.

### Modificabilidad

* Separación entre presentación, servicios, dominio e infraestructura.
* Uso de arquitectura hexagonal en el microservicio de agenda.
* Casos de uso desacoplados de los controladores.
* Adaptadores para persistencia e integración externa.
* DTOs para transferencia de información entre capas.
* Diseño preparado para agregar nuevas funcionalidades sin afectar todo el sistema.

## Validaciones de negocio

El sistema contempla diferentes reglas para mejorar la confiabilidad del agendamiento:

* Validación de horarios disponibles.
* Validación de intervalos de atención.
* Validación de ventana de agendamiento.
* Validación de días festivos.
* Validación de citas activas.
* Control de disponibilidad por médico o terapeuta.
* Registro de historial cuando una cita es reagendada.
* Control de acceso según rol del usuario.

## Patrones de diseño utilizados

### Factory

Se utiliza para centralizar la creación de usuarios según su rol, evitando que la lógica de construcción quede dispersa en los controladores.

### Observer

Se utiliza para notificar eventos o mensajes durante procesos como el registro de usuarios, permitiendo desacoplar servicios y controladores.

### Builder

Se utiliza para construir respuestas complejas de citas de forma clara y ordenada, evitando constructores extensos o poco legibles.

### Adapter

Se aplica en la integración con infraestructura, persistencia y servicios externos. Permite conectar la lógica del dominio con tecnologías como MariaDB, Keycloak o clientes HTTP sin acoplar directamente el núcleo del sistema.

## Arquitectura hexagonal

La arquitectura hexagonal se implementa principalmente en el microservicio `piedraazul-agenda-service`.

La estructura separa:

* **Dominio:** modelos y reglas principales del negocio.
* **Puertos de entrada:** operaciones que el sistema expone como casos de uso.
* **Puertos de salida:** contratos que el dominio necesita para persistir o consultar información.
* **Casos de uso:** lógica de aplicación para ejecutar procesos como crear citas, cambiar estados o reagendar.
* **Adaptadores de entrada:** controladores REST.
* **Adaptadores de salida:** persistencia e integración con servicios externos.

Esta organización permite que la lógica de negocio no dependa directamente de controladores, base de datos o frameworks externos.

## Autenticación con Keycloak

La autenticación se realiza mediante Keycloak. El flujo general es:

1. El usuario ingresa sus credenciales desde la aplicación JavaFX.
2. El backend valida las credenciales contra Keycloak.
3. Keycloak responde con un token JWT y los datos del usuario.
4. El sistema identifica el rol del usuario.
5. Según el rol, se redirige al panel correspondiente.

Roles manejados:

* `ADMIN`
* `AGENDADOR`
* `MEDICO`
* `PACIENTE`

Además, el registro de administradores cuenta con una validación adicional mediante clave de autorización, evitando que cualquier usuario pueda crear una cuenta con privilegios administrativos.

## Ejecución del proyecto

### 1. Levantar servicios con Docker Compose

Desde la raíz del proyecto:

```bash
docker compose up --build -d
```

Este comando levanta los servicios necesarios, incluyendo:

* Agenda Service
* Notification Service
* MariaDB
* Keycloak

### 2. Ejecutar la aplicación cliente JavaFX

Ingresar al módulo principal:

```bash
cd PiedraAzul
```

Ejecutar la aplicación:

```bash
mvn clean javafx:run
```

## Usuarios de prueba

| Rol           | Usuario     | Contraseña     |
| ------------- | ----------- | -------------- |
| Administrador | `admin`     | `admin123`     |
| Agendador     | `agendador` | `agendador123` |
| Médico        | `medico`    | `medico123`    |
| Paciente      | `paciente`  | `paciente123`  |

## Clave para registro administrativo

Para registrar un nuevo usuario con rol administrador desde la interfaz, se solicita una clave de autorización.

```text
admin123
```

Esta validación se agregó para evitar que usuarios no autorizados creen cuentas administrativas desde la pantalla pública de registro.

En un entorno productivo, esta clave debería manejarse mediante una variable de entorno, configuración segura o flujo de aprobación institucional.

## Comandos útiles

### Ver contenedores activos

```bash
docker compose ps
```

### Ver logs del microservicio de agenda

```bash
docker compose logs -f agenda-service
```

### Detener los servicios

```bash
docker compose down
```

### Compilar el cliente JavaFX

```bash
cd PiedraAzul
mvn clean compile
```

## Estado del proyecto

El proyecto se encuentra funcional para entrega académica. Integra:

* Aplicación cliente JavaFX.
* Microservicio de agenda médica.
* Microservicio de notificaciones.
* Persistencia en MariaDB.
* Autenticación con Keycloak.
* Control de acceso por roles.
* Arquitectura hexagonal en flujos críticos.
* Exportación de información.
* Historial de reagendamientos.
* Mejoras de seguridad, usabilidad y modificabilidad.

## Valor del producto

PiedraAzul permite pasar de un proceso de agendamiento manual o desorganizado a una gestión digital más controlada, segura y trazable.

Su valor principal está en:

* Reducir errores operativos.
* Ahorrar tiempo administrativo.
* Mejorar la experiencia del paciente.
* Controlar la disponibilidad médica.
* Proteger el acceso a la información.
* Facilitar la evolución futura del sistema.

## Autores

Proyecto académico desarrollado para Ingeniería de Software.

Equipo de desarrollo: 
. Charry Vela Brayan
. Puentes Figueroa Jhoiner
. Ruiz Segura Sebastian
