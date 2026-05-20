package co.edu.unicauca.piedraazul.controller;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.dto.AgendadorTablaModel;
import co.edu.unicauca.piedraazul.model.dto.CrearDisponibilidadRequest;
import co.edu.unicauca.piedraazul.model.dto.CrearMedicoRequest;
import co.edu.unicauca.piedraazul.model.dto.MedicoResponse;
import co.edu.unicauca.piedraazul.model.dto.MedicoTablaModel;
import co.edu.unicauca.piedraazul.service.IAgendadorService;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.UserSession;
import co.edu.unicauca.piedraazul.util.Vista;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

@Component
public class AdminPanelController {

    // ── Navegación ────────────────────────────────────────────────────────────
    @FXML private Button gestionMedicosButton;
    @FXML private Button gestionAgendadoresButton;
    @FXML private Label  tituloSeccionLabel;
    @FXML private Label  subtituloSeccionLabel;
    @FXML private VBox   gestionMedicosSection;
    @FXML private VBox   gestionAgendadoresSection;

    // ── Formulario médico ─────────────────────────────────────────────────────
    @FXML private TextField     nombreCompletoField;
    @FXML private TextField     especialidadField;
    @FXML private TextField     intervaloField;
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;

    // ── Formulario disponibilidad médico ──────────────────────────────────────
    @FXML private ComboBox<MedicoTablaModel> disponibilidadMedicoCombo;
    @FXML private ComboBox<DayOfWeek> diaSemanaCombo;
    @FXML private TextField horaInicioField;
    @FXML private TextField horaFinField;
    @FXML private TextField intervaloDisponibilidadField;
    @FXML private TextField ventanaSemanasField;

    // ── Formulario agendador ──────────────────────────────────────────────────
    @FXML private TextField     agendadorUsernameField;
    @FXML private PasswordField agendadorPasswordField;

    // ── Tabla médicos ─────────────────────────────────────────────────────────
    @FXML private TableView<MedicoTablaModel>              medicosTable;
    @FXML private TableColumn<MedicoTablaModel, Long>      idColumn;
    @FXML private TableColumn<MedicoTablaModel, String>    nombreColumn;
    @FXML private TableColumn<MedicoTablaModel, String>    especialidadColumn;
    @FXML private TableColumn<MedicoTablaModel, Integer>   intervaloColumn;
    @FXML private TableColumn<MedicoTablaModel, String>    usernameColumn;

    // ── Tabla agendadores ─────────────────────────────────────────────────────
    @FXML private TableView<AgendadorTablaModel>            agendadoresTable;
    @FXML private TableColumn<AgendadorTablaModel, Long>    agendadorIdColumn;
    @FXML private TableColumn<AgendadorTablaModel, String>  agendadorUsernameColumn;
    @FXML private TableColumn<AgendadorTablaModel, String>  agendadorStatusColumn;
    @FXML private TableColumn<AgendadorTablaModel, String>  agendadorRoleColumn;

    // ── Dependencias ──────────────────────────────────────────────────────────
    private final SceneManager sceneManager;
    private final AgendaServiceClient agendaServiceClient;
    private final IAgendadorService agendadorService;
    private final UserSession userSession;

    public AdminPanelController(SceneManager sceneManager,
                                AgendaServiceClient agendaServiceClient,
                                IAgendadorService agendadorService,
                                UserSession userSession) {
        this.sceneManager = sceneManager;
        this.agendaServiceClient = agendaServiceClient;
        this.agendadorService = agendadorService;
        this.userSession = userSession;
    }

    // ── Inicialización ────────────────────────────────────────────────────────

    @FXML
    private void initialize() {
        configurarTablaMedicos();
        configurarTablaAgendadores();
        configurarFormularioDisponibilidad();
        cargarMedicos();
        cargarAgendadores();
        mostrarGestionMedicos();
    }

