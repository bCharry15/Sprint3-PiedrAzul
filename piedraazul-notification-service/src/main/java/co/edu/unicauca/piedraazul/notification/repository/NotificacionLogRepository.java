package co.edu.unicauca.piedraazul.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unicauca.piedraazul.notification.model.NotificacionLog;

public interface NotificacionLogRepository extends JpaRepository<NotificacionLog, Long> {
}