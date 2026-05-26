package co.edu.unicauca.piedraazul.agenda.domain.port.out;

public interface RegistrarUsuarioKeycloakPort {

    void registrarUsuario(String username, String password, String role);

    void actualizarPassword(String username, String nuevaPassword, boolean temporal);
}