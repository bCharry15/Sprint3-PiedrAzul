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
            if (user == null) {
                notifyObservers("Registro fallido: el usuario no puede estar vacío.");
                return false;
            }

            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                notifyObservers("Registro fallido: el nombre de usuario es obligatorio.");
                return false;
            }

            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                notifyObservers("Registro fallido: la contraseña es obligatoria.");
                return false;
            }

            if (user.getRole() == null) {
                notifyObservers("Registro fallido: el rol es obligatorio.");
                return false;
            }

            agendaServiceClient.registrarUsuario(
                    user.getUsername().trim(),
                    user.getPassword(),
                    user.getRole().name()
            );

            notifyObservers("Usuario registrado correctamente: " + user.getUsername().trim());
            return true;

        } catch (Exception e) {
            String detalle = e.getMessage();

            if (detalle == null || detalle.trim().isEmpty()) {
                detalle = "El usuario ya existe o no pudo registrarse.";
            }

            notifyObservers("Registro fallido: " + detalle);
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