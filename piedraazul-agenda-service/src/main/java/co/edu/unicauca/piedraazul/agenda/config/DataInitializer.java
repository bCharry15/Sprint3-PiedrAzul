package co.edu.unicauca.piedraazul.agenda.config;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.agenda.repository.DisponibilidadMedicoRepository;
import co.edu.unicauca.piedraazul.agenda.repository.MedicoRepository;
import co.edu.unicauca.piedraazul.agenda.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MedicoRepository medicoRepository;
    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(MedicoRepository medicoRepository,
                           DisponibilidadMedicoRepository disponibilidadMedicoRepository,
                           UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.medicoRepository = medicoRepository;
        this.disponibilidadMedicoRepository = disponibilidadMedicoRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearAdminSiNoExiste();

        Medico jhoiner = obtenerOCrearMedico(
                "Jhoiner Puentes",
                "Terapista",
                15
        );

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

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        userRepository.save(admin);

        System.out.println("AGENDA-SERVICE -> Admin inicial creado en BD.");
    }

    private Medico obtenerOCrearMedico(String nombreCompleto,
                                       String especialidad,
                                       Integer intervaloMinutos) {
        List<Medico> medicos = medicoRepository.findAll();

        for (Medico medico : medicos) {
            if (medico.getNombreCompleto() != null
                    && medico.getNombreCompleto().equalsIgnoreCase(nombreCompleto)) {
                return medico;
            }
        }

        Medico medico = new Medico();
        medico.setNombreCompleto(nombreCompleto);
        medico.setEspecialidad(especialidad);
        medico.setIntervaloMinutos(intervaloMinutos);

        return medicoRepository.save(medico);
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