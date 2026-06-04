package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unicauca.piedraazul.agenda.domain.port.in.ConsultarHistorialReagendamientoUseCase;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarCitasPort;
import co.edu.unicauca.piedraazul.agenda.domain.port.out.BuscarHistorialReagendamientoPort;
import co.edu.unicauca.piedraazul.agenda.model.HistorialReagendamiento;

@Service
public class ConsultarHistorialReagendamientoService implements ConsultarHistorialReagendamientoUseCase {

    private final BuscarCitasPort buscarCitasPort;
    private final BuscarHistorialReagendamientoPort buscarHistorialReagendamientoPort;

    public ConsultarHistorialReagendamientoService(BuscarCitasPort buscarCitasPort,
                                                   BuscarHistorialReagendamientoPort buscarHistorialReagendamientoPort) {
        this.buscarCitasPort = buscarCitasPort;
        this.buscarHistorialReagendamientoPort = buscarHistorialReagendamientoPort;
    }

    @Override
    public List<Map<String, Object>> consultarPorCitaId(Long citaId) {
        if (citaId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El id de la cita es obligatorio."
            );
        }

        buscarCitasPort.buscarPorId(citaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe una cita con id: " + citaId
                ));

        return buscarHistorialReagendamientoPort.buscarPorCitaId(citaId)
                .stream()
                .map(this::convertirHistorialAMap)
                .toList();
    }

    private Map<String, Object> convertirHistorialAMap(HistorialReagendamiento historial) {
        Map<String, Object> response = new HashMap<>();

        response.put("id", historial.getId());
        response.put("citaId", historial.getCita().getId());
        response.put("fechaAnterior", historial.getFechaAnterior());
        response.put("horaAnterior", historial.getHoraAnterior());
        response.put("fechaNueva", historial.getFechaNueva());
        response.put("horaNueva", historial.getHoraNueva());
        response.put("responsable", historial.getResponsable());
        response.put("motivo", historial.getMotivo());
        response.put("fechaCambio", historial.getFechaCambio());

        return response;
    }
}