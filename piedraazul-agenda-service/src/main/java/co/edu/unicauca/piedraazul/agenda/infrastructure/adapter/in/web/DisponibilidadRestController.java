package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConsultarDisponibilidadUseCase;
import co.edu.unicauca.piedraazul.agenda.model.dto.DisponibilidadResponse;

@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadRestController {

    private final ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;

    public DisponibilidadRestController(ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase) {
        this.consultarDisponibilidadUseCase = consultarDisponibilidadUseCase;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public DisponibilidadResponse consultarDisponibilidad(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return consultarDisponibilidadUseCase.consultar(medicoId, fecha);
    }
}