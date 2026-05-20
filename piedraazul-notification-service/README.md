# Sistema Piedra Azul - Arquitectura con Microservicios

## Estructura principal

```txt
SistemaPiedraAzul-main/
│
├── piedraazul-agenda-service
│   └── Microservicio encargado de médicos, disponibilidad y citas.
│
├── piedraazul-notification-service
│   └── Microservicio encargado de recibir y procesar notificaciones.
│
└── SistemaPiedraAzul-main/
    └── PiedraAzul
        └── Aplicación principal JavaFX.