package co.edu.unicauca.piedraazul.controller.rest;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.dto.DisponibilidadResponse;

@RestController
@RequestMapping("/api/proxy/agenda")
public class AgendaProxyRestController {

    private final AgendaServiceClient agendaServiceClient;

    public AgendaProxyRestController(AgendaServiceClient agendaServiceClient) {
        this.agendaServiceClient = agendaServiceClient;
    }

    @GetMapping("/disponibilidad")
    public DisponibilidadResponse consultarDisponibilidadDesdeMicroservicio(
            @RequestParam Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return agendaServiceClient.consultarDisponibilidad(medicoId, fecha);
    }
}