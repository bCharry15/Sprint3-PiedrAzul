package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/agenda/health")
    public String health() {
        return "Microservicio de Agenda PiedraAzul funcionando correctamente";
    }
}




