package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.CodificarPasswordPort;

@Component
public class BCryptPasswordAdapter implements CodificarPasswordPort {

    private final BCryptPasswordEncoder passwordEncoder;

    public BCryptPasswordAdapter(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String codificar(String passwordPlano) {
        return passwordEncoder.encode(passwordPlano);
    }

    @Override
    public boolean coincide(String passwordPlano, String passwordCodificado) {
        return passwordEncoder.matches(passwordPlano, passwordCodificado);
    }
}