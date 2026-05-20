package co.edu.unicauca.piedraazul.pattern.factory;

import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.enums.UserRole;
import co.edu.unicauca.piedraazul.model.enums.UserStatus;

public class UsuarioFactory {

    private UsuarioFactory() {
    }

    public static User crearUsuario(String username, String password, UserRole role) {
        User usuario = new User();
        usuario.setUsername(username);
        usuario.setPassword(password);
        usuario.setRole(role);
        usuario.setStatus(UserStatus.ACTIVE);
        return usuario;
    }

    public static User crearAdministrador(String username, String password) {
        return crearUsuario(username, password, UserRole.ADMIN);
    }

    public static User crearAgendador(String username, String password) {
        return crearUsuario(username, password, UserRole.AGENDADOR);
    }

    public static User crearMedico(String username, String password) {
        return crearUsuario(username, password, UserRole.MEDICO);
    }

    public static User crearPaciente(String username, String password) {
        return crearUsuario(username, password, UserRole.PACIENTE);
    }
}