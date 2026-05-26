package co.edu.unicauca.piedraazul.service.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.enums.UserRole;
import co.edu.unicauca.piedraazul.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.service.IAgendadorService;
import co.edu.unicauca.piedraazul.service.IUserService;

@Service
public class AgendadorServiceImpl implements IAgendadorService {

    private final IUserService userService;
    private final AgendaServiceClient agendaServiceClient;

    public AgendadorServiceImpl(IUserService userService,
                                AgendaServiceClient agendaServiceClient) {
        this.userService = userService;
        this.agendaServiceClient = agendaServiceClient;
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

        return user;
    }

    @Override
    public List<User> listarAgendadores() {
        User[] usuarios = agendaServiceClient.listarUsuariosPorRol("AGENDADOR");

        if (usuarios == null) {
            return List.of();
        }

        return Arrays.stream(usuarios)
                .map(this::convertirAUsuario)
                .toList();
    }

    private User convertirAUsuario(User user) {
        if (user == null) {
            return null;
        }

        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }

        if (user.getRole() == null) {
            user.setRole(UserRole.AGENDADOR);
        }

        return user;
    }
}