package co.edu.unicauca.piedraazul.client;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import co.edu.unicauca.piedraazul.model.dto.CitaResponse;
import co.edu.unicauca.piedraazul.model.dto.CitasPorMedicoFechaResponse;
import co.edu.unicauca.piedraazul.model.dto.CrearCitaRequest;
import co.edu.unicauca.piedraazul.model.dto.CrearDisponibilidadRequest;
import co.edu.unicauca.piedraazul.model.dto.CrearMedicoRequest;
import co.edu.unicauca.piedraazul.model.dto.DisponibilidadResponse;
import co.edu.unicauca.piedraazul.model.dto.MedicoResponse;

@Component
public class AgendaServiceClient {

    @Value("${agenda.service.url}")
    private String agendaServiceUrl;

    private final RestTemplate restTemplate;

    public AgendaServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public MedicoResponse[] listarMedicos() {
        String url = agendaServiceUrl + "/api/medicos";

        ResponseEntity<MedicoResponse[]> response =
                restTemplate.getForEntity(url, MedicoResponse[].class);

        return response.getBody();
    }

    public MedicoResponse crearMedico(CrearMedicoRequest request) {
        String url = agendaServiceUrl + "/api/medicos";

        ResponseEntity<MedicoResponse> response =
                restTemplate.postForEntity(url, request, MedicoResponse.class);

        return response.getBody();
    }

    public DisponibilidadResponse consultarDisponibilidad(Long medicoId, LocalDate fecha) {
        String url = agendaServiceUrl
                + "/api/disponibilidad?medicoId=" + medicoId
                + "&fecha=" + fecha;

        ResponseEntity<DisponibilidadResponse> response =
                restTemplate.getForEntity(url, DisponibilidadResponse.class);

        return response.getBody();
    }

    public void crearDisponibilidad(CrearDisponibilidadRequest request) {
        String url = agendaServiceUrl + "/api/configuraciones-disponibilidad";

        Map<String, Object> body = new HashMap<>();
        body.put("medicoId", request.getMedicoId());
        body.put("diaSemana", request.getDiaSemana().name());
        body.put("horaInicio", request.getHoraInicio().toString());
        body.put("horaFin", request.getHoraFin().toString());
        body.put("intervaloMinutos", request.getIntervaloMinutos());
        body.put("ventanaSemanas", request.getVentanaSemanas());
        body.put("activo", true);

        restTemplate.postForEntity(url, body, Map.class);
    }

    public CitasPorMedicoFechaResponse listarCitasPorMedicoYFecha(Long medicoId, LocalDate fecha) {
        String url = agendaServiceUrl
                + "/api/citas?medicoId=" + medicoId
                + "&fecha=" + fecha;

        ResponseEntity<CitasPorMedicoFechaResponse> response =
                restTemplate.getForEntity(url, CitasPorMedicoFechaResponse.class);

        return response.getBody();
    }

    public CitaResponse crearCita(CrearCitaRequest request) {
        String url = agendaServiceUrl + "/api/citas";

        Map<String, Object> body = new HashMap<>();
        body.put("numeroDocumento", request.getNumeroDocumento());
        body.put("tipoDocumento", request.getTipoDocumento());
        body.put("nombres", request.getNombres());
        body.put("apellidos", request.getApellidos());
        body.put("celular", request.getCelular());
        body.put("genero", request.getGenero());
        body.put("fechaNacimiento", request.getFechaNacimiento() != null ? request.getFechaNacimiento().toString() : null);
        body.put("correo", request.getCorreo());
        body.put("medicoId", request.getMedicoId());
        body.put("fecha", request.getFecha() != null ? request.getFecha().toString() : null);
        body.put("hora", request.getHora() != null ? request.getHora().toString() : null);
        body.put("observacion", request.getObservacion());

        ResponseEntity<CitaResponse> response =
                restTemplate.postForEntity(url, body, CitaResponse.class);

        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> login(String username, String password) {
        String url = agendaServiceUrl + "/api/auth/login";

        Map<String, String> body = Map.of(
                "username", username,
                "password", password
        );

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, body, Map.class);

        return response.getBody();
    }

    public void registrarUsuario(String username, String password, String role) {
        String url = agendaServiceUrl + "/api/auth/register";

        Map<String, String> body = Map.of(
                "username", username,
                "password", password,
                "role", role
        );

        restTemplate.postForEntity(url, body, Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> recuperarPassword(String username) {
        String url = agendaServiceUrl + "/api/auth/forgot-password";

        Map<String, String> body = Map.of(
                "username", username
        );

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, body, Map.class);

        return response.getBody();
    }
}