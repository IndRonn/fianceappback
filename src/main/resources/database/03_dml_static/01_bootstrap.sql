-- ######################################################################
-- # ARCHIVO: 03_dml_static/01_bootstrap.sql
-- # OBJETIVO: Carga de Datos Maestros / Inicialización del Sistema.
-- # CORRECCIÓN: Uso de INSERT individuales para garantizar NEXTVAL único.
-- ######################################################################

--SET SERVEROUTPUT ON;

DECLARE
    v_admin_id NUMBER;
    v_count    NUMBER;
BEGIN
    -- 1. Verificar si ya existe el Admin (Idempotencia en DML)
    SELECT COUNT(*) INTO v_count FROM USERS WHERE EMAIL = 'admin@financeapp.com';

    IF v_count = 0 THEN
        -- Obtener siguiente ID para el Admin
        v_admin_id := USER_ID_SEQ.NEXTVAL;

        -- 2. Crear Usuario Admin
        INSERT INTO USERS (ID, FIRST_NAME, LAST_NAME, EMAIL, USERNAME, PASSWORD_HASH, ROLE)
        VALUES (
                   v_admin_id,
                   'System',
                   'Administrator',
                   'admin@financeapp.com',
                   'admin@financeapp.com',
                   -- Hash BCrypt
                   '$2a$10$e879l9l9K9k16cYK9YEsLu3qk.IU47wNy8mMxqRJlRJ.VVmAzEZLm',
                   'ADMIN'
               );

        -- 3. Crear Categorías Base
        -- Usamos INSERTs separados para asegurar que la secuencia avance correctamente

        -- --- INGRESOS ---
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Salario', 'INGRESO', 'PLANIFICADO_MENSUAL');
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Inversiones', 'INGRESO', 'PLANIFICADO_MENSUAL');
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Regalos/Bonos', 'INGRESO', 'DIA_A_DIA');

        -- --- GASTOS FIJOS ---
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Alquiler/Hipoteca', 'GASTO', 'PLANIFICADO_MENSUAL');
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Servicios (Luz/Agua)', 'GASTO', 'PLANIFICADO_MENSUAL');
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Internet/Teléfono', 'GASTO', 'PLANIFICADO_MENSUAL');
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Seguros', 'GASTO', 'PLANIFICADO_MENSUAL');
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Educación', 'GASTO', 'PLANIFICADO_MENSUAL');

        -- --- GASTOS VARIABLES ---
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Supermercado', 'GASTO', 'DIA_A_DIA');
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Transporte', 'GASTO', 'DIA_A_DIA');
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Comida Fuera', 'GASTO', 'DIA_A_DIA');
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Ocio/Entretenimiento', 'GASTO', 'DIA_A_DIA');
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Salud/Farmacia', 'GASTO', 'DIA_A_DIA');
        INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE) VALUES (CAT_ID_SEQ.NEXTVAL, v_admin_id, 'Mascotas', 'GASTO', 'DIA_A_DIA');

        DBMS_OUTPUT.PUT_LINE('>> Sistema inicializado: Admin y Categorías Base creados.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('>> Bootstrap omitido: El Admin ya existe.');
    END IF;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Error en Bootstrap: ' || SQLERRM);
        RAISE;
END;
/

select * from USERS;