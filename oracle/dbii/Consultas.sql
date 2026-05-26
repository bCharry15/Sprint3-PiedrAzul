/*==============================================================
 SCRIPT: CONSULTAS_COMENTADAS_PIEDRAZUL.sql
 SISTEMA: Gestión de Citas Médicas Piedra Azul
 OBJETIVO: Consultas SQL comentadas para la Segunda Entrega
==============================================================*/


/*==============================================================
A. RESTRICCIONES / FILTROS
==============================================================*/


/*--------------------------------------------------------------
1. IGUALDAD (=)
Objetivo: Mostrar todas las citas que se encuentran programadas.
Tabla implicada: CITA
Concepto evaluado: WHERE con operador =
--------------------------------------------------------------*/
SELECT *
FROM CITA
WHERE ESTADO = 'PROGRAMADA';



/*--------------------------------------------------------------
2. DIFERENTE (<>)
Objetivo: Mostrar profesionales activos excluyendo los inactivos.
Tabla implicada: MEDICO_TERAPISTA
Concepto evaluado: WHERE con operador <>
--------------------------------------------------------------*/
SELECT *
FROM MEDICO_TERAPISTA
WHERE ESTADO <> 'I';



/*--------------------------------------------------------------
3. BETWEEN
Objetivo: Consultar citas registradas entre dos fechas.
Tabla implicada: CITA
Concepto evaluado: WHERE con BETWEEN
--------------------------------------------------------------*/
SELECT *
FROM CITA
WHERE FECHA_CITA BETWEEN TO_DATE('2026-04-20','YYYY-MM-DD')
                    AND TO_DATE('2026-04-22','YYYY-MM-DD');



/*--------------------------------------------------------------
4. LIKE
Objetivo: Buscar pacientes cuyo apellido inicia con la letra L.
Tabla implicada: PACIENTE
Concepto evaluado: WHERE con LIKE
--------------------------------------------------------------*/
SELECT *
FROM PACIENTE
WHERE APELLIDOS LIKE 'L%';



/*--------------------------------------------------------------
5. IN
Objetivo: Mostrar citas con estado programada o reprogramada.
Tabla implicada: CITA
Concepto evaluado: WHERE con IN
--------------------------------------------------------------*/
SELECT *
FROM CITA
WHERE ESTADO IN ('PROGRAMADA', 'REPROGRAMADA');



/*--------------------------------------------------------------
6. IS NULL
Objetivo: Mostrar pacientes que no tienen correo registrado.
Tabla implicada: PACIENTE
Concepto evaluado: WHERE con IS NULL
--------------------------------------------------------------*/
SELECT *
FROM PACIENTE
WHERE EMAIL IS NULL;



/*==============================================================
B. JOINS
==============================================================*/


/*--------------------------------------------------------------
1. INNER JOIN
Objetivo: Mostrar citas con nombre del paciente y profesional.
Tablas implicadas: CITA, PACIENTE, MEDICO_TERAPISTA
Concepto evaluado: INNER JOIN
--------------------------------------------------------------*/
SELECT C.ID_CITA,
       P.NOMBRES || ' ' || P.APELLIDOS AS PACIENTE,
       M.NOMBRES || ' ' || M.APELLIDOS AS PROFESIONAL,
       C.FECHA_CITA,
       C.HORA_CITA,
       C.ESTADO
FROM CITA C
INNER JOIN PACIENTE P
ON C.ID_PACIENTE = P.ID_PACIENTE
INNER JOIN MEDICO_TERAPISTA M
ON C.ID_MEDICO = M.ID_MEDICO;



/*--------------------------------------------------------------
2. LEFT JOIN
Objetivo: Mostrar todos los pacientes tengan o no citas.
Tablas implicadas: PACIENTE, CITA
Concepto evaluado: LEFT JOIN
--------------------------------------------------------------*/
SELECT P.ID_PACIENTE,
       P.NOMBRES,
       P.APELLIDOS,
       C.ID_CITA,
       C.ESTADO
FROM PACIENTE P
LEFT JOIN CITA C
ON P.ID_PACIENTE = C.ID_PACIENTE;



/*--------------------------------------------------------------
3. RIGHT JOIN
Objetivo: Mostrar todos los médicos tengan o no citas asignadas.
Tablas implicadas: CITA, MEDICO_TERAPISTA
Concepto evaluado: RIGHT JOIN
--------------------------------------------------------------*/
SELECT C.ID_CITA,
       C.ESTADO,
       M.NOMBRES,
       M.APELLIDOS
FROM CITA C
RIGHT JOIN MEDICO_TERAPISTA M
ON C.ID_MEDICO = M.ID_MEDICO;



/*--------------------------------------------------------------
4. CROSS JOIN
Objetivo: Combinar todas las especialidades con todos los roles.
Tablas implicadas: ESPECIALIDAD, ROL
Concepto evaluado: CROSS JOIN
--------------------------------------------------------------*/
SELECT E.NOMBRE AS ESPECIALIDAD,
       R.NOMBRE AS ROL
