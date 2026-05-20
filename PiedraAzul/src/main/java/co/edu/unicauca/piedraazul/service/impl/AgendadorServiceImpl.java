package co.edu.unicauca.piedraazul.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.enums.UserRole;
import co.edu.unicauca.piedraazul.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.service.IAgendadorService;
import co.edu.unicauca.piedraazul.service.IUserService;

@Service
public class AgendadorServiceImpl implements IAgendadorService {

    private final IUserService userService;
    private final List<User> agendadores = new ArrayList<>();

    public AgendadorServiceImpl(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public User registrarAgendador(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(UserRole.AGENDADOR);
        user.setStatus(UserStatus.ACTIVE);

        boolean registrado = userService.registerUser(user);

        if (!registrado) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }

        agendadores.add(user);
        return user;
    }

    @Override
    public List<User> listarAgendadores() {
        return agendadores;
    }
}