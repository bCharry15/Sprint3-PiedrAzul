package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.application.service.SincronizarUsuariosKeycloakService;
import co.edu.unicauca.piedraazul.agenda.domain.port.in.GestionarMedicosUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.CodificarPasswordPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.GestionarMedicosPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.GestionarUsuariosPort;
import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.dto.MedicoRequest;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.agenda.repository.CitaRepository;
import co.edu.unicauca.piedraazul.agenda.repository.DisponibilidadMedicoRepository;
import co.edu.unicauca.piedraazul.agenda.repository.MedicoRepository;
import jakarta.transaction.Transactional;

@Service
public class GestionarMedicosService implements GestionarMedicosUseCase {

    private final GestionarMedicosPort gestionarMedicosPort;
    private final GestionarUsuariosPort gestionarUsuariosPort;
    private final CodificarPasswordPort codificarPasswordPort;
    private final SincronizarUsuariosKeycloakService sincronizarUsuariosKeycloakService;
    private final MedicoRepository medicoRepository;
    private final CitaRepository citaRepository;
    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;

    public GestionarMedicosService(GestionarMedicosPort gestionarMedicosPort,
                                   GestionarUsuariosPort gestionarUsuariosPort,
                                   CodificarPasswordPort codificarPasswordPort,
                                   SincronizarUsuariosKeycloakService sincronizarUsuariosKeycloakService,
                                   MedicoRepository medicoRepository,
                                   CitaRepository citaRepository,
                                   DisponibilidadMedicoRepository disponibilidadMedicoRepository) {
        this.gestionarMedicosPort = gestionarMedicosPort;
        this.gestionarUsuariosPort = gestionarUsuariosPort;
        this.codificarPasswordPort = codificarPasswordPort;
        this.sincronizarUsuariosKeycloakService = sincronizarUsuariosKeycloakService;
        this.medicoRepository = medicoRepository;
        this.citaRepository = citaRepository;
        this.disponibilidadMedicoRepository = disponibilidadMedicoRepository;
    }

    @Override
    public List<Medico> listarTodos() {
        return gestionarMedicosPort.listarTodos();
    }

    @Override
    public Medico crearMedico(MedicoRequest request) {
        validarSolicitudCreacion(request);

        String nombreCompleto = request.getNombreCompleto().trim();
        String especialidad = request.getEspecialidad().trim();
        String username = request.getUsername().trim();
        String password = request.getPassword();

        if (gestionarUsuariosPort.buscarPorUsername(username).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese username."
            );
        }

        sincronizarUsuariosKeycloakService.sincronizarUsuarioObligatorio(
                username,
                password,
                UserRole.MEDICO
        );

        User user = new User();
        user.setUsername(username);
        user.setPassword(codificarPasswordPort.codificar(password));
        user.setRole(UserRole.MEDICO);
        user.setStatus(UserStatus.ACTIVE);

        User usuarioGuardado = gestionarUsuariosPort.guardar(user);

        Medico medico = new Medico();
        medico.setNombreCompleto(nombreCompleto);
        medico.setEspecialidad(especialidad);
        medico.setIntervaloMinutos(
                request.getIntervaloMinutos() != null
                        ? request.getIntervaloMinutos()
                        : 15
        );
        medico.setActivo(true);
        medico.setUser(usuarioGuardado);

        return gestionarMedicosPort.guardar(medico);
    }

    @Override
    public Medico obtenerPorId(Long medicoId) {
        if (medicoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El id del médico es obligatorio."
            );
        }

        return gestionarMedicosPort.buscarPorId(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista activo con id: " + medicoId
                ));
    }

    @Override
    public Medico actualizarMedico(Long medicoId, MedicoRequest request) {
        if (medicoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El id del médico es obligatorio."
            );
        }

        validarSolicitudActualizacion(request);

        Medico medico = medicoRepository.findByIdAndActivoTrue(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista activo con id: " + medicoId
                ));

        medico.setNombreCompleto(request.getNombreCompleto().trim());
        medico.setEspecialidad(request.getEspecialidad().trim());
        medico.setIntervaloMinutos(request.getIntervaloMinutos());

        return medicoRepository.save(medico);
    }

    @Override
    @Transactional
    public void eliminarMedico(Long medicoId) {
        if (medicoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El id del médico es obligatorio."
            );
        }

        Medico medico = medicoRepository.findByIdAndActivoTrue(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un médico/terapista activo con id: " + medicoId
                ));

        long cantidadCitasConservadas = citaRepository.countByMedicoId(medicoId);

        medico.setActivo(false);

        User usuarioMedico = medico.getUser();
        if (usuarioMedico != null) {
            usuarioMedico.setStatus(UserStatus.INACTIVE);
            gestionarUsuariosPort.guardar(usuarioMedico);
        }

        List<DisponibilidadMedico> disponibilidades = disponibilidadMedicoRepository.findByMedicoId(medicoId);
        for (DisponibilidadMedico disponibilidad : disponibilidades) {
            disponibilidad.setActivo(false);
        }

        disponibilidadMedicoRepository.saveAll(disponibilidades);
        medicoRepository.save(medico);

        System.out.println("AGENDA-SERVICE -> Médico desactivado por administrador. ID: " + medicoId
                + ". Citas conservadas asociadas: " + cantidadCitasConservadas);
    }

    private void validarSolicitudCreacion(MedicoRequest request) {
        validarSolicitudBase(request);

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El username del médico es obligatorio."
            );
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La password del médico es obligatoria."
            );
        }

        if (request.getPassword().trim().length() < 6) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La password del médico debe tener mínimo 6 caracteres."
            );
        }
    }

    private void validarSolicitudActualizacion(MedicoRequest request) {
        validarSolicitudBase(request);
    }

    private void validarSolicitudBase(MedicoRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La solicitud no puede estar vacía."
            );
        }

        if (request.getNombreCompleto() == null || request.getNombreCompleto().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre completo del médico es obligatorio."
            );
        }

        if (request.getEspecialidad() == null || request.getEspecialidad().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La especialidad del médico es obligatoria."
            );
        }

        if (request.getIntervaloMinutos() == null || request.getIntervaloMinutos() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El intervalo de atención debe ser mayor que cero."
            );
        }
    }
}