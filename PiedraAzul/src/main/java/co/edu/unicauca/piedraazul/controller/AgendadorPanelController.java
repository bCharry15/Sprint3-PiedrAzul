package co.edu.unicauca.piedraazul.controller;

import co.edu.unicauca.piedraazul.model.Cita;
import co.edu.unicauca.piedraazul.model.enums.Genero;
import co.edu.unicauca.piedraazul.model.Medico;
import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.dto.CitaTablaModel;
import co.edu.unicauca.piedraazul.service.ICitaService;
import co.edu.unicauca.piedraazul.service.IMedicoService;
import co.edu.unicauca.piedraazul.service.IPacienteService;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.Vista;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class AgendadorPanelController {

    // ── Navegación ────────────────────────────────────────────────────────────
    @FXML private Button crearCitaButton;
    @FXML private Button consultarCitasButton;
    @FXML private Label  tituloSeccionLabel;
    @FXML private Label  subtituloSeccionLabel;
    @FXML private VBox   crearCitaSection;
    @FXML private VBox   consultarCitasSection;

    // ── Formulario cita ───────────────────────────────────────────────────────
    @FXML private ComboBox<String>  tipoDocumentoCombo;
    @FXML private TextField         numeroDocumentoField;
    @FXML private TextField         celularField;
    @FXML private TextField         nombresField;
    @FXML private TextField         apellidosField;
    @FXML private ComboBox<Genero>  generoCombo;
    @FXML private DatePicker        fechaNacimientoPicker;
    @FXML private TextField         correoField;
    @FXML private ComboBox<Medico>  medicoCombo;
    @FXML private DatePicker        fechaCitaPicker;
    @FXML private ComboBox<String>  horaCombo;
    @FXML private TextArea          observacionArea;

    // ── Consulta citas ────────────────────────────────────────────────────────
    @FXML private ComboBox<Medico>              medicoBusquedaCombo;
    @FXML private DatePicker                    fechaBusquedaPicker;
    @FXML private Label                         cantidadCitasLabel;
    @FXML private TableView<CitaTablaModel>     citasTable;
    @FXML private TableColumn<CitaTablaModel, Long>   idColumn;
    @FXML private TableColumn<CitaTablaModel, String> pacienteColumn;
    @FXML private TableColumn<CitaTablaModel, String> documentoColumn;
    @FXML private TableColumn<CitaTablaModel, String> medicoColumn;
    @FXML private TableColumn<CitaTablaModel, String> fechaColumn;
    @FXML private TableColumn<CitaTablaModel, String> horaColumn;
    @FXML private TableColumn<CitaTablaModel, String> estadoColumn;

    // ── Dependencias (interfaces — principio D) ───────────────────────────────
    private final SceneManager    sceneManager;
    private final IPacienteService pacienteService;
    private final IMedicoService   medicoService;
    private final ICitaService     citaService;

    private static final DateTimeFormatter FORMATO_HORA   = DateTimeFormatter.ofPattern("HH:mm");
    private static final LocalTime HORA_INICIO_MANANA     = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN_MANANA        = LocalTime.of(12, 0);
    private static final LocalTime HORA_INICIO_TARDE      = LocalTime.of(14, 0);
    private static final LocalTime HORA_FIN_TARDE         = LocalTime.of(18, 0);
    private static final Pattern   SOLO_NUMEROS           = Pattern.compile("\\d+");
    private static final Pattern   CORREO_VALIDO          =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public AgendadorPanelController(SceneManager sceneManager,
                                    IPacienteService pacienteService,
                                    IMedicoService medicoService,
                                    ICitaService citaService) {
        this.sceneManager    = sceneManager;
        this.pacienteService = pacienteService;
        this.medicoService   = medicoService;
        this.citaService     = citaService;
    }

    // ── Inicialización ────────────────────────────────────────────────────────

    @FXML
    private void initialize() {
        tipoDocumentoCombo.getItems().addAll(
                "Cédula de ciudadanía", "Tarjeta de identidad",
                "Cédula de extranjería", "Pasaporte");

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

        medicoCombo.valueProperty().addListener((obs, ant, act) -> cargarHorasDisponibles());
        fechaCitaPicker.valueProperty().addListener((obs, ant, act) -> cargarHorasDisponibles());

        numeroDocumentoField.focusedProperty().addListener((obs, antes, ahora) -> {
            if (!ahora) autocompletarPacientePorDocumento();
        });
        numeroDocumentoField.setOnAction(e -> autocompletarPacientePorDocumento());

        configurarDatePickers();
        mostrarCrearCita();
    }

    // ── Navegación ────────────────────────────────────────────────────────────

    @FXML
    private void mostrarCrearCita() {
        crearCitaSection.setVisible(true);
        crearCitaSection.setManaged(true);
        consultarCitasSection.setVisible(false);
        consultarCitasSection.setManaged(false);
        activarBoton(crearCitaButton, consultarCitasButton);
        tituloSeccionLabel.setText("Gestión de citas");
        subtituloSeccionLabel.setText("Aquí el agendador puede registrar una nueva cita.");
    }

    @FXML
    private void mostrarConsultarCitas() {
        crearCitaSection.setVisible(false);
        crearCitaSection.setManaged(false);
        consultarCitasSection.setVisible(true);
        consultarCitasSection.setManaged(true);
        activarBoton(consultarCitasButton, crearCitaButton);
        tituloSeccionLabel.setText("Consulta de citas");
        subtituloSeccionLabel.setText(
                "Aquí el agendador puede consultar las citas de un médico por fecha.");
    }

    private void activarBoton(Button activo, Button inactivo) {
        activo.getStyleClass().removeAll("menu-button", "menu-button-active");
        inactivo.getStyleClass().removeAll("menu-button", "menu-button-active");
        activo.getStyleClass().add("menu-button-active");
        inactivo.getStyleClass().add("menu-button");
    }

    // ── Guardar cita ──────────────────────────────────────────────────────────

    @FXML
    private void guardarCita() {
        try {
            validarCamposObligatorios();

            Paciente paciente = pacienteService.obtenerOCrearPaciente(
                    getText(numeroDocumentoField),
                    tipoDocumentoCombo.getValue(),
                    getText(nombresField),
                    getText(apellidosField),
                    getText(celularField),
                    generoCombo.getValue(),
                    fechaNacimientoPicker.getValue(),
                    getText(correoField));

            citaService.crearCita(
                    paciente,
                    medicoCombo.getValue(),
                    fechaCitaPicker.getValue(),
                    LocalTime.parse(horaCombo.getValue(), FORMATO_HORA),
                    getText(observacionArea));

            showAlert(Alert.AlertType.INFORMATION, "Éxito",
                    "La cita fue registrada correctamente.");
            limpiarFormulario();

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Ocurrió un error al guardar la cita.");
            e.printStackTrace();
        }
    }

    // ── Buscar citas ──────────────────────────────────────────────────────────

    @FXML
    private void buscarCitas() {
        Medico medico    = medicoBusquedaCombo.getValue();
        LocalDate fecha  = fechaBusquedaPicker.getValue();

        if (medico == null || fecha == null) {
            showAlert(Alert.AlertType.WARNING, "Validación",
                    "Debe seleccionar médico y fecha para realizar la búsqueda.");
            cantidadCitasLabel.setText("Cantidad de citas: 0");
            citasTable.setItems(FXCollections.observableArrayList());
            return;
        }

        List<CitaTablaModel> filas = citaService
                .buscarPorMedicoYFecha(medico, fecha)
                .stream()
                .map(c -> new CitaTablaModel(
                        c.getId(),
                        c.getPaciente().getNombreCompleto(),
                        c.getPaciente().getNumeroDocumento(),
                        c.getMedico().getNombreCompleto(),
                        c.getFecha().toString(),
                        c.getHora().format(FORMATO_HORA),
                        c.getEstado().name()))
                .toList();

        citasTable.setItems(FXCollections.observableArrayList(filas));
        cantidadCitasLabel.setText("Cantidad de citas: " + filas.size());

        if (filas.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Sin resultados",
                    "No se encontraron citas para el médico y la fecha seleccionados.");
        }
    }

    // ── Limpiar formulario ────────────────────────────────────────────────────

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

    // ── Sesión ────────────────────────────────────────────────────────────────

    @FXML
    private void logout() {
        sceneManager.switchScene(Vista.LOGIN);
    }

    // ── Autocomplete paciente ─────────────────────────────────────────────────

    @FXML
    private void autocompletarPacientePorDocumento() {
        String numero = getText(numeroDocumentoField);
        if (numero.isEmpty()) return;

        if (!SOLO_NUMEROS.matcher(numero).matches()) {
            showAlert(Alert.AlertType.WARNING, "Validación",
                    "El número de documento solo debe contener números.");
            return;
        }

        Paciente p = pacienteService.buscarPorNumeroDocumento(numero);
        if (p == null) {
            limpiarDatosPacienteManteniendoDocumento();
            return;
        }

        tipoDocumentoCombo.setValue(p.getTipoDocumento());
        nombresField.setText(safe(p.getNombres()));
        apellidosField.setText(safe(p.getApellidos()));
        celularField.setText(safe(p.getCelular()));
        correoField.setText(safe(p.getCorreo()));
        generoCombo.setValue(p.getGenero());
        fechaNacimientoPicker.setValue(p.getFechaNacimiento());
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

    // ── Horas disponibles ─────────────────────────────────────────────────────

    private void cargarHorasDisponibles() {
        horaCombo.getItems().clear();
        horaCombo.setValue(null);

        Medico medico    = medicoCombo.getValue();
        LocalDate fecha  = fechaCitaPicker.getValue();
        if (medico == null || fecha == null) return;

        if (fecha.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Fecha inválida",
                    "No se pueden asignar citas en fechas pasadas.");
            return;
        }

        int intervalo = (medico.getIntervaloMinutos() != null
                && medico.getIntervaloMinutos() > 0)
                ? medico.getIntervaloMinutos() : 30;

        Set<String> ocupadas = citaService
                .buscarPorMedicoYFecha(medico, fecha)
                .stream()
                .map(c -> c.getHora().format(FORMATO_HORA))
                .collect(Collectors.toSet());

        List<String> disponibles = generarHorasPorIntervalo(intervalo)
                .stream()
                .filter(h -> !ocupadas.contains(h))
                .toList();

        horaCombo.setItems(FXCollections.observableArrayList(disponibles));

        if (disponibles.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Sin disponibilidad",
                    "No hay horas disponibles para ese médico en esa fecha.");
        }
    }

    private List<String> generarHorasPorIntervalo(int intervalo) {
        List<String> horas = new ArrayList<>();
        agregarRango(horas, HORA_INICIO_MANANA, HORA_FIN_MANANA, intervalo);
        agregarRango(horas, HORA_INICIO_TARDE,  HORA_FIN_TARDE,  intervalo);
        return horas;
    }

    private void agregarRango(List<String> horas, LocalTime inicio,
                               LocalTime fin, int intervalo) {
        LocalTime actual = inicio;
        while (actual.isBefore(fin)) {
            horas.add(actual.format(FORMATO_HORA));
            actual = actual.plusMinutes(intervalo);
        }
    }

    // ── Validación ────────────────────────────────────────────────────────────

    private void validarCamposObligatorios() {
        if (tipoDocumentoCombo.getValue() == null)
            throw new IllegalArgumentException("Debe seleccionar el tipo de documento.");

        String doc = getText(numeroDocumentoField);
        if (doc.isEmpty())
            throw new IllegalArgumentException("Debe ingresar el número de documento.");
        if (!SOLO_NUMEROS.matcher(doc).matches())
            throw new IllegalArgumentException(
                    "El número de documento solo debe contener números.");
        if (getText(nombresField).isEmpty())
            throw new IllegalArgumentException("Debe ingresar los nombres.");
        if (getText(apellidosField).isEmpty())
            throw new IllegalArgumentException("Debe ingresar los apellidos.");

        String cel = getText(celularField);
        if (cel.isEmpty())
            throw new IllegalArgumentException("Debe ingresar el celular.");
        if (!SOLO_NUMEROS.matcher(cel).matches())
            throw new IllegalArgumentException("El celular solo debe contener números.");
        if (generoCombo.getValue() == null)
            throw new IllegalArgumentException("Debe seleccionar el género.");

        String correo = getText(correoField);
        if (!correo.isEmpty() && !CORREO_VALIDO.matcher(correo).matches())
            throw new IllegalArgumentException(
                    "El correo electrónico no tiene un formato válido.");
        if (medicoCombo.getValue() == null)
            throw new IllegalArgumentException("Debe seleccionar el médico.");
        if (fechaCitaPicker.getValue() == null)
            throw new IllegalArgumentException("Debe seleccionar la fecha de la cita.");
        if (fechaCitaPicker.getValue().isBefore(LocalDate.now()))
            throw new IllegalArgumentException(
                    "No se pueden registrar citas en fechas pasadas.");
        if (horaCombo.getValue() == null || horaCombo.getValue().isBlank())
            throw new IllegalArgumentException("Debe seleccionar la hora de la cita.");
    }

    // ── DatePickers ───────────────────────────────────────────────────────────

    private void configurarDatePickers() {
        fechaCitaPicker.setDayCellFactory(dp -> new DateCell() {
            @Override public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(LocalDate.now()));
            }
        });
        fechaBusquedaPicker.setDayCellFactory(dp -> new DateCell() {
            @Override public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(LocalDate.now()));
            }
        });
        fechaNacimientoPicker.setDayCellFactory(dp -> new DateCell() {
            @Override public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isAfter(LocalDate.now()));
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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
