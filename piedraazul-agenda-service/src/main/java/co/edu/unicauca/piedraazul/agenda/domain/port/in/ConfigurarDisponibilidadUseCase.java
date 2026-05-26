package co.edu.unicauca.piedraazul.agenda.domain.port.in;

import java.util.List;

import co.edu.unicauca.piedraazul.agenda.model.DisponibilidadMedico;
import co.edu.unicauca.piedraazul.agenda.model.dto.ConfiguracionDisponibilidadRequest;

public interface ConfigurarDisponibilidadUseCase {

    DisponibilidadMedico configurar(ConfiguracionDisponibilidadRequest request);

    List<DisponibilidadMedico> listarPorMedico(Long medicoId);
}