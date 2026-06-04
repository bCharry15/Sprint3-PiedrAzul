package co.edu.unicauca.piedraazul.controller;

import java.text.Normalizer;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.enums.Genero;
import co.edu.unicauca.piedraazul.model.enums.UserRole;
import co.edu.unicauca.piedraazul.observer.Observer;
import co.edu.unicauca.piedraazul.pattern.factory.UsuarioFactory;
import co.edu.unicauca.piedraazul.service.IPacienteService;
import co.edu.unicauca.piedraazul.service.IUserService;
import co.edu.unicauca.piedraazul.util.DatePickerUtils;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.SesionUsuario;
import co.edu.unicauca.piedraazul.util.Vista;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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

    @FXML private Button adminRoleButton;
    @FXML private Button patientRoleButton;

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
        configurarCombos();
        configurarCalendario();
        actualizarCampoRol();
        actualizarEstiloBotonesRol();
    }

    private void configurarCombos() {
        if (documentTypeCombo != null) {
            documentTypeCombo.setItems(FXCollections.observableArrayList(
                    "Cédula de ciudadanía",
                    "Tarjeta de identidad",
                    "Cédula de extranjería",
                    "Pasaporte"
            ));
        }
    }

    private void configurarCalendario() {
        if (birthDatePicker != null) {
            DatePickerUtils.configurarDatePicker(birthDatePicker);
        }
    }

    @FXML
    private void selectAdminRole() {
        selectedRole = UserRole.ADMIN;
        actualizarCampoRol();
        actualizarEstiloBotonesRol();
    }

    @FXML
    private void selectPatientRole() {
        selectedRole = UserRole.PACIENTE;
        actualizarCampoRol();
        actualizarEstiloBotonesRol();
    }

    @FXML
    private void goToLogin() {
        sceneManager.switchScene(Vista.LOGIN);
    }

    @FXML
    private void register() {
        String primerNombre = normalizarNombreOApellido(getText(firstNameField));
        String segundoNombre = normalizarNombreOApellido(getText(secondNameField));
        String primerApellido = normalizarNombreOApellido(getText(lastNameField));
        String segundoApellido = normalizarNombreOApellido(getText(secondLastNameField));

        String usuario = normalizarUsername(getText(usernameField));
        String celular = normalizarTextoSimple(getText(phoneField));
        String numeroDocumento = normalizarTextoSimple(getText(documentNumberField));
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

        if (contrasena.length() < 6) {
            showAlert(Alert.AlertType.WARNING,
                    "Contraseña débil",
                    "La contraseña debe tener mínimo 6 caracteres.");
            return;
        }

        User user = UsuarioFactory.crearUsuario(usuario, contrasena, selectedRole);

        boolean registrado = userService.registerUser(user, this);

        if (!registrado) {
            showAlert(Alert.AlertType.ERROR,
                    "Registro fallido",
                    "El usuario ya existe o no pudo registrarse.");
            return;
        }

        boolean perfilPacienteGuardado = true;

        if (selectedRole == UserRole.PACIENTE) {
            perfilPacienteGuardado = guardarPerfilPaciente(
                    usuario,
                    contrasena,
                    numeroDocumento,
                    tipoDocumento,
                    primerNombre,
                    segundoNombre,
                    primerApellido,
                    segundoApellido,
                    celular
            );
        }

        if (selectedRole == UserRole.PACIENTE && !perfilPacienteGuardado) {
            showAlert(Alert.AlertType.WARNING,
                    "Registro parcialmente exitoso",
                    "El usuario fue registrado y ya puede iniciar sesión, pero no se pudo guardar el perfil del paciente automáticamente. "
                            + "Al ingresar, complete y guarde sus datos desde el panel paciente.");
        } else {
            showAlert(Alert.AlertType.INFORMATION,
                    "Registro exitoso",
                    "El usuario fue registrado correctamente.");
        }

        clearForm();
    }

    private boolean guardarPerfilPaciente(String usuario,
                                          String contrasena,
                                          String numeroDocumento,
                                          String tipoDocumento,
                                          String primerNombre,
                                          String segundoNombre,
                                          String primerApellido,
                                          String segundoApellido,
                                          String celular) {

        String nombresCompletos = unirNombres(primerNombre, segundoNombre);
        String apellidosCompletos = unirNombres(primerApellido, segundoApellido);

        try {
            /*
             * El endpoint de perfil del paciente requiere token JWT.
             * Como el usuario acaba de registrarse, todavía no hay sesión activa.
             * Por eso se autentica temporalmente para obtener token, se guarda el perfil
             * y luego se limpia el token para no dejar una sesión iniciada en registro.
             */
            User usuarioAutenticado = userService.authenticate(usuario, contrasena);

            if (usuarioAutenticado == null) {
                System.err.println("REGISTER-USER-CONTROLLER -> No se pudo autenticar temporalmente el usuario paciente: " + usuario);
                return false;
            }

            pacienteService.obtenerOCrearPaciente(
                    usuario,
                    numeroDocumento,
                    normalizarTipoDocumento(tipoDocumento),
                    nombresCompletos,
                    apellidosCompletos,
                    celular.isEmpty() ? "Sin celular" : celular,
                    Genero.OTRO,
                    birthDatePicker != null ? birthDatePicker.getValue() : null,
                    crearCorreoSeguro(usuario)
            );

            System.out.println("REGISTER-USER-CONTROLLER -> Perfil paciente guardado automáticamente para: " + usuario);
            return true;

        } catch (Exception e) {
            System.err.println("REGISTER-USER-CONTROLLER -> Usuario creado, pero no se pudo guardar perfil paciente automáticamente.");
            System.err.println("REGISTER-USER-CONTROLLER -> Detalle: " + e.getMessage());
            return false;

        } finally {
            SesionUsuario.limpiarSesion();
            AgendaServiceClient.limpiarToken();
        }
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

            if (birthDatePicker.getEditor() != null) {
                birthDatePicker.getEditor().clear();
            }
        }

        if (documentTypeCombo != null) {
            documentTypeCombo.setValue(null);
        }

        selectedRole = UserRole.ADMIN;
        actualizarCampoRol();
        actualizarEstiloBotonesRol();
    }

    @Override
    public void update(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        System.out.println("REGISTER-USER-CONTROLLER -> " + message);
    }

    private void actualizarCampoRol() {
        if (selectedRoleField != null) {
            selectedRoleField.setText(selectedRole != null ? selectedRole.name() : "");
        }
    }

    private void actualizarEstiloBotonesRol() {
        actualizarBotonRol(adminRoleButton, selectedRole == UserRole.ADMIN);
        actualizarBotonRol(patientRoleButton, selectedRole == UserRole.PACIENTE);
    }

    private void actualizarBotonRol(Button boton, boolean activo) {
        if (boton == null) {
            return;
        }

        boton.getStyleClass().remove("register-role-button-active");
        boton.getStyleClass().remove("register-role-button");

        if (activo) {
            boton.getStyleClass().add("register-role-button-active");
        } else {
            boton.getStyleClass().add("register-role-button");
        }
    }

    private String unirNombres(String principal, String secundario) {
        String textoCompleto;

        if (secundario == null || secundario.trim().isEmpty()) {
            textoCompleto = principal;
        } else {
            textoCompleto = principal + " " + secundario;
        }

        return normalizarNombreOApellido(textoCompleto);
    }

    private String normalizarNombreOApellido(String valor) {
        if (valor == null) {
            return "";
        }

        String sinTildes = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        String limpio = sinTildes.trim().replaceAll("\\s+", " ");

        if (limpio.isEmpty()) {
            return "";
        }

        String[] palabras = limpio.toLowerCase().split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (palabra.isBlank()) {
                continue;
            }

            resultado.append(Character.toUpperCase(palabra.charAt(0)));

            if (palabra.length() > 1) {
                resultado.append(palabra.substring(1));
            }

            resultado.append(" ");
        }

        return resultado.toString().trim();
    }

    private String normalizarTextoSimple(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim().replaceAll("\\s+", " ");
    }

    private String normalizarUsername(String valor) {
        if (valor == null) {
            return "";
        }

        return valor.trim().replaceAll("\\s+", "");
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

    private String crearCorreoSeguro(String usuario) {
        String usuarioLimpio = usuario == null ? "usuario" : usuario.trim().toLowerCase();

        usuarioLimpio = usuarioLimpio
                .replaceAll("[^a-z0-9._-]", ".")
                .replaceAll("\\.+", ".")
                .replaceAll("^\\.|\\.$", "");

        if (usuarioLimpio.isBlank()) {
            usuarioLimpio = "usuario";
        }

        return usuarioLimpio + "@example.com";
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