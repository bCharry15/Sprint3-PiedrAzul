package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.notification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.NotificarCitaCreadaPort;

@Component
public class NotificationRestAdapter implements NotificarCitaCreadaPort {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    @Override
    public void notificarCitaCreada(
            Long citaId,
            String nombrePaciente,
            String correoPaciente,
            String celularPaciente,
            String nombreMedico,
            LocalDate fecha,
            LocalTime hora
    ) {
        String url = notificationServiceUrl + "/api/notificaciones/cita-creada";

        Map<String, Object> body = new HashMap<>();
        body.put("citaId", citaId);
        body.put("nombrePaciente", nombrePaciente);
        body.put("correoPaciente", correoPaciente);
        body.put("celularPaciente", celularPaciente);
        body.put("nombreMedico", nombreMedico);
        body.put("fecha", fecha.toString());
        body.put("hora", hora.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );
        } catch (Exception ex) {
            System.err.println("No fue posible notificar la cita creada al notification-service. CitaId="
                    + citaId + ". Error: " + ex.getMessage());
        }
    }
}