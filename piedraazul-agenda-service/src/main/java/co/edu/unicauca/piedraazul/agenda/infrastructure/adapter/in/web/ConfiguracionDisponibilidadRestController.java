package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConfigurarDisponibilidadUseCase;
import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.dto.ConfiguracionDisponibilidadRequest;

@RestController
@RequestMapping("/api/configuraciones-disponibilidad")
public class ConfiguracionDisponibilidadRestController {

    private final ConfigurarDisponibilidadUseCase configurarDisponibilidadUseCase;

    public ConfiguracionDisponibilidadRestController(ConfigurarDisponibilidadUseCase configurarDisponibilidadUseCase) {
        this.configurarDisponibilidadUseCase = configurarDisponibilidadUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibilidadMedico configurar(@RequestBody ConfiguracionDisponibilidadRequest request) {
        return configurarDisponibilidadUseCase.configurar(request);
    }

    @GetMapping("/medico/{medicoId}")
    @ResponseStatus(HttpStatus.OK)
    public List<DisponibilidadMedico> listarPorMedico(@PathVariable Long medicoId) {
        return configurarDisponibilidadUseCase.listarPorMedico(medicoId);
    }
}