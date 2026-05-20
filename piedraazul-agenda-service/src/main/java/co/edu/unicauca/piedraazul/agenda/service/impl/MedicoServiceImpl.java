package co.edu.unicauca.piedraazul.agenda.service.impl;

import co.edu.unicauca.piedraazul.agenda.model.Medico;
import co.edu.unicauca.piedraazul.agenda.model.User;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;
import co.edu.unicauca.piedraazul.agenda.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.agenda.repository.MedicoRepository;
import co.edu.unicauca.piedraazul.agenda.repository.UserRepository;
import co.edu.unicauca.piedraazul.agenda.service.IMedicoService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedicoServiceImpl implements IMedicoService {

    private final MedicoRepository medicoRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public MedicoServiceImpl(MedicoRepository medicoRepository,
                              UserRepository userRepository,
                              BCryptPasswordEncoder passwordEncoder) {
        this.medicoRepository = medicoRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Medico> listarTodos() {
        return medicoRepository.findAll();
    }

    @Override
    public Optional<Medico> buscarPorId(Long id) {
        return medicoRepository.findById(id);
    }

    @Override
    public Optional<Medico> buscarPorUsernameUsuario(String username) {
        return medicoRepository.findByUserUsername(username);
    }

    @Override
    public Medico registrarMedico(String nombreCompleto, String especialidad,
                                  Integer intervaloMinutos, String username,
                                  String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("El nombre de usuario ya existe.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.MEDICO);
        user.setStatus(UserStatus.ACTIVE);

        User userGuardado = userRepository.save(user);

        Medico medico = new Medico();
        medico.setNombreCompleto(nombreCompleto);
        medico.setEspecialidad(especialidad);
        medico.setIntervaloMinutos(intervaloMinutos);
        medico.setUser(userGuardado);

        return medicoRepository.save(medico);
    }

    @Override
    public Medico guardar(Medico medico) {
        return medicoRepository.save(medico);
    }
}

