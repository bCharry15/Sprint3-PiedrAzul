package co.edu.unicauca.piedraazul.agenda.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.application.service.SincronizarUsuariosKeycloakService;
import co.edu.unicauca.piedraazul.agenda.domain.service.factory.UsuarioFactory;
import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.Paciente;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.enums.Genero;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.repository.DisponibilidadMedicoRepository;
import co.edu.unicauca.piedraazul.agenda.repository.MedicoRepository;
import co.edu.unicauca.piedraazul.agenda.repository.PacienteRepository;
import co.edu.unicauca.piedraazul.agenda.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Map<String, String> PASSWORDS_BASE = Map.of(
            "admin", "admin123",
            "agendador", "agendador123",
            "medico", "medico123",
            "paciente", "paciente123"
    );

    private final MedicoRepository medicoRepository;
    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    private final UserRepository userRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioFactory usuarioFactory;
    private final SincronizarUsuariosKeycloakService sincronizarUsuariosKeycloakService;

    public DataInitializer(MedicoRepository medicoRepository,
                           DisponibilidadMedicoRepository disponibilidadMedicoRepository,
                           UserRepository userRepository,
                           PacienteRepository pacienteRepository,
                           UsuarioFactory usuarioFactory,
                           SincronizarUsuariosKeycloakService sincronizarUsuariosKeycloakService) {
        this.medicoRepository = medicoRepository;
        this.disponibilidadMedicoRepository = disponibilidadMedicoRepository;
        this.userRepository = userRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioFactory = usuarioFactory;
        this.sincronizarUsuariosKeycloakService = sincronizarUsuariosKeycloakService;
    }

    @Override
    public void run(String... args) {
        crearAdminSiNoExiste();
        crearAgendadorSiNoExiste();

        User usuarioPaciente = obtenerOCrearUsuarioPaciente();
        crearPerfilPacienteSiNoExiste(usuarioPaciente);

        User usuarioMedico = obtenerOCrearUsuarioMedico();

        Medico jhoiner = obtenerOCrearMedico(
                "Jhoiner Puentes",
                "Terapista",
                15
        );

        asociarUsuarioAMedico(jhoiner, usuarioMedico);

        crearDisponibilidadSiNoExiste(
                jhoiner,
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                15,
                4
        );

        crearDisponibilidadSiNoExiste(
                jhoiner,
                DayOfWeek.SUNDAY,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                15,
                4
        );

        sincronizarUsuariosLocalesConKeycloak();

        System.out.println("AGENDA-SERVICE -> Datos iniciales cargados correctamente en piedraazul_agenda");
    }

    private void crearAdminSiNoExiste() {
        if (userRepository.findByUsername("admin").isPresent()) {
            return;
        }

        User admin = usuarioFactory.crearAdministrador("admin", "admin123");
        userRepository.save(admin);

        System.out.println("AGENDA-SERVICE -> Admin inicial creado en BD usando UsuarioFactory.");
    }

    private void crearAgendadorSiNoExiste() {
        if (userRepository.findByUsername("agendador").isPresent()) {
            return;
        }

        User agendador = usuarioFactory.crearAgendador("agendador", "agendador123");
        userRepository.save(agendador);

        System.out.println("AGENDA-SERVICE -> Agendador inicial creado en BD usando UsuarioFactory.");
    }

    private User obtenerOCrearUsuarioPaciente() {
        return userRepository.findByUsername("paciente")
                .orElseGet(() -> {
                    User paciente = usuarioFactory.crearPaciente("paciente", "paciente123");
                    User pacienteGuardado = userRepository.save(paciente);

                    System.out.println("AGENDA-SERVICE -> Usuario paciente inicial creado en BD usando UsuarioFactory.");

                    return pacienteGuardado;
                });
    }

    private void crearPerfilPacienteSiNoExiste(User usuarioPaciente) {
        if (pacienteRepository.findByUsername(usuarioPaciente.getUsername()).isPresent()) {
            return;
        }

        Paciente pacienteExistentePorDocumento = pacienteRepository
                .findByNumeroDocumento("1234567890")
                .orElse(null);

        if (pacienteExistentePorDocumento != null) {
            pacienteExistentePorDocumento.setUsername(usuarioPaciente.getUsername());
            pacienteRepository.save(pacienteExistentePorDocumento);

            System.out.println("AGENDA-SERVICE -> Perfil paciente existente asociado al usuario paciente.");

            return;
        }

        Paciente paciente = new Paciente();
        paciente.setUsername(usuarioPaciente.getUsername());
        paciente.setNumeroDocumento("1234567890");
        paciente.setTipoDocumento("CC");
        paciente.setNombres("Paciente");
        paciente.setApellidos("Demo");
        paciente.setCelular("3001234567");
        paciente.setGenero(Genero.HOMBRE);
        paciente.setFechaNacimiento(LocalDate.of(2000, 1, 1));
        paciente.setCorreo("paciente.demo@piedraazul.com");

        pacienteRepository.save(paciente);

        System.out.println("AGENDA-SERVICE -> Perfil inicial de paciente creado en BD.");
    }

    private User obtenerOCrearUsuarioMedico() {
        return userRepository.findByUsername("medico")
                .orElseGet(() -> {
                    User medico = usuarioFactory.crearMedico("medico", "medico123");
                    User medicoGuardado = userRepository.save(medico);

                    System.out.println("AGENDA-SERVICE -> Usuario medico inicial creado en BD usando UsuarioFactory.");

                    return medicoGuardado;
                });
    }

    private Medico obtenerOCrearMedico(String nombreCompleto,
                                       String especialidad,
                                       Integer intervaloMinutos) {
        List<Medico> medicos = medicoRepository.findAll();

        for (Medico medico : medicos) {
            if (medico.getNombreCompleto() != null
                    && medico.getNombreCompleto().equalsIgnoreCase(nombreCompleto)) {
                medico.setEspecialidad(especialidad);
                medico.setIntervaloMinutos(intervaloMinutos);
                return medicoRepository.save(medico);
            }
        }

        Medico medico = new Medico();
        medico.setNombreCompleto(nombreCompleto);
        medico.setEspecialidad(especialidad);
        medico.setIntervaloMinutos(intervaloMinutos);

        return medicoRepository.save(medico);
    }

    private void asociarUsuarioAMedico(Medico medico, User usuarioMedico) {
        if (medico.getUser() != null
                && medico.getUser().getUsername() != null
                && medico.getUser().getUsername().equalsIgnoreCase(usuarioMedico.getUsername())) {
            return;
        }

        medico.setUser(usuarioMedico);
        medicoRepository.save(medico);

        System.out.println("AGENDA-SERVICE -> Usuario medico asociado al perfil de Jhoiner Puentes.");
    }

    private void crearDisponibilidadSiNoExiste(Medico medico,
                                               DayOfWeek diaSemana,
                                               LocalTime horaInicio,
                                               LocalTime horaFin,
                                               Integer intervaloMinutos,
                                               Integer ventanaSemanas) {
        boolean existe = disponibilidadMedicoRepository
                .findByMedicoAndActivoTrue(medico)
                .stream()
                .anyMatch(disponibilidad ->
                        disponibilidad.getDiaSemana() == diaSemana
                                && disponibilidad.getHoraInicio().equals(horaInicio)
                                && disponibilidad.getHoraFin().equals(horaFin)
                );

        if (existe) {
            return;
        }

        DisponibilidadMedico disponibilidad = new DisponibilidadMedico();
        disponibilidad.setMedico(medico);
        disponibilidad.setDiaSemana(diaSemana);
        disponibilidad.setHoraInicio(horaInicio);
        disponibilidad.setHoraFin(horaFin);
        disponibilidad.setIntervaloMinutos(intervaloMinutos);
        disponibilidad.setVentanaSemanas(ventanaSemanas);
        disponibilidad.setActivo(true);

        disponibilidadMedicoRepository.save(disponibilidad);
    }

    private void sincronizarUsuariosLocalesConKeycloak() {
    List<User> usuarios = userRepository.findAll();

    for (User usuario : usuarios) {
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            continue;
        }

        if (usuario.getRole() == null) {
            continue;
        }

        String username = usuario.getUsername().trim();

        /*
         * Solo se sincronizan automáticamente los usuarios base del sistema.
         * No se deben sincronizar usuarios creados desde la app porque la contraseña
         * guardada localmente está cifrada o puede no coincidir con Keycloak.
         */
        if (!PASSWORDS_BASE.containsKey(username)) {
            System.out.println("AGENDA-SERVICE -> Usuario omitido en sincronización automática con Keycloak: " + username);
            continue;
        }

        String password = PASSWORDS_BASE.get(username);

        sincronizarUsuariosKeycloakService.sincronizarUsuarioEnArranque(
                username,
                password,
                usuario.getRole()
        );
    }
}

    private String obtenerPasswordParaSincronizacion(String username, UserRole role) {
        String usernameNormalizado = username.trim();

        if (PASSWORDS_BASE.containsKey(usernameNormalizado)) {
            return PASSWORDS_BASE.get(usernameNormalizado);
        }

        return SincronizarUsuariosKeycloakService.PASSWORD_RECUPERACION;
    }
}