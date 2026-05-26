package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.util.Map;

public interface AutenticarUsuarioPort {

    Map<String, Object> obtenerToken(String username, String password);
}