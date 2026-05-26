package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.GestionarMedicosUseCase;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.dto.MedicoRequest;

@RestController
public class MedicoRestController {

    private final GestionarMedicosUseCase gestionarMedicosUseCase;

    public MedicoRestController(GestionarMedicosUseCase gestionarMedicosUseCase) {
        this.gestionarMedicosUseCase = gestionarMedicosUseCase;
    }

    @GetMapping("/api/medicos")
    @ResponseStatus(HttpStatus.OK)
    public List<Map<String, Object>> listarTodos() {
        return gestionarMedicosUseCase.listarTodos()
                .stream()
                .map(this::convertirMedicoAResponse)
                .toList();
    }

    @GetMapping("/api/medicos/{medicoId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> obtenerPorId(@PathVariable Long medicoId) {
        return convertirMedicoAResponse(
                gestionarMedicosUseCase.obtenerPorId(medicoId)
        );
    }

    @PostMapping("/api/medicos")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crearMedico(@RequestBody MedicoRequest request) {
        return convertirMedicoAResponse(
                gestionarMedicosUseCase.crearMedico(request)
        );
    }

    @PutMapping("/api/medicos/{medicoId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> actualizarMedico(@PathVariable Long medicoId,
                                                @RequestBody MedicoRequest request) {
        return convertirMedicoAResponse(
                gestionarMedicosUseCase.actualizarMedico(medicoId, request)
        );
    }

    @DeleteMapping("/api/medicos/{medicoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarMedico(@PathVariable Long medicoId) {
        gestionarMedicosUseCase.eliminarMedico(medicoId);
    }

    private Map<String, Object> convertirMedicoAResponse(Medico medico) {
        Map<String, Object> response = new HashMap<>();

        response.put("id", medico.getId());
        response.put("nombreCompleto", medico.getNombreCompleto());
        response.put("especialidad", medico.getEspecialidad());
        response.put("intervaloMinutos", medico.getIntervaloMinutos());

        User user = medico.getUser();

        if (user != null) {
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("userRole", user.getRole() != null ? user.getRole().name() : "");
            response.put("userStatus", user.getStatus() != null ? user.getStatus().name() : "");
        } else {
            response.put("userId", null);
            response.put("username", "");
            response.put("userRole", "");
            response.put("userStatus", "");
        }

        return response;
    }
}