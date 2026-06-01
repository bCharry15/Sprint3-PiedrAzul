package co.edu.unicauca.piedraazul.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.dto.CitaResponse;
import co.edu.unicauca.piedraazul.model.dto.CrearCitaRequest;
import co.edu.unicauca.piedraazul.model.dto.DisponibilidadResponse;
import co.edu.unicauca.piedraazul.model.dto.MedicoResponse;
import co.edu.unicauca.piedraazul.model.enums.Genero;
import co.edu.unicauca.piedraazul.service.IPacienteService;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.SesionUsuario;
import co.edu.unicauca.piedraazul.util.Vista;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

@Controller
public class PacientePanelController {

    private final SceneManager sceneManager;
    private final AgendaServiceClient agendaServiceClient;
    private final IPacienteService pacienteService;

    @FXML private ComboBox<MedicoResponse> cmbMedico;
    @FXML private TextField txtFechaCitaVisible;
    @FXML private DatePicker dpFechaCita;
    @FXML private ComboBox<String> cmbHoraDisponible;
    @FXML private Label lblEstadoDisponibilidad;

    @FXML private TextField txtNumeroDocumento;
    @FXML private ComboBox<String> cmbTipoDocumento;
    @FXML private TextField txtNombres;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtCelular;
    @FXML private ComboBox<String> cmbGenero;
    @FXML private DatePicker dpFechaNacimiento;
    @FXML private TextField txtCorreo;
    @FXML private TextArea txtObservacion;

    @FXML private Button btnEditarPerfil;
    @FXML private Button btnGuardarPerfil;

    @FXML private TableView<CitaResponse> tablaCitaPendiente;
    @FXML private TableColumn<CitaResponse, Long> pendienteIdColumn;
    @FXML private TableColumn<CitaResponse, String> pendienteMedicoColumn;
    @FXML private TableColumn<CitaResponse, LocalDate> pendienteFechaColumn;
    @FXML private TableColumn<CitaResponse, LocalTime> pendienteHoraColumn;
    @FXML private TableColumn<CitaResponse, String> pendienteEstadoColumn;
    @FXML private TableColumn<CitaResponse, String> pendienteObservacionColumn;

