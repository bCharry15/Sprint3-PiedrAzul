package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.util.List;
import java.util.Optional;

import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;

public interface GestionarUsuariosPort {

    Optional<User> buscarPorUsername(String username);

    List<User> listarPorRol(UserRole role);

    User guardar(User user);
}