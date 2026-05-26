package co.edu.unicauca.piedraazul.agenda.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import co.edu.unicauca.piedraazul.agenda.model.HistorialReagendamiento;
import jakarta.transaction.Transactional;

public interface HistorialReagendamientoRepository extends JpaRepository<HistorialReagendamiento, Long> {

    List<HistorialReagendamiento> findByCitaIdOrderByFechaCambioDesc(Long citaId);

    @Modifying
    @Transactional
    @Query("DELETE FROM HistorialReagendamiento h WHERE h.cita.medico.id = :medicoId")
    void deleteByMedicoId(Long medicoId);
}