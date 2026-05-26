package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.port.out.GestionarUsuariosPort;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.repository.UserRepository;

@Component
public class UsuarioPersistenceAdapter implements GestionarUsuariosPort {

    private final UserRepository userRepository;

    public UsuarioPersistenceAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> buscarPorUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User guardar(User user) {
        return userRepository.save(user);
    }
}