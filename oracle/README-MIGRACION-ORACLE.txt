# Migración rápida de PiedraAzul a Oracle

Esta carpeta contiene solo los archivos que debes reemplazar en el proyecto para usar Oracle manteniendo las mismas entidades/tablas que ya usa la aplicación.

Estrategia aplicada:
- No se cambia el modelo Java.
- No se adaptan controladores, servicios ni repositorios.
- Se cambia MariaDB por Oracle JDBC.
- Se deja spring.jpa.hibernate.ddl-auto=update para que Hibernate cree en Oracle las tablas actuales: users, pacientes, medicos, citas, disponibilidades_medico, historial_reagendamientos y notificaciones_log.

Credenciales locales:
- Oracle host: localhost
- Puerto: 1521
- Service name: XEPDB1
- Usuario: PIEDRAAZUL
- Password: piedraazul

Comandos sugeridos:

docker compose down

docker compose up -d oracle-db keycloak

docker logs -f piedraazul-oracle-db

Cuando Oracle ya esté listo:

docker compose up --build -d notification-service agenda-service

Validación:

curl.exe -i http://localhost:8081/api/agenda/health
curl.exe -i http://localhost:8082/api/notifications
