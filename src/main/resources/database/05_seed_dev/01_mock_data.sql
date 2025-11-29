-- ######################################################################
-- # ARCHIVO: 05_seed_dev/01_mock_data.sql
-- # OBJETIVO: Poblado de datos masivos para Desarrollo y QA.
-- # LOGICA:
-- #    1. Crea un usuario 'tester' si no existe.
-- #    2. Le asigna cuentas y categorías.
-- #    3. Genera miles de transacciones aleatorias para probar rendimiento.
-- ######################################################################

--SET SERVEROUTPUT ON;

DECLARE
v_user_email VARCHAR2(100) := 'ronny@test.com'; -- Tu usuario de prueba
    v_user_id    NUMBER;
    v_acc_deb_id NUMBER;
    v_acc_cre_id NUMBER;
    v_cat_id     NUMBER;
    v_count      NUMBER;

    -- Configuración del Stress Test
    v_total_trx  NUMBER := 2500; -- Cantidad de transacciones a generar
BEGIN
    -- ==================================================================
    -- 1. CREAR USUARIO DE PRUEBA (Si no existe)
    -- ==================================================================
SELECT COUNT(*) INTO v_count FROM USERS WHERE EMAIL = v_user_email;

IF v_count = 0 THEN
        v_user_id := USER_ID_SEQ.NEXTVAL;

INSERT INTO USERS (ID, FIRST_NAME, LAST_NAME, EMAIL, USERNAME, PASSWORD_HASH, ROLE)
VALUES (
           v_user_id,
           'Ronny',
           'Tester',
           v_user_email,
           v_user_email,
           -- Hash para '12345678' (Para pruebas rápidas)
           '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOcd7.jRjD.a.',
           'USER'
       );

DBMS_OUTPUT.PUT_LINE('>> Usuario de prueba creado: ' || v_user_email);
ELSE
SELECT ID INTO v_user_id FROM USERS WHERE EMAIL = v_user_email;
DBMS_OUTPUT.PUT_LINE('>> Usuario de prueba ya existía. ID: ' || v_user_id);
END IF;

    -- ==================================================================
    -- 2. CREAR CUENTAS BANCARIAS
    -- ==================================================================
    -- Cuenta Débito (Billetera Principal)
SELECT COUNT(*) INTO v_count FROM ACCOUNTS WHERE USER_ID = v_user_id AND TYPE = 'DEBITO';
IF v_count = 0 THEN
        v_acc_deb_id := ACC_ID_SEQ.NEXTVAL;
INSERT INTO ACCOUNTS (ID, USER_ID, NAME, TYPE, BANK_NAME, INITIAL_BALANCE, IS_ACTIVE)
VALUES (v_acc_deb_id, v_user_id, 'Billetera Principal', 'DEBITO', 'Banco Nacional', 1500.00, 1);
ELSE
SELECT ID INTO v_acc_deb_id FROM ACCOUNTS WHERE USER_ID = v_user_id AND TYPE = 'DEBITO' FETCH FIRST 1 ROWS ONLY;
END IF;

    -- Cuenta Crédito (Tarjeta Oro)
SELECT COUNT(*) INTO v_count FROM ACCOUNTS WHERE USER_ID = v_user_id AND TYPE = 'CREDITO';
IF v_count = 0 THEN
        v_acc_cre_id := ACC_ID_SEQ.NEXTVAL;
INSERT INTO ACCOUNTS (ID, USER_ID, NAME, TYPE, BANK_NAME, INITIAL_BALANCE, CLOSING_DATE, PAYMENT_DATE, IS_ACTIVE)
VALUES (v_acc_cre_id, v_user_id, 'Tarjeta Oro', 'CREDITO', 'Banco Internacional', 0.00, 5, 20, 1);
ELSE
SELECT ID INTO v_acc_cre_id FROM ACCOUNTS WHERE USER_ID = v_user_id AND TYPE = 'CREDITO' FETCH FIRST 1 ROWS ONLY;
END IF;

    -- ==================================================================
    -- 3. CLONAR CATEGORÍAS (De Admin a Tester)
    -- ==================================================================
    -- Nota: En tu app real, esto lo haría el código Java al registrarse.
    -- Aquí lo simulamos para tener datos.
INSERT INTO CATEGORIES (ID, USER_ID, NAME, TYPE, MANAGEMENT_TYPE)
SELECT CAT_ID_SEQ.NEXTVAL, v_user_id, NAME, TYPE, MANAGEMENT_TYPE
FROM CATEGORIES
WHERE USER_ID = (SELECT ID FROM USERS WHERE EMAIL = 'admin@financeapp.com') -- Copiar del Admin
  AND NOT EXISTS (SELECT 1 FROM CATEGORIES c2 WHERE c2.USER_ID = v_user_id AND c2.NAME = CATEGORIES.NAME);

DBMS_OUTPUT.PUT_LINE('>> Categorías sincronizadas para el usuario.');

    -- ==================================================================
    -- 4. GENERADOR MASIVO DE TRANSACCIONES (The Loop)
    -- ==================================================================
    DBMS_OUTPUT.PUT_LINE('>> Generando ' || v_total_trx || ' transacciones aleatorias...');

FOR i IN 1..v_total_trx LOOP
        -- Seleccionar una categoría aleatoria del usuario
SELECT ID INTO v_cat_id FROM (
                                 SELECT ID FROM CATEGORIES WHERE USER_ID = v_user_id ORDER BY DBMS_RANDOM.VALUE
                             ) WHERE ROWNUM = 1;

INSERT INTO TRANSACTIONS (
    ID,
    USER_ID,
    ACCOUNT_ID,
    CATEGORY_ID,
    TYPE,
    AMOUNT,
    DESCRIPTION,
    TRANSACTION_DATE
) VALUES (
             TRX_ID_SEQ.NEXTVAL,
             v_user_id,
             -- Aleatoriamente usa Debito (70%) o Credito (30%)
             CASE WHEN DBMS_RANDOM.VALUE(0, 1) > 0.3 THEN v_acc_deb_id ELSE v_acc_cre_id END,
             v_cat_id,
             'GASTO', -- Por simplicidad, todo gasto. Podrías randomizar INGRESO también.
             ROUND(DBMS_RANDOM.VALUE(5, 200), 2), -- Montos entre 5.00 y 200.00
             'Movimiento simulado #' || i,
             -- Fecha aleatoria: Desde hace 365 días hasta HOY
             TRUNC(SYSDATE - DBMS_RANDOM.VALUE(0, 365)) + (DBMS_RANDOM.VALUE(0, 86399)/86400)
         );
END LOOP;

COMMIT;
DBMS_OUTPUT.PUT_LINE('>> SEEDING FINALIZADO: Datos de prueba cargados exitosamente.');

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Error Fatal en Seeding: ' || SQLERRM);
        RAISE;
END;
/