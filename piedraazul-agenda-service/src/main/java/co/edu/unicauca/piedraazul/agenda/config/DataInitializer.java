package co.edu.unicauca.piedraazul.agenda.config;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.domain.service.factory.UsuarioFactory;
import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.repository.DisponibilidadMedicoRepository;
import co.edu.unicauca.piedraazul.agenda.repository.MedicoRepository;
import co.edu.unicauca.piedraazul.agenda.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MedicoRepository medicoRepository;
    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    private final UserRepository userRepository;
    private final UsuarioFactory usuarioFactory;

    public DataInitializer(MedicoRepository medicoRepository,
                           DisponibilidadMedicoRepository disponibilidadMedicoRepository,
                           UserRepository userRepository,
                           UsuarioFactory usuarioFactory) {
        this.medicoRepository = medicoRepository;
        this.disponibilidadMedicoRepository = disponibilidadMedicoRepository;
        this.userRepository = userRepository;
        this.usuarioFactory = usuarioFactory;
    }

    @Override
    public void run(String... args) {
        crearAdminSiNoExiste();

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
}