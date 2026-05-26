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
                .filter(medico -> medico != null)
                .toList();
    }

    @Override
    public Optional<Medico> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return listarTodos()
                .stream()
                .filter(medico -> medico.getId() != null)
                .filter(medico -> medico.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Medico> buscarPorUsernameUsuario(String username) {
        String usernameBuscado = normalizarUsername(username);

        if (usernameBuscado.isEmpty()) {
            return Optional.empty();
        }

        List<Medico> medicos = listarTodos();

        Optional<Medico> encontrado = medicos.stream()
                .filter(medico -> medico.getUser() != null)
                .filter(medico -> normalizarUsername(medico.getUser().getUsername())
                        .equalsIgnoreCase(usernameBuscado))
                .findFirst();

        if (encontrado.isPresent()) {
            return encontrado;
        }

        /*
         * Fallback defensivo:
         * Si por alguna razón el username llega con diferencia de formato
         * desde Keycloak o desde sesión, se compara ignorando mayúsculas,
         * espacios y dejando trazabilidad en consola.
         */
        System.out.println("MEDICO-SERVICE -> No se encontró médico para username: " + usernameBuscado);
        System.out.println("MEDICO-SERVICE -> Médicos recibidos desde agenda-service:");

        for (Medico medico : medicos) {
            String usernameMedico = medico.getUser() != null
                    ? medico.getUser().getUsername()
                    : "";

            System.out.println(" - ID: " + medico.getId()
                    + " | Médico: " + medico.getNombreCompleto()
                    + " | Username: " + usernameMedico);
        }

        return Optional.empty();
    }

    @Override
    public Medico registrarMedico(String nombreCompleto,
                                  String especialidad,
                                  Integer intervaloMinutos,
                                  String username,
                                  String password) {

        String usernameNormalizado = normalizarUsername(username);

        User user = new User();
        user.setUsername(usernameNormalizado);
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
                usernameNormalizado,
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

        String username = normalizarUsername(response.getUsername());

        if (!username.isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setRole(UserRole.MEDICO);
            user.setStatus(UserStatus.ACTIVE);
            medico.setUser(user);
        }

        return medico;
    }

    private String normalizarUsername(String username) {
        if (username == null) {
            return "";
        }

        return username.trim();
    }
}