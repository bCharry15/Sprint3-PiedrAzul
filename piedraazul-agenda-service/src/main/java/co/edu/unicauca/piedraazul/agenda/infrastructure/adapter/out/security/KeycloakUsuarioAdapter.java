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
            String usernameNormalizado = validarTexto(username, "El username es obligatorio.");
            String passwordNormalizada = validarTexto(password, "La contraseña es obligatoria.");
            String rolNormalizado = validarTexto(role, "El rol es obligatorio.").toUpperCase();

            String adminToken = obtenerAdminToken();

            crearUsuarioSiNoExiste(usernameNormalizado, adminToken);

            String userId = obtenerIdUsuario(usernameNormalizado, adminToken);

            habilitarUsuarioYLimpiarAccionesPendientes(userId, usernameNormalizado, adminToken);
            actualizarPasswordPorId(userId, passwordNormalizada, false, adminToken);
            asignarRolAUsuario(userId, rolNormalizado, adminToken);

            System.out.println("AGENDA-SERVICE -> Usuario sincronizado con Keycloak: "
                    + usernameNormalizado + " / rol: " + rolNormalizado);

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible sincronizar el usuario con Keycloak: " + obtenerMensajeCompleto(ex),
                    ex
            );
        }
    }

    @Override
    public void actualizarPassword(String username, String nuevaPassword, boolean temporal) {
        try {
            String usernameNormalizado = validarTexto(username, "El username es obligatorio.");
            String passwordNormalizada = validarTexto(nuevaPassword, "La nueva contraseña es obligatoria.");

            String adminToken = obtenerAdminToken();
            String userId = obtenerIdUsuario(usernameNormalizado, adminToken);

            habilitarUsuarioYLimpiarAccionesPendientes(userId, usernameNormalizado, adminToken);
            actualizarPasswordPorId(userId, passwordNormalizada, temporal, adminToken);

            System.out.println("AGENDA-SERVICE -> Password actualizada en Keycloak para: " + usernameNormalizado);

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible actualizar la contraseña en Keycloak: " + obtenerMensajeCompleto(ex),
                    ex
            );
        }
    }

    private String obtenerAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=password"
                + "&client_id=admin-cli"
                + "&username=" + URLEncoder.encode(adminUsername, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(adminPassword, StandardCharsets.UTF_8);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                adminTokenUri,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getBody() == null || response.getBody().get("access_token") == null) {
            throw new IllegalStateException("Keycloak no retornó access_token de administrador.");
        }

        return response.getBody().get("access_token").toString();
    }

    private void crearUsuarioSiNoExiste(String username, String adminToken) {
        String url = keycloakBaseUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = crearHeadersAdmin(adminToken);

        Map<String, Object> body = Map.of(
                "username", username,
                "enabled", true,
                "emailVerified", true,
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

            System.out.println("AGENDA-SERVICE -> Usuario creado en Keycloak: " + username);

        } catch (HttpClientErrorException.Conflict ex) {
            System.out.println("AGENDA-SERVICE -> Usuario ya existía en Keycloak: " + username);
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
            throw new IllegalStateException("No se encontró el usuario en Keycloak: " + username);
        }

        Map usuario = (Map) response.getBody().get(0);

        if (usuario.get("id") == null) {
            throw new IllegalStateException("Keycloak no retornó id para el usuario: " + username);
        }

        return usuario.get("id").toString();
    }

    private void habilitarUsuarioYLimpiarAccionesPendientes(String userId,
                                                            String username,
                                                            String adminToken) {
        String url = keycloakBaseUrl
                + "/admin/realms/" + realm
                + "/users/" + userId;

        HttpHeaders headers = crearHeadersAdmin(adminToken);

        Map<String, Object> body = Map.of(
                "username", username,
                "enabled", true,
                "emailVerified", true,
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

    private void actualizarPasswordPorId(String userId,
                                         String nuevaPassword,
                                         boolean temporal,
                                         String adminToken) {
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
            System.out.println("AGENDA-SERVICE -> El usuario ya tenía asignado el rol: " + role);
        }
    }

    private Map obtenerRepresentacionRol(String role, String adminToken) {
        String rolCodificado = URLEncoder.encode(role, StandardCharsets.UTF_8);

        String url = keycloakBaseUrl + "/admin/realms/" + realm + "/roles/" + rolCodificado;

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

    private HttpHeaders crearHeadersAdmin(String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        return headers;
    }

    private String validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }

        return valor.trim();
    }

    private String obtenerMensajeCompleto(Exception ex) {
        if (ex == null) {
            return "";
        }

        StringBuilder mensaje = new StringBuilder();

        Throwable actual = ex;
        while (actual != null) {
            if (actual.getMessage() != null) {
                mensaje.append(actual.getMessage()).append(" | ");
            }
            actual = actual.getCause();
        }

        return mensaje.toString();
    }
}