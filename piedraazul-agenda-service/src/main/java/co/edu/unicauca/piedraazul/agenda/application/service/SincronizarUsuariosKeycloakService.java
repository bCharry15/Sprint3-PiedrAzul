package co.edu.unicauca.piedraazul.agenda.application.service;

import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.RegistrarUsuarioKeycloakPort;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;

@Service
public class SincronizarUsuariosKeycloakService {

    public static final String PASSWORD_RECUPERACION = "PiedraAzul123";

    private static final int INTENTOS_MAXIMOS = 10;
    private static final long ESPERA_ENTRE_INTENTOS_MS = 3000;

    private final RegistrarUsuarioKeycloakPort registrarUsuarioKeycloakPort;

    public SincronizarUsuariosKeycloakService(RegistrarUsuarioKeycloakPort registrarUsuarioKeycloakPort) {
        this.registrarUsuarioKeycloakPort = registrarUsuarioKeycloakPort;
    }

    public void sincronizarUsuarioObligatorio(String username, String password, UserRole role) {
        sincronizarUsuario(username, password, role, true);
    }

    public void sincronizarUsuarioEnArranque(String username, String password, UserRole role) {
        sincronizarUsuario(username, password, role, false);
    }

    private void sincronizarUsuario(String username,
                                    String password,
                                    UserRole role,
                                    boolean obligatorio) {
        validarDatos(username, password, role);

        String usernameNormalizado = username.trim();
        String passwordNormalizado = password.trim();

        Exception ultimoError = null;

        for (int intento = 1; intento <= INTENTOS_MAXIMOS; intento++) {
            try {
                registrarUsuarioKeycloakPort.registrarUsuario(
                        usernameNormalizado,
                        passwordNormalizado,
                        role.name()
                );

                System.out.println("AGENDA-SERVICE -> Usuario sincronizado con Keycloak: "
                        + usernameNormalizado + " / rol: " + role.name());

                return;

            } catch (Exception ex) {
                ultimoError = ex;

                if (esConflictoPorUsuarioExistente(ex)) {
                    actualizarPasswordUsuarioExistente(usernameNormalizado, passwordNormalizado);
                    return;
                }

                System.out.println("AGENDA-SERVICE -> Intento " + intento + "/"
                        + INTENTOS_MAXIMOS
                        + " falló sincronizando usuario con Keycloak: "
                        + usernameNormalizado
                        + ". Detalle: "
                        + obtenerMensaje(ex));

                esperarAntesDeReintentar();
            }
        }

        String mensaje = "No se pudo sincronizar el usuario con Keycloak después de "
                + INTENTOS_MAXIMOS
                + " intentos: "
                + usernameNormalizado
                + ". Detalle: "
                + obtenerMensaje(ultimoError);

        if (obligatorio) {
            throw new IllegalStateException(mensaje, ultimoError);
        }

        System.out.println("AGENDA-SERVICE -> " + mensaje);
    }

    private void actualizarPasswordUsuarioExistente(String username, String password) {
        try {
            registrarUsuarioKeycloakPort.actualizarPassword(
                    username,
                    password,
                    false
            );

            System.out.println("AGENDA-SERVICE -> Usuario ya existía en Keycloak. Password sincronizada para: "
                    + username);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "El usuario ya existía en Keycloak, pero no se pudo actualizar su password: "
                            + username
                            + ". Detalle: "
                            + obtenerMensaje(ex),
                    ex
            );
        }
    }

    private void validarDatos(String username, String password, UserRole role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El username es obligatorio para sincronizar con Keycloak.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria para sincronizar con Keycloak.");
        }

        if (role == null) {
            throw new IllegalArgumentException("El rol es obligatorio para sincronizar con Keycloak.");
        }
    }

    private boolean esConflictoPorUsuarioExistente(Exception ex) {
        String mensaje = obtenerMensaje(ex).toLowerCase();

        return mensaje.contains("409")
                || mensaje.contains("conflict")
                || mensaje.contains("already exists")
                || mensaje.contains("ya existe")
                || mensaje.contains("user exists")
                || mensaje.contains("exists");
    }

    private void esperarAntesDeReintentar() {
        try {
            Thread.sleep(ESPERA_ENTRE_INTENTOS_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String obtenerMensaje(Exception ex) {
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