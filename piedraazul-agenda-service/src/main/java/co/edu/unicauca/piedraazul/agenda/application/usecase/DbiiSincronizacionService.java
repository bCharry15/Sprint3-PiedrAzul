package co.edu.unicauca.piedraazul.agenda.application.usecase;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import co.edu.unicauca.piedraazul.agenda.model.enums.UserRole;

@Service
public class DbiiSincronizacionService {

    private final JdbcTemplate jdbcTemplate;

    public DbiiSincronizacionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void sincronizarUsuarioSistema(String username, String clave, UserRole role) {
        validarTexto(username, "El username es obligatorio para sincronizar con USUARIO_SISTEMA.");
        validarTexto(clave, "La clave es obligatoria para sincronizar con USUARIO_SISTEMA.");

        if (role == null) {
            throw new IllegalArgumentException("El rol es obligatorio para sincronizar con USUARIO_SISTEMA.");
        }

        asegurarRolesBase();

        String usernameNormalizado = username.trim();
        String claveNormalizada = limitarTexto(clave.trim(), 100);
        int idRol = obtenerIdRolDbii(role);

        Long cantidadExistente = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM USUARIO_SISTEMA
                WHERE LOWER(NOMBRE_USUARIO) = LOWER(?)
                """,
                Long.class,
                usernameNormalizado
        );

        Long idUsuario;

        if (cantidadExistente != null && cantidadExistente > 0) {
            idUsuario = jdbcTemplate.queryForObject(
                    """
                    SELECT ID_USUARIO
                    FROM USUARIO_SISTEMA
                    WHERE LOWER(NOMBRE_USUARIO) = LOWER(?)
                    FETCH FIRST 1 ROWS ONLY
                    """,
                    Long.class,
                    usernameNormalizado
            );

            jdbcTemplate.update(
                    """
                    UPDATE USUARIO_SISTEMA
                    SET CLAVE = ?,
                        NOMBRE_COMPLETO = ?,
                        EMAIL = ?,
                        ESTADO = 'A',
                        ID_ROL = ?
                    WHERE ID_USUARIO = ?
                    """,
                    claveNormalizada,
                    construirNombreCompleto(usernameNormalizado, role),
                    construirEmailAcademico(usernameNormalizado),
                    idRol,
                    idUsuario
            );

            registrarAuditoriaUsuario(
                    idUsuario,
                    "SINCRONIZAR_USUARIO",
                    "Usuario actualizado desde la aplicación principal: " + usernameNormalizado
            );

        } else {
            idUsuario = obtenerSiguienteIdUsuarioSistema();

            jdbcTemplate.update(
                    """
                    INSERT INTO USUARIO_SISTEMA (
                        ID_USUARIO,
                        NOMBRE_USUARIO,
                        CLAVE,
                        NOMBRE_COMPLETO,
                        EMAIL,
                        ESTADO,
                        ID_ROL
                    )
                    VALUES (?, ?, ?, ?, ?, 'A', ?)
                    """,
                    idUsuario,
                    usernameNormalizado,
                    claveNormalizada,
                    construirNombreCompleto(usernameNormalizado, role),
                    construirEmailAcademico(usernameNormalizado),
                    idRol
            );

            registrarAuditoriaUsuario(
                    idUsuario,
                    "CREAR_USUARIO",
                    "Usuario creado desde la aplicación principal: " + usernameNormalizado
            );
        }
    }

    private void asegurarRolesBase() {
        asegurarRol(1, "ADMINISTRADOR", "Administra todo el sistema");
        asegurarRol(2, "AGENDADOR", "Gestiona citas y pacientes");
        asegurarRol(3, "MEDICO", "Atiende pacientes y registra historia clínica");
        asegurarRol(4, "PACIENTE", "Paciente con acceso al agendamiento autónomo");
    }

    private void asegurarRol(int idRol, String nombre, String descripcion) {
        jdbcTemplate.update(
                """
                MERGE INTO ROL r
                USING (
                    SELECT ? AS ID_ROL,
                           ? AS NOMBRE,
                           ? AS DESCRIPCION
                    FROM DUAL
                ) datos
                ON (r.ID_ROL = datos.ID_ROL)
                WHEN MATCHED THEN
                    UPDATE SET
                        r.NOMBRE = datos.NOMBRE,
                        r.DESCRIPCION = datos.DESCRIPCION,
                        r.ESTADO = 'A'
                WHEN NOT MATCHED THEN
                    INSERT (
                        ID_ROL,
                        NOMBRE,
                        DESCRIPCION,
                        ESTADO
                    )
                    VALUES (
                        datos.ID_ROL,
                        datos.NOMBRE,
                        datos.DESCRIPCION,
                        'A'
                    )
                """,
                idRol,
                nombre,
                descripcion
        );
    }

    private int obtenerIdRolDbii(UserRole role) {
        return switch (role) {
            case ADMIN -> 1;
            case AGENDADOR -> 2;
            case MEDICO -> 3;
            case PACIENTE -> 4;
        };
    }

    private Long obtenerSiguienteIdUsuarioSistema() {
        Long siguienteId = jdbcTemplate.queryForObject(
                """
                SELECT NVL(MAX(ID_USUARIO), 0) + 1
                FROM USUARIO_SISTEMA
                """,
                Long.class
        );

        if (siguienteId == null) {
            return 1L;
        }

        return siguienteId;
    }

    private void registrarAuditoriaUsuario(Long idUsuario, String accion, String descripcion) {
        if (idUsuario == null) {
            return;
        }

        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO AUDITORIA (
                        ID_AUDITORIA,
                        ID_USUARIO,
                        FECHA_EVENTO,
                        ACCION,
                        TABLA_AFECTADA,
                        DESCRIPCION
                    )
                    VALUES (
                        SEQ_AUDITORIA.NEXTVAL,
                        ?,
                        SYSDATE,
                        ?,
                        'USUARIO_SISTEMA',
                        ?
                    )
                    """,
                    idUsuario,
                    accion,
                    limitarTexto(descripcion, 300)
            );
        } catch (DataAccessException ex) {
            System.out.println(
                    "DBII-SYNC -> No se pudo registrar auditoría de usuario: "
                            + ex.getMessage()
            );
        }
    }

    private String construirNombreCompleto(String username, UserRole role) {
        String rolLegible = switch (role) {
            case ADMIN -> "Administrador";
            case AGENDADOR -> "Agendador";
            case MEDICO -> "Medico";
            case PACIENTE -> "Paciente";
        };

        return limitarTexto(rolLegible + " " + username, 100);
    }

    private String construirEmailAcademico(String username) {
        String usuarioLimpio = username
                .toLowerCase()
                .replaceAll("[^a-z0-9._-]", "");

        if (usuarioLimpio.isBlank()) {
            usuarioLimpio = "usuario";
        }

        return limitarTexto(usuarioLimpio + "@piedraazul.local", 100);
    }

    private String limitarTexto(String valor, int longitudMaxima) {
        if (valor == null) {
            return "";
        }

        if (valor.length() <= longitudMaxima) {
            return valor;
        }

        return valor.substring(0, longitudMaxima);
    }

    private void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}