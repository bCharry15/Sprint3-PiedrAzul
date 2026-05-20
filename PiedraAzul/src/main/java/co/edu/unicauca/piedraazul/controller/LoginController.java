package co.edu.unicauca.piedraazul.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.enums.UserRole;
import co.edu.unicauca.piedraazul.service.IUserService;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.SesionUsuario;
import co.edu.unicauca.piedraazul.util.Vista;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

@Component
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private final IUserService userService;
    private final SceneManager sceneManager;
    private final AgendaServiceClient agendaServiceClient;

    public LoginController(IUserService userService,
                           SceneManager sceneManager,
                           AgendaServiceClient agendaServiceClient) {
        this.userService = userService;
        this.sceneManager = sceneManager;
        this.agendaServiceClient = agendaServiceClient;
    }

    @FXML
    private void login() {
        String username = obtenerTexto(usernameField);
        String password = obtenerTexto(passwordField);

        if (username.isEmpty() || password.isEmpty()) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Campos obligatorios",
                    "Debe ingresar usuario y contraseña."
            );
            return;
        }

        User usuarioAutenticado = userService.authenticate(username, password);

        if (usuarioAutenticado == null) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Inicio de sesión fallido",
                    "Usuario o contraseña incorrectos."
            );
            return;
        }

        SesionUsuario.setUsuarioActual(usuarioAutenticado);

        redirigirSegunRol(usuarioAutenticado);
    }

    private void redirigirSegunRol(User usuario) {
        if (usuario.getRole() == null) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Rol inválido",
                    "El usuario no tiene un rol asignado."
            );
            return;
        }

        UserRole rol = usuario.getRole();

        switch (rol) {
            case ADMIN -> sceneManager.switchScene(Vista.ADMIN_PANEL);
            case AGENDADOR -> sceneManager.switchScene(Vista.AGENDADOR_PANEL);
            case MEDICO -> sceneManager.switchScene(Vista.MEDICO_PANEL);
            case PACIENTE -> sceneManager.switchScene(Vista.PACIENTE_PANEL);
            default -> mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Rol no soportado",
                    "El rol del usuario no tiene una vista asociada."
            );
        }
    }

    @FXML
    private void goToRegister() {
        sceneManager.switchScene(Vista.REGISTER);
    }

    @FXML
    private void forgotPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Recuperar contraseña");
        dialog.setHeaderText(null);
        dialog.setContentText("Ingrese su nombre de usuario:");

        Optional<String> resultado = dialog.showAndWait();

        if (resultado.isEmpty()) {
            return;
        }

        String username = resultado.get() == null ? "" : resultado.get().trim();

        if (username.isEmpty()) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Validación",
                    "Debe ingresar un nombre de usuario."
            );
            return;
        }

        try {
            Map<String, String> response = agendaServiceClient.recuperarPassword(username);

            if (response == null || response.get("passwordTemporal") == null) {
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "Error",
                        "No se pudo generar la contraseña temporal."
                );
                return;
            }

            String passwordTemporal = response.get("passwordTemporal");

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Contraseña recuperada",
                    "Se generó una contraseña temporal para el usuario: " + username
                            + "\n\nContraseña temporal: " + passwordTemporal
                            + "\n\nUse esta contraseña para iniciar sesión."
            );

        } catch (Exception e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo recuperar la contraseña.\n\n"
                            + "Verifique que el usuario exista y que agenda-service esté corriendo."
            );
        }
    }

    private String obtenerTexto(TextField campo) {
        if (campo == null || campo.getText() == null) {
            return "";
        }

        return campo.getText().trim();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}