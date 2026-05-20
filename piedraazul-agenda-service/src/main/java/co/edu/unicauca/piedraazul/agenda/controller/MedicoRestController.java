package co.edu.unicauca.piedraazul.agenda.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.dto.CrearMedicoRequest;
import co.edu.unicauca.piedraazul.agenda.model.dto.MedicoResponse;
import co.edu.unicauca.piedraazul.agenda.service.IMedicoService;

@RestController
@RequestMapping("/api/medicos")
public class MedicoRestController {

    private final IMedicoService medicoService;

    public MedicoRestController(IMedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @GetMapping
    public List<MedicoResponse> listarMedicos() {
        return medicoService.listarTodos()
                .stream()
                .map(this::convertirAMedicoResponse)
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> registrarMedico(@RequestBody CrearMedicoRequest request) {
        try {
            validarRequest(request);

            Medico medicoCreado = medicoService.registrarMedico(
                    request.getNombreCompleto(),
                    request.getEspecialidad(),
                    request.getIntervaloMinutos(),
                    request.getUsername(),
                    request.getPassword()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(convertirAMedicoResponse(medicoCreado));

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error registrando médico: " + e.getMessage());
        }
    }

    private void validarRequest(CrearMedicoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud no puede estar vacía.");
        }

        if (request.getNombreCompleto() == null || request.getNombreCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        }

        if (request.getEspecialidad() == null || request.getEspecialidad().trim().isEmpty()) {
            throw new IllegalArgumentException("La especialidad es obligatoria.");
        }

        if (request.getIntervaloMinutos() == null || request.getIntervaloMinutos() <= 0) {
            throw new IllegalArgumentException("El intervalo de atención debe ser mayor a cero.");
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
    }

    private MedicoResponse convertirAMedicoResponse(Medico medico) {
        MedicoResponse response = new MedicoResponse();
        response.setId(medico.getId());
        response.setNombreCompleto(medico.getNombreCompleto());
        response.setEspecialidad(medico.getEspecialidad());
        response.setIntervaloMinutos(medico.getIntervaloMinutos());

        if (medico.getUser() != null) {
            response.setUsername(medico.getUser().getUsername());
        }

        return response;
    }
}