package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.application.service.SincronizarUsuariosKeycloakService;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.GestionarUsuariosUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.AutenticarUsuarioPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.CodificarPasswordPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.GestionarUsuariosPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.RegistrarUsuarioKeycloakPort;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.agenda.repository.PacienteRepository;

@Service
public class GestionarUsuariosService implements GestionarUsuariosUseCase {

    private final GestionarUsuariosPort gestionarUsuariosPort;
    private final CodificarPasswordPort codificarPasswordPort;
    private final AutenticarUsuarioPort autenticarUsuarioPort;
    private final RegistrarUsuarioKeycloakPort registrarUsuarioKeycloakPort;
    private final SincronizarUsuariosKeycloakService sincronizarUsuariosKeycloakService;
    private final PacienteRepository pacienteRepository;

    public GestionarUsuariosService(GestionarUsuariosPort gestionarUsuariosPort,
                                    CodificarPasswordPort codificarPasswordPort,
                                    AutenticarUsuarioPort autenticarUsuarioPort,
                                    RegistrarUsuarioKeycloakPort registrarUsuarioKeycloakPort,
                                    SincronizarUsuariosKeycloakService sincronizarUsuariosKeycloakService,
                                    PacienteRepository pacienteRepository) {
        this.gestionarUsuariosPort = gestionarUsuariosPort;
        this.codificarPasswordPort = codificarPasswordPort;
        this.autenticarUsuarioPort = autenticarUsuarioPort;
        this.registrarUsuarioKeycloakPort = registrarUsuarioKeycloakPort;
        this.sincronizarUsuariosKeycloakService = sincronizarUsuariosKeycloakService;
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public Map<String, Object> login(String username, String password) {
        validarTexto(username, "El username es obligatorio.");
        validarTexto(password, "La password es obligatoria.");

        String usernameNormalizado = username.trim();

        Map<String, Object> tokenKeycloak = autenticarUsuarioPort.obtenerToken(
                usernameNormalizado,
                password
        );

        Map<String, Object> respuesta = new HashMap<>(tokenKeycloak);
        respuesta.put("username", usernameNormalizado);
        respuesta.put("mensaje", "Autenticación exitosa con Keycloak.");

        gestionarUsuariosPort.buscarPorUsername(usernameNormalizado)
                .ifPresent(user -> {
                    respuesta.put("role", user.getRole().name());
                    respuesta.put("status", user.getStatus().name());
                });

        respuesta.putIfAbsent("role", "ADMIN");
        respuesta.putIfAbsent("status", "ACTIVE");

        return respuesta;
    }

    @Override
    public Map<String, String> registrar(String username, String password, String role) {
        validarTexto(username, "El username es obligatorio.");
        validarTexto(password, "La password es obligatoria.");
        validarTexto(role, "El rol es obligatorio.");

        String usernameNormalizado = username.trim();

        if (gestionarUsuariosPort.buscarPorUsername(usernameNormalizado).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario ya existe."
            );
        }

        validarPasswordSegura(password);

        UserRole userRole = convertirRol(role);

        sincronizarUsuariosKeycloakService.sincronizarUsuarioObligatorio(
                usernameNormalizado,
                password,
                userRole
        );

        User user = new User();
        user.setUsername(usernameNormalizado);
        user.setPassword(codificarPasswordPort.codificar(password));
        user.setRole(userRole);
        user.setStatus(UserStatus.ACTIVE);

        gestionarUsuariosPort.guardar(user);

        return Map.of(
                "mensaje",
                "Usuario registrado correctamente y sincronizado con Keycloak."
        );
    }

    @Override
    public Map<String, String> generarPasswordTemporal(String username) {
        validarTexto(username, "El username es obligatorio.");

        gestionarUsuariosPort.buscarPorUsername(username.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un usuario con ese username."
                ));

        return Map.of(
                "mensaje",
                "Por seguridad, el sistema ya no entrega contraseñas temporales. Use el restablecimiento seguro."
        );
    }

    @Override
    public Map<String, String> restablecerPasswordSeguro(String username,
                                                         String numeroDocumento,
                                                         String nuevaPassword) {
        validarTexto(username, "El username es obligatorio.");
        validarTexto(numeroDocumento, "El número de documento es obligatorio.");
        validarTexto(nuevaPassword, "La nueva contraseña es obligatoria.");
        validarPasswordSegura(nuevaPassword);

        String usernameNormalizado = username.trim();
        String documentoNormalizado = normalizarDocumento(numeroDocumento);

        User user = gestionarUsuariosPort.buscarPorUsername(usernameNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un usuario con ese username."
                ));

        if (user.getRole() == UserRole.PACIENTE) {
            pacienteRepository.findByNumeroDocumento(documentoNormalizado)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Los datos de verificación no coinciden con un paciente registrado."
                    ));
        }

        registrarUsuarioKeycloakPort.actualizarPassword(
                usernameNormalizado,
                nuevaPassword,
                false
        );

        user.setPassword(codificarPasswordPort.codificar(nuevaPassword));
        user.setStatus(UserStatus.ACTIVE);
        gestionarUsuariosPort.guardar(user);

        return Map.of(
                "mensaje",
                "La contraseña fue restablecida correctamente. Ya puede iniciar sesión con la nueva contraseña."
        );
    }

    @Override
    public List<User> listarPorRol(String role) {
        validarTexto(role, "El rol es obligatorio.");

        UserRole userRole = convertirRol(role);

        return gestionarUsuariosPort.listarPorRol(userRole);
    }

    private void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    private void validarPasswordSegura(String password) {
        if (password == null || password.trim().length() < 6) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener mínimo 6 caracteres."
            );
        }
    }

    private UserRole convertirRol(String role) {
        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rol inválido. Valores permitidos: ADMIN, AGENDADOR, MEDICO, PACIENTE."
            );
        }
    }

    private String normalizarDocumento(String numeroDocumento) {
        if (numeroDocumento == null) {
            return "";
        }

        return numeroDocumento.trim().replaceAll("[^0-9A-Za-z]", "");
    }
}