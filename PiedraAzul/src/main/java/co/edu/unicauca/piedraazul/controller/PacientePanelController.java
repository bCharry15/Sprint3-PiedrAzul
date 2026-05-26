package co.edu.unicauca.piedraazul.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.dto.CitaResponse;
import co.edu.unicauca.piedraazul.model.dto.CrearCitaRequest;
import co.edu.unicauca.piedraazul.model.dto.DisponibilidadResponse;
import co.edu.unicauca.piedraazul.model.dto.MedicoResponse;
import co.edu.unicauca.piedraazul.model.enums.Genero;
import co.edu.unicauca.piedraazul.service.IPacienteService;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

@Controller
public class PacientePanelController {

    private final SceneManager sceneManager;
    private final AgendaServiceClient agendaServiceClient;
    private final IPacienteService pacienteService;

    @FXML private ComboBox<MedicoResponse> cmbMedico;
    @FXML private DatePicker dpFechaCita;
    @FXML private ComboBox<String> cmbHoraDisponible;

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
        configurarCombos();
        configurarTablasCitas();
        cargarMedicos();
        cargarDatosPacienteLogueado();
    }

    private void configurarCalendarios() {
        DatePickerUtils.configurarDatePicker(dpFechaNacimiento);
        DatePickerUtils.configurarDatePicker(dpFechaCita);
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

            /*
             * Ya no mostramos alerta automática.
             * El paciente simplemente queda con los campos habilitados
             * para completar y guardar su perfil.
             */
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

            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Perfil guardado",
                    "Los datos personales fueron guardados correctamente.\n\n" +
                            "Ahora puede consultar disponibilidad y agendar citas.");

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
        if (!validarPerfilAntesDeAgendar()) {
            return;
        }

        MedicoResponse medicoSeleccionado = cmbMedico.getValue();

        if (medicoSeleccionado == null && !cmbMedico.getItems().isEmpty()) {
            cmbMedico.getSelectionModel().selectFirst();
            medicoSeleccionado = cmbMedico.getValue();
        }

        LocalDate fechaSeleccionada = obtenerFechaCitaSeleccionada();

        if (medicoSeleccionado == null || fechaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Datos incompletos",
                    "Debe seleccionar un médico/terapista y una fecha para consultar disponibilidad.");
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

                mostrarAlerta(Alert.AlertType.INFORMATION,
                        "Sin disponibilidad",
                        "El médico/terapista seleccionado no tiene franjas disponibles para la fecha "
                                + fechaSeleccionada + ".");
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

            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Disponibilidad encontrada",
                    "Se cargaron las franjas disponibles para la fecha seleccionada.");

        } catch (Exception e) {
            e.printStackTrace();

            mostrarAlerta(Alert.AlertType.ERROR,
                    "Error consultando disponibilidad",
                    "No se pudo consultar la disponibilidad.\n\nDetalle: " + e.getMessage());
        }
    }

    @FXML
    private void agendarCita() {
        if (!validarPerfilAntesDeAgendar()) {
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
                            "Fecha: " + request.getFecha() + "\n" +
                            "Hora: " + request.getHora());

            limpiarFormularioDespuesDeAgendar();
            consultarMisCitasSilencioso();

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

    private boolean validarPerfilAntesDeAgendar() {
        if (modoEdicionPerfil) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Perfil en edición",
                    "Debe guardar su perfil antes de continuar.");
            return false;
        }

        if (!perfilExiste || campoVacio(txtNumeroDocumento)) {
            mostrarAlerta(Alert.AlertType.WARNING,
                    "Perfil incompleto",
                    "Debe completar y guardar sus datos personales antes de agendar o consultar disponibilidad.");
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
        if (cmbMedico.getValue() == null ||
                obtenerFechaCitaSeleccionada() == null ||
                cmbHoraDisponible.getValue() == null ||
                cmbHoraDisponible.getValue().trim().isEmpty()) {

            mostrarAlerta(Alert.AlertType.WARNING,
                    "Formulario incompleto",
                    "Debe seleccionar médico/terapista, fecha y hora disponible.");
            return false;
        }

        return true;
    }

    private LocalDate obtenerFechaCitaSeleccionada() {
        if (dpFechaCita.getValue() != null) {
            return dpFechaCita.getValue();
        }

        if (dpFechaCita.getEditor() == null) {
            return null;
        }

        String textoFecha = dpFechaCita.getEditor().getText();

        if (textoFecha == null || textoFecha.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(textoFecha.trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            try {
                return LocalDate.parse(textoFecha.trim(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            } catch (Exception ignored) {
                return null;
            }
        }
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