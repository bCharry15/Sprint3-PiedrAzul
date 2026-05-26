package co.edu.unicauca.piedraazul.agenda.domain.port.in;

import java.util.List;
import java.util.Map;

public interface GestionarUsuariosUseCase {

    Map<String, Object> login(String username, String password);

    Map<String, String> registrar(String username, String password, String role);

    Map<String, String> generarPasswordTemporal(String username);

    Map<String, String> restablecerPasswordSeguro(
            String username,
            String numeroDocumento,
            String nuevaPassword
    );

    List<Map<String, Object>> listarUsuariosPorRol(String role);
}