package co.edu.unicauca.piedraazul.agenda.infrastructure.adapter.in.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unicauca.piedraazul.agenda.application.usecase.DbiiOracleService;

@RestController
public class DbiiRestController {

    private final DbiiOracleService dbiiOracleService;

    public DbiiRestController(DbiiOracleService dbiiOracleService) {
        this.dbiiOracleService = dbiiOracleService;
    }

    @GetMapping("/api/dbii/health")
    public Map<String, String> health() {
        return Map.of(
                "estado", "OK",
                "mensaje", "Modulo DBII conectado a Oracle correctamente."
        );
    }

    @GetMapping("/api/dbii/diccionario/tablas")
    public List<Map<String, Object>> listarTablasBdii() {
        return dbiiOracleService.listarTablasBdii();
    }

    @GetMapping("/api/dbii/diccionario/procedimientos")
    public List<Map<String, Object>> listarProcedimientosPaquetes() {
        return dbiiOracleService.listarProcedimientosPaquetes();
    }

    @GetMapping("/api/dbii/objetos")
    public List<Map<String, Object>> listarObjetosOracle() {
        return dbiiOracleService.listarObjetosOracle();
    }

    @GetMapping("/api/dbii/resumen-citas")
    public List<Map<String, Object>> listarResumenCitasProfesional() {
        return dbiiOracleService.listarResumenCitasProfesional();
    }

    @GetMapping("/api/dbii/citas-por-estado")
    public List<Map<String, Object>> listarCitasPorEstado() {
        return dbiiOracleService.listarCitasPorEstado();
    }

    @GetMapping("/api/dbii/pacientes/documento/{numDocumento}")
    public Map<String, Object> buscarPacientePorDocumento(@PathVariable String numDocumento) {
        return dbiiOracleService.buscarPacientePorDocumento(numDocumento);
    }

    @GetMapping("/api/dbii/citas/documento/{numDocumento}")
    public Map<String, Object> buscarCitasPorDocumento(@PathVariable String numDocumento) {
        return dbiiOracleService.buscarCitasPorDocumento(numDocumento);
    }

    @GetMapping("/api/dbii/auditoria/documento/{numDocumento}")
    public Map<String, Object> buscarAuditoriaPorDocumento(@PathVariable String numDocumento) {
        return dbiiOracleService.buscarAuditoriaPorDocumento(numDocumento);
    }

    @GetMapping("/api/dbii/evidencia/documento/{numDocumento}")
    public Map<String, Object> buscarEvidenciaCompletaPorDocumento(@PathVariable String numDocumento) {
        return dbiiOracleService.buscarEvidenciaCompletaPorDocumento(numDocumento);
    }

    @PostMapping("/api/dbii/procedimientos/pacientes")
    public Map<String, Object> insertarPacienteConProcedimiento(@RequestBody Map<String, String> body) {
        return dbiiOracleService.insertarPacienteConProcedimiento(body);
    }
}