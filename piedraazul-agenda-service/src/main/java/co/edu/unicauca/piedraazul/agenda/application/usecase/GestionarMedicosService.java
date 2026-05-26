package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.GestionarMedicosUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.CodificarPasswordPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.GestionarMedicosPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.GestionarUsuariosPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.RegistrarUsuarioKeycloakPort;
import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.dto.MedicoRequest;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.agenda.repository.CitaRepository;
import co.edu.unicauca.piedraazul.agenda.repository.DisponibilidadMedicoRepository;
import co.edu.unicauca.piedraazul.agenda.repository.HistorialReagendamientoRepository;
import co.edu.unicauca.piedraazul.agenda.repository.MedicoRepository;
import jakarta.transaction.Transactional;

@Service
public class GestionarMedicosService implements GestionarMedicosUseCase {

    private final GestionarMedicosPort gestionarMedicosPort;
    private final GestionarUsuariosPort gestionarUsuariosPort;
    private final CodificarPasswordPort codificarPasswordPort;
    private final RegistrarUsuarioKeycloakPort registrarUsuarioKeycloakPort;
    private final MedicoRepository medicoRepository;
    private final CitaRepository citaRepository;
    private final DisponibilidadMedicoRepository disponibilidadMedicoRepository;
    private final HistorialReagendamientoRepository historialReagendamientoRepository;
    private final DbiiSincronizacionService dbiiSincronizacionService;

    public GestionarMedicosService(
            GestionarMedicosPort gestionarMedicosPort,
            GestionarUsuariosPort gestionarUsuariosPort,
            CodificarPasswordPort codificarPasswordPort,
            RegistrarUsuarioKeycloakPort registrarUsuarioKeycloakPort,
            MedicoRepository medicoRepository,
            CitaRepository citaRepository,
            DisponibilidadMedicoRepository disponibilidadMedicoRepository,
            HistorialReagendamientoRepository historialReagendamientoRepository,
            DbiiSincronizacionService dbiiSincronizacionService
    ) {
        this.gestionarMedicosPort = gestionarMedicosPort;
        this.gestionarUsuariosPort = gestionarUsuariosPort;
        this.codificarPasswordPort = codificarPasswordPort;
        this.registrarUsuarioKeycloakPort = registrarUsuarioKeycloakPort;
        this.medicoRepository = medicoRepository;
        this.citaRepository = citaRepository;
        this.disponibilidadMedicoRepository = disponibilidadMedicoRepository;
        this.historialReagendamientoRepository = historialReagendamientoRepository;
        this.dbiiSincronizacionService = dbiiSincronizacionService;
    }

    @Override
    public List<Medico> listarTodos() {
        return gestionarMedicosPort.listarTodos();
    }

    @Override
    @Transactional
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

        registrarUsuarioKeycloakPort.registrarUsuario(
                username,
                password,
                UserRole.MEDICO.name()
        );

        User user = new User();
        user.setUsername(username);
        user.setPassword(codificarPasswordPort.codificar(password));
        user.setRole(UserRole.MEDICO);
        user.setStatus(UserStatus.ACTIVE);

        User usuarioGuardado = gestionarUsuariosPort.guardar(user);

        dbiiSincronizacionService.sincronizarUsuarioSistema(
                usuarioGuardado.getUsername(),
                usuarioGuardado.getPassword(),
                usuarioGuardado.getRole()
        );

        Medico medico = new Medico();
        medico.setNombreCompleto(nombreCompleto);
        medico.setEspecialidad(especialidad);
        medico.setIntervaloMinutos(
                request.getIntervaloMinutos() != null
                        ? request.getIntervaloMinutos()
                        : 15
        );
        medico.setUser(usuarioGuardado);

        Medico medicoGuardado = gestionarMedicosPort.guardar(medico);

        dbiiSincronizacionService.sincronizarMedicoTerapeuta(medicoGuardado);

        return medicoGuardado;
    }

    @Override
    public Medico obtenerPorId(Long medicoId) {
        if (medicoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El id del medico es obligatorio."
            );
        }

        return gestionarMedicosPort.buscarPorId(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un medico/terapista con id: " + medicoId
                ));
    }

    @Override
    @Transactional
    public Medico actualizarMedico(Long medicoId, MedicoRequest request) {
        if (medicoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El id del medico es obligatorio."
            );
        }

        validarSolicitudActualizacion(request);

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un medico/terapista con id: " + medicoId
                ));

        medico.setNombreCompleto(request.getNombreCompleto().trim());
        medico.setEspecialidad(request.getEspecialidad().trim());
        medico.setIntervaloMinutos(request.getIntervaloMinutos());

        Medico medicoActualizado = medicoRepository.save(medico);

        dbiiSincronizacionService.sincronizarMedicoTerapeuta(medicoActualizado);

        return medicoActualizado;
    }

    @Override
    @Transactional
    public void eliminarMedico(Long medicoId) {
        if (medicoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El id del medico es obligatorio."
            );
        }

        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un medico/terapista con id: " + medicoId
                ));

        long cantidadCitas = citaRepository.countByMedicoId(medicoId);

        dbiiSincronizacionService.desactivarMedicoTerapeuta(medico);

        historialReagendamientoRepository.deleteByMedicoId(medicoId);
        citaRepository.deleteByMedicoId(medicoId);
        disponibilidadMedicoRepository.deleteByMedicoId(medicoId);
        medicoRepository.delete(medico);

        System.out.println("AGENDA-SERVICE -> Medico eliminado por administrador. ID: " + medicoId
                + ". Citas eliminadas asociadas: " + cantidadCitas);
    }

    private void validarSolicitudCreacion(MedicoRequest request) {
        validarSolicitudBase(request);

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El username del medico es obligatorio."
            );
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La password del medico es obligatoria."
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
                    "La solicitud no puede estar vacia."
            );
        }

        if (request.getNombreCompleto() == null || request.getNombreCompleto().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre completo del medico es obligatorio."
            );
        }

        if (request.getEspecialidad() == null || request.getEspecialidad().trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La especialidad del medico es obligatoria."
            );
        }

        if (request.getIntervaloMinutos() == null || request.getIntervaloMinutos() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El intervalo de atencion debe ser mayor que cero."
            );
        }
    }
}