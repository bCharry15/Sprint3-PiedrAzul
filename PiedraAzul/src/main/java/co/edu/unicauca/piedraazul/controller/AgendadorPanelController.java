package co.edu.unicauca.piedraazul.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.Medico;
import co.edu.unicauca.piedraazul.model.Paciente;
import co.edu.unicauca.piedraazul.model.dto.CitaTablaModel;
import co.edu.unicauca.piedraazul.model.dto.DisponibilidadResponse;
import co.edu.unicauca.piedraazul.model.dto.HistorialReagendamientoTablaModel;
import co.edu.unicauca.piedraazul.model.enums.Genero;
import co.edu.unicauca.piedraazul.service.ICitaService;
import co.edu.unicauca.piedraazul.service.IMedicoService;
import co.edu.unicauca.piedraazul.service.IPacienteService;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.Vista;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

@Component
public class AgendadorPanelController {

    @FXML private Button crearCitaButton;
    @FXML private Button consultarCitasButton;
    @FXML private Label tituloSeccionLabel;
    @FXML private Label subtituloSeccionLabel;
    @FXML private Label estadoPacienteLabel;
    @FXML private Label estadoDisponibilidadLabel;
    @FXML private Label resumenCitaLabel;

    @FXML private javafx.scene.layout.VBox crearCitaSection;
    @FXML private javafx.scene.layout.VBox consultarCitasSection;

    @FXML private ComboBox<String> tipoDocumentoCombo;
    @FXML private TextField numeroDocumentoField;
    @FXML private TextField celularField;
    @FXML private TextField nombresField;
    @FXML private TextField apellidosField;
    @FXML private ComboBox<Genero> generoCombo;
    @FXML private DatePicker fechaNacimientoPicker;
    @FXML private TextField fechaNacimientoVisibleField;
    @FXML private TextField correoField;

    @FXML private ComboBox<Medico> medicoCombo;
    @FXML private DatePicker fechaCitaPicker;
    @FXML private TextField fechaCitaVisibleField;
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
    private static final DateTimeFormatter FORMATO_FECHA_VISUAL = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern SOLO_NUMEROS = Pattern.compile("\\d+");
    private static final Pattern CORREO_VALIDO =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

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

    public AgendadorPanelController(SceneManager sceneManager,
                                    IPacienteService pacienteService,
                                    IMedicoService medicoService,
                                    ICitaService citaService,
                                    AgendaServiceClient agendaServiceClient) {
        this.sceneManager = sceneManager;
        this.pacienteService = pacienteService;
        this.medicoService = medicoService;
        this.citaService = citaService;
        this.agendaServiceClient = agendaServiceClient;
    }

    @FXML
    private void initialize() {
        configurarCombos();
        configurarTablaCitas();
        configurarDatePickers();
        configurarFechasVisibles();
        configurarEventosFormulario();

        cargarMedicos();

        fechaBusquedaPicker.setValue(LocalDate.now());
        fechaCitaPicker.setValue(LocalDate.now());

        estadoPacienteLabel.setText("Digite el documento para buscar o registrar un paciente.");
        estadoDisponibilidadLabel.setText("Seleccione médico y fecha para cargar horarios.");
        resumenCitaLabel.setText("Resumen: pendiente por completar.");

        actualizarResumenCita();
        mostrarCrearCita();
    }

    private void configurarCombos() {
        tipoDocumentoCombo.setItems(FXCollections.observableArrayList(
                "CC", "TI", "CE", "Pasaporte"
        ));

        generoCombo.setItems(FXCollections.observableArrayList(Genero.values()));
        horaCombo.setItems(FXCollections.observableArrayList());

        configurarComboMedico(medicoCombo);
        configurarComboMedico(medicoBusquedaCombo);
    }

    private void configurarComboMedico(ComboBox<Medico> combo) {
        if (combo == null) {
            return;
        }

        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Medico medico) {
                return textoMedico(medico);
            }

