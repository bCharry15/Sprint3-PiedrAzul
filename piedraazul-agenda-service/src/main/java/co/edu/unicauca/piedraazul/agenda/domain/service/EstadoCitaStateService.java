package co.edu.unicauca.piedraazul.agenda.domain.service;

import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.agenda.model.enums.EstadoCita;

@Service
public class EstadoCitaStateService {

    public EstadoCita obtenerEstadoSeguro(EstadoCita estadoCita) {
        if (estadoCita == null) {
            return EstadoCita.PROGRAMADA;
        }

        return estadoCita;
    }

    public boolean esEstadoActivo(EstadoCita estadoCita) {
        if (estadoCita == null) {
            return false;
        }

        return estadoCita == EstadoCita.PROGRAMADA
                || estadoCita == EstadoCita.CONFIRMADA
                || estadoCita == EstadoCita.PENDIENTE;
    }

    public boolean esEstadoFinal(EstadoCita estadoCita) {
        if (estadoCita == null) {
            return false;
        }

        return estadoCita == EstadoCita.ATENDIDA
                || estadoCita == EstadoCita.COMPLETADA
                || estadoCita == EstadoCita.CANCELADA
                || estadoCita == EstadoCita.NO_VINO;
    }
}