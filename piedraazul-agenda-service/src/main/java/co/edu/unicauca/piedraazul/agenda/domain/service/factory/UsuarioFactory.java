package co.edu.unicauca.piedraazul.agenda.domain.service.factory;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;

@Component
public class UsuarioFactory {

    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioFactory(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User crearAdministrador(String username, String passwordPlano) {
        return crearUsuario(username, passwordPlano, UserRole.ADMIN);
    }

    public User crearMedico(String username, String passwordPlano) {
        return crearUsuario(username, passwordPlano, UserRole.MEDICO);
    }

    public User crearPaciente(String username, String passwordPlano) {
        return crearUsuario(username, passwordPlano, UserRole.PACIENTE);
    }

    public User crearAgendador(String username, String passwordPlano) {
        return crearUsuario(username, passwordPlano, UserRole.AGENDADOR);
    }

    private User crearUsuario(String username, String passwordPlano, UserRole role) {
        User usuario = new User();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(passwordPlano));
        usuario.setRole(role);
        usuario.setStatus(UserStatus.ACTIVE);
        return usuario;
    }
}