package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.GestionarUsuariosUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.AutenticarUsuarioPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.CodificarPasswordPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.GestionarUsuariosPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.RegistrarUsuarioKeycloakPort;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.agenda.repository.PacienteRepository;
import co.edu.unicauca.piedraazul.agenda.repository.UserRepository;

@Service
public class GestionarUsuariosService implements GestionarUsuariosUseCase {

    private final GestionarUsuariosPort gestionarUsuariosPort;
    private final CodificarPasswordPort codificarPasswordPort;
    private final AutenticarUsuarioPort autenticarUsuarioPort;
    private final RegistrarUsuarioKeycloakPort registrarUsuarioKeycloakPort;
    private final UserRepository userRepository;
    private final PacienteRepository pacienteRepository;
    private final DbiiSincronizacionService dbiiSincronizacionService;

    public GestionarUsuariosService(
            GestionarUsuariosPort gestionarUsuariosPort,
            CodificarPasswordPort codificarPasswordPort,
            AutenticarUsuarioPort autenticarUsuarioPort,
            RegistrarUsuarioKeycloakPort registrarUsuarioKeycloakPort,
            UserRepository userRepository,
            PacienteRepository pacienteRepository,
            DbiiSincronizacionService dbiiSincronizacionService
    ) {
        this.gestionarUsuariosPort = gestionarUsuariosPort;
        this.codificarPasswordPort = codificarPasswordPort;
        this.autenticarUsuarioPort = autenticarUsuarioPort;
        this.registrarUsuarioKeycloakPort = registrarUsuarioKeycloakPort;
        this.userRepository = userRepository;
        this.pacienteRepository = pacienteRepository;
        this.dbiiSincronizacionService = dbiiSincronizacionService;
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
        respuesta.put("mensaje", "Autenticacion exitosa con Keycloak.");

        gestionarUsuariosPort.buscarPorUsername(usernameNormalizado)
                .ifPresent(user -> {
                    respuesta.put("role", user.getRole().name());
                    respuesta.put("status", user.getStatus().name());
                });

        respuesta.putIfAbsent("role", obtenerRolPorUsername(usernameNormalizado));
        respuesta.putIfAbsent("status", "ACTIVE");

        return respuesta;
    }

    @Override
    public Map<String, String> registrar(String username, String password, String role) {
        validarTexto(username, "El username es obligatorio.");
        validarTexto(password, "La password es obligatoria.");
        validarTexto(role, "El rol es obligatorio.");

        String usernameNormalizado = username.trim();
        UserRole userRole = convertirRol(role);

        registrarUsuarioKeycloakPort.registrarUsuario(
                usernameNormalizado,
                password,
                userRole.name()
        );

        User user = gestionarUsuariosPort.buscarPorUsername(usernameNormalizado)
                .orElseGet(User::new);

        user.setUsername(usernameNormalizado);
        user.setPassword(codificarPasswordPort.codificar(password));
        user.setRole(userRole);
        user.setStatus(UserStatus.ACTIVE);

        gestionarUsuariosPort.guardar(user);

        dbiiSincronizacionService.sincronizarUsuarioSistema(
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );

        return Map.of(
                "mensaje",
                "Usuario registrado correctamente en Keycloak, tabla users y USUARIO_SISTEMA de Oracle DBII."
        );
    }

    @Override
    public Map<String, String> generarPasswordTemporal(String username) {
        validarTexto(username, "El username es obligatorio.");

        User user = gestionarUsuariosPort.buscarPorUsername(username.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un usuario con ese username."
                ));

        String nuevaPasswordTemporal = generarPasswordTemporalInterna();

        user.setPassword(codificarPasswordPort.codificar(nuevaPasswordTemporal));
        gestionarUsuariosPort.guardar(user);

        dbiiSincronizacionService.sincronizarUsuarioSistema(
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );

        return Map.of(
                "mensaje",
                "Contraseña temporal generada y sincronizada en USUARIO_SISTEMA de Oracle DBII."
        );
    }

    @Override
    public Map<String, String> restablecerPasswordSeguro(
            String username,
            String numeroDocumento,
            String nuevaPassword
    ) {
        validarTexto(username, "El username es obligatorio.");
        validarTexto(numeroDocumento, "El numero de documento es obligatorio.");
        validarTexto(nuevaPassword, "La nueva contraseña es obligatoria.");
        validarPasswordSegura(nuevaPassword);

        String usernameNormalizado = username.trim();
        String documentoNormalizado = normalizarDocumento(numeroDocumento);

        User user = gestionarUsuariosPort.buscarPorUsername(usernameNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un usuario con ese username."
                ));

        Paciente paciente = pacienteRepository.findByUsername(usernameNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No existe un paciente asociado a ese usuario."
                ));

        String documentoPaciente = normalizarDocumento(paciente.getNumeroDocumento());

        if (!documentoPaciente.equals(documentoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El numero de documento no coincide con el usuario."
            );
        }

        user.setPassword(codificarPasswordPort.codificar(nuevaPassword));
        gestionarUsuariosPort.guardar(user);

        dbiiSincronizacionService.sincronizarUsuarioSistema(
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );

        return Map.of(
                "mensaje",
                "Contraseña restablecida correctamente y sincronizada en USUARIO_SISTEMA de Oracle DBII."
        );
    }

    @Override
    public List<Map<String, Object>> listarUsuariosPorRol(String role) {
        validarTexto(role, "El rol es obligatorio.");

        UserRole userRole = convertirRol(role);

        return userRepository.findByRoleOrderByUsernameAsc(userRole)
                .stream()
                .map(this::convertirUsuarioARespuestaSegura)
                .toList();
    }

    private Map<String, Object> convertirUsuarioARespuestaSegura(User user) {
        Map<String, Object> response = new HashMap<>();

        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("role", user.getRole() != null ? user.getRole().name() : "");
        response.put("status", user.getStatus() != null ? user.getStatus().name() : "");

        return response;
    }

    private void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
    }

    private void validarPasswordSegura(String password) {
        if (password.length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener minimo 8 caracteres."
            );
        }

        boolean tieneMayuscula = password.matches(".*[A-Z].*");
        boolean tieneMinuscula = password.matches(".*[a-z].*");
        boolean tieneNumero = password.matches(".*\\d.*");

        if (!tieneMayuscula || !tieneMinuscula || !tieneNumero) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener mayusculas, minusculas y numeros."
            );
        }
    }

    private UserRole convertirRol(String role) {
        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rol invalido. Valores permitidos: ADMIN, AGENDADOR, MEDICO, PACIENTE."
            );
        }
    }

    private String normalizarDocumento(String numeroDocumento) {
        return numeroDocumento == null ? "" : numeroDocumento.replaceAll("\\s+", "").trim();
    }

    private String generarPasswordTemporalInterna() {
        int numero = (int) (Math.random() * 900000) + 100000;
        return "Temp" + numero;
    }

    private String obtenerRolPorUsername(String username) {
        if ("admin".equalsIgnoreCase(username)) {
            return "ADMIN";
        }

        if ("agendador".equalsIgnoreCase(username)) {
            return "AGENDADOR";
        }

        if ("medico".equalsIgnoreCase(username)) {
            return "MEDICO";
        }

        if ("paciente".equalsIgnoreCase(username)) {
            return "PACIENTE";
        }

        return "SIN_ROL_LOCAL";
    }
}