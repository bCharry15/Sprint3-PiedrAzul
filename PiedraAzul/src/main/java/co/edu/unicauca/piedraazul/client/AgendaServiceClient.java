package co.edu.unicauca.piedraazul.client;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.dto.CitaResponse;
import co.edu.unicauca.piedraazul.model.dto.CitasPorMedicoFechaResponse;
import co.edu.unicauca.piedraazul.model.dto.CrearCitaRequest;
import co.edu.unicauca.piedraazul.model.dto.CrearDisponibilidadRequest;
import co.edu.unicauca.piedraazul.model.dto.CrearMedicoRequest;
import co.edu.unicauca.piedraazul.model.dto.DisponibilidadResponse;
import co.edu.unicauca.piedraazul.model.dto.DisponibilidadTablaModel;
import co.edu.unicauca.piedraazul.model.dto.MedicoResponse;
import co.edu.unicauca.piedraazul.model.enums.Genero;

@Component
public class AgendaServiceClient {

    @Value("${agenda.service.url}")
    private String agendaServiceUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static String accessToken;

    public AgendaServiceClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    public static String getAccessToken() {
        return accessToken;
    }

    public static void limpiarToken() {
        accessToken = null;
    }

    private HttpHeaders crearHeadersJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (accessToken != null && !accessToken.isBlank()) {
            headers.setBearerAuth(accessToken);
        }

