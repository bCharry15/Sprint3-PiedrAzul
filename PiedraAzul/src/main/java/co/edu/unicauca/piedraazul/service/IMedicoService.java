package co.edu.unicauca.piedraazul.service;

import co.edu.unicauca.piedraazul.model.Medico;

import java.util.List;
import java.util.Optional;

public interface IMedicoService {

    /**
     * Retorna todos los médicos registrados.
     */
    List<Medico> listarTodos();

    /**
     * Busca un médico por su ID.
     */
    Optional<Medico> buscarPorId(Long id);

    /**
     * Busca el médico asociado a un username de usuario.
     */
    Optional<Medico> buscarPorUsernameUsuario(String username);

    /**
     * Registra un nuevo médico junto con su usuario asociado.
     * Lanza IllegalArgumentException si el username ya existe.
     */
    Medico registrarMedico(String nombreCompleto, String especialidad,
                           Integer intervaloMinutos, String username, String password);

    /**
     * Persiste los cambios de un médico existente.
     */
    Medico guardar(Medico medico);
}
