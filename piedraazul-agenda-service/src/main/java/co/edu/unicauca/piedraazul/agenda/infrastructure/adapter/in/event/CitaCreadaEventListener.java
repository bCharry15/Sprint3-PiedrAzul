package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.event;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.event.CitaCreadaEvent;

@Component
public class CitaCreadaEventListener {

    private final RestTemplate restTemplate;

    @Value("${notification.service.url:http://notification-service:8082}")
    private String notificationServiceUrl;

    public CitaCreadaEventListener() {
        this.restTemplate = new RestTemplate();
    }

    @EventListener
    public void manejarCitaCreada(CitaCreadaEvent event) {
        if (event == null) {
            return;
        }

        try {
            String url = notificationServiceUrl + "/api/notificaciones/cita-creada";

            Map<String, Object> body = new HashMap<>();
            body.put("citaId", event.getCitaId());
            body.put("pacienteId", event.getPacienteId());
            body.put("paciente", event.getPaciente());
            body.put("correoPaciente", event.getCorreoPaciente());
            body.put("celularPaciente", event.getCelularPaciente());
            body.put("medicoId", event.getMedicoId());
            body.put("medico", event.getMedico());
            body.put("fecha", event.getFecha() != null ? event.getFecha().toString() : null);
            body.put("hora", event.getHora() != null ? event.getHora().toString() : null);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(url, request, Void.class);

            System.out.println("AGENDA-SERVICE -> Evento CitaCreadaEvent enviado a notification-service. Cita ID: "
                    + event.getCitaId());

        } catch (Exception e) {
            System.err.println("AGENDA-SERVICE -> No se pudo enviar el evento de cita creada a notification-service.");
            System.err.println("AGENDA-SERVICE -> Detalle: " + e.getMessage());
        }
    }
}