package co.edu.unicauca.piedraazul.notification.service;

import co.edu.unicauca.piedraazul.notification.dto.CitaCreadaNotificationRequest;
import co.edu.unicauca.piedraazul.notification.model.NotificacionLog;
import co.edu.unicauca.piedraazul.notification.repository.NotificacionLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificacionLogRepository notificacionLogRepository;
    private final JavaMailSender javaMailSender;

    @Value("${notification.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${notification.mail.from:no-reply@piedraazul.com}")
    private String mailFrom;

    public NotificationService(NotificacionLogRepository notificacionLogRepository,
                               JavaMailSender javaMailSender) {
        this.notificacionLogRepository = notificacionLogRepository;
        this.javaMailSender = javaMailSender;
    }

    public void procesarNotificacionCitaCreada(CitaCreadaNotificationRequest request) {
        String estado = "FALLIDO";

        try {
            if (mailEnabled) {
                enviarCorreoConfirmacion(request);
            } else {
                System.out.println("NOTIFICATION-SERVICE -> Envio real de correo desactivado. Modo local.");
            }

            estado = "ENVIADO";
            guardarLog(request, estado, null);

        } catch (Exception e) {
            estado = "FALLIDO";
            guardarLog(request, estado, e.getMessage());
            System.err.println("NOTIFICATION-SERVICE -> Error al procesar notificacion: " + e.getMessage());
        }
    }

    private void enviarCorreoConfirmacion(CitaCreadaNotificationRequest request) {
        if (request.getCorreoPaciente() == null || request.getCorreoPaciente().isBlank()) {
            throw new IllegalArgumentException("El paciente no tiene correo registrado.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(request.getCorreoPaciente());
        message.setSubject("Confirmación de cita - Piedra Azul");
        message.setText(
                "Hola " + request.getPaciente() + ",\n\n" +
                "Tu cita fue agendada correctamente.\n\n" +
                "Médico/Terapista: " + request.getMedico() + "\n" +
                "Fecha: " + request.getFecha() + "\n" +
                "Hora: " + request.getHora() + "\n\n" +
                "Gracias por usar el sistema Piedra Azul."
        );

        javaMailSender.send(message);
    }

    private void guardarLog(CitaCreadaNotificationRequest request, String estado, String detalleError) {
        NotificacionLog log = new NotificacionLog();
        log.setCitaId(request.getCitaId());
        log.setPaciente(request.getPaciente());
        log.setMedico(request.getMedico());
        log.setCorreoPaciente(request.getCorreoPaciente());
        log.setCelularPaciente(request.getCelularPaciente());
        log.setEstado(estado);
        log.setFechaEnvio(LocalDateTime.now());

        notificacionLogRepository.save(log);

        System.out.println("NOTIFICATION-SERVICE -> Notificacion procesada con estado: " + estado);

        if (detalleError != null && !detalleError.isBlank()) {
            System.err.println("NOTIFICATION-SERVICE -> Detalle error: " + detalleError);
        }
    }

    public List<NotificacionLog> listarNotificaciones() {
        return notificacionLogRepository.findAll();
    }
}