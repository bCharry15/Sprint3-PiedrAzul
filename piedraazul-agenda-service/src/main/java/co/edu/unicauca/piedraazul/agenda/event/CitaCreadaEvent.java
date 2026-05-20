package co.edu.unicauca.piedraazul.agenda.event;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaCreadaEvent {

    private final Long citaId;
    private final Long pacienteId;
    private final String paciente;
    private final String correoPaciente;
    private final String celularPaciente;
    private final Long medicoId;
    private final String medico;
    private final LocalDate fecha;
    private final LocalTime hora;

    public CitaCreadaEvent(Long citaId,
                           Long pacienteId,
                           String paciente,
                           String correoPaciente,
                           String celularPaciente,
                           Long medicoId,
                           String medico,
                           LocalDate fecha,
                           LocalTime hora) {
        this.citaId = citaId;
        this.pacienteId = pacienteId;
        this.paciente = paciente;
        this.correoPaciente = correoPaciente;
        this.celularPaciente = celularPaciente;
        this.medicoId = medicoId;
        this.medico = medico;
        this.fecha = fecha;
        this.hora = hora;
    }

    public Long getCitaId() {
        return citaId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public String getPaciente() {
        return paciente;
    }

    public String getCorreoPaciente() {
        return correoPaciente;
    }

    public String getCelularPaciente() {
        return celularPaciente;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public String getMedico() {
        return medico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }
}