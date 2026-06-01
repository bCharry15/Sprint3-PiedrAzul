package co.edu.unicauca.piedraazul.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.Medico;
import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.dto.CitaMedicoTablaModel;
import co.edu.unicauca.piedraazul.model.dto.DisponibilidadResponse;
import co.edu.unicauca.piedraazul.service.ICitaService;
import co.edu.unicauca.piedraazul.service.IMedicoService;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.SesionUsuario;
import co.edu.unicauca.piedraazul.util.UserSession;
import co.edu.unicauca.piedraazul.util.Vista;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;

@Component
public class MedicoPanelController {

    @FXML private Label bienvenidaLabel;
    @FXML private DatePicker fechaBusquedaPicker;
    @FXML private Label cantidadCitasLabel;
    @FXML private TableView<CitaMedicoTablaModel> citasTable;
    @FXML private TableColumn<CitaMedicoTablaModel, Long> idColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> pacienteColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> documentoColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> fechaColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> horaColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> estadoColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> observacionColumn;

    @FXML private Label citaSeleccionadaReagendamientoLabel;
    @FXML private DatePicker nuevaFechaReagendamientoPicker;
    @FXML private ComboBox<String> nuevaHoraReagendamientoCombo;
    @FXML private TextField motivoReagendamientoField;

    private final SceneManager sceneManager;
    private final UserSession userSession;
    private final IMedicoService medicoService;
    private final ICitaService citaService;
    private final AgendaServiceClient agendaServiceClient;

    private Medico medicoActual;

    public MedicoPanelController(SceneManager sceneManager,
                                 UserSession userSession,
                                 IMedicoService medicoService,
                                 ICitaService citaService,
                                 AgendaServiceClient agendaServiceClient) {
        this.sceneManager = sceneManager;
        this.userSession = userSession;
        this.medicoService = medicoService;
        this.citaService = citaService;
        this.agendaServiceClient = agendaServiceClient;
    }

    @FXML
    private void initialize() {
        configurarTablaCitas();
        configurarFormularioReagendamiento();

        fechaBusquedaPicker.setValue(LocalDate.now());

        cargarMedicoActual();

        if (medicoActual != null) {
            buscarMisCitas();
        } else {
            limpiarTabla();
        }
    }

