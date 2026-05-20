package co.edu.unicauca.piedraazul.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.client.AgendaServiceClient;
import co.edu.unicauca.piedraazul.model.Medico;
import co.edu.unicauca.piedraazul.model.User;
import co.edu.unicauca.piedraazul.model.dto.CrearMedicoRequest;
import co.edu.unicauca.piedraazul.model.dto.MedicoResponse;
import co.edu.unicauca.piedraazul.model.enums.UserRole;
import co.edu.unicauca.piedraazul.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.service.IMedicoService;
import co.edu.unicauca.piedraazul.service.IUserService;

@Service
public class MedicoServiceImpl implements IMedicoService {

    private final AgendaServiceClient agendaServiceClient;
    private final IUserService userService;

    public MedicoServiceImpl(AgendaServiceClient agendaServiceClient,
                             IUserService userService) {
        this.agendaServiceClient = agendaServiceClient;
        this.userService = userService;
    }

    @Override
    public List<Medico> listarTodos() {
        MedicoResponse[] response = agendaServiceClient.listarMedicos();

        if (response == null) {
            return List.of();
        }

        return Arrays.stream(response)
                .map(this::convertirAMedico)
                .toList();
    }

    @Override
    public Optional<Medico> buscarPorId(Long id) {
        return listarTodos()
                .stream()
                .filter(medico -> medico.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Medico> buscarPorUsernameUsuario(String username) {
        if (username == null) {
            return Optional.empty();
        }

        return listarTodos()
                .stream()
                .filter(medico -> medico.getUser() != null)
                .filter(medico -> username.equals(medico.getUser().getUsername()))
                .findFirst();
    }

    @Override
    public Medico registrarMedico(String nombreCompleto, String especialidad,
                                  Integer intervaloMinutos, String username,
                                  String password) {

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(UserRole.MEDICO);
        user.setStatus(UserStatus.ACTIVE);

        boolean usuarioRegistrado = userService.registerUser(user);

        if (!usuarioRegistrado) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }

        CrearMedicoRequest request = new CrearMedicoRequest(
                nombreCompleto,
                especialidad,
                intervaloMinutos,
                username,
                password
        );

        MedicoResponse response = agendaServiceClient.crearMedico(request);

        return convertirAMedico(response);
    }

    @Override
    public Medico guardar(Medico medico) {
        return medico;
    }

    private Medico convertirAMedico(MedicoResponse response) {
        if (response == null) {
            return null;
        }

        Medico medico = new Medico();
        medico.setId(response.getId());
        medico.setNombreCompleto(response.getNombreCompleto());
        medico.setEspecialidad(response.getEspecialidad());
        medico.setIntervaloMinutos(response.getIntervaloMinutos());

        if (response.getUsername() != null) {
            User user = new User();
            user.setUsername(response.getUsername());
            user.setRole(UserRole.MEDICO);
            user.setStatus(UserStatus.ACTIVE);
            medico.setUser(user);
        }

        return medico;
    }
}