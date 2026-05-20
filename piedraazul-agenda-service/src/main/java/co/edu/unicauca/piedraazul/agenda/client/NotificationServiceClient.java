package co.edu.unicauca.piedraazul.agenda.client;

import co.edu.unicauca.piedraazul.agenda.event.CitaCreadaEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationServiceClient {

    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void notificarCitaCreada(CitaCreadaEvent event) {
        String url = notificationServiceUrl + "/api/notificaciones/cita-creada";

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("citaId", event.getCitaId());
            body.put("paciente", event.getPaciente());
            body.put("medico", event.getMedico());
            body.put("fecha", event.getFecha() != null ? event.getFecha().toString() : null);
            body.put("hora", event.getHora() != null ? event.getHora().toString() : null);
            body.put("correoPaciente", event.getCorreoPaciente());
            body.put("celularPaciente", event.getCelularPaciente());

            String response = restTemplate.postForEntity(url, body, String.class).getBody();

            System.out.println("AGENDA-SERVICE -> Notification-service respondio: " + response);
            System.out.println("AGENDA-SERVICE -> URL usada: " + url);

        } catch (Exception e) {
            System.err.println("AGENDA-SERVICE -> No se pudo notificar a notification-service");
            System.err.println("AGENDA-SERVICE -> URL usada: " + url);
            System.err.println("Detalle: " + e.getMessage());
        }
    }
}