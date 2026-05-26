package co.edu.unicauca.piedraazul.agenda.domain.port.out;

import java.util.Optional;

import co.edu.unicauca.piedraazul.agenda.model.User;

public interface GestionarUsuariosPort {

    Optional<User> buscarPorUsername(String username);

    User guardar(User user);
}