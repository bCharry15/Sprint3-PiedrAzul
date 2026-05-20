package co.edu.unicauca.piedraazul.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.dto.CitaResponse;
import co.edu.unicauca.piedraazul.model.dto.CrearCitaRequest;
import co.edu.unicauca.piedraazul.model.dto.DisponibilidadResponse;
import co.edu.unicauca.piedraazul.model.dto.MedicoResponse;
import co.edu.unicauca.piedraazul.service.IPacienteService;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.SesionUsuario;
import co.edu.unicauca.piedraazul.util.Vista;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;

import org.springframework.stereotype.Controller;

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

    public PacientePanelController(SceneManager sceneManager,
                                   AgendaServiceClient agendaServiceClient,
                                   IPacienteService pacienteService) {
        this.sceneManager = sceneManager;
        this.agendaServiceClient = agendaServiceClient;
        this.pacienteService = pacienteService;
    }

    @FXML
    public void initialize() {
        configurarCombos();
        cargarMedicos();
        configurarAutocompletadoPorTab();
        cargarDatosPacienteLogueado();
    }

    private void configurarCombos() {
        cmbTipoDocumento.setItems(FXCollections.observableArrayList("CC", "TI", "CE", "PASAPORTE"));
        cmbTipoDocumento.setValue("CC");

        cmbGenero.setItems(FXCollections.observableArrayList("HOMBRE", "MUJER", "OTRO"));

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
                            "Detalle: " + e.getMessage() + "\n\n" +
                            "Verifica que agenda-service esté corriendo en el puerto 8081.");
        }
    }

    private void configurarAutocompletadoPorTab() {
        txtNumeroDocumento.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.TAB) {
                cargarDatosPacienteLogueado();
            }
        });

        txtNumeroDocumento.focusedProperty().addListener((observable, estabaEnfocado, estaEnfocado) -> {
            if (Boolean.TRUE.equals(estabaEnfocado) && Boolean.FALSE.equals(estaEnfocado)) {
                cargarDatosPacienteLogueado();
            }
        });
    }

    private void cargarDatosPacienteLogueado() {
        String username = SesionUsuario.getUsernameActual();

        if (username == null || username.trim().isEmpty()) {
            return;
        }

        Paciente paciente = pacienteService.buscarPorUsername(username);

        if (paciente == null) {
            return;
        }

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
    }

    private void cargarDatosPaciente(String numeroDocumento,
                                     String tipoDocumento,
                                     String nombres,
                                     String apellidos,
                                     String celular,
                                     String genero,
                                     LocalDate fechaNacimiento,
                                     String correo) {

        txtNumeroDocumento.setText(numeroDocumento);
        cmbTipoDocumento.setValue(tipoDocumento);
        txtNombres.setText(nombres);
        txtApellidos.setText(apellidos);
        txtCelular.setText(celular);
        cmbGenero.setValue(genero);
        dpFechaNacimiento.setValue(fechaNacimiento);
        txtCorreo.setText(correo);
    }

    @FXML
    private void consultarDisponibilidad() {
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
                        "No hay franjas disponibles para el médico/terapista seleccionado en esa fecha.");
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
                    "No se pudo consultar la disponibilidad.\n\n" +
                            "Detalle: " + e.getMessage() + "\n\n" +
                            "Verifica que agenda-service esté corriendo en el puerto 8081.");
        }
    }

    @FXML
    private void agendarCita() {
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
            CitaResponse citaCreada = agendaServiceClient.crearCita(request);

            mostrarAlerta(Alert.AlertType.INFORMATION,
                    "Cita agendada",
                    "La cita fue agendada correctamente.\n\n" +
                            "Paciente: " + citaCreada.getPaciente() + "\n" +
                            "Médico/Terapista: " + citaCreada.getMedico() + "\n" +
                            "Fecha: " + citaCreada.getFecha() + "\n" +
                            "Hora: " + citaCreada.getHora());

            limpiarFormularioDespuesDeAgendar();

        } catch (Exception e) {
            e.printStackTrace();

            mostrarAlerta(Alert.AlertType.ERROR,
                    "No se pudo agendar la cita",
                    "La cita no pudo ser creada.\n\n" +
                            "Detalle: " + e.getMessage());
        }
    }

    private boolean formularioValido() {
        if (campoVacio(txtNumeroDocumento) ||
                cmbTipoDocumento.getValue() == null ||
                campoVacio(txtNombres) ||
                campoVacio(txtApellidos) ||
                campoVacio(txtCelular) ||
                cmbGenero.getValue() == null ||
                cmbMedico.getValue() == null ||
                obtenerFechaCitaSeleccionada() == null ||
                cmbHoraDisponible.getValue() == null ||
                cmbHoraDisponible.getValue().trim().isEmpty()) {

            mostrarAlerta(Alert.AlertType.WARNING,
                    "Formulario incompleto",
                    "Debe completar todos los campos obligatorios y seleccionar una franja disponible.");
            return false;
        }

        return true;
    }

    private LocalDate obtenerFechaCitaSeleccionada() {
        if (dpFechaCita.getValue() != null) {
            return dpFechaCita.getValue();
        }

        String textoFecha = dpFechaCita.getEditor().getText();

        if (textoFecha == null || textoFecha.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(textoFecha.trim(), DateTimeFormatter.ofPattern("d/M/yyyy"));
        } catch (Exception e) {
            return null;
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