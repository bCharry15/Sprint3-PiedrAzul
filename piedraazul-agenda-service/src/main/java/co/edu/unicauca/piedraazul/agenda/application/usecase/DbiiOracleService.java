package co.edu.unicauca.piedraazul.agenda.application.usecase;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
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

    public Map<String, Object> buscarPacientePorDocumento(String numDocumento) {
        String documento = normalizarDocumento(numDocumento);

        String sql = """
                SELECT
                    ID_PACIENTE,
                    TIPO_DOCUMENTO,
                    NUM_DOCUMENTO,
                    NOMBRES,
                    APELLIDOS,
                    FECHA_NACIMIENTO,
                    CELULAR,
                    DIRECCION,
                    EMAIL,
                    ESTADO
                FROM PACIENTE
                WHERE NUM_DOCUMENTO = ?
                ORDER BY ID_PACIENTE DESC
                """;

        List<Map<String, Object>> pacientes = jdbcTemplate.queryForList(sql, documento);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("documentoConsultado", documento);
        respuesta.put("total", pacientes.size());
        respuesta.put("pacientes", pacientes);

        return respuesta;
    }

    public Map<String, Object> buscarCitasPorDocumento(String numDocumento) {
        String documento = normalizarDocumento(numDocumento);

        String sql = """
                SELECT
                    c.ID_CITA,
                    p.NUM_DOCUMENTO,
                    p.NOMBRES || ' ' || p.APELLIDOS AS PACIENTE,
                    m.NOMBRES || ' ' || m.APELLIDOS AS MEDICO,
                    m.TIPO_PROFESIONAL,
                    c.FECHA_CITA,
                    c.HORA_CITA,
                    c.ESTADO,
                    c.MOTIVO,
                    c.OBSERVACIONES
                FROM CITA c
                JOIN PACIENTE p ON p.ID_PACIENTE = c.ID_PACIENTE
                JOIN MEDICO_TERAPISTA m ON m.ID_MEDICO = c.ID_MEDICO
                WHERE p.NUM_DOCUMENTO = ?
                ORDER BY c.ID_CITA DESC
                """;

        List<Map<String, Object>> citas = jdbcTemplate.queryForList(sql, documento);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("documentoConsultado", documento);
        respuesta.put("total", citas.size());
        respuesta.put("citas", citas);

        return respuesta;
    }

    public Map<String, Object> buscarAuditoriaPorDocumento(String numDocumento) {
        String documento = normalizarDocumento(numDocumento);

        String sql = """
                SELECT
                    a.ID_AUDITORIA,
                    a.ID_USUARIO,
                    a.FECHA_EVENTO,
                    a.ACCION,
                    a.TABLA_AFECTADA,
                    a.DESCRIPCION
                FROM AUDITORIA a
                WHERE a.DESCRIPCION LIKE '%' || ? || '%'
                   OR EXISTS (
                        SELECT 1
                        FROM CITA c
                        JOIN PACIENTE p ON p.ID_PACIENTE = c.ID_PACIENTE
                        WHERE p.NUM_DOCUMENTO = ?
                          AND a.DESCRIPCION LIKE '%ID_CITA_DBII: ' || TO_CHAR(c.ID_CITA) || '%'
                   )
                ORDER BY a.ID_AUDITORIA DESC
                FETCH FIRST 20 ROWS ONLY
                """;

        List<Map<String, Object>> auditorias = jdbcTemplate.queryForList(sql, documento, documento);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("documentoConsultado", documento);
        respuesta.put("total", auditorias.size());
        respuesta.put("auditoria", auditorias);

        return respuesta;
    }

    public Map<String, Object> buscarEvidenciaCompletaPorDocumento(String numDocumento) {
        String documento = normalizarDocumento(numDocumento);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("documentoConsultado", documento);
        respuesta.put("paciente", buscarPacientePorDocumento(documento).get("pacientes"));
        respuesta.put("citas", buscarCitasPorDocumento(documento).get("citas"));
        respuesta.put("auditoria", buscarAuditoriaPorDocumento(documento).get("auditoria"));

        return respuesta;
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

    private String normalizarDocumento(String numDocumento) {
        if (numDocumento == null || numDocumento.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El numero de documento es obligatorio."
            );
        }

        return numDocumento.trim();
    }
}