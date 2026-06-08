package co.edu.unicauca.piedraazul.controller;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.dto.AgendadorTablaModel;
import co.edu.unicauca.piedraazul.model.dto.CrearDisponibilidadRequest;
import co.edu.unicauca.piedraazul.model.dto.CrearMedicoRequest;
import co.edu.unicauca.piedraazul.model.dto.DisponibilidadTablaModel;
import co.edu.unicauca.piedraazul.model.dto.MedicoResponse;
import co.edu.unicauca.piedraazul.model.dto.MedicoTablaModel;
import co.edu.unicauca.piedraazul.service.IAgendadorService;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.UserSession;
import co.edu.unicauca.piedraazul.util.Vista;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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

    @FXML private Button gestionMedicosButton;
    @FXML private Button gestionAgendadoresButton;
    @FXML private Label tituloSeccionLabel;
    @FXML private Label subtituloSeccionLabel;
    @FXML private VBox gestionMedicosSection;
    @FXML private VBox gestionAgendadoresSection;

    @FXML private TextField nombreCompletoField;
    @FXML private TextField especialidadField;
    @FXML private TextField intervaloField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private ComboBox<MedicoTablaModel> disponibilidadMedicoCombo;
    @FXML private ComboBox<DayOfWeek> diaSemanaCombo;
    @FXML private ComboBox<String> horaInicioField;
    @FXML private ComboBox<String> horaFinField;
    @FXML private TextField intervaloDisponibilidadField;
    @FXML private TextField ventanaSemanasField;

    @FXML private TableView<DisponibilidadTablaModel> disponibilidadesTable;
    @FXML private TableColumn<DisponibilidadTablaModel, Long> disponibilidadIdColumn;
    @FXML private TableColumn<DisponibilidadTablaModel, String> disponibilidadDiaColumn;
    @FXML private TableColumn<DisponibilidadTablaModel, String> disponibilidadHoraInicioColumn;
    @FXML private TableColumn<DisponibilidadTablaModel, String> disponibilidadHoraFinColumn;
    @FXML private TableColumn<DisponibilidadTablaModel, Integer> disponibilidadIntervaloColumn;
    @FXML private TableColumn<DisponibilidadTablaModel, Integer> disponibilidadVentanaColumn;
    @FXML private TableColumn<DisponibilidadTablaModel, String> disponibilidadActivoColumn;

    @FXML private TextField agendadorUsernameField;
    @FXML private PasswordField agendadorPasswordField;

    @FXML private TableView<MedicoTablaModel> medicosTable;
    @FXML private TableColumn<MedicoTablaModel, Long> idColumn;
    @FXML private TableColumn<MedicoTablaModel, String> nombreColumn;
    @FXML private TableColumn<MedicoTablaModel, String> especialidadColumn;
    @FXML private TableColumn<MedicoTablaModel, Integer> intervaloColumn;
    @FXML private TableColumn<MedicoTablaModel, String> usernameColumn;

    @FXML private TableView<AgendadorTablaModel> agendadoresTable;
    @FXML private TableColumn<AgendadorTablaModel, Long> agendadorIdColumn;
    @FXML private TableColumn<AgendadorTablaModel, String> agendadorUsernameColumn;
    @FXML private TableColumn<AgendadorTablaModel, String> agendadorStatusColumn;
    @FXML private TableColumn<AgendadorTablaModel, String> agendadorRoleColumn;

    private final SceneManager sceneManager;
    private final AgendaServiceClient agendaServiceClient;
    private final IAgendadorService agendadorService;
    private final UserSession userSession;

    private MedicoTablaModel medicoSeleccionado;
    private DisponibilidadTablaModel disponibilidadSeleccionada;
    private boolean cargandoDisponibilidadDesdeTabla = false;

    public AdminPanelController(SceneManager sceneManager,
                                AgendaServiceClient agendaServiceClient,
                                IAgendadorService agendadorService,
                                UserSession userSession) {
        this.sceneManager = sceneManager;
        this.agendaServiceClient = agendaServiceClient;
        this.agendadorService = agendadorService;
        this.userSession = userSession;
    }

   @FXML
