package co.edu.unicauca.piedraazul.agenda.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.agenda.repository.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthRestController(UserRepository userRepository,
                              BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario o contraseña incorrectos."
                ));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario o contraseña incorrectos."
            );
        }

        return Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "status", user.getStatus().name()
        );
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String role = body.get("role");

        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El username es obligatorio.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria.");
        }

        if (role == null || role.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol es obligatorio.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe.");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.valueOf(role));
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        return Map.of("mensaje", "Usuario registrado correctamente.");
    }
    @PostMapping("/forgot-password")
public Map<String, String> forgotPassword(@RequestBody Map<String, String> body) {
    String username = body.get("username");

    if (username == null || username.trim().isEmpty()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El username es obligatorio."
        );
    }

    User user = userRepository.findByUsername(username.trim())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No existe un usuario con ese username."
            ));

    String nuevaPasswordTemporal = generarPasswordTemporal();

    user.setPassword(passwordEncoder.encode(nuevaPasswordTemporal));
    userRepository.save(user);

    return Map.of(
            "mensaje", "Contraseña temporal generada correctamente.",
            "username", user.getUsername(),
            "passwordTemporal", nuevaPasswordTemporal
    );
}

private String generarPasswordTemporal() {
    int numero = (int) (Math.random() * 900000) + 100000;
    return "Temp" + numero;
}
}