FROM ESPECIALIDAD E
CROSS JOIN ROL R;



/*==============================================================
C. AGRUPACIÓN CON GROUP BY Y HAVING
==============================================================*/


/*--------------------------------------------------------------
1. GROUP BY
Objetivo: Contar cantidad de citas por profesional.
Tablas implicadas: MEDICO_TERAPISTA, CITA
Concepto evaluado: GROUP BY
--------------------------------------------------------------*/
SELECT M.NOMBRES || ' ' || M.APELLIDOS AS PROFESIONAL,
       COUNT(C.ID_CITA) AS TOTAL_CITAS
FROM MEDICO_TERAPISTA M
LEFT JOIN CITA C
ON M.ID_MEDICO = C.ID_MEDICO
GROUP BY M.NOMBRES, M.APELLIDOS;



/*--------------------------------------------------------------
2. GROUP BY + HAVING
Objetivo: Mostrar especialidades con más de un profesional.
Tablas implicadas: ESPECIALIDAD, MEDICO_ESPECIALIDAD
Concepto evaluado: GROUP BY con HAVING
--------------------------------------------------------------*/
SELECT E.NOMBRE AS ESPECIALIDAD,
       COUNT(ME.ID_MEDICO) AS TOTAL_PROFESIONALES
FROM ESPECIALIDAD E
INNER JOIN MEDICO_ESPECIALIDAD ME
ON E.ID_ESPECIALIDAD = ME.ID_ESPECIALIDAD
GROUP BY E.NOMBRE
HAVING COUNT(ME.ID_MEDICO) > 1;



/*==============================================================
D. ORDER BY
==============================================================*/


/*--------------------------------------------------------------
1. ORDER BY SIMPLE
Objetivo: Ordenar pacientes por apellido ascendente.
Tabla implicada: PACIENTE
Concepto evaluado: ORDER BY simple
--------------------------------------------------------------*/
SELECT *
FROM PACIENTE
ORDER BY APELLIDOS ASC;



/*--------------------------------------------------------------
2. ORDER BY MÚLTIPLE
Objetivo: Ordenar citas por fecha y hora.
Tabla implicada: CITA
Concepto evaluado: ORDER BY múltiple
--------------------------------------------------------------*/
SELECT *
FROM CITA
ORDER BY FECHA_CITA ASC,
         HORA_CITA ASC;



/*--------------------------------------------------------------
3. ORDER BY CON FUNCIÓN DE GRUPO
Objetivo: Contar citas por estado y ordenar de mayor a menor.
Tabla implicada: CITA
Concepto evaluado: GROUP BY + ORDER BY
--------------------------------------------------------------*/
SELECT ESTADO,
       COUNT(*) AS TOTAL
FROM CITA
GROUP BY ESTADO
ORDER BY TOTAL DESC;



/*--------------------------------------------------------------
4. ORDER BY CON FUNCIÓN DE GRUPO
Objetivo: Contar citas por médico y ordenar de mayor a menor.
Tabla implicada: CITA
Concepto evaluado: GROUP BY + ORDER BY
--------------------------------------------------------------*/
SELECT ID_MEDICO,
       COUNT(*) AS TOTAL_CITAS
FROM CITA
GROUP BY ID_MEDICO
ORDER BY TOTAL_CITAS DESC;

/*==============================================================
E. SUBCONSULTAS
==============================================================*/

/* Subconsulta 1: Pacientes que tienen al menos una cita programada */
SELECT ID_PACIENTE, NOMBRES, APELLIDOS
FROM PACIENTE
WHERE ID_PACIENTE IN (
    SELECT ID_PACIENTE
    FROM CITA
    WHERE ESTADO = 'PROGRAMADA'
);

/* Subconsulta 2: Profesionales con más citas que el promedio del equipo */
SELECT ID_MEDICO, NOMBRES, APELLIDOS
FROM MEDICO_TERAPISTA
WHERE ID_MEDICO IN (
    SELECT ID_MEDICO
    FROM CITA
    GROUP BY ID_MEDICO
    HAVING COUNT(*) > (
        SELECT AVG(TOTAL)
        FROM (
            SELECT COUNT(*) AS TOTAL
            FROM CITA
            GROUP BY ID_MEDICO
        )CONTEO_POR_MEDICO 
    )
);

/*==============================================================
F. FULL OUTER JOIN
==============================================================*/

/* Muestra todos los pacientes y todas las citas,
   aunque no tengan correspondencia entre sí */
SELECT P.ID_PACIENTE,
       P.NOMBRES || ' ' || P.APELLIDOS AS PACIENTE,
       C.ID_CITA,
       C.ESTADO
FROM PACIENTE P
FULL OUTER JOIN CITA C
ON P.ID_PACIENTE = C.ID_PACIENTE
ORDER BY P.ID_PACIENTE NULLS LAST;