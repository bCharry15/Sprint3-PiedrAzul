package co.edu.unicauca.piedraazul.controller.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthRestController {

    @GetMapping("/api/health")
    public String health() {
        return "API REST de PiedraAzul funcionando correctamente";
    }
}