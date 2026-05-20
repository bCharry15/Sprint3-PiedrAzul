package co.edu.unicauca.piedraazul.controller;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.enums.Genero;
import co.edu.unicauca.piedraazul.model.enums.UserRole;
import co.edu.unicauca.piedraazul.observer.Observer;
import co.edu.unicauca.piedraazul.pattern.factory.UsuarioFactory;
import co.edu.unicauca.piedraazul.service.IPacienteService;
import co.edu.unicauca.piedraazul.service.IUserService;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.Vista;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

@Component
public class RegisterUserController implements Observer {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField secondNameField;
    @FXML private TextField secondLastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthDatePicker;
    @FXML private ComboBox<String> documentTypeCombo;
    @FXML private TextField documentNumberField;
    @FXML private TextField selectedRoleField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private final IUserService userService;
    private final IPacienteService pacienteService;
    private final SceneManager sceneManager;

    private UserRole selectedRole = UserRole.ADMIN;

    public RegisterUserController(IUserService userService,
                                  IPacienteService pacienteService,
                                  SceneManager sceneManager) {
        this.userService = userService;
        this.pacienteService = pacienteService;
        this.sceneManager = sceneManager;
    }

    @FXML
    public void initialize() {
        if (documentTypeCombo != null) {
            documentTypeCombo.setItems(FXCollections.observableArrayList(
                    "Cédula de ciudadanía",
                    "Tarjeta de identidad",
                    "Cédula de extranjería",
                    "Pasaporte"
            ));
        }

        actualizarCampoRol();
    }

    @FXML
    private void selectAdminRole() {
        selectedRole = UserRole.ADMIN;
        actualizarCampoRol();
    }

    @FXML
    private void selectPatientRole() {
        selectedRole = UserRole.PACIENTE;
        actualizarCampoRol();
    }

    @FXML
    private void goToLogin() {
        sceneManager.switchScene(Vista.LOGIN);
    }

    @FXML
    private void register() {
        String primerNombre = getText(firstNameField);
        String segundoNombre = getText(secondNameField);
        String primerApellido = getText(lastNameField);
        String segundoApellido = getText(secondLastNameField);
        String usuario = getText(usernameField);
        String celular = getText(phoneField);
        String numeroDocumento = getText(documentNumberField);
        String contrasena = getText(passwordField);
        String confirmarContrasena = getText(confirmPasswordField);

        String tipoDocumento = documentTypeCombo != null
                ? documentTypeCombo.getValue()
                : null;

        if (primerNombre.isEmpty() ||
                primerApellido.isEmpty() ||
                usuario.isEmpty() ||
                tipoDocumento == null ||
                tipoDocumento.trim().isEmpty() ||
                numeroDocumento.isEmpty() ||
                contrasena.isEmpty() ||
                confirmarContrasena.isEmpty()) {

            showAlert(Alert.AlertType.WARNING,
                    "Campos obligatorios",
                    "Debe completar todos los campos marcados con *.");
            return;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            showAlert(Alert.AlertType.ERROR,
                    "Validación",
                    "Las contraseñas no coinciden.");
            return;
        }

        User user = UsuarioFactory.crearUsuario(usuario, contrasena, selectedRole);

        boolean registrado = userService.registerUser(user, this);

        if (registrado) {
            if (selectedRole == UserRole.PACIENTE) {
                guardarPerfilPaciente(
                        usuario,
                        numeroDocumento,
                        tipoDocumento,
                        primerNombre,
                        segundoNombre,
                        primerApellido,
                        segundoApellido,
                        celular
                );
            }

            showAlert(Alert.AlertType.INFORMATION,
                    "Registro exitoso",
                    "El usuario fue registrado correctamente.");

            clearForm();
        } else {
            showAlert(Alert.AlertType.ERROR,
                    "Registro fallido",
                    "El usuario ya existe o no pudo registrarse.");
        }
    }

    private void guardarPerfilPaciente(String usuario,
                                       String numeroDocumento,
                                       String tipoDocumento,
                                       String primerNombre,
                                       String segundoNombre,
                                       String primerApellido,
                                       String segundoApellido,
                                       String celular) {

        String nombresCompletos = unirNombres(primerNombre, segundoNombre);
        String apellidosCompletos = unirNombres(primerApellido, segundoApellido);

        pacienteService.obtenerOCrearPaciente(
                usuario,
                numeroDocumento,
                normalizarTipoDocumento(tipoDocumento),
                nombresCompletos,
                apellidosCompletos,
                celular.isEmpty() ? "Sin celular" : celular,
                Genero.OTRO,
                birthDatePicker != null ? birthDatePicker.getValue() : null,
                usuario + "@example.com"
        );
    }

    @FXML
    private void clearForm() {
        clearIfNotNull(firstNameField);
        clearIfNotNull(lastNameField);
        clearIfNotNull(secondNameField);
        clearIfNotNull(secondLastNameField);
        clearIfNotNull(usernameField);
        clearIfNotNull(phoneField);
        clearIfNotNull(documentNumberField);
        clearIfNotNull(passwordField);
        clearIfNotNull(confirmPasswordField);

        if (birthDatePicker != null) {
            birthDatePicker.setValue(null);
        }

        if (documentTypeCombo != null) {
            documentTypeCombo.setValue(null);
        }

        actualizarCampoRol();
    }

    @Override
    public void update(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Información", message);
    }

    private void actualizarCampoRol() {
        if (selectedRoleField != null) {
            selectedRoleField.setText(selectedRole != null ? selectedRole.name() : "");
        }
    }

    private String unirNombres(String principal, String secundario) {
        if (secundario == null || secundario.trim().isEmpty()) {
            return principal;
        }

        return principal + " " + secundario.trim();
    }

    private String normalizarTipoDocumento(String tipoDocumento) {
        if (tipoDocumento == null) {
            return "CC";
        }

        return switch (tipoDocumento) {
            case "Cédula de ciudadanía" -> "CC";
            case "Tarjeta de identidad" -> "TI";
            case "Cédula de extranjería" -> "CE";
            case "Pasaporte" -> "PASAPORTE";
            default -> tipoDocumento;
        };
    }

    private void clearIfNotNull(TextField field) {
        if (field != null) {
            field.clear();
        }
    }

    private String getText(TextField field) {
        return (field == null || field.getText() == null)
                ? ""
                : field.getText().trim();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}