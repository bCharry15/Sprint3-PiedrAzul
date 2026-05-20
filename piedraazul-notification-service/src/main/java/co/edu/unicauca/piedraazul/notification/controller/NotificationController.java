package co.edu.unicauca.piedraazul.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.piedraazul.notification.dto.CitaCreadaNotificationRequest;
import co.edu.unicauca.piedraazul.notification.model.NotificacionLog;
import co.edu.unicauca.piedraazul.notification.service.NotificationService;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/cita-creada")
    @ResponseStatus(HttpStatus.OK)
    public String recibirNotificacionCitaCreada(@RequestBody CitaCreadaNotificationRequest request) {
        notificationService.procesarNotificacionCitaCreada(request);
        return "Notificación de cita creada recibida correctamente";
    }

    @GetMapping
    public List<NotificacionLog> listarNotificaciones() {
        return notificationService.listarNotificaciones();
    }

    @GetMapping("/health")
    @ResponseStatus(HttpStatus.OK)
    public String health() {
        return "notification-service OK";
    }
}