INSERT INTO ROL VALUES (1, 'ADMINISTRADOR', 'Administra todo el sistema', 'A');
INSERT INTO ROL VALUES (2, 'AGENDADOR', 'Gestiona citas y pacientes', 'A');
INSERT INTO ROL VALUES (3, 'MEDICO', 'Atiende pacientes y registra historia clínica', 'A');

INSERT INTO USUARIO_SISTEMA VALUES (1, 'admin1', 'admin123', 'Laura Gómez', 'laura@piedrazul.com', 'A', 1);
INSERT INTO USUARIO_SISTEMA VALUES (2, 'agenda1', 'agenda123', 'Carlos Pérez', 'carlos@piedrazul.com', 'A', 2);
INSERT INTO USUARIO_SISTEMA VALUES (3, 'medico1', 'medico123', 'Dra. Ana Torres', 'ana@piedrazul.com', 'A', 3);
INSERT INTO USUARIO_SISTEMA VALUES (4, 'medico2', 'medico456', 'Dr. Luis Ramírez', 'luis@piedrazul.com', 'A', 3);

INSERT INTO PACIENTE VALUES (1, 'CC', '1001001001', 'Juliana', 'López', TO_DATE('1998-05-10','YYYY-MM-DD'), '3101112233', 'Popayán', 'juliana@gmail.com', 'A');
INSERT INTO PACIENTE VALUES (2, 'CC', '1001001002', 'Mateo', 'Fernández', TO_DATE('1985-08-21','YYYY-MM-DD'), '3101112244', 'Popayán', 'mateo@gmail.com', 'A');
INSERT INTO PACIENTE VALUES (3, 'TI', '1001001003', 'Sara', 'Muñoz', TO_DATE('2007-03-17','YYYY-MM-DD'), '3101112255', 'Timbío', NULL, 'A');
INSERT INTO PACIENTE VALUES (4, 'CC', '1001001004', 'Juan', 'Casas', TO_DATE('1990-12-01','YYYY-MM-DD'), '3101112266', 'Popayán', 'juan@gmail.com', 'A');

INSERT INTO ESPECIALIDAD VALUES (1, 'TERAPIA NEURAL', 'Tratamiento alternativo', 'A');
INSERT INTO ESPECIALIDAD VALUES (2, 'QUIROPRAXIA', 'Tratamiento quiropráctico', 'A');
INSERT INTO ESPECIALIDAD VALUES (3, 'FISIOTERAPIA', 'Rehabilitación física', 'A');

INSERT INTO MEDICO_TERAPISTA VALUES (1, 'CC', '900001', 'Ana', 'Torres', 'MEDICO', 30, '3120001111', 'ana@piedrazul.com', 'A');
INSERT INTO MEDICO_TERAPISTA VALUES (2, 'CC', '900002', 'Luis', 'Ramírez', 'TERAPISTA', 45, '3120002222', 'luis@piedrazul.com', 'A');
INSERT INTO MEDICO_TERAPISTA VALUES (3, 'CC', '900003', 'Marta', 'Rojas', 'FISIOTERAPISTA', 60, '3120003333', 'marta@piedrazul.com', 'I');

INSERT INTO MEDICO_ESPECIALIDAD VALUES (1, 1, SYSDATE);
INSERT INTO MEDICO_ESPECIALIDAD VALUES (1, 2, SYSDATE);
INSERT INTO MEDICO_ESPECIALIDAD VALUES (2, 2, SYSDATE);
INSERT INTO MEDICO_ESPECIALIDAD VALUES (3, 3, SYSDATE);

INSERT INTO CITA VALUES (1, 1, 1, TO_DATE('2026-04-20','YYYY-MM-DD'), '08:00', 'ATENDIDA', 'Dolor lumbar', 'Primera consulta', SYSDATE);
INSERT INTO CITA VALUES (2, 2, 1, TO_DATE('2026-04-20','YYYY-MM-DD'), '09:00', 'PROGRAMADA', 'Control', NULL, SYSDATE);
INSERT INTO CITA VALUES (3, 3, 2, TO_DATE('2026-04-21','YYYY-MM-DD'), '10:00', 'REPROGRAMADA', 'Dolor cervical', 'Se movió por solicitud', SYSDATE);
INSERT INTO CITA VALUES (4, 4, 2, TO_DATE('2026-04-21','YYYY-MM-DD'), '11:00', 'CANCELADA', 'Chequeo general', NULL, SYSDATE);
INSERT INTO CITA VALUES (5, 1, 2, TO_DATE('2026-04-22','YYYY-MM-DD'), '14:00', 'PROGRAMADA', 'Terapia de control', NULL, SYSDATE);
INSERT INTO CITA VALUES (6, 2, 1, TO_DATE('2026-04-23','YYYY-MM-DD'), '15:00', 'ATENDIDA', 'Revisión post tratamiento', 'Paciente evoluciona bien', SYSDATE);

INSERT INTO HISTORIA_CLINICA VALUES (1, 1, SYSDATE, 'Lumbalgia leve', 'Terapia neural inicial', 'Buena respuesta al tratamiento', 3);
INSERT INTO HISTORIA_CLINICA VALUES (2, 6, SYSDATE, 'Mejoría clínica', 'Control general', 'Continuar seguimiento', 3);

INSERT INTO MEDICAMENTO VALUES (1, 'IBUPROFENO', 'Tabletas 400 mg', 'A');
INSERT INTO MEDICAMENTO VALUES (2, 'ACETAMINOFEN', 'Tabletas 500 mg', 'A');
INSERT INTO MEDICAMENTO VALUES (3, 'DICLOFENACO', 'Gel tópico', 'A');
INSERT INTO MEDICAMENTO VALUES (4, 'COMPLEJO B', 'Ampollas', 'A');

INSERT INTO HISTORIA_MEDICAMENTO VALUES (1, 1, '1 tableta', 'Cada 8 horas', 5);
INSERT INTO HISTORIA_MEDICAMENTO VALUES (1, 3, 'Aplicar', 'Cada 12 horas', 7);
INSERT INTO HISTORIA_MEDICAMENTO VALUES (2, 2, '1 tableta', 'Cada 6 horas', 3);
INSERT INTO HISTORIA_MEDICAMENTO VALUES (2, 4, '1 ampolla', 'Cada semana', 14);

INSERT INTO AUDITORIA VALUES (1, 1, SYSDATE, 'CREAR_USUARIO', 'USUARIO_SISTEMA', 'Se creó el usuario agenda1');
INSERT INTO AUDITORIA VALUES (2, 2, SYSDATE, 'CREAR_CITA', 'CITA', 'Se registró la cita 2');
INSERT INTO AUDITORIA VALUES (3, 2, SYSDATE, 'REAGENDAR_CITA', 'REAGENDAMIENTO', 'Se reprogramó la cita 3');
INSERT INTO AUDITORIA VALUES (4, 3, SYSDATE, 'REGISTRAR_HISTORIA', 'HISTORIA_CLINICA', 'Se registró la historia clínica 1');

INSERT INTO REAGENDAMIENTO VALUES (
    1, 3, 2,
    TO_DATE('2026-04-19','YYYY-MM-DD'), '10:00',
    TO_DATE('2026-04-21','YYYY-MM-DD'), '10:00',
    'Paciente no podía asistir',
    SYSDATE
);

INSERT INTO REAGENDAMIENTO VALUES (
    2, 5, 2,
    TO_DATE('2026-04-22','YYYY-MM-DD'), '13:00',
    TO_DATE('2026-04-22','YYYY-MM-DD'), '14:00',
    'Ajuste de agenda',
    SYSDATE
);

COMMIT;