    private void configurarTablaMedicos() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        especialidadColumn.setCellValueFactory(new PropertyValueFactory<>("especialidad"));
        intervaloColumn.setCellValueFactory(new PropertyValueFactory<>("intervaloMinutos"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
    }

    private void configurarTablaAgendadores() {
        agendadorIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        agendadorUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        agendadorStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        agendadorRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    private void configurarFormularioDisponibilidad() {
        if (diaSemanaCombo != null) {
            diaSemanaCombo.setItems(FXCollections.observableArrayList(DayOfWeek.values()));

            diaSemanaCombo.setConverter(new StringConverter<DayOfWeek>() {
                @Override
                public String toString(DayOfWeek dia) {
                    return nombreDiaEnEspanol(dia);
                }

                @Override
                public DayOfWeek fromString(String texto) {
                    return null;
                }
            });

            diaSemanaCombo.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(DayOfWeek item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : nombreDiaEnEspanol(item));
                }
            });

            diaSemanaCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(DayOfWeek item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : nombreDiaEnEspanol(item));
                }
            });
        }

        if (disponibilidadMedicoCombo != null) {
            disponibilidadMedicoCombo.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(MedicoTablaModel item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null
                            ? null
                            : item.getNombreCompleto() + " - " + item.getEspecialidad());
                }
            });

            disponibilidadMedicoCombo.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(MedicoTablaModel item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null
                            ? null
                            : item.getNombreCompleto() + " - " + item.getEspecialidad());
                }
            });
        }
    }

    // ── Navegación entre secciones ────────────────────────────────────────────

    @FXML
    private void mostrarGestionMedicos() {
        gestionMedicosSection.setVisible(true);
        gestionMedicosSection.setManaged(true);
        gestionAgendadoresSection.setVisible(false);
        gestionAgendadoresSection.setManaged(false);

        activarBoton(gestionMedicosButton, gestionAgendadoresButton);

        tituloSeccionLabel.setText("Gestión administrativa");
        subtituloSeccionLabel.setText(
                "Desde este panel el administrador puede registrar, consultar y configurar médicos.");
    }

    @FXML
    private void mostrarGestionAgendadores() {
        gestionMedicosSection.setVisible(false);
        gestionMedicosSection.setManaged(false);
        gestionAgendadoresSection.setVisible(true);
        gestionAgendadoresSection.setManaged(true);

        activarBoton(gestionAgendadoresButton, gestionMedicosButton);

        tituloSeccionLabel.setText("Gestión administrativa");
        subtituloSeccionLabel.setText(
                "Desde este panel el administrador puede registrar y consultar agendadores.");
    }

    private void activarBoton(Button activo, Button inactivo) {
        activo.getStyleClass().removeAll("menu-button", "menu-button-active");
        inactivo.getStyleClass().removeAll("menu-button", "menu-button-active");
        activo.getStyleClass().add("menu-button-active");
        inactivo.getStyleClass().add("menu-button");
    }

    // ── Acciones médico ───────────────────────────────────────────────────────

    @FXML
    private void registrarMedico() {
        try {
            String nombreCompleto = getText(nombreCompletoField);
            String especialidad = getText(especialidadField);
            String intervaloTexto = getText(intervaloField);
            String username = getText(usernameField);
            String password = passwordField.getText() == null
                    ? "" : passwordField.getText().trim();

            if (nombreCompleto.isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar el nombre completo.");
            }

            if (especialidad.isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar la especialidad.");
            }

            if (intervaloTexto.isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar el intervalo de atención.");
            }

            if (username.isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar el nombre de usuario.");
            }

            if (password.isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar la contraseña.");
            }

            int intervalo = Integer.parseInt(intervaloTexto);

            if (intervalo <= 0) {
                throw new IllegalArgumentException("El intervalo debe ser mayor que cero.");
            }

            CrearMedicoRequest request = new CrearMedicoRequest(
                    nombreCompleto,
                    especialidad,
                    intervalo,
                    username,
                    password
            );

            agendaServiceClient.crearMedico(request);

            showAlert(Alert.AlertType.INFORMATION, "Éxito",
                    "Médico registrado correctamente en agenda-service.");

            limpiarFormularioMedico();
            cargarMedicos();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validación",
                    "El intervalo debe ser un número entero.");

        } catch (RestClientResponseException e) {
            showAlert(Alert.AlertType.WARNING, "Respuesta de agenda-service",
                    e.getResponseBodyAsString());

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", e.getMessage());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Ocurrió un error al registrar el médico desde agenda-service.");
            e.printStackTrace();
        }
    }

    @FXML
    private void limpiarFormularioMedico() {
        nombreCompletoField.clear();
        especialidadField.clear();
        intervaloField.clear();
        usernameField.clear();
        passwordField.clear();
    }

    @FXML
    private void guardarDisponibilidadMedico() {
        try {
            MedicoTablaModel medico = disponibilidadMedicoCombo.getValue();
            DayOfWeek dia = diaSemanaCombo.getValue();
            String horaInicioTexto = getText(horaInicioField);
            String horaFinTexto = getText(horaFinField);
            String intervaloTexto = getText(intervaloDisponibilidadField);
            String ventanaTexto = getText(ventanaSemanasField);

            if (medico == null) {
                throw new IllegalArgumentException("Debe seleccionar un médico.");
            }

            if (dia == null) {
                throw new IllegalArgumentException("Debe seleccionar el día de la semana.");
            }

            if (horaInicioTexto.isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar la hora de inicio.");
            }

            if (horaFinTexto.isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar la hora de fin.");
            }

            if (intervaloTexto.isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar el intervalo.");
            }

            if (ventanaTexto.isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar la ventana de semanas.");
            }

            LocalTime horaInicio = LocalTime.parse(horaInicioTexto);
            LocalTime horaFin = LocalTime.parse(horaFinTexto);

            if (!horaFin.isAfter(horaInicio)) {
                throw new IllegalArgumentException("La hora fin debe ser posterior a la hora inicio.");
            }

            int intervalo = Integer.parseInt(intervaloTexto);
            int ventanaSemanas = Integer.parseInt(ventanaTexto);

            if (intervalo <= 0) {
                throw new IllegalArgumentException("El intervalo debe ser mayor que cero.");
            }

            if (ventanaSemanas <= 0) {
                throw new IllegalArgumentException("La ventana de semanas debe ser mayor que cero.");
            }

            CrearDisponibilidadRequest request = new CrearDisponibilidadRequest(
                    medico.getId(),
                    dia,
                    horaInicio,
                    horaFin,
                    intervalo,
                    ventanaSemanas
            );

            agendaServiceClient.crearDisponibilidad(request);

            showAlert(Alert.AlertType.INFORMATION,
                    "Éxito",
                    "Disponibilidad configurada correctamente.");

            limpiarFormularioDisponibilidad();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING,
                    "Validación",
                    "El intervalo y la ventana de semanas deben ser números enteros.");

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", e.getMessage());

        } catch (RestClientResponseException e) {
            showAlert(Alert.AlertType.WARNING,
                    "Respuesta de agenda-service",
                    e.getResponseBodyAsString());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo configurar la disponibilidad del médico.");
            e.printStackTrace();
        }
    }

    private void limpiarFormularioDisponibilidad() {
        if (disponibilidadMedicoCombo != null) {
            disponibilidadMedicoCombo.setValue(null);
        }

        if (diaSemanaCombo != null) {
            diaSemanaCombo.setValue(null);
        }

        if (horaInicioField != null) {
            horaInicioField.clear();
        }

        if (horaFinField != null) {
            horaFinField.clear();
        }

        if (intervaloDisponibilidadField != null) {
            intervaloDisponibilidadField.clear();
        }

        if (ventanaSemanasField != null) {
            ventanaSemanasField.clear();
        }
    }

    // ── Acciones agendador ────────────────────────────────────────────────────

    @FXML
    private void registrarAgendador() {
        try {
            String username = getText(agendadorUsernameField);
            String password = agendadorPasswordField.getText() == null
                    ? "" : agendadorPasswordField.getText().trim();

            if (username.isEmpty()) {
                throw new IllegalArgumentException(
                        "Debe ingresar el nombre de usuario del agendador.");
            }

            if (password.isEmpty()) {
                throw new IllegalArgumentException(
                        "Debe ingresar la contraseña del agendador.");
            }

            agendadorService.registrarAgendador(username, password);

            showAlert(Alert.AlertType.INFORMATION, "Éxito",
                    "Agendador registrado correctamente.");

            limpiarFormularioAgendador();
            cargarAgendadores();

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", e.getMessage());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Ocurrió un error al registrar el agendador.");
            e.printStackTrace();
        }
    }

    @FXML
    private void limpiarFormularioAgendador() {
        agendadorUsernameField.clear();
        agendadorPasswordField.clear();
    }

    // ── Carga de tablas ───────────────────────────────────────────────────────

    private void cargarMedicos() {
        try {
            MedicoResponse[] medicos = agendaServiceClient.listarMedicos();

            if (medicos == null) {
                medicosTable.setItems(FXCollections.observableArrayList());

                if (disponibilidadMedicoCombo != null) {
                    disponibilidadMedicoCombo.setItems(FXCollections.observableArrayList());
                }

                return;
            }

            List<MedicoTablaModel> filas = Arrays.stream(medicos)
                    .map(this::toMedicoTablaModel)
                    .toList();

            medicosTable.setItems(FXCollections.observableArrayList(filas));

            if (disponibilidadMedicoCombo != null) {
                disponibilidadMedicoCombo.setItems(FXCollections.observableArrayList(filas));
            }

        } catch (Exception e) {
            medicosTable.setItems(FXCollections.observableArrayList());

            if (disponibilidadMedicoCombo != null) {
                disponibilidadMedicoCombo.setItems(FXCollections.observableArrayList());
            }

            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar los médicos desde agenda-service.");
            e.printStackTrace();
        }
    }

    private void cargarAgendadores() {
        List<AgendadorTablaModel> filas = agendadorService.listarAgendadores()
                .stream()
                .map(this::toAgendadorTablaModel)
                .toList();

        agendadoresTable.setItems(FXCollections.observableArrayList(filas));
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private MedicoTablaModel toMedicoTablaModel(MedicoResponse medico) {
        String username = medico.getUsername() != null ? medico.getUsername() : "";

        return new MedicoTablaModel(
                medico.getId(),
                medico.getNombreCompleto(),
                medico.getEspecialidad(),
                medico.getIntervaloMinutos(),
                username
        );
    }

    private AgendadorTablaModel toAgendadorTablaModel(User u) {
        return new AgendadorTablaModel(
                u.getId(),
                u.getUsername(),
                u.getStatus().name(),
                u.getRole().name()
        );
    }

    // ── Sesión ────────────────────────────────────────────────────────────────

    @FXML
    private void volver() {
        userSession.clear();
        sceneManager.switchScene(Vista.LOGIN);
    }

    @FXML
    private void logout() {
        userSession.clear();
        sceneManager.switchScene(Vista.LOGIN);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String nombreDiaEnEspanol(DayOfWeek dia) {
        if (dia == null) {
            return "";
        }

        return switch (dia) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> "Domingo";
        };
    }

    private String getText(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}