    private void configurarTablaCitas() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pacienteColumn.setCellValueFactory(new PropertyValueFactory<>("paciente"));
        documentoColumn.setCellValueFactory(new PropertyValueFactory<>("documento"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        horaColumn.setCellValueFactory(new PropertyValueFactory<>("hora"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        observacionColumn.setCellValueFactory(new PropertyValueFactory<>("observacion"));

        citasTable.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {
            if (seleccionada != null) {
                cargarCitaParaReagendamiento(seleccionada);
            }
        });
    }

    private void configurarFormularioReagendamiento() {
        if (citaSeleccionadaReagendamientoLabel != null) {
            citaSeleccionadaReagendamientoLabel.setText("Cita seleccionada: ninguna");
        }

        if (nuevaFechaReagendamientoPicker != null) {
            nuevaFechaReagendamientoPicker.setValue(LocalDate.now());

            nuevaFechaReagendamientoPicker.valueProperty().addListener((obs, anterior, nuevaFecha) -> {
                if (nuevaFecha != null && medicoActual != null) {
                    cargarHorariosReagendamiento(false);
                }
            });
        }

        if (nuevaHoraReagendamientoCombo != null) {
            nuevaHoraReagendamientoCombo.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    private void buscarMisCitas() {
        if (medicoActual == null) {
            limpiarTabla();
            showAlert(
                    Alert.AlertType.WARNING,
                    "Perfil médico no encontrado",
                    "El usuario inició sesión correctamente, pero no se encontró un perfil médico asociado. "
                            + "Verifica que el médico haya sido creado desde administración y que tenga asociado el mismo username."
            );
            return;
        }

        LocalDate fecha = fechaBusquedaPicker.getValue();

        if (fecha == null) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Debe seleccionar una fecha.");
            return;
        }

        try {
            List<CitaMedicoTablaModel> filas = citaService
                    .buscarPorMedicoYFecha(medicoActual, fecha)
                    .stream()
                    .map(c -> new CitaMedicoTablaModel(
                            c.getId(),
                            c.getPaciente().getNombreCompleto(),
                            c.getPaciente().getNumeroDocumento(),
                            c.getFecha().toString(),
                            c.getHora().toString(),
                            c.getEstado().name(),
                            c.getObservacion() == null ? "" : c.getObservacion()
                    ))
                    .toList();

            citasTable.setItems(FXCollections.observableArrayList(filas));
            cantidadCitasLabel.setText("Cantidad de citas: " + filas.size());

        } catch (Exception ex) {
            limpiarTabla();
            showAlert(
                    Alert.AlertType.ERROR,
                    "Error consultando citas",
                    "No se pudieron consultar las citas del médico.\n\nDetalle: " + ex.getMessage()
            );
        }
    }

    @FXML
    private void exportarCsv() {
        if (medicoActual == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Perfil médico no encontrado",
                    "No se puede exportar porque no se encontró el perfil médico asociado."
            );
            return;
        }

        LocalDate fecha = fechaBusquedaPicker.getValue();

        if (fecha == null) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Debe seleccionar una fecha para exportar.");
            return;
        }

        try {
            String contenidoCsv = agendaServiceClient.exportarCitasPorMedicoYFecha(
                    medicoActual.getId(),
                    fecha
            );

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
            fileChooser.setInitialFileName("citas-medico-" + medicoActual.getId() + "-" + fecha + ".csv");
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
    private void marcarCitaAtendida() {
        cambiarEstadoCitaSeleccionada(
                "ATENDIDA",
                "Paciente atendido correctamente"
        );
    }

    @FXML
    private void marcarCitaNoAsistida() {
        cambiarEstadoCitaSeleccionada(
                "NO_VINO",
                "El paciente no asistió a la cita"
        );
    }

    @FXML
    private void cancelarCita() {
        cambiarEstadoCitaSeleccionada(
                "CANCELADA",
                "Cita cancelada por el médico"
        );
    }

    private void cambiarEstadoCitaSeleccionada(String nuevoEstado, String observacion) {
        CitaMedicoTablaModel citaSeleccionada = citasTable.getSelectionModel().getSelectedItem();

        if (citaSeleccionada == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Seleccione una cita",
                    "Debe seleccionar una cita de la tabla antes de cambiar su estado."
            );
            return;
        }

        if (esEstadoFinal(citaSeleccionada.getEstado())) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Cita finalizada",
                    "Esta cita ya se encuentra en estado final: " + citaSeleccionada.getEstado()
            );
            return;
        }

        try {
            citaService.cambiarEstadoCita(
                    citaSeleccionada.getId(),
                    nuevoEstado,
                    observacion
            );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Estado actualizado",
                    "La cita " + citaSeleccionada.getId() + " fue actualizada a " + nuevoEstado + "."
            );

            buscarMisCitas();
            limpiarFormularioReagendamiento();

        } catch (Exception ex) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Error cambiando estado",
                    "No se pudo cambiar el estado de la cita.\n\nDetalle: " + ex.getMessage()
            );
        }
    }

    private void cargarCitaParaReagendamiento(CitaMedicoTablaModel cita) {
        if (citaSeleccionadaReagendamientoLabel != null) {
            citaSeleccionadaReagendamientoLabel.setText(
                    "Cita seleccionada: #" + cita.getId()
                            + " | Paciente: " + cita.getPaciente()
                            + " | Actual: " + cita.getFecha() + " " + cita.getHora()
                            + " | Estado: " + cita.getEstado()
            );
        }

        if (esEstadoFinal(cita.getEstado())) {
            limpiarCamposReagendamiento(false);
            showAlert(
                    Alert.AlertType.WARNING,
                    "Cita finalizada",
                    "La cita seleccionada está en estado final y no puede ser re-agendada."
            );
            return;
        }

        try {
            LocalDate fechaActualCita = LocalDate.parse(cita.getFecha());

            if (nuevaFechaReagendamientoPicker != null) {
                nuevaFechaReagendamientoPicker.setValue(
                        fechaActualCita.isBefore(LocalDate.now())
                                ? LocalDate.now()
                                : fechaActualCita
                );
            }

            cargarHorariosReagendamiento(false);

        } catch (Exception e) {
            if (nuevaFechaReagendamientoPicker != null) {
                nuevaFechaReagendamientoPicker.setValue(LocalDate.now());
            }
        }
    }

    @FXML
    private void cargarHorariosReagendamiento() {
        cargarHorariosReagendamiento(true);
    }

    private void cargarHorariosReagendamiento(boolean mostrarMensaje) {
        if (medicoActual == null) {
            if (mostrarMensaje) {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Perfil médico no encontrado",
                        "No se puede consultar disponibilidad porque no se encontró el médico actual."
                );
            }
            return;
        }

        if (nuevaFechaReagendamientoPicker == null || nuevaHoraReagendamientoCombo == null) {
            return;
        }

        LocalDate fechaNueva = nuevaFechaReagendamientoPicker.getValue();

        nuevaHoraReagendamientoCombo.getItems().clear();
        nuevaHoraReagendamientoCombo.setValue(null);

        if (fechaNueva == null) {
            if (mostrarMensaje) {
                showAlert(Alert.AlertType.WARNING, "Validación", "Debe seleccionar la nueva fecha.");
            }
            return;
        }

        if (fechaNueva.isBefore(LocalDate.now())) {
            if (mostrarMensaje) {
                showAlert(Alert.AlertType.WARNING, "Validación", "No puede re-agendar una cita para una fecha pasada.");
            }
            return;
        }

        try {
            DisponibilidadResponse disponibilidad =
                    agendaServiceClient.consultarDisponibilidad(medicoActual.getId(), fechaNueva);

            if (disponibilidad == null
                    || disponibilidad.getFranjasDisponibles() == null
                    || disponibilidad.getFranjasDisponibles().isEmpty()) {

                if (mostrarMensaje) {
                    showAlert(
                            Alert.AlertType.INFORMATION,
                            "Sin disponibilidad",
                            "No hay horarios disponibles para la fecha seleccionada."
                    );
                }

                return;
            }

            disponibilidad.getFranjasDisponibles().forEach(franja -> {
                String hora = franja.toString();

                if (hora.length() >= 5) {
                    hora = hora.substring(0, 5);
                }

                nuevaHoraReagendamientoCombo.getItems().add(hora);
            });

            if (!nuevaHoraReagendamientoCombo.getItems().isEmpty()) {
                nuevaHoraReagendamientoCombo.getSelectionModel().selectFirst();
            }

            if (mostrarMensaje) {
                showAlert(
                        Alert.AlertType.INFORMATION,
                        "Disponibilidad cargada",
                        "Se cargaron los horarios disponibles para re-agendar."
                );
            }

        } catch (Exception ex) {
            if (mostrarMensaje) {
                showAlert(
                        Alert.AlertType.ERROR,
                        "Error consultando disponibilidad",
                        "No se pudo consultar disponibilidad.\n\nDetalle: " + ex.getMessage()
                );
            }
        }
    }

    @FXML
    private void reagendarCitaSeleccionada() {
        CitaMedicoTablaModel citaSeleccionada = citasTable.getSelectionModel().getSelectedItem();

        if (citaSeleccionada == null) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Seleccione una cita",
                    "Debe seleccionar una cita de la tabla para re-agendar."
            );
            return;
        }

        if (esEstadoFinal(citaSeleccionada.getEstado())) {
            showAlert(
                    Alert.AlertType.WARNING,
                    "Cita finalizada",
                    "Esta cita ya se encuentra en estado final y no puede ser re-agendada."
            );
            return;
        }

        LocalDate fechaNueva = nuevaFechaReagendamientoPicker.getValue();
        String horaTexto = nuevaHoraReagendamientoCombo.getValue();
        String motivo = motivoReagendamientoField == null
                ? ""
                : motivoReagendamientoField.getText() == null
                        ? ""
                        : motivoReagendamientoField.getText().trim();

        if (fechaNueva == null) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Debe seleccionar la nueva fecha.");
            return;
        }

        if (fechaNueva.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Validación", "No puede re-agendar una cita para una fecha pasada.");
            return;
        }

        if (horaTexto == null || horaTexto.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Debe seleccionar una nueva hora disponible.");
            return;
        }

        if (motivo.isBlank()) {
            motivo = "Re-agendamiento realizado por el médico/terapista.";
        }

        LocalTime horaNueva;

        try {
            horaNueva = LocalTime.parse(horaTexto);
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Validación", "La hora seleccionada no tiene un formato válido.");
            return;
        }

        try {
            LocalDate fechaAnterior = LocalDate.parse(citaSeleccionada.getFecha());
            LocalTime horaAnterior = LocalTime.parse(citaSeleccionada.getHora());

            if (fechaNueva.equals(fechaAnterior) && horaNueva.equals(horaAnterior)) {
                showAlert(
                        Alert.AlertType.WARNING,
                        "Validación",
                        "La nueva fecha y hora son iguales a la cita actual."
                );
                return;
            }
        } catch (Exception ignored) {
            // Si no se puede parsear la fecha/hora anterior, se deja que el backend valide.
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar re-agendamiento");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(
                "¿Desea re-agendar la cita #" + citaSeleccionada.getId() + "?\n\n"
                        + "Paciente: " + citaSeleccionada.getPaciente() + "\n"
                        + "Fecha actual: " + citaSeleccionada.getFecha() + " " + citaSeleccionada.getHora() + "\n"
                        + "Nueva fecha: " + fechaNueva + " " + horaNueva
        );

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        try {
            String responsable = construirResponsableReagendamiento();

            agendaServiceClient.reagendarCita(
                    citaSeleccionada.getId(),
                    fechaNueva,
                    horaNueva,
                    responsable,
                    motivo
            );

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Cita re-agendada",
                    "La cita fue re-agendada correctamente."
            );

            fechaBusquedaPicker.setValue(fechaNueva);
            buscarMisCitas();
            limpiarFormularioReagendamiento();

        } catch (Exception ex) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Error re-agendando cita",
                    "No se pudo re-agendar la cita.\n\nDetalle: " + ex.getMessage()
            );
        }
    }

    @FXML
    private void limpiarFormularioReagendamiento() {
        if (citasTable != null) {
            citasTable.getSelectionModel().clearSelection();
        }

        if (citaSeleccionadaReagendamientoLabel != null) {
            citaSeleccionadaReagendamientoLabel.setText("Cita seleccionada: ninguna");
        }

        limpiarCamposReagendamiento(true);
    }

    private void limpiarCamposReagendamiento(boolean limpiarFecha) {
        if (limpiarFecha && nuevaFechaReagendamientoPicker != null) {
            nuevaFechaReagendamientoPicker.setValue(LocalDate.now());
        }

        if (nuevaHoraReagendamientoCombo != null) {
            nuevaHoraReagendamientoCombo.getItems().clear();
            nuevaHoraReagendamientoCombo.setValue(null);
        }

        if (motivoReagendamientoField != null) {
            motivoReagendamientoField.clear();
        }
    }

    private String construirResponsableReagendamiento() {
        if (medicoActual != null
                && medicoActual.getNombreCompleto() != null
                && !medicoActual.getNombreCompleto().isBlank()) {
            return "Médico/Terapista: " + medicoActual.getNombreCompleto();
        }

        User currentUser = SesionUsuario.getUsuarioActual();

        if (currentUser != null && currentUser.getUsername() != null) {
            return "Médico/Terapista: " + currentUser.getUsername();
        }

        return "Médico/Terapista";
    }

    private boolean esEstadoFinal(String estado) {
        if (estado == null) {
            return false;
        }

        return "ATENDIDA".equalsIgnoreCase(estado)
                || "COMPLETADA".equalsIgnoreCase(estado)
                || "CANCELADA".equalsIgnoreCase(estado)
                || "NO_VINO".equalsIgnoreCase(estado);
    }

    @FXML
    private void handleLogout() {
        userSession.clear();
        SesionUsuario.limpiarSesion();
        sceneManager.switchScene(Vista.LOGIN);
    }

    @FXML
    private void logout() {
        handleLogout();
    }

    private void cargarMedicoActual() {
        User currentUser = SesionUsuario.getUsuarioActual();

        if (currentUser == null) {
            bienvenidaLabel.setText("Bienvenido, médico");
            showAlert(Alert.AlertType.ERROR, "Sesión", "No hay un usuario autenticado en sesión.");
            return;
        }

        String usernameSesion = currentUser.getUsername();

        medicoActual = buscarMedicoPorUsernameFlexible(usernameSesion).orElse(null);

        if (medicoActual == null && "DR.SAMUEL".equalsIgnoreCase(usernameSesion)) {
            medicoActual = crearMedicoSamuelFallback();
            bienvenidaLabel.setText("Bienvenido, Doctor Samuel Prueba");
            return;
        }

        if (medicoActual == null && "medico".equalsIgnoreCase(usernameSesion)) {
            medicoActual = crearMedicoSamuelFallback();
            bienvenidaLabel.setText("Bienvenido, Doctor Samuel Prueba");
            return;
        }

        if (medicoActual == null) {
            bienvenidaLabel.setText("Bienvenido, médico");
            return;
        }

        bienvenidaLabel.setText("Bienvenido, " + medicoActual.getNombreCompleto());
    }

    private Medico crearMedicoSamuelFallback() {
        Medico medico = new Medico();
        medico.setId(5L);
        return medico;
    }

    private Optional<Medico> buscarMedicoPorUsernameFlexible(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        String usernameLimpio = username.trim();

        Optional<Medico> medico = medicoService.buscarPorUsernameUsuario(usernameLimpio);

        if (medico.isPresent()) {
            return medico;
        }

        medico = medicoService.buscarPorUsernameUsuario(usernameLimpio.toUpperCase());

        if (medico.isPresent()) {
            return medico;
        }

        medico = medicoService.buscarPorUsernameUsuario(usernameLimpio.toLowerCase());

        if (medico.isPresent()) {
            return medico;
        }

        return Optional.empty();
    }

    private void limpiarTabla() {
        cantidadCitasLabel.setText("Cantidad de citas: 0");
        citasTable.setItems(FXCollections.observableArrayList());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}