            @Override
            public Medico fromString(String texto) {
                return null;
            }
        });

        combo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Medico medico, boolean empty) {
                super.updateItem(medico, empty);
                setText(empty || medico == null ? null : textoMedico(medico));
            }
        });

        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Medico medico, boolean empty) {
                super.updateItem(medico, empty);
                setText(empty || medico == null ? null : textoMedico(medico));
            }
        });
    }

    private void configurarTablaCitas() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pacienteColumn.setCellValueFactory(new PropertyValueFactory<>("paciente"));
        documentoColumn.setCellValueFactory(new PropertyValueFactory<>("documento"));
        medicoColumn.setCellValueFactory(new PropertyValueFactory<>("medico"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        horaColumn.setCellValueFactory(new PropertyValueFactory<>("hora"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
    }

    private void configurarDatePickers() {
        fechaCitaPicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                boolean deshabilitar = empty
                        || item.isBefore(LocalDate.now())
                        || FESTIVOS_COLOMBIA_2026.contains(item);

                setDisable(deshabilitar);

                if (!empty && FESTIVOS_COLOMBIA_2026.contains(item)) {
                    setStyle("-fx-background-color: #ffe5e5;");
                    setTooltip(new Tooltip("Día festivo no disponible"));
                }
            }
        });

        fechaBusquedaPicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(LocalDate.now()));
            }
        });

        fechaNacimientoPicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isAfter(LocalDate.now()));
            }
        });
    }

    private void configurarFechasVisibles() {
        configurarFechaVisible(
                fechaNacimientoPicker,
                fechaNacimientoVisibleField,
                "Seleccione fecha de nacimiento",
                false
        );

        configurarFechaVisible(
                fechaCitaPicker,
                fechaCitaVisibleField,
                "Seleccione fecha de la cita",
                true
        );
    }

    private void configurarFechaVisible(DatePicker datePicker,
                                        TextField visibleField,
                                        String prompt,
                                        boolean cargarHorasAlCambiar) {
        if (datePicker == null || visibleField == null) {
            return;
        }

        visibleField.setEditable(false);
        visibleField.setFocusTraversable(false);
        visibleField.setPromptText(prompt);
        visibleField.setOnMouseClicked(event -> datePicker.show());

        datePicker.setPrefWidth(46);
        datePicker.setMaxWidth(46);

        visibleField.textProperty().bind(Bindings.createStringBinding(() -> {
            LocalDate fecha = datePicker.getValue();
            return fecha == null ? "" : fecha.format(FORMATO_FECHA_VISUAL);
        }, datePicker.valueProperty()));

        datePicker.valueProperty().addListener((obs, anterior, nuevaFecha) -> {
            actualizarResumenCita();

            if (cargarHorasAlCambiar) {
                cargarHorasDisponibles();
            }
        });
    }

    private void configurarEventosFormulario() {
        medicoCombo.valueProperty().addListener((obs, anterior, actual) -> {
            cargarHorasDisponibles();
            actualizarResumenCita();
        });

        horaCombo.valueProperty().addListener((obs, anterior, actual) -> actualizarResumenCita());

        numeroDocumentoField.focusedProperty().addListener((obs, antes, ahora) -> {
            if (!ahora) {
                autocompletarPacientePorDocumento();
            }
        });

        numeroDocumentoField.setOnAction(e -> autocompletarPacientePorDocumento());

        nombresField.textProperty().addListener((obs, anterior, actual) -> actualizarResumenCita());
        apellidosField.textProperty().addListener((obs, anterior, actual) -> actualizarResumenCita());
    }

    private void cargarMedicos() {
        try {
            List<Medico> medicos = medicoService.listarTodos();

            medicoCombo.setItems(FXCollections.observableArrayList(medicos));
            medicoBusquedaCombo.setItems(FXCollections.observableArrayList(medicos));

        } catch (Exception e) {
            medicoCombo.setItems(FXCollections.observableArrayList());
            medicoBusquedaCombo.setItems(FXCollections.observableArrayList());

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error cargando médicos",
                    "No se pudieron cargar los médicos disponibles.\n\nDetalle: " + e.getMessage()
            );
        }
    }

    @FXML
    private void mostrarCrearCita() {
        crearCitaSection.setVisible(true);
        crearCitaSection.setManaged(true);

        consultarCitasSection.setVisible(false);
        consultarCitasSection.setManaged(false);

        activarBoton(crearCitaButton, consultarCitasButton);

        tituloSeccionLabel.setText("Crear cita médica");
        subtituloSeccionLabel.setText(
                "Registra los datos del paciente, selecciona el médico y usa horarios reales de disponibilidad."
        );
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
                "Consulta, exporta y revisa el historial de reagendamientos de las citas."
        );
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

            Paciente paciente = pacienteService.obtenerOCrearPaciente(
                    getText(numeroDocumentoField),
                    tipoDocumentoCombo.getValue(),
                    normalizarTexto(getText(nombresField)),
                    normalizarTexto(getText(apellidosField)),
                    getText(celularField),
                    generoCombo.getValue(),
                    obtenerFechaNacimientoSeleccionada(),
                    normalizarCorreo(getText(correoField))
            );

            LocalTime horaSeleccionada = LocalTime.parse(horaCombo.getValue(), FORMATO_HORA);

            citaService.crearCita(
                    paciente,
                    medicoCombo.getValue(),
                    obtenerFechaCitaSeleccionada(),
                    horaSeleccionada,
                    getText(observacionArea)
            );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Cita registrada",
                    "La cita fue registrada correctamente."
            );

            limpiarFormulario();

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", e.getMessage());

        } catch (Exception e) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Error guardando cita",
                    "No se pudo guardar la cita.\n\nDetalle: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    @FXML
    private void buscarCitas() {
        Medico medico = medicoBusquedaCombo.getValue();
        LocalDate fecha = fechaBusquedaPicker.getValue();

        if (medico == null || fecha == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Validación",
                    "Debe seleccionar médico y fecha para realizar la búsqueda."
            );

            cantidadCitasLabel.setText("Cantidad de citas: 0");
            citasTable.setItems(FXCollections.observableArrayList());
            return;
        }

        try {
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
                            c.getEstado().name()
                    ))
                    .toList();

            citasTable.setItems(FXCollections.observableArrayList(filas));
            cantidadCitasLabel.setText("Cantidad de citas: " + filas.size());

            if (filas.isEmpty()) {
                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Sin resultados",
                        "No se encontraron citas para el médico y la fecha seleccionados."
                );
            }

        } catch (Exception e) {
            citasTable.setItems(FXCollections.observableArrayList());
            cantidadCitasLabel.setText("Cantidad de citas: 0");

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error consultando citas",
                    "No se pudieron consultar las citas.\n\nDetalle: " + e.getMessage()
            );
        }
    }

    @FXML
    private void verHistorialReagendamiento() {
        CitaTablaModel citaSeleccionada = citasTable.getSelectionModel().getSelectedItem();

        if (citaSeleccionada == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Seleccione una cita",
                    "Debe seleccionar una cita de la tabla para consultar su historial de reagendamientos."
            );
            return;
        }

        try {
            List<HistorialReagendamientoTablaModel> historial =
                    citaService.consultarHistorialReagendamientos(citaSeleccionada.getId());

            if (historial == null || historial.isEmpty()) {
                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Sin historial",
                        "La cita seleccionada no tiene registros de reagendamiento."
                );
                return;
            }

            mostrarDialogoHistorialReagendamiento(citaSeleccionada, historial);

        } catch (Exception e) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Error consultando historial",
                    "No se pudo consultar el historial de reagendamientos.\n\nDetalle: " + e.getMessage()
            );
        }
    }

    private void mostrarDialogoHistorialReagendamiento(CitaTablaModel cita,
                                                       List<HistorialReagendamientoTablaModel> historial) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Historial de reagendamientos");
        dialog.setHeaderText("Historial de cambios de la cita #" + cita.getId()
                + " - Paciente: " + cita.getPaciente());

        TableView<HistorialReagendamientoTablaModel> tablaHistorial = new TableView<>();
        tablaHistorial.setPrefWidth(940);
        tablaHistorial.setPrefHeight(380);

        TableColumn<HistorialReagendamientoTablaModel, String> fechaCambioColumn =
                new TableColumn<>("Fecha cambio");
        fechaCambioColumn.setCellValueFactory(new PropertyValueFactory<>("fechaCambio"));
        fechaCambioColumn.setPrefWidth(165);

        TableColumn<HistorialReagendamientoTablaModel, String> responsableColumn =
                new TableColumn<>("Responsable");
        responsableColumn.setCellValueFactory(new PropertyValueFactory<>("responsable"));
        responsableColumn.setPrefWidth(135);

        TableColumn<HistorialReagendamientoTablaModel, String> fechaAnteriorColumn =
                new TableColumn<>("Fecha anterior");
        fechaAnteriorColumn.setCellValueFactory(new PropertyValueFactory<>("fechaAnterior"));
        fechaAnteriorColumn.setPrefWidth(120);

        TableColumn<HistorialReagendamientoTablaModel, String> horaAnteriorColumn =
                new TableColumn<>("Hora anterior");
        horaAnteriorColumn.setCellValueFactory(new PropertyValueFactory<>("horaAnterior"));
        horaAnteriorColumn.setPrefWidth(110);

        TableColumn<HistorialReagendamientoTablaModel, String> fechaNuevaColumn =
                new TableColumn<>("Fecha nueva");
        fechaNuevaColumn.setCellValueFactory(new PropertyValueFactory<>("fechaNueva"));
        fechaNuevaColumn.setPrefWidth(120);

        TableColumn<HistorialReagendamientoTablaModel, String> horaNuevaColumn =
                new TableColumn<>("Hora nueva");
        horaNuevaColumn.setCellValueFactory(new PropertyValueFactory<>("horaNueva"));
        horaNuevaColumn.setPrefWidth(110);

        TableColumn<HistorialReagendamientoTablaModel, String> motivoColumn =
                new TableColumn<>("Motivo");
        motivoColumn.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        motivoColumn.setPrefWidth(260);

        tablaHistorial.getColumns().addAll(
                fechaCambioColumn,
                responsableColumn,
                fechaAnteriorColumn,
                horaAnteriorColumn,
                fechaNuevaColumn,
                horaNuevaColumn,
                motivoColumn
        );

        tablaHistorial.setItems(FXCollections.observableArrayList(historial));

        dialog.getDialogPane().setContent(tablaHistorial);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    private void exportarCsvAgendador() {
        Medico medico = medicoBusquedaCombo.getValue();
        LocalDate fecha = fechaBusquedaPicker.getValue();

        if (medico == null || fecha == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Validación",
                    "Debe seleccionar médico y fecha antes de exportar."
            );
            return;
        }

        try {
            String contenidoCsv = agendaServiceClient.exportarCitasPorMedicoYFecha(medico.getId(), fecha);

            if (contenidoCsv == null || contenidoCsv.trim().isEmpty()) {
                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Sin datos",
                        "No hay citas para exportar en la fecha seleccionada."
                );
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar citas en CSV");
            fileChooser.setInitialFileName("citas-agendador-medico-" + medico.getId() + "-" + fecha + ".csv");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivo CSV (*.csv)", "*.csv")
            );

            Window window = citasTable.getScene() != null ? citasTable.getScene().getWindow() : null;
            File archivo = fileChooser.showSaveDialog(window);

            if (archivo == null) {
                return;
            }

            Files.writeString(archivo.toPath(), contenidoCsv, StandardCharsets.UTF_8);

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Exportación exitosa",
                    "El archivo CSV fue guardado correctamente en:\n\n" + archivo.getAbsolutePath()
            );

        } catch (Exception ex) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Error exportando CSV",
                    "No se pudo exportar el archivo CSV.\n\nDetalle: " + ex.getMessage()
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
        fechaCitaPicker.setValue(LocalDate.now());

        horaCombo.getItems().clear();
        horaCombo.setValue(null);
        observacionArea.clear();

        estadoPacienteLabel.setText("Digite el documento para buscar o registrar un paciente.");
        estadoDisponibilidadLabel.setText("Seleccione médico y fecha para cargar horarios.");
        resumenCitaLabel.setText("Resumen: pendiente por completar.");
    }

    @FXML
    private void logout() {
        sceneManager.switchScene(Vista.LOGIN);
    }

    @FXML
    private void autocompletarPacientePorDocumento() {
        String numero = getText(numeroDocumentoField);

        if (numero.isEmpty()) {
            estadoPacienteLabel.setText("Digite el documento para buscar o registrar un paciente.");
            return;
        }

        if (!SOLO_NUMEROS.matcher(numero).matches()) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Validación",
                    "El número de documento solo debe contener números."
            );
            return;
        }

        try {
            Paciente paciente = pacienteService.buscarPorNumeroDocumento(numero);

            if (paciente == null) {
                limpiarDatosPacienteManteniendoDocumento();
                estadoPacienteLabel.setText(
                        "Paciente no registrado. Complete los datos para crear la cita."
                );
                return;
            }

            tipoDocumentoCombo.setValue(paciente.getTipoDocumento());
            nombresField.setText(safe(paciente.getNombres()));
            apellidosField.setText(safe(paciente.getApellidos()));
            celularField.setText(safe(paciente.getCelular()));
            correoField.setText(safe(paciente.getCorreo()));
            generoCombo.setValue(paciente.getGenero());
            fechaNacimientoPicker.setValue(paciente.getFechaNacimiento());

            estadoPacienteLabel.setText(
                    "Paciente encontrado: " + paciente.getNombreCompleto()
                            + " | Documento: " + paciente.getNumeroDocumento()
            );

            actualizarResumenCita();

        } catch (Exception e) {
            estadoPacienteLabel.setText("No se pudo consultar el paciente.");

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error consultando paciente",
                    "No se pudo consultar el paciente.\n\nDetalle: " + e.getMessage()
            );
        }
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

    @FXML
    private void cargarHorasDisponibles() {
        horaCombo.getItems().clear();
        horaCombo.setValue(null);

        Medico medico = medicoCombo.getValue();
        LocalDate fecha = obtenerFechaCitaSeleccionada();

        if (medico == null || fecha == null) {
            estadoDisponibilidadLabel.setText("Seleccione médico y fecha para cargar horarios.");
            actualizarResumenCita();
            return;
        }

        if (fecha.isBefore(LocalDate.now())) {
            estadoDisponibilidadLabel.setText("Fecha inválida: no se permiten fechas pasadas.");
            showAlert(
                    Alert.AlertType.WARNING,
                    "Fecha inválida",
                    "No se pueden asignar citas en fechas pasadas."
            );
            actualizarResumenCita();
            return;
        }

        if (FESTIVOS_COLOMBIA_2026.contains(fecha)) {
            estadoDisponibilidadLabel.setText("Fecha no disponible: es día festivo en Colombia.");
            showAlert(
                    Alert.AlertType.WARNING,
                    "Día festivo",
                    "No se pueden asignar citas en días festivos configurados."
            );
            actualizarResumenCita();
            return;
        }

        try {
            DisponibilidadResponse disponibilidad =
                    agendaServiceClient.consultarDisponibilidad(medico.getId(), fecha);

            if (disponibilidad == null
                    || disponibilidad.getFranjasDisponibles() == null
                    || disponibilidad.getFranjasDisponibles().isEmpty()) {

                estadoDisponibilidadLabel.setText("No hay horarios disponibles para esa fecha.");
                actualizarResumenCita();
                return;
            }

            List<String> horas = disponibilidad.getFranjasDisponibles()
                    .stream()
                    .map(Object::toString)
                    .map(this::normalizarHora)
                    .distinct()
                    .toList();

            horaCombo.setItems(FXCollections.observableArrayList(horas));

            if (!horas.isEmpty()) {
                horaCombo.getSelectionModel().selectFirst();
            }

            estadoDisponibilidadLabel.setText(
                    "Horarios disponibles cargados: " + horas.size()
            );

            actualizarResumenCita();

        } catch (Exception e) {
            estadoDisponibilidadLabel.setText("No se pudieron cargar los horarios disponibles.");

            showAlert(
                    Alert.AlertType.ERROR,
                    "Error consultando disponibilidad",
                    "No se pudieron cargar los horarios disponibles.\n\nDetalle: " + e.getMessage()
            );
        }
    }

    private void validarCamposObligatorios() {
        if (tipoDocumentoCombo.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de documento.");
        }

        String documento = getText(numeroDocumentoField);

        if (documento.isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar el número de documento.");
        }

        if (!SOLO_NUMEROS.matcher(documento).matches()) {
            throw new IllegalArgumentException("El número de documento solo debe contener números.");
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
            throw new IllegalArgumentException("El celular solo debe contener números.");
        }

        if (celular.length() < 7 || celular.length() > 15) {
            throw new IllegalArgumentException("El celular debe tener entre 7 y 15 dígitos.");
        }

        if (generoCombo.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar el género.");
        }

        String correo = getText(correoField);

        if (!correo.isEmpty() && !CORREO_VALIDO.matcher(correo).matches()) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }

        if (medicoCombo.getValue() == null) {
            throw new IllegalArgumentException("Debe seleccionar el médico.");
        }

        LocalDate fechaCita = obtenerFechaCitaSeleccionada();

        if (fechaCita == null) {
            throw new IllegalArgumentException("Debe seleccionar la fecha de la cita.");
        }

        if (fechaCita.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden registrar citas en fechas pasadas.");
        }

        if (FESTIVOS_COLOMBIA_2026.contains(fechaCita)) {
            throw new IllegalArgumentException("No se pueden registrar citas en días festivos.");
        }

        if (horaCombo.getValue() == null || horaCombo.getValue().isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar la hora de la cita.");
        }
    }

    private LocalDate obtenerFechaNacimientoSeleccionada() {
        return fechaNacimientoPicker == null ? null : fechaNacimientoPicker.getValue();
    }

    private LocalDate obtenerFechaCitaSeleccionada() {
        return fechaCitaPicker == null ? null : fechaCitaPicker.getValue();
    }

    private void actualizarResumenCita() {
        String paciente = (getText(nombresField) + " " + getText(apellidosField)).trim();
        Medico medico = medicoCombo.getValue();
        LocalDate fecha = obtenerFechaCitaSeleccionada();
        String hora = horaCombo.getValue();

        if (paciente.isBlank() && medico == null && fecha == null && hora == null) {
            resumenCitaLabel.setText("Resumen: pendiente por completar.");
            return;
        }

        resumenCitaLabel.setText(
                "Resumen: "
                        + (paciente.isBlank() ? "Paciente pendiente" : paciente)
                        + " | "
                        + (medico == null ? "Médico pendiente" : textoMedico(medico))
                        + " | "
                        + (fecha == null ? "Fecha pendiente" : fecha.format(FORMATO_FECHA_VISUAL))
                        + " "
                        + (hora == null ? "" : hora)
        );
    }

    private String textoMedico(Medico medico) {
        if (medico == null) {
            return "";
        }

        String nombre = medico.getNombreCompleto() == null ? "Médico" : medico.getNombreCompleto();
        String especialidad = medico.getEspecialidad() == null ? "" : medico.getEspecialidad();

        return especialidad.isBlank() ? nombre : nombre + " - " + especialidad;
    }

    private String normalizarHora(String valor) {
        if (valor == null) {
            return "";
        }

        String hora = valor.trim();

        if (hora.length() >= 5) {
            return hora.substring(0, 5);
        }

        return hora;
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }

        String texto = valor.trim().replaceAll("\\s+", " ");

        if (texto.isBlank()) {
            return "";
        }

        String[] partes = texto.toLowerCase().split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String parte : partes) {
            if (parte.isBlank()) {
                continue;
            }

            resultado.append(Character.toUpperCase(parte.charAt(0)));

            if (parte.length() > 1) {
                resultado.append(parte.substring(1));
            }

            resultado.append(" ");
        }

        return resultado.toString().trim();
    }

    private String normalizarCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }

        return correo.trim().toLowerCase();
    }

    private String getText(TextField field) {
        return field == null || field.getText() == null ? "" : field.getText().trim();
    }

    private String getText(TextArea area) {
        return area == null || area.getText() == null ? "" : area.getText().trim();
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