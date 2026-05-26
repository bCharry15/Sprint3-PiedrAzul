package co.edu.unicauca.piedraazul.agenda.domain.port.in;

import java.util.List;

import co.edu.unicauca.piedraazul.agenda.model.dto.CitaResponse;

public interface ConsultarCitasPacienteUseCase {

    List<CitaResponse> consultarPorNumeroDocumento(String numeroDocumento);
}