private void initialize() {
    configurarTablaMedicos();
    configurarTablaAgendadores();
    configurarTablaDisponibilidades();
    configurarFormularioDisponibilidad();

    mostrarGestionMedicos();

    cargarMedicos();
    cargarAgendadores();
}

    private void configurarTablaMedicos() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        especialidadColumn.setCellValueFactory(new PropertyValueFactory<>("especialidad"));
        intervaloColumn.setCellValueFactory(new PropertyValueFactory<>("intervaloMinutos"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        medicosTable.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                cargarMedicoEnFormulario(seleccionado);
            }
        });
    }

    private void configurarTablaAgendadores() {
        agendadorIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        agendadorUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        agendadorStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        agendadorRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    private void configurarTablaDisponibilidades() {
        if (disponibilidadesTable == null) {
            return;
        }

        disponibilidadIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        disponibilidadDiaColumn.setCellValueFactory(new PropertyValueFactory<>("diaSemanaTexto"));
        disponibilidadHoraInicioColumn.setCellValueFactory(new PropertyValueFactory<>("horaInicioTexto"));
        disponibilidadHoraFinColumn.setCellValueFactory(new PropertyValueFactory<>("horaFinTexto"));
        disponibilidadIntervaloColumn.setCellValueFactory(new PropertyValueFactory<>("intervaloMinutos"));
        disponibilidadVentanaColumn.setCellValueFactory(new PropertyValueFactory<>("ventanaSemanas"));
        disponibilidadActivoColumn.setCellValueFactory(new PropertyValueFactory<>("activoTexto"));

        disponibilidadesTable.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                cargarDisponibilidadEnFormulario(seleccionado);
            }
        });
    }

    private void configurarFormularioDisponibilidad() {
        if (diaSemanaCombo != null) {
            diaSemanaCombo.setItems(FXCollections.observableArrayList(DayOfWeek.values()));

            diaSemanaCombo.setConverter(new StringConverter<>() {
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

        configurarComboHoras(horaInicioField);
        configurarComboHoras(horaFinField);

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

            disponibilidadMedicoCombo.valueProperty().addListener((obs, anterior, seleccionado) -> {
                if (!cargandoDisponibilidadDesdeTabla) {
                    disponibilidadSeleccionada = null;
                    limpiarCamposDisponibilidad(false);
                    cargarDisponibilidadesMedicoSeleccionado();
                }
            });
        }
    }

    @FXML
    private void mostrarGestionMedicos() {
        gestionMedicosSection.setVisible(true);
        gestionMedicosSection.setManaged(true);
        gestionAgendadoresSection.setVisible(false);
        gestionAgendadoresSection.setManaged(false);

        activarBoton(gestionMedicosButton, gestionAgendadoresButton);

        tituloSeccionLabel.setText("Gestión administrativa");
        subtituloSeccionLabel.setText(
                "Desde este panel el administrador puede registrar, editar, eliminar y configurar médicos.");
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

    @FXML
    private void registrarMedico() {
        try {
            String nombreCompleto = getText(nombreCompletoField);
            String especialidad = getText(especialidadField);
            String intervaloTexto = getText(intervaloField);
            String username = getText(usernameField);
            String password = passwordField.getText() == null
                    ? "" : passwordField.getText().trim();

            validarFormularioMedico(nombreCompleto, especialidad, intervaloTexto);

            if (username.isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar el nombre de usuario.");
            }

            if (password.isEmpty()) {
                throw new IllegalArgumentException("Debe ingresar la contraseña.");
            }

            int intervalo = Integer.parseInt(intervaloTexto);

            CrearMedicoRequest request = new CrearMedicoRequest(
                    nombreCompleto,
                    especialidad,
                    intervalo,
                    username,
                    password
            );

            agendaServiceClient.crearMedico(request);

            showAlert(Alert.AlertType.INFORMATION, "Éxito",
                    "Médico registrado correctamente.");

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
                    "Ocurrió un error al registrar el médico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void actualizarMedico() {
        try {
            if (medicoSeleccionado == null) {
                throw new IllegalArgumentException("Debe seleccionar un médico de la tabla para actualizar.");
            }

            String nombreCompleto = getText(nombreCompletoField);
            String especialidad = getText(especialidadField);
            String intervaloTexto = getText(intervaloField);

            validarFormularioMedico(nombreCompleto, especialidad, intervaloTexto);

            int intervalo = Integer.parseInt(intervaloTexto);

            CrearMedicoRequest request = new CrearMedicoRequest(
                    nombreCompleto,
                    especialidad,
                    intervalo,
                    medicoSeleccionado.getUsername(),
                    ""
            );

            agendaServiceClient.actualizarMedico(medicoSeleccionado.getId(), request);

            showAlert(Alert.AlertType.INFORMATION,
                    "Médico actualizado",
                    "El médico fue actualizado correctamente.");

            limpiarFormularioMedico();
            cargarMedicos();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING,
                    "Validación",
                    "El intervalo debe ser un número entero.");

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", e.getMessage());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "Error actualizando médico",
                    "No se pudo actualizar el médico.\n\nDetalle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminarMedico() {
        try {
            if (medicoSeleccionado == null) {
                throw new IllegalArgumentException("Debe seleccionar un médico de la tabla para eliminar.");
            }

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText(
                    "¿Está seguro de eliminar el médico?\n\n"
                            + medicoSeleccionado.getNombreCompleto()
                            + "\n\nSi tiene citas asociadas, el sistema eliminará también sus registros relacionados."
            );

            Optional<ButtonType> resultado = confirmacion.showAndWait();

            if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                return;
            }

            agendaServiceClient.eliminarMedico(medicoSeleccionado.getId());

            showAlert(Alert.AlertType.INFORMATION,
                    "Médico eliminado",
                    "El médico fue eliminado correctamente.");

            limpiarFormularioMedico();
            limpiarFormularioDisponibilidad();
            cargarMedicos();

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", e.getMessage());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "No se pudo eliminar",
                    "No se pudo eliminar el médico.\n\nDetalle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void limpiarFormularioMedico() {
        medicoSeleccionado = null;

        if (medicosTable != null) {
            medicosTable.getSelectionModel().clearSelection();
        }

        nombreCompletoField.clear();
        especialidadField.clear();
        intervaloField.clear();
        usernameField.clear();
        passwordField.clear();

        usernameField.setDisable(false);
        passwordField.setDisable(false);
    }

    private void cargarMedicoEnFormulario(MedicoTablaModel medico) {
        medicoSeleccionado = medico;

        nombreCompletoField.setText(medico.getNombreCompleto());
        especialidadField.setText(medico.getEspecialidad());
        intervaloField.setText(medico.getIntervaloMinutos() == null
                ? ""
                : String.valueOf(medico.getIntervaloMinutos()));
        usernameField.setText(medico.getUsername());
        passwordField.clear();

        usernameField.setDisable(true);
        passwordField.setDisable(true);
    }

    private void validarFormularioMedico(String nombreCompleto,
                                         String especialidad,
                                         String intervaloTexto) {
        if (nombreCompleto.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el nombre completo.");
        }

        if (especialidad.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar la especialidad.");
        }

        if (intervaloTexto.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el intervalo de atención.");
        }

        int intervalo = Integer.parseInt(intervaloTexto);

        if (intervalo <= 0) {
            throw new IllegalArgumentException("El intervalo debe ser mayor que cero.");
        }
    }

    @FXML
    private void guardarDisponibilidadMedico() {
        try {
            CrearDisponibilidadRequest request = construirRequestDisponibilidad();

            agendaServiceClient.crearDisponibilidad(request);

            showAlert(Alert.AlertType.INFORMATION,
                    "Disponibilidad creada",
                    "La disponibilidad fue configurada correctamente.");

            cargarDisponibilidadesMedicoSeleccionado();
            limpiarCamposDisponibilidad(false);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING,
                    "Validación",
                    "El intervalo y la ventana de semanas deben ser números enteros.");

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", e.getMessage());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "Error",
                    "No se pudo configurar la disponibilidad del médico.\n\nDetalle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void actualizarDisponibilidadMedico() {
        try {
            if (disponibilidadSeleccionada == null) {
                throw new IllegalArgumentException("Debe seleccionar una configuración de la tabla para actualizar.");
            }

            CrearDisponibilidadRequest request = construirRequestDisponibilidad();

            agendaServiceClient.actualizarDisponibilidad(disponibilidadSeleccionada.getId(), request);

            showAlert(Alert.AlertType.INFORMATION,
                    "Disponibilidad actualizada",
                    "La configuración de disponibilidad fue actualizada correctamente.");

            cargarDisponibilidadesMedicoSeleccionado();
            limpiarCamposDisponibilidad(false);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING,
                    "Validación",
                    "El intervalo y la ventana de semanas deben ser números enteros.");

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", e.getMessage());

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                    "Error actualizando disponibilidad",
                    "No se pudo actualizar la disponibilidad.\n\nDetalle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void limpiarFormularioDisponibilidad() {
        disponibilidadSeleccionada = null;

        if (disponibilidadesTable != null) {
            disponibilidadesTable.getSelectionModel().clearSelection();
            disponibilidadesTable.setItems(FXCollections.observableArrayList());
        }

        if (disponibilidadMedicoCombo != null) {
            disponibilidadMedicoCombo.setValue(null);
        }

        limpiarCamposDisponibilidad(true);
    }

    private void limpiarCamposDisponibilidad(boolean limpiarMedico) {
        disponibilidadSeleccionada = null;

        if (disponibilidadesTable != null) {
            disponibilidadesTable.getSelectionModel().clearSelection();
        }

        if (limpiarMedico && disponibilidadMedicoCombo != null) {
            disponibilidadMedicoCombo.setValue(null);
        }

        if (diaSemanaCombo != null) {
            diaSemanaCombo.setValue(null);
        }

        if (horaInicioField != null) {
            horaInicioField.setValue(null);
        }

        if (horaFinField != null) {
            horaFinField.setValue(null);
        }

        if (intervaloDisponibilidadField != null) {
            intervaloDisponibilidadField.clear();
        }

        if (ventanaSemanasField != null) {
            ventanaSemanasField.clear();
        }
    }

    private CrearDisponibilidadRequest construirRequestDisponibilidad() {
        MedicoTablaModel medico = disponibilidadMedicoCombo.getValue();
        DayOfWeek dia = diaSemanaCombo.getValue();

        String horaInicioTexto = horaInicioField == null || horaInicioField.getValue() == null
                ? ""
                : horaInicioField.getValue().trim();

        String horaFinTexto = horaFinField == null || horaFinField.getValue() == null
                ? ""
                : horaFinField.getValue().trim();

        String intervaloTexto = getText(intervaloDisponibilidadField);
        String ventanaTexto = getText(ventanaSemanasField);

        if (medico == null) {
            throw new IllegalArgumentException("Debe seleccionar un médico.");
        }

        if (dia == null) {
            throw new IllegalArgumentException("Debe seleccionar el día de la semana.");
        }

        if (horaInicioTexto.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar la hora de inicio.");
        }

        if (horaFinTexto.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar la hora de fin.");
        }

        if (intervaloTexto.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el intervalo.");
        }

        if (ventanaTexto.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar la ventana de semanas.");
        }

        LocalTime horaInicio = convertirHoraAmPmALocalTime(horaInicioTexto);
        LocalTime horaFin = convertirHoraAmPmALocalTime(horaFinTexto);

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

        return new CrearDisponibilidadRequest(
                medico.getId(),
                dia,
                horaInicio,
                horaFin,
                intervalo,
                ventanaSemanas
        );
    }

    private void cargarDisponibilidadesMedicoSeleccionado() {
        if (disponibilidadesTable == null) {
            return;
        }

        MedicoTablaModel medico = disponibilidadMedicoCombo != null
                ? disponibilidadMedicoCombo.getValue()
                : null;

        if (medico == null) {
            disponibilidadesTable.setItems(FXCollections.observableArrayList());
            return;
        }

        Task<List<DisponibilidadTablaModel>> task = new Task<>() {
            @Override
            protected List<DisponibilidadTablaModel> call() {
                DisponibilidadTablaModel[] disponibilidades =
                        agendaServiceClient.listarDisponibilidadesPorMedico(medico.getId());

                if (disponibilidades == null) {
                    return List.of();
                }

                return Arrays.asList(disponibilidades);
            }
        };

        task.setOnSucceeded(event -> {
            List<DisponibilidadTablaModel> filas = task.getValue();
            disponibilidadesTable.setItems(FXCollections.observableArrayList(filas));
        });

        task.setOnFailed(event -> {
            disponibilidadesTable.setItems(FXCollections.observableArrayList());

            Throwable error = task.getException();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error cargando disponibilidad",
                    "No se pudieron cargar las configuraciones de disponibilidad.\n\nDetalle: "
                            + (error == null ? "Error desconocido." : error.getMessage())
            );
        });

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.setName("admin-cargar-disponibilidades");
        hilo.start();
    }

    private void cargarDisponibilidadEnFormulario(DisponibilidadTablaModel disponibilidad) {
        disponibilidadSeleccionada = disponibilidad;

        cargandoDisponibilidadDesdeTabla = true;

        try {
            if (disponibilidadMedicoCombo != null) {
                MedicoTablaModel medico = buscarMedicoEnCombo(disponibilidad.getMedicoId());

                if (medico != null) {
                    disponibilidadMedicoCombo.setValue(medico);
                }
            }

            if (diaSemanaCombo != null) {
                diaSemanaCombo.setValue(disponibilidad.getDiaSemana());
            }

            if (horaInicioField != null) {
                horaInicioField.setValue(disponibilidad.getHoraInicio() == null
                        ? null
                        : formatearHoraAmPm(disponibilidad.getHoraInicio()));
            }

            if (horaFinField != null) {
                horaFinField.setValue(disponibilidad.getHoraFin() == null
                        ? null
                        : formatearHoraAmPm(disponibilidad.getHoraFin()));
            }

            if (intervaloDisponibilidadField != null) {
                intervaloDisponibilidadField.setText(disponibilidad.getIntervaloMinutos() == null
                        ? ""
                        : String.valueOf(disponibilidad.getIntervaloMinutos()));
            }

            if (ventanaSemanasField != null) {
                ventanaSemanasField.setText(disponibilidad.getVentanaSemanas() == null
                        ? ""
                        : String.valueOf(disponibilidad.getVentanaSemanas()));
            }

        } finally {
            cargandoDisponibilidadDesdeTabla = false;
        }
    }

    private MedicoTablaModel buscarMedicoEnCombo(Long medicoId) {
        if (medicoId == null || disponibilidadMedicoCombo == null) {
            return null;
        }

        return disponibilidadMedicoCombo.getItems()
                .stream()
                .filter(medico -> medicoId.equals(medico.getId()))
                .findFirst()
                .orElse(null);
    }

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

    private void cargarMedicos() {
        Task<List<MedicoTablaModel>> task = new Task<>() {
            @Override
            protected List<MedicoTablaModel> call() {
                MedicoResponse[] medicos = agendaServiceClient.listarMedicos();

                if (medicos == null) {
                    return List.of();
                }

                return Arrays.stream(medicos)
                        .map(AdminPanelController.this::toMedicoTablaModel)
                        .toList();
            }
        };

        task.setOnSucceeded(event -> {
            List<MedicoTablaModel> filas = task.getValue();

            medicosTable.setItems(FXCollections.observableArrayList(filas));

            if (disponibilidadMedicoCombo != null) {
                disponibilidadMedicoCombo.setItems(FXCollections.observableArrayList(filas));
            }
        });

        task.setOnFailed(event -> {
            medicosTable.setItems(FXCollections.observableArrayList());

            if (disponibilidadMedicoCombo != null) {
                disponibilidadMedicoCombo.setItems(FXCollections.observableArrayList());
            }

            Throwable error = task.getException();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error cargando médicos",
                    "No se pudieron cargar los médicos desde agenda-service.\n\nDetalle: "
                            + (error == null ? "Error desconocido." : error.getMessage())
            );
        });

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.setName("admin-cargar-medicos");
        hilo.start();
    }

    private void cargarAgendadores() {
        Task<List<AgendadorTablaModel>> task = new Task<>() {
            @Override
            protected List<AgendadorTablaModel> call() {
                return agendadorService.listarAgendadores()
                        .stream()
                        .map(AdminPanelController.this::toAgendadorTablaModel)
                        .toList();
            }
        };

        task.setOnSucceeded(event -> {
            List<AgendadorTablaModel> filas = task.getValue();
            agendadoresTable.setItems(FXCollections.observableArrayList(filas));
        });

        task.setOnFailed(event -> {
            agendadoresTable.setItems(FXCollections.observableArrayList());

            Throwable error = task.getException();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error cargando agendadores",
                    "No se pudieron cargar los agendadores desde agenda-service.\n\nDetalle: "
                            + (error == null ? "Error desconocido." : error.getMessage())
            );
        });

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.setName("admin-cargar-agendadores");
        hilo.start();
    }

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
                u.getStatus() == null ? "ACTIVE" : u.getStatus().name(),
                u.getRole() == null ? "AGENDADOR" : u.getRole().name()
        );
    }

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

    private void configurarComboHoras(ComboBox<String> combo) {
    if (combo == null) {
        return;
    }

    List<String> horas = new ArrayList<>();

    for (int minutosDelDia = 0; minutosDelDia < 24 * 60; minutosDelDia += 15) {
        LocalTime hora = LocalTime.of(minutosDelDia / 60, minutosDelDia % 60);
        horas.add(formatearHoraAmPm(hora));
    }

    combo.setItems(FXCollections.observableArrayList(horas));
    combo.setVisibleRowCount(10);
}

    private String formatearHoraAmPm(LocalTime hora) {
        if (hora == null) {
            return "";
        }

        int hora24 = hora.getHour();
        int minuto = hora.getMinute();

        String periodo = hora24 < 12 ? "AM" : "PM";
        int hora12 = hora24 % 12;

        if (hora12 == 0) {
            hora12 = 12;
        }

        return String.format("%02d:%02d %s", hora12, minuto, periodo);
    }

    private LocalTime convertirHoraAmPmALocalTime(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar una hora válida.");
        }

        String texto = valor.trim()
                .toUpperCase()
                .replace("A. M.", "AM")
                .replace("P. M.", "PM")
                .replace("A.M.", "AM")
                .replace("P.M.", "PM");

        if (texto.matches("^\\d{2}:\\d{2}$")) {
            return LocalTime.parse(texto);
        }

        if (!texto.matches("^(0?[1-9]|1[0-2]):[0-5][0-9]\\s*(AM|PM)$")) {
            throw new IllegalArgumentException("Formato de hora inválido. Seleccione una hora de la lista.");
        }

        String[] partes = texto.split("\\s+");
        String horaMinuto = partes[0];
        String periodo = partes[1];

        String[] horaPartes = horaMinuto.split(":");

        int hora = Integer.parseInt(horaPartes[0]);
        int minuto = Integer.parseInt(horaPartes[1]);

        if ("AM".equals(periodo)) {
            if (hora == 12) {
                hora = 0;
            }
        } else {
            if (hora != 12) {
                hora += 12;
            }
        }

        return LocalTime.of(hora, minuto);
    }

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