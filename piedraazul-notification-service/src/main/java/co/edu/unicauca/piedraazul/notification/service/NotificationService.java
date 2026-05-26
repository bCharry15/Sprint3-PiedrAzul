package co.edu.unicauca.piedraazul.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.notification.dto.CitaCreadaNotificationRequest;
import co.edu.unicauca.piedraazul.notification.model.NotificacionLog;
import co.edu.unicauca.piedraazul.notification.repository.NotificacionLogRepository;

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
        if (request == null) {
            System.err.println("NOTIFICATION-SERVICE -> Solicitud de notificación vacía.");
            return;
        }

        try {
            if (mailEnabled) {
                enviarCorreoConfirmacion(request);
                guardarLog(request, "ENVIADO", null);

                System.out.println("NOTIFICATION-SERVICE -> Correo real enviado.");
                System.out.println("NOTIFICATION-SERVICE -> Cita ID: " + request.getCitaId());
                System.out.println("NOTIFICATION-SERVICE -> Paciente: " + request.getPaciente());
                System.out.println("NOTIFICATION-SERVICE -> Correo: " + request.getCorreoPaciente());
            } else {
                guardarLog(request, "SIMULADO", "Envío real de correo desactivado en ambiente local.");

                System.out.println("NOTIFICATION-SERVICE -> Envío real de correo desactivado. Modo SIMULADO.");
                System.out.println("NOTIFICATION-SERVICE -> Cita ID: " + request.getCitaId());
                System.out.println("NOTIFICATION-SERVICE -> Paciente: " + request.getPaciente());
                System.out.println("NOTIFICATION-SERVICE -> Correo: " + request.getCorreoPaciente());
                System.out.println("NOTIFICATION-SERVICE -> Celular: " + request.getCelularPaciente());
                System.out.println("NOTIFICATION-SERVICE -> Médico/Terapista: " + request.getMedico());
                System.out.println("NOTIFICATION-SERVICE -> Fecha: " + request.getFecha());
                System.out.println("NOTIFICATION-SERVICE -> Hora: " + request.getHora());
            }

        } catch (Exception e) {
            guardarLog(request, "FALLIDO", e.getMessage());

            System.err.println("NOTIFICATION-SERVICE -> Error al procesar notificación.");
            System.err.println("NOTIFICATION-SERVICE -> Detalle: " + e.getMessage());
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

        System.out.println("NOTIFICATION-SERVICE -> Notificación procesada con estado: " + estado);

        if (detalleError != null && !detalleError.isBlank()) {
            System.out.println("NOTIFICATION-SERVICE -> Detalle: " + detalleError);
        }
    }

    public List<NotificacionLog> listarNotificaciones() {
        return notificacionLogRepository.findAll();
    }
}