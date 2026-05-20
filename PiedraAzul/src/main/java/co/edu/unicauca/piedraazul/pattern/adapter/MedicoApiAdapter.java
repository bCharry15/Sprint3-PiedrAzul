package co.edu.unicauca.piedraazul.pattern.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import co.edu.unicauca.piedraazul.model.Medico;
import co.edu.unicauca.piedraazul.model.dto.MedicoResponse;

@Component
public class MedicoApiAdapter {

    public MedicoResponse adaptarAMedicoResponse(Medico medico) {
        return new MedicoResponse(
                medico.getId(),
                medico.getNombreCompleto(),
                medico.getEspecialidad(),
                medico.getIntervaloMinutos()
        );
    }

    public List<MedicoResponse> adaptarListaAMedicoResponse(List<Medico> medicos) {
        return medicos.stream()
                .map(this::adaptarAMedicoResponse)
                .toList();
    }
}