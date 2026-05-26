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
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

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
        Dialog<ResetPasswordData> dialog = new Dialog<>();
        dialog.setTitle("Restablecer contraseña");
        dialog.setHeaderText("Verifique sus datos y defina una nueva contraseña");

        ButtonType aceptarButtonType = new ButtonType("Restablecer", ButtonType.OK.getButtonData());
        dialog.getDialogPane().getButtonTypes().addAll(aceptarButtonType, ButtonType.CANCEL);

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Usuario");

        TextField documentoInput = new TextField();
        documentoInput.setPromptText("Número de documento");

        PasswordField nuevaPasswordInput = new PasswordField();
        nuevaPasswordInput.setPromptText("Nueva contraseña");

        PasswordField confirmarPasswordInput = new PasswordField();
        confirmarPasswordInput.setPromptText("Confirmar nueva contraseña");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Usuario:"), 0, 0);
        grid.add(usernameInput, 1, 0);

        grid.add(new Label("Documento:"), 0, 1);
        grid.add(documentoInput, 1, 1);

        grid.add(new Label("Nueva contraseña:"), 0, 2);
        grid.add(nuevaPasswordInput, 1, 2);

        grid.add(new Label("Confirmar contraseña:"), 0, 3);
        grid.add(confirmarPasswordInput, 1, 3);

        Label ayuda = new Label("Por seguridad, la contraseña no será mostrada ni enviada en pantalla.");
        ayuda.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        grid.add(ayuda, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == aceptarButtonType) {
                return new ResetPasswordData(
                        obtenerTexto(usernameInput),
                        obtenerTexto(documentoInput),
                        obtenerTexto(nuevaPasswordInput),
                        obtenerTexto(confirmarPasswordInput)
                );
            }

            return null;
        });

        Optional<ResetPasswordData> resultado = dialog.showAndWait();

        if (resultado.isEmpty()) {
            return;
        }

        ResetPasswordData data = resultado.get();

        if (data.username().isEmpty()
                || data.numeroDocumento().isEmpty()
                || data.nuevaPassword().isEmpty()
                || data.confirmarPassword().isEmpty()) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Campos obligatorios",
                    "Debe completar usuario, documento, nueva contraseña y confirmación."
            );
            return;
        }

        if (!data.nuevaPassword().equals(data.confirmarPassword())) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Validación",
                    "Las contraseñas no coinciden."
            );
            return;
        }

        if (data.nuevaPassword().length() < 6) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Contraseña débil",
                    "La nueva contraseña debe tener mínimo 6 caracteres."
            );
            return;
        }

        try {
            Map<String, String> response = agendaServiceClient.restablecerPasswordSeguro(
                    data.username(),
                    data.numeroDocumento(),
                    data.nuevaPassword()
            );

            String mensaje = response != null && response.get("mensaje") != null
                    ? response.get("mensaje")
                    : "La contraseña fue restablecida correctamente.";

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Contraseña restablecida",
                    mensaje
            );

        } catch (Exception e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "No se pudo restablecer la contraseña",
                    "Verifique que el usuario exista, que el documento sea correcto y que agenda-service esté corriendo."
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

    private record ResetPasswordData(
            String username,
            String numeroDocumento,
            String nuevaPassword,
            String confirmarPassword
    ) {
    }
}