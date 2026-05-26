package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.RegistrarUsuarioKeycloakPort;

@Component
public class KeycloakUsuarioAdapter implements RegistrarUsuarioKeycloakPort {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.base-url}")
    private String keycloakBaseUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-token-uri}")
    private String adminTokenUri;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    @Override
    public void registrarUsuario(String username, String password, String role) {
        try {
            validarTexto(username, "El username es obligatorio para Keycloak.");
            validarTexto(password, "La password es obligatoria para Keycloak.");
            validarTexto(role, "El rol es obligatorio para Keycloak.");

            String usernameNormalizado = username.trim();
            String roleNormalizado = role.trim().toUpperCase();

            String adminToken = obtenerAdminToken();

            crearUsuarioSiNoExiste(usernameNormalizado, password, roleNormalizado, adminToken);

            String userId = obtenerIdUsuario(usernameNormalizado, adminToken);

            actualizarPerfilUsuario(userId, usernameNormalizado, roleNormalizado, adminToken);
            actualizarPasswordPorId(userId, password, false, adminToken);
            asignarRolAUsuario(userId, roleNormalizado, adminToken);

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible sincronizar el usuario con Keycloak: " + ex.getMessage(),
                    ex
            );
        }
    }

    @Override
    public void actualizarPassword(String username, String nuevaPassword, boolean temporal) {
        try {
            validarTexto(username, "El username es obligatorio para actualizar contraseña en Keycloak.");
            validarTexto(nuevaPassword, "La nueva contraseña es obligatoria para Keycloak.");

            String adminToken = obtenerAdminToken();
            String userId = obtenerIdUsuario(username.trim(), adminToken);

            actualizarPerfilUsuario(userId, username.trim(), "PACIENTE", adminToken);
            actualizarPasswordPorId(userId, nuevaPassword, temporal, adminToken);

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible actualizar la contraseña en Keycloak: " + ex.getMessage(),
                    ex
            );
        }
    }

    private String obtenerAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=password"
                + "&client_id=admin-cli"
                + "&username=" + adminUsername
                + "&password=" + adminPassword;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                adminTokenUri,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getBody() == null || response.getBody().get("access_token") == null) {
            throw new IllegalStateException("Keycloak no retorno access_token de administrador.");
        }

        return response.getBody().get("access_token").toString();
    }

    private void crearUsuarioSiNoExiste(
            String username,
            String password,
            String role,
            String adminToken
    ) {
        String url = keycloakBaseUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = crearHeadersAdmin(adminToken);

        Map<String, Object> credential = Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        );

        Map<String, Object> body = Map.of(
                "username", username,
                "enabled", true,
                "email", construirEmail(username),
                "emailVerified", true,
                "firstName", construirFirstName(role),
                "lastName", username,
                "credentials", List.of(credential),
                "requiredActions", List.of()
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Void.class
            );

        } catch (HttpClientErrorException.Conflict ex) {
            System.out.println("KEYCLOAK -> Usuario ya existia, se actualizara perfil, contraseña y rol: " + username);
        }
    }

    private String obtenerIdUsuario(String username, String adminToken) {
        String usernameCodificado = URLEncoder.encode(username, StandardCharsets.UTF_8);

        String url = keycloakBaseUrl
                + "/admin/realms/" + realm
                + "/users?username=" + usernameCodificado
                + "&exact=true";

        HttpHeaders headers = crearHeadersAdmin(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                List.class
        );

        if (response.getBody() == null || response.getBody().isEmpty()) {
            throw new IllegalStateException("No se encontro el usuario en Keycloak: " + username);
        }

        Map usuario = (Map) response.getBody().get(0);

        Object id = usuario.get("id");

        if (id == null) {
            throw new IllegalStateException("Keycloak no retorno ID para el usuario: " + username);
        }

        return id.toString();
    }

    private void actualizarPerfilUsuario(
            String userId,
            String username,
            String role,
            String adminToken
    ) {
        String url = keycloakBaseUrl
                + "/admin/realms/" + realm
                + "/users/" + userId;

        HttpHeaders headers = crearHeadersAdmin(adminToken);

        Map<String, Object> body = Map.of(
                "username", username,
                "enabled", true,
                "email", construirEmail(username),
                "emailVerified", true,
                "firstName", construirFirstName(role),
                "lastName", username,
                "requiredActions", List.of()
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                request,
                Void.class
        );
    }

    private void actualizarPasswordPorId(
            String userId,
            String nuevaPassword,
            boolean temporal,
            String adminToken
    ) {
        String url = keycloakBaseUrl
                + "/admin/realms/" + realm
                + "/users/" + userId
                + "/reset-password";

        HttpHeaders headers = crearHeadersAdmin(adminToken);

        Map<String, Object> credential = Map.of(
                "type", "password",
                "value", nuevaPassword,
                "temporary", temporal
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(credential, headers);

        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                request,
                Void.class
        );
    }

    private void asignarRolAUsuario(String userId, String role, String adminToken) {
        Map rolKeycloak = obtenerRepresentacionRol(role, adminToken);

        String url = keycloakBaseUrl
                + "/admin/realms/" + realm
                + "/users/" + userId
                + "/role-mappings/realm";

        HttpHeaders headers = crearHeadersAdmin(adminToken);

        HttpEntity<List<Map>> request = new HttpEntity<>(
                List.of(rolKeycloak),
                headers
        );

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Void.class
            );

        } catch (HttpClientErrorException.Conflict ex) {
            System.out.println("KEYCLOAK -> El rol ya estaba asignado al usuario.");
        }
    }

    private Map obtenerRepresentacionRol(String role, String adminToken) {
        String url = keycloakBaseUrl + "/admin/realms/" + realm + "/roles/" + role;

        HttpHeaders headers = crearHeadersAdmin(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Map.class
        );

        if (response.getBody() == null) {
            throw new IllegalStateException("No existe el rol en Keycloak: " + role);
        }

        return response.getBody();
    }

    private String construirEmail(String username) {
        String limpio = username == null
                ? "usuario"
                : username.toLowerCase().replaceAll("[^a-z0-9._-]", "");

        if (limpio.isBlank()) {
            limpio = "usuario";
        }

        return limpio + "@piedraazul.local";
    }

    private String construirFirstName(String role) {
        if (role == null || role.isBlank()) {
            return "Usuario";
        }

        return switch (role.trim().toUpperCase()) {
            case "ADMIN" -> "Administrador";
            case "AGENDADOR" -> "Agendador";
            case "MEDICO" -> "Medico";
            case "PACIENTE" -> "Paciente";
            default -> "Usuario";
        };
    }

    private HttpHeaders crearHeadersAdmin(String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        return headers;
    }

    private void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}