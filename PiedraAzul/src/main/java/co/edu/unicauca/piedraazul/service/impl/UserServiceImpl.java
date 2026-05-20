package co.edu.unicauca.piedraazul.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.enums.UserRole;
import co.edu.unicauca.piedraazul.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.observer.Observer;
import co.edu.unicauca.piedraazul.observer.Subject;
import co.edu.unicauca.piedraazul.service.IUserService;

@Service
public class UserServiceImpl extends Subject implements IUserService {

    private final AgendaServiceClient agendaServiceClient;

    public UserServiceImpl(AgendaServiceClient agendaServiceClient) {
        this.agendaServiceClient = agendaServiceClient;
    }

    @Override
    public boolean registerUser(User user, Observer vista) {
        attach(vista);
        return registerUser(user);
    }

    @Override
    public boolean registerUser(User user) {
        try {
            agendaServiceClient.registrarUsuario(
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole().name()
            );

            notifyObservers("Usuario registrado: " + user.getUsername());
            return true;

        } catch (Exception e) {
            notifyObservers("Registro fallido: " + user.getUsername());
            return false;
        }
    }

    @Override
    public User authenticate(String username, String rawPassword) {
        try {
            Map<String, String> response = agendaServiceClient.login(username, rawPassword);

            if (response == null) {
                notifyObservers("Login fallido para: " + username);
                return null;
            }

            User user = new User();
            user.setUsername(response.get("username"));
            user.setRole(UserRole.valueOf(response.get("role")));
            user.setStatus(UserStatus.valueOf(response.get("status")));

            notifyObservers("Login exitoso: " + username);
            return user;

        } catch (Exception e) {
            notifyObservers("Login fallido para: " + username);
            return null;
        }
    }
}