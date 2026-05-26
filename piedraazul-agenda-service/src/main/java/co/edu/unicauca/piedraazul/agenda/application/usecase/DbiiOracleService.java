package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DbiiOracleService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DbiiOracleService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    public List<Map<String, Object>> listarTablasBdii() {
        String sql = """
                SELECT TABLE_NAME
                FROM USER_TABLES
                WHERE TABLE_NAME IN (
                    'ROL',
                    'USUARIO_SISTEMA',
                    'PACIENTE',
                    'MEDICO_TERAPISTA',
                    'ESPECIALIDAD',
                    'MEDICO_ESPECIALIDAD',
                    'CITA',
                    'HISTORIA_CLINICA',
                    'MEDICAMENTO',
                    'HISTORIA_MEDICAMENTO',
                    'AUDITORIA',
                    'REAGENDAMIENTO'
                )
                ORDER BY TABLE_NAME
                """;

        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> listarProcedimientosPaquetes() {
        String sql = """
                SELECT
                    OBJECT_NAME AS PAQUETE,
                    PROCEDURE_NAME AS PROCEDIMIENTO
                FROM USER_PROCEDURES
                WHERE OBJECT_NAME LIKE 'PKG_GESTION_%'
                  AND PROCEDURE_NAME IS NOT NULL
                ORDER BY OBJECT_NAME, PROCEDURE_NAME
                """;

        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> listarObjetosOracle() {
        String sql = """
                SELECT
                    OBJECT_NAME,
                    OBJECT_TYPE,
                    STATUS
                FROM USER_OBJECTS
                WHERE OBJECT_TYPE IN (
                    'PACKAGE',
                    'PACKAGE BODY',
                    'TRIGGER',
                    'VIEW',
                    'SEQUENCE',
                    'INDEX'
                )
                ORDER BY OBJECT_TYPE, OBJECT_NAME
                """;

        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> listarResumenCitasProfesional() {
        String sql = """
                SELECT
                    ID_MEDICO,
                    PROFESIONAL,
                    TIPO_PROFESIONAL,
                    TOTAL_CITAS,
                    CITAS_PROGRAMADAS,
                    CITAS_ATENDIDAS,
                    CITAS_CANCELADAS,
                    CITAS_REPROGRAMADAS
                FROM VW_RESUMEN_CITAS_PROFESIONAL
                ORDER BY PROFESIONAL
                """;

        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> listarCitasPorEstado() {
        String sql = """
                SELECT
                    ESTADO,
                    COUNT(*) AS TOTAL
                FROM CITA
                GROUP BY ESTADO
                ORDER BY ESTADO
                """;

        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> insertarPacienteConProcedimiento(Map<String, String> body) {
        String tipoDocumento = obtenerValor(body, "tipoDocumento", "CC");
        String numDocumento = obtenerValor(body, "numDocumento", generarDocumentoPrueba());
        String nombres = obtenerValor(body, "nombres", "Paciente");
        String apellidos = obtenerValor(body, "apellidos", "Prueba BDII");
        String fechaNacimiento = obtenerValor(body, "fechaNacimiento", "2000-01-01");
        String celular = obtenerValor(body, "celular", "3000000000");
        String direccion = obtenerValor(body, "direccion", "Direccion de prueba");
        String email = obtenerValor(body, "email", "paciente.bdii@test.com");

        Long idGenerado = ejecutarProcedimientoInsertarPaciente(
                tipoDocumento,
                numDocumento,
                nombres,
                apellidos,
                fechaNacimiento,
                celular,
                direccion,
                email
        );

        return Map.of(
                "mensaje", "Paciente creado usando procedimiento almacenado Oracle.",
                "procedimiento", "PKG_GESTION_PACIENTES.INSERTAR_PACIENTE",
                "idGenerado", idGenerado,
                "numDocumento", numDocumento
        );
    }

    private Long ejecutarProcedimientoInsertarPaciente(
            String tipoDocumento,
            String numDocumento,
            String nombres,
            String apellidos,
            String fechaNacimiento,
            String celular,
            String direccion,
            String email
    ) {
        String llamada = "{call PKG_GESTION_PACIENTES.INSERTAR_PACIENTE(?,?,?,?,?,?,?,?,?)}";

        try (
                Connection connection = dataSource.getConnection();
                CallableStatement cs = connection.prepareCall(llamada)
        ) {
            cs.setString(1, tipoDocumento);
            cs.setString(2, numDocumento);
            cs.setString(3, nombres);
            cs.setString(4, apellidos);
            cs.setDate(5, Date.valueOf(fechaNacimiento));
            cs.setString(6, celular);
            cs.setString(7, direccion);
            cs.setString(8, email);
            cs.registerOutParameter(9, Types.NUMERIC);

            cs.execute();

            Number id = (Number) cs.getObject(9);

            if (id == null) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "El procedimiento no retorno el ID del paciente."
                );
            }

            return id.longValue();

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento debe tener formato YYYY-MM-DD.",
                    ex
            );
        } catch (SQLException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error ejecutando el procedimiento PKG_GESTION_PACIENTES.INSERTAR_PACIENTE: " + ex.getMessage(),
                    ex
            );
        }
    }

    private String obtenerValor(Map<String, String> body, String llave, String valorPorDefecto) {
        if (body == null) {
            return valorPorDefecto;
        }

        String valor = body.get(llave);

        if (valor == null || valor.trim().isEmpty()) {
            return valorPorDefecto;
        }

        return valor.trim();
    }

    private String generarDocumentoPrueba() {
        return "BDII" + System.currentTimeMillis();
    }
}