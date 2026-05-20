package co.edu.unicauca.piedraazul.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.model.Medico;
import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.dto.CitaMedicoTablaModel;
import co.edu.unicauca.piedraazul.service.ICitaService;
import co.edu.unicauca.piedraazul.service.IMedicoService;
import co.edu.unicauca.piedraazul.util.SceneManager;
import co.edu.unicauca.piedraazul.util.SesionUsuario;
import co.edu.unicauca.piedraazul.util.UserSession;
import co.edu.unicauca.piedraazul.util.Vista;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

@Component
public class MedicoPanelController {

    @FXML private Label                              bienvenidaLabel;
    @FXML private DatePicker                         fechaBusquedaPicker;
    @FXML private Label                              cantidadCitasLabel;
    @FXML private TableView<CitaMedicoTablaModel>    citasTable;
    @FXML private TableColumn<CitaMedicoTablaModel, Long>   idColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> pacienteColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> documentoColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> fechaColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> horaColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> estadoColumn;
    @FXML private TableColumn<CitaMedicoTablaModel, String> observacionColumn;

    // Dependencias sobre interfaces — principio D
    private final SceneManager   sceneManager;
    private final UserSession    userSession;
    private final IMedicoService medicoService;
    private final ICitaService   citaService;

    private Medico medicoActual;

    public MedicoPanelController(SceneManager sceneManager,
                                 UserSession userSession,
                                 IMedicoService medicoService,
                                 ICitaService citaService) {
        this.sceneManager  = sceneManager;
        this.userSession   = userSession;
        this.medicoService = medicoService;
        this.citaService   = citaService;
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pacienteColumn.setCellValueFactory(new PropertyValueFactory<>("paciente"));
        documentoColumn.setCellValueFactory(new PropertyValueFactory<>("documento"));
        fechaColumn.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        horaColumn.setCellValueFactory(new PropertyValueFactory<>("hora"));
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));
        observacionColumn.setCellValueFactory(new PropertyValueFactory<>("observacion"));

        fechaBusquedaPicker.setValue(LocalDate.now());

        cargarMedicoActual();
        buscarMisCitas();
    }

    @FXML
    private void buscarMisCitas() {
        if (medicoActual == null) {
            cantidadCitasLabel.setText("Cantidad de citas: 0");
            citasTable.setItems(FXCollections.observableArrayList());
            return;
        }

        LocalDate fecha = fechaBusquedaPicker.getValue();
        if (fecha == null) {
            showAlert(Alert.AlertType.WARNING, "Validación",
                    "Debe seleccionar una fecha.");
            return;
        }

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
                        c.getObservacion() == null ? "" : c.getObservacion()))
                .toList();

        citasTable.setItems(FXCollections.observableArrayList(filas));
        cantidadCitasLabel.setText("Cantidad de citas: " + filas.size());
    }

    // Requerido por medicoPanel.xml — botón "Cerrar sesión"
    @FXML
    private void handleLogout() {
        userSession.clear();
SesionUsuario.limpiarSesion();
        sceneManager.switchScene(Vista.LOGIN);
    }

    // Alias por si se usa en algún otro lugar
    @FXML
    private void logout() {
        handleLogout();
    }

    private void cargarMedicoActual() {
        User currentUser = SesionUsuario.getUsuarioActual();
if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Sesión",
                    "No hay un usuario autenticado en sesión.");
            return;
        }

        medicoActual = medicoService
                .buscarPorUsernameUsuario(currentUser.getUsername())
                .orElse(null);

        if (medicoActual == null) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se encontró un perfil de médico asociado al usuario.");
            return;
        }

        bienvenidaLabel.setText("Bienvenido, " + medicoActual.getNombreCompleto());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