    @FXML private TableView<CitaResponse> tablaHistorialCitas;
    @FXML private TableColumn<CitaResponse, Long> historialIdColumn;
    @FXML private TableColumn<CitaResponse, String> historialMedicoColumn;
    @FXML private TableColumn<CitaResponse, LocalDate> historialFechaColumn;
    @FXML private TableColumn<CitaResponse, LocalTime> historialHoraColumn;
    @FXML private TableColumn<CitaResponse, String> historialEstadoColumn;
    @FXML private TableColumn<CitaResponse, String> historialObservacionColumn;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Set<LocalDate> FESTIVOS_COLOMBIA_2026 = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 12),
            LocalDate.of(2026, 3, 23),
            LocalDate.of(2026, 4, 2),
            LocalDate.of(2026, 4, 3),
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 18),
            LocalDate.of(2026, 6, 8),
            LocalDate.of(2026, 6, 15),
            LocalDate.of(2026, 6, 29),
            LocalDate.of(2026, 7, 20),
            LocalDate.of(2026, 8, 7),
            LocalDate.of(2026, 8, 17),
            LocalDate.of(2026, 10, 12),
            LocalDate.of(2026, 11, 2),
            LocalDate.of(2026, 11, 16),
            LocalDate.of(2026, 12, 8),
            LocalDate.of(2026, 12, 25)
    );

    private boolean modoEdicionPerfil = false;
    private boolean perfilExiste = false;
    private boolean mensajePerfilIncompletoMostrado = false;

    public PacientePanelController(SceneManager sceneManager,
                                   AgendaServiceClient agendaServiceClient,
                                   IPacienteService pacienteService) {
        this.sceneManager = sceneManager;
        this.agendaServiceClient = agendaServiceClient;
        this.pacienteService = pacienteService;
    }

    @FXML
    public void initialize() {
        configurarCalendarios();
        configurarCampoFechaVisible();
        configurarCombos();
        configurarTablasCitas();
        configurarCargaAutomaticaDisponibilidad();

        cargarMedicos();
        cargarDatosPacienteLogueado();

        actualizarEstadoDisponibilidad("Seleccione médico/terapista y fecha para cargar las horas automáticamente.");
        actualizarFechaSeleccionada();
    }

    private void configurarCalendarios() {
        configurarDatePickerBase(dpFechaCita);
        configurarDatePickerBase(dpFechaNacimiento);

        configurarCalendarioCita();
        configurarCalendarioNacimiento();
    }

    private void configurarDatePickerBase(DatePicker datePicker) {
        datePicker.setEditable(false);
        datePicker.setPromptText("");

        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate fecha) {
                return fecha == null ? "" : fecha.format(FORMATO_FECHA);
            }

            @Override
            public LocalDate fromString(String texto) {
                if (texto == null || texto.trim().isEmpty()) {
                    return null;
                }

                try {
                    return LocalDate.parse(texto.trim(), FORMATO_FECHA);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        datePicker.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #2563eb;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-font-size: 14px;"
        );

        if (datePicker.getEditor() != null) {
            datePicker.getEditor().setStyle(
                    "-fx-text-fill: transparent;" +
                    "-fx-background-color: white;"
            );
            datePicker.getEditor().setOnMouseClicked(event -> datePicker.show());
        }

        datePicker.setOnMouseClicked(event -> datePicker.show());
    }

    private void configurarCampoFechaVisible() {
        if (txtFechaCitaVisible == null) {
            return;
        }

        txtFechaCitaVisible.setEditable(false);
        txtFechaCitaVisible.setFocusTraversable(false);
        txtFechaCitaVisible.setPromptText("Seleccione fecha");
        txtFechaCitaVisible.setStyle(
                "-fx-text-fill: #111827;" +
                "-fx-background-color: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-border-color: #2563eb;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 0 12 0 12;"
        );

        txtFechaCitaVisible.setOnMouseClicked(event -> {
            if (dpFechaCita != null) {
                dpFechaCita.show();
            }
        });
    }

    private void configurarCalendarioCita() {
        dpFechaCita.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);

                setStyle("");
                setTooltip(null);

                if (empty || fecha == null) {
                    return;
                }

                if (fecha.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8;");
                    setTooltip(new Tooltip("Fecha pasada"));
                    return;
                }

                if (esFestivo(fecha)) {
                    setDisable(true);
                    setStyle(
                            "-fx-background-color: #fee2e2;" +
                            "-fx-text-fill: #b91c1c;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;"
                    );
                    setTooltip(new Tooltip("Día festivo no disponible"));
                    return;
                }

                if (fecha.equals(dpFechaCita.getValue())) {
                    setStyle(
                            "-fx-background-color: #2563eb;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;"
                    );
                    setTooltip(new Tooltip("Fecha seleccionada"));
                    return;
                }

                if (fecha.equals(LocalDate.now())) {
                    setStyle(
                            "-fx-border-color: #2563eb;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 6;" +
                            "-fx-font-weight: bold;"
                    );
                    setTooltip(new Tooltip("Hoy"));
                }
            }
        });
    }

    private void configurarCalendarioNacimiento() {
        dpFechaNacimiento.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);

                setStyle("");
                setTooltip(null);

                if (empty || fecha == null) {
                    return;
                }

                if (fecha.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8;");
                    setTooltip(new Tooltip("La fecha de nacimiento no puede ser futura"));
                    return;
                }

                if (fecha.equals(dpFechaNacimiento.getValue())) {
                    setStyle(
                            "-fx-background-color: #7c3aed;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;"
                    );
                    setTooltip(new Tooltip("Fecha seleccionada"));
                }
            }
        });
    }

    private void configurarCargaAutomaticaDisponibilidad() {
        cmbMedico.valueProperty().addListener((obs, anterior, actual) -> cargarDisponibilidadAutomatica());

        dpFechaCita.valueProperty().addListener((obs, anterior, actual) -> {
            actualizarFechaSeleccionada();
            cargarDisponibilidadAutomatica();
        });
    }

    private void configurarCombos() {
        cmbTipoDocumento.setItems(FXCollections.observableArrayList("CC", "TI", "CE", "PASAPORTE"));
        cmbTipoDocumento.setValue("CC");

        cmbGenero.setItems(FXCollections.observableArrayList("HOMBRE", "MUJER", "OTRO"));
        cmbGenero.setValue("HOMBRE");

        cmbMedico.setConverter(new StringConverter<MedicoResponse>() {
            @Override
            public String toString(MedicoResponse medico) {
                if (medico == null) {
                    return "";
                }

                return medico.getNombreCompleto() + " - " + medico.getEspecialidad();
            }

            @Override
            public MedicoResponse fromString(String string) {
                return null;
            }
        });

        cmbMedico.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(MedicoResponse medico, boolean empty) {
                super.updateItem(medico, empty);

                if (empty || medico == null) {
                    setText(null);
                } else {
                    setText(medico.getNombreCompleto() + " - " + medico.getEspecialidad());
                }
            }
        });
    }

    private void configurarTablasCitas() {
        pendienteIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pendienteMedicoColumn.setCellValueFactory(new PropertyValueFactory<>("medico"));
        pendienteFechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        pendienteHoraColumn.setCellValueFactory(new PropertyValueFactory<>("hora"));
        pendienteEstadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        pendienteObservacionColumn.setCellValueFactory(new PropertyValueFactory<>("observacion"));

        historialIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        historialMedicoColumn.setCellValueFactory(new PropertyValueFactory<>("medico"));
        historialFechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        historialHoraColumn.setCellValueFactory(new PropertyValueFactory<>("hora"));
        historialEstadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        historialObservacionColumn.setCellValueFactory(new PropertyValueFactory<>("observacion"));
    }

    private void cargarMedicos() {
        try {
            MedicoResponse[] medicos = agendaServiceClient.listarMedicos();

            if (medicos == null || medicos.length == 0) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Sin médicos",
                        "No hay médicos o terapistas disponibles para agendar citas.");
                return;
            }

            cmbMedico.setItems(FXCollections.observableArrayList(medicos));
            cmbMedico.getSelectionModel().selectFirst();

        } catch (Exception e) {
            e.printStackTrace();

            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error de conexión",
                    "No se pudieron cargar los médicos desde el microservicio de agenda.\n\n" +
                            "Detalle: " + e.getMessage());
        }
    }

    private void cargarDatosPacienteLogueado() {
        String username = SesionUsuario.getUsernameActual();

        if (username == null || username.trim().isEmpty()) {
            prepararPerfilNuevo();
            return;
        }

        Paciente paciente = pacienteService.buscarPorUsername(username.trim());

        if (paciente == null) {
            prepararPerfilNuevo();
            return;
        }

        perfilExiste = true;
        mensajePerfilIncompletoMostrado = false;

        cargarDatosPaciente(
                paciente.getNumeroDocumento(),
                paciente.getTipoDocumento(),
                paciente.getNombres(),
                paciente.getApellidos(),
                paciente.getCelular(),
                paciente.getGenero() != null ? paciente.getGenero().name() : "OTRO",
                paciente.getFechaNacimiento(),
                paciente.getCorreo()
        );

        bloquearDatosPersonales();
        consultarMisCitasSilencioso();
    }

    private void prepararPerfilNuevo() {
        perfilExiste = false;

        limpiarDatosPaciente();
        habilitarDatosPersonales();

        tablaCitaPendiente.setItems(FXCollections.observableArrayList());
        tablaHistorialCitas.setItems(FXCollections.observableArrayList());

        if (!mensajePerfilIncompletoMostrado) {
            mensajePerfilIncompletoMostrado = true;
            System.out.println("PACIENTE-PANEL -> Perfil incompleto. El usuario debe completar y guardar sus datos.");
        }
    }

    private void cargarDatosPaciente(String numeroDocumento,
                                     String tipoDocumento,
                                     String nombres,
                                     String apellidos,
                                     String celular,
                                     String genero,
                                     LocalDate fechaNacimiento,
                                     String correo) {

        txtNumeroDocumento.setText(numeroDocumento != null ? numeroDocumento : "");
        cmbTipoDocumento.setValue(tipoDocumento != null && !tipoDocumento.isBlank() ? tipoDocumento : "CC");
        txtNombres.setText(nombres != null ? nombres : "");
        txtApellidos.setText(apellidos != null ? apellidos : "");
        txtCelular.setText(celular != null ? celular : "");
        cmbGenero.setValue(genero != null && !genero.isBlank() ? genero : "OTRO");
        dpFechaNacimiento.setValue(fechaNacimiento);
        txtCorreo.setText(correo != null ? correo : "");
    }

    private void limpiarDatosPaciente() {
        txtNumeroDocumento.clear();
        cmbTipoDocumento.setValue("CC");
        txtNombres.clear();
        txtApellidos.clear();
        txtCelular.clear();
        cmbGenero.setValue("HOMBRE");
        dpFechaNacimiento.setValue(null);
        txtCorreo.clear();
    }

    @FXML
    private void editarPerfil() {
        habilitarDatosPersonales();
        actualizarEstadoDisponibilidad("Guarde su perfil antes de agendar citas.");
    }

    @FXML
    private void guardarPerfil() {
        if (!validarDatosPersonales()) {
            return;
        }

        String username = SesionUsuario.getUsernameActual();

        if (username == null || username.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR,
                    "Sesión no encontrada",
                    "No se pudo identificar el usuario autenticado. Inicie sesión nuevamente.");
            return;
        }

        try {
            Genero genero = Genero.valueOf(cmbGenero.getValue());

            Paciente perfilGuardado = pacienteService.guardarPerfil(
                    username.trim(),
                    txtNumeroDocumento.getText(),
                    cmbTipoDocumento.getValue(),
                    txtNombres.getText(),
                    txtApellidos.getText(),
                    txtCelular.getText(),
                    genero,
                    dpFechaNacimiento.getValue(),
                    txtCorreo.getText()
            );

            perfilExiste = true;
            modoEdicionPerfil = false;
            mensajePerfilIncompletoMostrado = false;

            cargarDatosPaciente(
                    perfilGuardado.getNumeroDocumento(),
                    perfilGuardado.getTipoDocumento(),
                    perfilGuardado.getNombres(),
                    perfilGuardado.getApellidos(),
                    perfilGuardado.getCelular(),
                    perfilGuardado.getGenero() != null ? perfilGuardado.getGenero().name() : "OTRO",
                    perfilGuardado.getFechaNacimiento(),
                    perfilGuardado.getCorreo()
            );

            bloquearDatosPersonales();
            consultarMisCitasSilencioso();
            cargarDisponibilidadAutomatica();

            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Perfil guardado",
                    "Los datos personales fueron guardados correctamente.\n\n" +
                            "Ahora puede seleccionar médico y fecha para cargar disponibilidad automáticamente.");

        } catch (Exception e) {
            e.printStackTrace();

            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se pudo guardar el perfil",
                    "Ocurrió un error guardando los datos del paciente.\n\n" +
                            "Detalle: " + e.getMessage());
        }
    }

    private void bloquearDatosPersonales() {
        modoEdicionPerfil = false;

        txtNumeroDocumento.setEditable(false);
        txtNombres.setEditable(false);
        txtApellidos.setEditable(false);
        txtCelular.setEditable(false);
        txtCorreo.setEditable(false);

        cmbTipoDocumento.setDisable(true);
        cmbGenero.setDisable(true);
        dpFechaNacimiento.setDisable(true);

        if (btnEditarPerfil != null) {
            btnEditarPerfil.setDisable(false);
        }

        if (btnGuardarPerfil != null) {
            btnGuardarPerfil.setDisable(true);
        }
    }

    private void habilitarDatosPersonales() {
        modoEdicionPerfil = true;

        txtNumeroDocumento.setEditable(true);
        txtNombres.setEditable(true);
        txtApellidos.setEditable(true);
        txtCelular.setEditable(true);
        txtCorreo.setEditable(true);

        cmbTipoDocumento.setDisable(false);
        cmbGenero.setDisable(false);
        dpFechaNacimiento.setDisable(false);

        if (btnEditarPerfil != null) {
            btnEditarPerfil.setDisable(true);
        }

        if (btnGuardarPerfil != null) {
            btnGuardarPerfil.setDisable(false);
        }
    }

    private boolean validarDatosPersonales() {
        if (campoVacio(txtNumeroDocumento) ||
                cmbTipoDocumento.getValue() == null ||
                campoVacio(txtNombres) ||
                campoVacio(txtApellidos) ||
                campoVacio(txtCelular) ||
                cmbGenero.getValue() == null) {

            mostrarAlerta(Alert.AlertType.WARNING,
                    "Datos personales incompletos",
                    "Debe completar los datos personales obligatorios antes de guardar el perfil.");
            return false;
        }

        return true;
    }

    @FXML
    private void consultarDisponibilidad() {
        consultarDisponibilidad(true);
    }

    private void cargarDisponibilidadAutomatica() {
        cmbHoraDisponible.getItems().clear();
        cmbHoraDisponible.setValue(null);

        if (!validarPerfilAntesDeAgendar(false)) {
            actualizarEstadoDisponibilidad("Guarde su perfil antes de consultar disponibilidad.");
            return;
        }

        MedicoResponse medicoSeleccionado = cmbMedico.getValue();
        LocalDate fechaSeleccionada = obtenerFechaCitaSeleccionada();

        if (medicoSeleccionado == null || fechaSeleccionada == null) {
            actualizarEstadoDisponibilidad("Seleccione médico/terapista y fecha para cargar disponibilidad.");
            return;
        }

        if (fechaSeleccionada.isBefore(LocalDate.now())) {
            actualizarEstadoDisponibilidad("No se pueden seleccionar fechas pasadas.");
            return;
        }

        if (esFestivo(fechaSeleccionada)) {
            actualizarEstadoDisponibilidad(
                    "La fecha " + fechaSeleccionada.format(FORMATO_FECHA) +
                            " es festivo en Colombia. Seleccione otra fecha.");
            return;
        }

        consultarDisponibilidad(false);
    }

    private void consultarDisponibilidad(boolean mostrarMensajes) {
        if (!validarPerfilAntesDeAgendar(mostrarMensajes)) {
            return;
        }

        MedicoResponse medicoSeleccionado = cmbMedico.getValue();

        if (medicoSeleccionado == null && !cmbMedico.getItems().isEmpty()) {
            cmbMedico.getSelectionModel().selectFirst();
            medicoSeleccionado = cmbMedico.getValue();
        }

        LocalDate fechaSeleccionada = obtenerFechaCitaSeleccionada();

        if (medicoSeleccionado == null || fechaSeleccionada == null) {
            actualizarEstadoDisponibilidad("Seleccione médico/terapista y fecha para cargar disponibilidad.");

            if (mostrarMensajes) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Datos incompletos",
                        "Debe seleccionar un médico/terapista y una fecha para consultar disponibilidad.");
            }
            return;
        }

        if (fechaSeleccionada.isBefore(LocalDate.now())) {
            actualizarEstadoDisponibilidad("No se pueden seleccionar fechas pasadas.");

            if (mostrarMensajes) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Fecha inválida",
                        "No se pueden seleccionar fechas pasadas.");
            }
            return;
        }

        if (esFestivo(fechaSeleccionada)) {
            actualizarEstadoDisponibilidad(
                    "La fecha " + fechaSeleccionada.format(FORMATO_FECHA) +
                            " es festivo en Colombia. Seleccione otra fecha.");

            if (mostrarMensajes) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Fecha no disponible",
                        "No se pueden agendar citas en días festivos.");
            }
            return;
        }

        try {
            DisponibilidadResponse disponibilidad =
                    agendaServiceClient.consultarDisponibilidad(medicoSeleccionado.getId(), fechaSeleccionada);

            cmbHoraDisponible.getItems().clear();
            cmbHoraDisponible.setValue(null);

            if (disponibilidad == null ||
                    disponibilidad.getFranjasDisponibles() == null ||
                    disponibilidad.getFranjasDisponibles().isEmpty()) {

                actualizarEstadoDisponibilidad(
                        "Sin disponibilidad para " + fechaSeleccionada.format(FORMATO_FECHA) + ".");

                if (mostrarMensajes) {
                    mostrarAlerta(Alert.AlertType.INFORMATION,
                            "Sin disponibilidad",
                            "El médico/terapista seleccionado no tiene franjas disponibles para la fecha "
                                    + fechaSeleccionada.format(FORMATO_FECHA) + ".");
                }
                return;
            }

            disponibilidad.getFranjasDisponibles().forEach(franja -> {
                String hora = franja.toString();

                if (hora.length() == 5) {
                    hora = hora + ":00";
                }

                cmbHoraDisponible.getItems().add(hora);
            });

            if (!cmbHoraDisponible.getItems().isEmpty()) {
                cmbHoraDisponible.getSelectionModel().selectFirst();
            }

            actualizarEstadoDisponibilidad(
                    "Disponibilidad cargada automáticamente: "
                            + cmbHoraDisponible.getItems().size()
                            + " franja(s) disponible(s).");

            if (mostrarMensajes) {
                mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Disponibilidad encontrada",
                        "Se cargaron las franjas disponibles para la fecha seleccionada.");
            }

        } catch (Exception e) {
            e.printStackTrace();

            actualizarEstadoDisponibilidad("No se pudo consultar disponibilidad. Revise la fecha seleccionada.");

            if (mostrarMensajes) {
                mostrarAlerta(Alert.AlertType.ERROR,
                        "Error consultando disponibilidad",
                        "No se pudo consultar la disponibilidad.\n\nDetalle: " + e.getMessage());
            }
        }
    }

    @FXML
    private void agendarCita() {
        if (!validarPerfilAntesDeAgendar(true)) {
            return;
        }

        if (!formularioValido()) {
            return;
        }

        MedicoResponse medicoSeleccionado = cmbMedico.getValue();

        if (medicoSeleccionado == null && !cmbMedico.getItems().isEmpty()) {
            cmbMedico.getSelectionModel().selectFirst();
            medicoSeleccionado = cmbMedico.getValue();
        }

        CrearCitaRequest request = new CrearCitaRequest();
        request.setNumeroDocumento(txtNumeroDocumento.getText().trim());
        request.setTipoDocumento(cmbTipoDocumento.getValue());
        request.setNombres(txtNombres.getText().trim());
        request.setApellidos(txtApellidos.getText().trim());
        request.setCelular(txtCelular.getText().trim());
        request.setGenero(cmbGenero.getValue());
        request.setFechaNacimiento(dpFechaNacimiento.getValue());
        request.setCorreo(obtenerTextoOpcional(txtCorreo));
        request.setMedicoId(medicoSeleccionado.getId());
        request.setFecha(obtenerFechaCitaSeleccionada());
        request.setHora(LocalTime.parse(cmbHoraDisponible.getValue()));
        request.setObservacion(obtenerTextoOpcional(txtObservacion));

        try {
            agendaServiceClient.crearCita(request);

            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Cita agendada",
                    "La cita fue agendada correctamente.\n\n" +
                            "Paciente: " + txtNombres.getText().trim() + " " + txtApellidos.getText().trim() + "\n" +
                            "Médico/Terapista: " + medicoSeleccionado.getNombreCompleto() + "\n" +
                            "Fecha: " + request.getFecha().format(FORMATO_FECHA) + "\n" +
                            "Hora: " + request.getHora());

            limpiarFormularioDespuesDeAgendar();
            consultarMisCitasSilencioso();
            cargarDisponibilidadAutomatica();

        } catch (Exception e) {
            e.printStackTrace();

            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se pudo agendar la cita",
                    "La cita no pudo ser creada.\n\nDetalle: " + e.getMessage());
        }
    }

    @FXML
    private void consultarMisCitas() {
        consultarMisCitasConMensaje(true);
    }

    private void consultarMisCitasSilencioso() {
        consultarMisCitasConMensaje(false);
    }

    private void consultarMisCitasConMensaje(boolean mostrarMensaje) {
        if (!perfilExiste || campoVacio(txtNumeroDocumento)) {
            if (mostrarMensaje) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Perfil incompleto",
                        "Debe guardar primero sus datos personales antes de consultar sus citas.");
            }
            return;
        }

        try {
            CitaResponse[] citas = agendaServiceClient.listarCitasPorPaciente(txtNumeroDocumento.getText().trim());

            if (citas == null || citas.length == 0) {
                tablaCitaPendiente.setItems(FXCollections.observableArrayList());
                tablaHistorialCitas.setItems(FXCollections.observableArrayList());

                if (mostrarMensaje) {
                    mostrarAlerta(Alert.AlertType.INFORMATION,
                            "Sin citas",
                            "No se encontraron citas registradas para este paciente.");
                }
                return;
            }

            List<CitaResponse> todasLasCitas = Arrays.asList(citas);

            List<CitaResponse> citasPendientes = todasLasCitas.stream()
                    .filter(cita -> esEstadoActivo(cita.getEstado()))
                    .toList();

            List<CitaResponse> historial = todasLasCitas.stream()
                    .filter(cita -> !esEstadoActivo(cita.getEstado()))
                    .toList();

            tablaCitaPendiente.setItems(FXCollections.observableArrayList(citasPendientes));
            tablaHistorialCitas.setItems(FXCollections.observableArrayList(historial));

            if (mostrarMensaje) {
                mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Citas consultadas",
                        "Se cargó la cita pendiente y el historial del paciente.");
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (mostrarMensaje) {
                mostrarAlerta(Alert.AlertType.ERROR,
                        "Error consultando citas",
                        "No se pudieron consultar las citas del paciente.\n\nDetalle: " + e.getMessage());
            }
        }
    }

    private boolean validarPerfilAntesDeAgendar(boolean mostrarMensaje) {
        if (modoEdicionPerfil) {
            if (mostrarMensaje) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Perfil en edición",
                        "Debe guardar su perfil antes de continuar.");
            }
            return false;
        }

        if (!perfilExiste || campoVacio(txtNumeroDocumento)) {
            if (mostrarMensaje) {
                mostrarAlerta(Alert.AlertType.WARNING,
                        "Perfil incompleto",
                        "Debe completar y guardar sus datos personales antes de agendar o consultar disponibilidad.");
            }
            return false;
        }

        return true;
    }

    private boolean esEstadoActivo(String estado) {
        if (estado == null) {
            return false;
        }

        return "PROGRAMADA".equalsIgnoreCase(estado)
                || "PENDIENTE".equalsIgnoreCase(estado)
                || "CONFIRMADA".equalsIgnoreCase(estado);
    }

    private boolean formularioValido() {
        LocalDate fechaSeleccionada = obtenerFechaCitaSeleccionada();

        if (cmbMedico.getValue() == null ||
                fechaSeleccionada == null ||
                cmbHoraDisponible.getValue() == null ||
                cmbHoraDisponible.getValue().trim().isEmpty()) {

            mostrarAlerta(Alert.AlertType.WARNING,
                    "Formulario incompleto",
                    "Debe seleccionar médico/terapista, fecha y hora disponible.");
            return false;
        }

        if (fechaSeleccionada.isBefore(LocalDate.now())) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Fecha inválida",
                    "No se pueden agendar citas en fechas pasadas.");
            return false;
        }

        if (esFestivo(fechaSeleccionada)) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Fecha no disponible",
                    "No se pueden agendar citas en días festivos.");
            return false;
        }

        return true;
    }

    private LocalDate obtenerFechaCitaSeleccionada() {
        if (dpFechaCita.getValue() != null) {
            return dpFechaCita.getValue();
        }

        if (txtFechaCitaVisible != null &&
                txtFechaCitaVisible.getText() != null &&
                !txtFechaCitaVisible.getText().trim().isEmpty()) {

            try {
                return LocalDate.parse(txtFechaCitaVisible.getText().trim(), FORMATO_FECHA);
            } catch (Exception ignored) {
                return null;
            }
        }

        return null;
    }

    private boolean esFestivo(LocalDate fecha) {
        return fecha != null && FESTIVOS_COLOMBIA_2026.contains(fecha);
    }

    private void actualizarFechaSeleccionada() {
        if (dpFechaCita == null) {
            return;
        }

        LocalDate fecha = dpFechaCita.getValue();

        if (fecha == null) {
            Platform.runLater(() -> {
                if (txtFechaCitaVisible != null) {
                    txtFechaCitaVisible.setText("");
                }

                if (dpFechaCita.getEditor() != null) {
                    dpFechaCita.getEditor().setText("");
                }
            });

            return;
        }

        String fechaFormateada = fecha.format(FORMATO_FECHA);

        Platform.runLater(() -> {
            if (txtFechaCitaVisible != null) {
                txtFechaCitaVisible.setText(fechaFormateada);
            }

            if (dpFechaCita.getEditor() != null) {
                dpFechaCita.getEditor().setText("");
            }
        });
    }

    private boolean campoVacio(TextField campo) {
        return campo == null || campo.getText() == null || campo.getText().trim().isEmpty();
    }

    private String obtenerTextoOpcional(TextField campo) {
        if (campo == null || campo.getText() == null || campo.getText().trim().isEmpty()) {
            return null;
        }

        return campo.getText().trim();
    }

    private String obtenerTextoOpcional(TextArea campo) {
        if (campo == null || campo.getText() == null || campo.getText().trim().isEmpty()) {
            return null;
        }

        return campo.getText().trim();
    }

    private void limpiarFormularioDespuesDeAgendar() {
        cmbHoraDisponible.getItems().clear();
        cmbHoraDisponible.setValue(null);
        txtObservacion.clear();
    }

    private void actualizarEstadoDisponibilidad(String mensaje) {
        if (lblEstadoDisponibilidad != null) {
            lblEstadoDisponibilidad.setText(mensaje);
        }
    }

    @FXML
    private void logout() {
        SesionUsuario.limpiarSesion();
        AgendaServiceClient.limpiarToken();
        sceneManager.switchScene(Vista.LOGIN);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}