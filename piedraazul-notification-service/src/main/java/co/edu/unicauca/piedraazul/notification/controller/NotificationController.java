package co.edu.unicauca.piedraazul.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.piedraazul.notification.dto.CitaCreadaNotificationRequest;
import co.edu.unicauca.piedraazul.notification.model.NotificacionLog;
import co.edu.unicauca.piedraazul.notification.service.NotificationService;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/api/notificaciones/cita-creada")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void notificarCitaCreada(@RequestBody CitaCreadaNotificationRequest request) {
        notificationService.procesarNotificacionCitaCreada(request);
    }

    @GetMapping("/api/notificaciones")
    @ResponseStatus(HttpStatus.OK)
    public List<NotificacionLog> listarNotificaciones() {
        return notificationService.listarNotificaciones();
    }
}