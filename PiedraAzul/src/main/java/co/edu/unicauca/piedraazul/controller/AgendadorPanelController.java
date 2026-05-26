package co.edu.unicauca.piedraazul.controller;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.Cita;
import co.edu.unicauca.piedraazul.model.Medico;
import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.dto.CitaTablaModel;
import co.edu.unicauca.piedraazul.model.dto.DisponibilidadResponse;
import co.edu.unicauca.piedraazul.model.enums.Genero;
import co.edu.unicauca.piedraazul.service.ICitaService;
import co.edu.unicauca.piedraazul.service.IMedicoService;
import co.edu.unicauca.piedraazul.service.IPacienteService;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.Vista;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class AgendadorPanelController {

    @FXML private Button crearCitaButton;
    @FXML private Button consultarCitasButton;
    @FXML private Label tituloSeccionLabel;
    @FXML private Label subtituloSeccionLabel;
    @FXML private VBox crearCitaSection;
    @FXML private VBox consultarCitasSection;

    @FXML private ComboBox<String> tipoDocumentoCombo;
    @FXML private TextField numeroDocumentoField;
    @FXML private TextField celularField;
    @FXML private TextField nombresField;
    @FXML private TextField apellidosField;
    @FXML private ComboBox<Genero> generoCombo;
    @FXML private DatePicker fechaNacimientoPicker;
    @FXML private TextField correoField;
    @FXML private ComboBox<Medico> medicoCombo;
    @FXML private DatePicker fechaCitaPicker;
    @FXML private ComboBox<String> horaCombo;
    @FXML private TextArea observacionArea;

    @FXML private ComboBox<Medico> medicoBusquedaCombo;
    @FXML private DatePicker fechaBusquedaPicker;
    @FXML private Label cantidadCitasLabel;
    @FXML private TableView<CitaTablaModel> citasTable;
    @FXML private TableColumn<CitaTablaModel, Long> idColumn;
    @FXML private TableColumn<CitaTablaModel, String> pacienteColumn;
    @FXML private TableColumn<CitaTablaModel, String> documentoColumn;
    @FXML private TableColumn<CitaTablaModel, String> medicoColumn;
    @FXML private TableColumn<CitaTablaModel, String> fechaColumn;
    @FXML private TableColumn<CitaTablaModel, String> horaColumn;
    @FXML private TableColumn<CitaTablaModel, String> estadoColumn;

    private final SceneManager sceneManager;
    private final IPacienteService pacienteService;
    private final IMedicoService medicoService;
    private final ICitaService citaService;
    private final AgendaServiceClient agendaServiceClient;

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final Pattern SOLO_NUMEROS = Pattern.compile("\\d+");
    private static final Pattern CORREO_VALIDO =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public AgendadorPanelController(
            SceneManager sceneManager,
            IPacienteService pacienteService,
            IMedicoService medicoService,
            ICitaService citaService,
            AgendaServiceClient agendaServiceClient
    ) {
        this.sceneManager = sceneManager;
        this.pacienteService = pacienteService;
        this.medicoService = medicoService;
        this.citaService = citaService;
        this.agendaServiceClient = agendaServiceClient;
    }

    @FXML
    private void initialize() {
        tipoDocumentoCombo.getItems().addAll(
                "Cedula de ciudadania",
                "Tarjeta de identidad",
                "Cedula de extranjeria",
                "Pasaporte"
        );

        generoCombo.getItems().addAll(Genero.values());

        List<Medico> medicos = medicoService.listarTodos();
        medicoCombo.setItems(FXCollections.observableArrayList(medicos));
        medicoBusquedaCombo.setItems(FXCollections.observableArrayList(medicos));

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pacienteColumn.setCellValueFactory(new PropertyValueFactory<>("paciente"));
        documentoColumn.setCellValueFactory(new PropertyValueFactory<>("documento"));
        medicoColumn.setCellValueFactory(new PropertyValueFactory<>("medico"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        horaColumn.setCellValueFactory(new PropertyValueFactory<>("hora"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));

        horaCombo.setItems(FXCollections.observableArrayList());

        medicoCombo.valueProperty().addListener((obs, anterior, actual) -> cargarHorasDisponibles());
        fechaCitaPicker.valueProperty().addListener((obs, anterior, actual) -> cargarHorasDisponibles());

        numeroDocumentoField.focusedProperty().addListener((obs, antes, ahora) -> {
            if (!ahora) {
                autocompletarPacientePorDocumento();
            }
        });

        numeroDocumentoField.setOnAction(e -> autocompletarPacientePorDocumento());

        configurarDatePickers();
        mostrarCrearCita();
    }

    @FXML
    private void mostrarCrearCita() {
        crearCitaSection.setVisible(true);
        crearCitaSection.setManaged(true);

        consultarCitasSection.setVisible(false);
        consultarCitasSection.setManaged(false);

        activarBoton(crearCitaButton, consultarCitasButton);

        tituloSeccionLabel.setText("Gestion de citas");
        subtituloSeccionLabel.setText("Aqui el agendador puede registrar una nueva cita.");
    }

    @FXML
    private void mostrarConsultarCitas() {
        crearCitaSection.setVisible(false);
        crearCitaSection.setManaged(false);

        consultarCitasSection.setVisible(true);
        consultarCitasSection.setManaged(true);

        activarBoton(consultarCitasButton, crearCitaButton);

        tituloSeccionLabel.setText("Consulta de citas");
        subtituloSeccionLabel.setText("Aqui el agendador puede consultar las citas de un medico por fecha.");
    }

    private void activarBoton(Button activo, Button inactivo) {
        activo.getStyleClass().removeAll("menu-button", "menu-button-active");
        inactivo.getStyleClass().removeAll("menu-button", "menu-button-active");

        activo.getStyleClass().add("menu-button-active");
        inactivo.getStyleClass().add("menu-button");
    }

    @FXML
    private void guardarCita() {
        try {
            validarCamposObligatorios();

            Paciente paciente = construirPacienteDesdeFormulario();

            citaService.crearCita(
                    paciente,
                    medicoCombo.getValue(),
                    fechaCitaPicker.getValue(),
                    LocalTime.parse(horaCombo.getValue(), FORMATO_HORA),
                    getText(observacionArea)
            );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Exito",
                    "La cita fue registrada correctamente."
            );

            limpiarFormulario();

        } catch (IllegalArgumentException e) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Validacion",
                    e.getMessage()
            );

        } catch (Exception e) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Error",
                    e.getMessage() != null && !e.getMessage().isBlank()
                            ? e.getMessage()
                            : "Ocurrio un error al guardar la cita."
            );
            e.printStackTrace();
        }
    }

    private Paciente construirPacienteDesdeFormulario() {
        Paciente paciente = new Paciente();

        paciente.setUsername(generarUsernameTecnicoPaciente(getText(numeroDocumentoField)));
        paciente.setNumeroDocumento(getText(numeroDocumentoField));
        paciente.setTipoDocumento(tipoDocumentoCombo.getValue());
        paciente.setNombres(getText(nombresField));
        paciente.setApellidos(getText(apellidosField));
        paciente.setCelular(getText(celularField));
        paciente.setGenero(generoCombo.getValue());
        paciente.setFechaNacimiento(fechaNacimientoPicker.getValue());
        paciente.setCorreo(getText(correoField));

        return paciente;
    }

    private String generarUsernameTecnicoPaciente(String numeroDocumento) {
        String documentoLimpio = numeroDocumento == null
                ? ""
                : numeroDocumento.trim().toLowerCase().replaceAll("[^a-z0-9]", "");

        if (documentoLimpio.isBlank()) {
            throw new IllegalArgumentException("El numero de documento es obligatorio para crear el paciente.");
        }

        return "paciente_" + documentoLimpio;
    }

    @FXML
    private void buscarCitas() {
        Medico medico = medicoBusquedaCombo.getValue();
        LocalDate fecha = fechaBusquedaPicker.getValue();

        if (medico == null || fecha == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Validacion",
                    "Debe seleccionar medico y fecha para realizar la busqueda."
            );

            cantidadCitasLabel.setText("Cantidad de citas: 0");
            citasTable.setItems(FXCollections.observableArrayList());
            return;
        }

        List<CitaTablaModel> filas = citaService
                .buscarPorMedicoYFecha(medico, fecha)
                .stream()
                .map(cita -> new CitaTablaModel(
                        cita.getId(),
                        cita.getPaciente().getNombreCompleto(),
                        cita.getPaciente().getNumeroDocumento(),
                        cita.getMedico().getNombreCompleto(),
                        cita.getFecha().toString(),
                        cita.getHora().format(FORMATO_HORA),
                        cita.getEstado().name()
                ))
                .toList();

        citasTable.setItems(FXCollections.observableArrayList(filas));
        cantidadCitasLabel.setText("Cantidad de citas: " + filas.size());

        if (filas.isEmpty()) {
            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Sin resultados",
                    "No se encontraron citas para el medico y la fecha seleccionados."
            );
        }
    }

    @FXML
    private void limpiarFormulario() {
        tipoDocumentoCombo.setValue(null);
        numeroDocumentoField.clear();
        celularField.clear();
        nombresField.clear();
        apellidosField.clear();
        generoCombo.setValue(null);
        fechaNacimientoPicker.setValue(null);
        correoField.clear();
        medicoCombo.setValue(null);
        fechaCitaPicker.setValue(null);
        horaCombo.getItems().clear();
        horaCombo.setValue(null);
        observacionArea.clear();
    }

    @FXML
    private void logout() {
        sceneManager.switchScene(Vista.LOGIN);
    }

    @FXML
    private void autocompletarPacientePorDocumento() {
        String numero = getText(numeroDocumentoField);

        if (numero.isEmpty()) {
            return;
        }

        if (!SOLO_NUMEROS.matcher(numero).matches()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Validacion",
                    "El numero de documento solo debe contener numeros."
            );
            return;
        }

        Paciente paciente = pacienteService.buscarPorNumeroDocumento(numero);

        if (paciente == null) {
            limpiarDatosPacienteManteniendoDocumento();
            return;
        }

        tipoDocumentoCombo.setValue(paciente.getTipoDocumento());
        nombresField.setText(safe(paciente.getNombres()));
        apellidosField.setText(safe(paciente.getApellidos()));
        celularField.setText(safe(paciente.getCelular()));
        correoField.setText(safe(paciente.getCorreo()));
        generoCombo.setValue(paciente.getGenero());
        fechaNacimientoPicker.setValue(paciente.getFechaNacimiento());
    }

    private void limpiarDatosPacienteManteniendoDocumento() {
        tipoDocumentoCombo.setValue(null);
        nombresField.clear();
        apellidosField.clear();
        celularField.clear();
        generoCombo.setValue(null);
        fechaNacimientoPicker.setValue(null);
        correoField.clear();
    }

    private void cargarHorasDisponibles() {
        horaCombo.getItems().clear();
        horaCombo.setValue(null);

        Medico medico = medicoCombo.getValue();
        LocalDate fecha = fechaCitaPicker.getValue();

        if (medico == null || fecha == null) {
            return;
        }

        if (fecha.isBefore(LocalDate.now())) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Fecha invalida",
                    "No se pueden asignar citas en fechas pasadas."
            );
            return;
        }

        try {
            DisponibilidadResponse disponibilidad = agendaServiceClient.consultarDisponibilidad(
                    medico.getId(),
                    fecha
            );

            if (disponibilidad == null
                    || disponibilidad.getFranjasDisponibles() == null
                    || disponibilidad.getFranjasDisponibles().isEmpty()) {
                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Sin disponibilidad",
                        "No hay horas disponibles para ese medico en esa fecha."
                );
                return;
            }

            List<String> horasDisponibles = disponibilidad.getFranjasDisponibles()
                    .stream()
                    .map(hora -> hora.format(FORMATO_HORA))
                    .toList();

            horaCombo.setItems(FXCollections.observableArrayList(horasDisponibles));

            if (!horasDisponibles.isEmpty()) {
                horaCombo.setValue(horasDisponibles.get(0));
            }

        } catch (Exception e) {
            horaCombo.getItems().clear();
            horaCombo.setValue(null);

            showAlert(
                    Alert.AlertType.WARNING,
                    "Disponibilidad",
                    e.getMessage() != null && !e.getMessage().isBlank()
                            ? e.getMessage()
                            : "No se pudo consultar la disponibilidad del medico."
            );
        }
    }

    private void validarCamposObligatorios() {
        if (tipoDocumentoCombo.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de documento.");
        }

        String documento = getText(numeroDocumentoField);

        if (documento.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el numero de documento.");
        }

        if (!SOLO_NUMEROS.matcher(documento).matches()) {
            throw new IllegalArgumentException("El numero de documento solo debe contener numeros.");
        }

        if (getText(nombresField).isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar los nombres.");
        }

        if (getText(apellidosField).isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar los apellidos.");
        }

        String celular = getText(celularField);

        if (celular.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el celular.");
        }

        if (!SOLO_NUMEROS.matcher(celular).matches()) {
            throw new IllegalArgumentException("El celular solo debe contener numeros.");
        }

        if (generoCombo.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar el genero.");
        }

        String correo = getText(correoField);

        if (!correo.isEmpty() && !CORREO_VALIDO.matcher(correo).matches()) {
            throw new IllegalArgumentException("El correo electronico no tiene un formato valido.");
        }

        if (medicoCombo.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar el medico.");
        }

        if (fechaCitaPicker.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar la fecha de la cita.");
        }

        if (fechaCitaPicker.getValue().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden registrar citas en fechas pasadas.");
        }

        if (horaCombo.getValue() == null || horaCombo.getValue().isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar la hora de la cita.");
        }
    }

    private void configurarDatePickers() {
        fechaCitaPicker.setDayCellFactory(datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                setDisable(empty || fecha.isBefore(LocalDate.now()));
            }
        });

        fechaBusquedaPicker.setDayCellFactory(datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                setDisable(empty || fecha.isBefore(LocalDate.now()));
            }
        });

        fechaNacimientoPicker.setDayCellFactory(datePicker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                setDisable(empty || fecha.isAfter(LocalDate.now()));
            }
        });
    }

    private String getText(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String getText(TextArea area) {
        return area.getText() == null ? "" : area.getText().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}