        return headers;
    }

    private HttpHeaders crearHeadersCsv() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.valueOf("text/csv")));

        if (accessToken != null && !accessToken.isBlank()) {
            headers.setBearerAuth(accessToken);
        }

        return headers;
    }

    private HttpEntity<Void> crearEntidadAutenticada() {
        return new HttpEntity<>(crearHeadersJson());
    }

    private HttpEntity<Void> crearEntidadCsvAutenticada() {
        return new HttpEntity<>(crearHeadersCsv());
    }

    private <T> HttpEntity<T> crearEntidadAutenticada(T body) {
        return new HttpEntity<>(body, crearHeadersJson());
    }

    public MedicoResponse[] listarMedicos() {
        String url = agendaServiceUrl + "/api/medicos";

        try {
            ResponseEntity<MedicoResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    crearEntidadAutenticada(),
                    MedicoResponse[].class
            );

            return response.getBody();

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    public MedicoResponse crearMedico(CrearMedicoRequest request) {
        String url = agendaServiceUrl + "/api/medicos";

        try {
            ResponseEntity<MedicoResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    crearEntidadAutenticada(request),
                    MedicoResponse.class
            );

            return response.getBody();

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    public MedicoResponse actualizarMedico(Long medicoId, CrearMedicoRequest request) {
        String url = agendaServiceUrl + "/api/medicos/" + medicoId;

        Map<String, Object> body = new HashMap<>();
        body.put("nombreCompleto", request.getNombreCompleto());
        body.put("especialidad", request.getEspecialidad());
        body.put("intervaloMinutos", request.getIntervaloMinutos());
        body.put("username", request.getUsername());
        body.put("password", request.getPassword());

        try {
            ResponseEntity<MedicoResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    crearEntidadAutenticada(body),
                    MedicoResponse.class
            );

            return response.getBody();

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    public void eliminarMedico(Long medicoId) {
        String url = agendaServiceUrl + "/api/medicos/" + medicoId;

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    crearEntidadAutenticada(),
                    Void.class
            );

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    public DisponibilidadResponse consultarDisponibilidad(Long medicoId, LocalDate fecha) {
        String url = agendaServiceUrl
                + "/api/disponibilidad?medicoId=" + medicoId
                + "&fecha=" + fecha;

        try {
            ResponseEntity<DisponibilidadResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    crearEntidadAutenticada(),
                    DisponibilidadResponse.class
            );

            return response.getBody();

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    public void crearDisponibilidad(CrearDisponibilidadRequest request) {
        String url = agendaServiceUrl + "/api/configuraciones-disponibilidad";

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    crearEntidadAutenticada(crearBodyDisponibilidad(request)),
                    Map.class
            );

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    public void actualizarDisponibilidad(Long disponibilidadId, CrearDisponibilidadRequest request) {
        String url = agendaServiceUrl + "/api/configuraciones-disponibilidad/" + disponibilidadId;

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    crearEntidadAutenticada(crearBodyDisponibilidad(request)),
                    Map.class
            );

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    public DisponibilidadTablaModel[] listarDisponibilidadesPorMedico(Long medicoId) {
        String url = agendaServiceUrl + "/api/configuraciones-disponibilidad/medico/" + medicoId;

        try {
            ResponseEntity<Map[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    crearEntidadAutenticada(),
                    Map[].class
            );

            Map[] body = response.getBody();

            if (body == null) {
                return new DisponibilidadTablaModel[0];
            }

            DisponibilidadTablaModel[] disponibilidades = new DisponibilidadTablaModel[body.length];

            for (int i = 0; i < body.length; i++) {
                disponibilidades[i] = convertirMapaADisponibilidad(body[i]);
            }

            return disponibilidades;

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    private Map<String, Object> crearBodyDisponibilidad(CrearDisponibilidadRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("medicoId", request.getMedicoId());
        body.put("diaSemana", request.getDiaSemana() != null ? request.getDiaSemana().name() : null);
        body.put("horaInicio", request.getHoraInicio() != null ? request.getHoraInicio().toString() : null);
        body.put("horaFin", request.getHoraFin() != null ? request.getHoraFin().toString() : null);
        body.put("intervaloMinutos", request.getIntervaloMinutos());
        body.put("ventanaSemanas", request.getVentanaSemanas());
        body.put("activo", true);
        return body;
    }

    public CitasPorMedicoFechaResponse listarCitasPorMedicoYFecha(Long medicoId, LocalDate fecha) {
        String url = agendaServiceUrl
                + "/api/citas?medicoId=" + medicoId
                + "&fecha=" + fecha;

        try {
            ResponseEntity<CitasPorMedicoFechaResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    crearEntidadAutenticada(),
                    CitasPorMedicoFechaResponse.class
            );

            return response.getBody();

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    public String exportarCitasPorMedicoYFecha(Long medicoId, LocalDate fecha) {
        String url = agendaServiceUrl
                + "/api/citas/exportar?medicoId=" + medicoId
                + "&fecha=" + fecha;

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    crearEntidadCsvAutenticada(),
                    String.class
            );

            return response.getBody();

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    public CitaResponse[] listarCitasPorPaciente(String numeroDocumento) {
        String url = agendaServiceUrl + "/api/citas/paciente/" + numeroDocumento;

        try {
            ResponseEntity<CitaResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    crearEntidadAutenticada(),
                    CitaResponse[].class
            );

            return response.getBody();

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
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

        try {
            ResponseEntity<CitaResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    crearEntidadAutenticada(body),
                    CitaResponse.class
            );

            return response.getBody();

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    public CitaResponse cambiarEstadoCita(Long citaId, String estado, String observacion) {
        String url = agendaServiceUrl + "/api/citas/" + citaId + "/estado";

        Map<String, Object> body = new HashMap<>();
        body.put("estado", estado);
        body.put("observacion", observacion);

        try {
            ResponseEntity<CitaResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    crearEntidadAutenticada(body),
                    CitaResponse.class
            );

            return response.getBody();

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> login(String username, String password) {
        String url = agendaServiceUrl + "/api/auth/login";

        Map<String, String> body = Map.of(
                "username", username,
                "password", password
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, String> responseBody = response.getBody();

            if (responseBody != null && responseBody.get("access_token") != null) {
                accessToken = responseBody.get("access_token");
            }

            return responseBody;

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    public void registrarUsuario(String username, String password, String role) {
        String url = agendaServiceUrl + "/api/auth/register";

        Map<String, String> body = Map.of(
                "username", username,
                "password", password,
                "role", role
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> recuperarPassword(String username) {
        String url = agendaServiceUrl + "/api/auth/forgot-password";

        Map<String, String> body = Map.of(
                "username", username
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );

        return response.getBody();
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> restablecerPasswordSeguro(String username,
                                                         String numeroDocumento,
                                                         String nuevaPassword) {
        String url = agendaServiceUrl + "/api/auth/reset-password";

        Map<String, String> body = Map.of(
                "username", username,
                "numeroDocumento", numeroDocumento,
                "nuevaPassword", nuevaPassword
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );

        return response.getBody();
    }

    public User[] listarUsuariosPorRol(String role) {
        String url = agendaServiceUrl + "/api/auth/users/role/" + role;

        try {
            ResponseEntity<User[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    crearEntidadAutenticada(),
                    User[].class
            );

            return response.getBody() == null ? new User[0] : response.getBody();

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public Paciente buscarPerfilPacientePorUsername(String username) {
        String url = agendaServiceUrl + "/api/pacientes/perfil/" + username;

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    crearEntidadAutenticada(),
                    Map.class
            );

            return convertirMapaAPaciente(response.getBody());

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public Paciente guardarPerfilPaciente(Paciente paciente) {
        String url = agendaServiceUrl + "/api/pacientes/perfil";

        Map<String, Object> body = new HashMap<>();
        body.put("username", paciente.getUsername());
        body.put("numeroDocumento", paciente.getNumeroDocumento());
        body.put("tipoDocumento", paciente.getTipoDocumento());
        body.put("nombres", paciente.getNombres());
        body.put("apellidos", paciente.getApellidos());
        body.put("celular", paciente.getCelular());
        body.put("genero", paciente.getGenero() != null ? paciente.getGenero().name() : null);
        body.put("fechaNacimiento", paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento().toString() : null);
        body.put("correo", paciente.getCorreo());

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    crearEntidadAutenticada(body),
                    Map.class
            );

            return convertirMapaAPaciente(response.getBody());

        } catch (RestClientResponseException ex) {
            throw new RuntimeException(extraerMensajeError(ex));
        }
    }

    private Paciente convertirMapaAPaciente(Map<String, Object> body) {
        if (body == null) {
            return null;
        }

        Paciente paciente = new Paciente();

        Object id = body.get("id");
        if (id instanceof Number numero) {
            paciente.setId(numero.longValue());
        }

        paciente.setUsername(convertirAString(body.get("username")));
        paciente.setNumeroDocumento(convertirAString(body.get("numeroDocumento")));
        paciente.setTipoDocumento(convertirAString(body.get("tipoDocumento")));
        paciente.setNombres(convertirAString(body.get("nombres")));
        paciente.setApellidos(convertirAString(body.get("apellidos")));
        paciente.setCelular(convertirAString(body.get("celular")));

        String genero = convertirAString(body.get("genero"));
        if (genero != null && !genero.isBlank()) {
            try {
                paciente.setGenero(Genero.valueOf(genero.trim().toUpperCase()));
            } catch (Exception e) {
                paciente.setGenero(Genero.OTRO);
            }
        }

        String fechaNacimiento = convertirAString(body.get("fechaNacimiento"));
        if (fechaNacimiento != null && !fechaNacimiento.isBlank()) {
            try {
                paciente.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
            } catch (Exception ignored) {
                paciente.setFechaNacimiento(null);
            }
        }

        paciente.setCorreo(convertirAString(body.get("correo")));

        return paciente;
    }

    @SuppressWarnings("unchecked")
    private DisponibilidadTablaModel convertirMapaADisponibilidad(Map<String, Object> body) {
        if (body == null) {
            return new DisponibilidadTablaModel();
        }

        Long medicoId = convertirALong(body.get("medicoId"));
        String medicoNombre = "";

        Object medicoObjeto = body.get("medico");

        if (medicoObjeto instanceof Map<?, ?> medicoMap) {
            medicoId = convertirALong(medicoMap.get("id"));
            medicoNombre = convertirAString(medicoMap.get("nombreCompleto"));
        }

        return new DisponibilidadTablaModel(
                convertirALong(body.get("id")),
                medicoId,
                medicoNombre,
                convertirADayOfWeek(body.get("diaSemana")),
                convertirALocalTime(body.get("horaInicio")),
                convertirALocalTime(body.get("horaFin")),
                convertirAInteger(body.get("intervaloMinutos")),
                convertirAInteger(body.get("ventanaSemanas")),
                convertirABoolean(body.get("activo"))
        );
    }

    private String extraerMensajeError(RestClientResponseException ex) {
        String cuerpo = ex.getResponseBodyAsString();

        if (cuerpo != null && !cuerpo.isBlank()) {
            try {
                JsonNode json = objectMapper.readTree(cuerpo);

                if (json.hasNonNull("message")) {
                    String mensaje = json.get("message").asText();

                    if (mensaje != null && !mensaje.isBlank()) {
                        return mensaje;
                    }
                }

                if (json.hasNonNull("error")) {
                    String error = json.get("error").asText();

                    if (error != null && !error.isBlank()) {
                        return "Error " + ex.getRawStatusCode() + ": " + error;
                    }
                }

            } catch (Exception ignored) {
                return cuerpo;
            }
        }

        return "Error " + ex.getRawStatusCode() + " al comunicarse con agenda-service.";
    }

    private String convertirAString(Object valor) {
        if (valor == null) {
            return null;
        }

        String texto = valor.toString().trim();

        return texto.isEmpty() ? null : texto;
    }

    private Long convertirALong(Object valor) {
        if (valor == null) {
            return null;
        }

        if (valor instanceof Number numero) {
            return numero.longValue();
        }

        try {
            return Long.parseLong(valor.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer convertirAInteger(Object valor) {
        if (valor == null) {
            return null;
        }

        if (valor instanceof Number numero) {
            return numero.intValue();
        }

        try {
            return Integer.parseInt(valor.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean convertirABoolean(Object valor) {
        if (valor == null) {
            return false;
        }

        if (valor instanceof Boolean booleano) {
            return booleano;
        }

        return Boolean.parseBoolean(valor.toString());
    }

    private DayOfWeek convertirADayOfWeek(Object valor) {
        if (valor == null) {
            return null;
        }

        try {
            return DayOfWeek.valueOf(valor.toString().trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime convertirALocalTime(Object valor) {
        if (valor == null) {
            return null;
        }

        if (valor instanceof java.util.List<?> lista && lista.size() >= 2) {
            try {
                int hora = ((Number) lista.get(0)).intValue();
                int minuto = ((Number) lista.get(1)).intValue();
                return LocalTime.of(hora, minuto);
            } catch (Exception ignored) {
                return null;
            }
        }

        try {
            return LocalTime.parse(valor.toString());
        } catch (Exception e) {
            return null;
        }
    }
}