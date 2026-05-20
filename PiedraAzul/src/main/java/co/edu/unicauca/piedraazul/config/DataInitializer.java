package co.edu.unicauca.piedraazul.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) {
        // El admin se crea en el agenda-service al arrancar.
        // PiedraAzul no necesita crear usuarios iniciales.
    }
}