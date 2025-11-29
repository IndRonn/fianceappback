-- ######################################################################
-- # ARCHIVO: 01_ddl/00_drop_all.sql
-- # OBJETIVO: Limpieza TOTAL del esquema (Idempotencia).
-- # DESCRIPCIÓN: Elimina todos los objetos en orden correcto (Hijos -> Padres).
-- #              Usa bloques PL/SQL para ignorar errores si no existen.
-- ######################################################################

--SET SERVEROUTPUT ON;

-- ======================================================================
-- 1. ELIMINACIÓN DE TABLAS (Orden: Tablas con FKs primero)
-- ======================================================================

BEGIN
    -- 1. Transacciones (Depende de Users, Accounts, Categories)
BEGIN EXECUTE IMMEDIATE 'DROP TABLE TRANSACTIONS CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;

    -- 2. Presupuestos (Depende de Users, Categories)
BEGIN EXECUTE IMMEDIATE 'DROP TABLE BUDGETS CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;

    -- 3. Categorías (Depende de Users)
BEGIN EXECUTE IMMEDIATE 'DROP TABLE CATEGORIES CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;

    -- 4. Deudas Externas (Depende de Users)
BEGIN EXECUTE IMMEDIATE 'DROP TABLE EXTERNAL_DEBTS CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;

    -- 5. Metas de Ahorro (Depende de Users)
BEGIN EXECUTE IMMEDIATE 'DROP TABLE SAVINGS_GOALS CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;

    -- 6. Notificaciones (Depende de Users)
BEGIN EXECUTE IMMEDIATE 'DROP TABLE NOTIFICATIONS CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;

    -- 7. Cuentas (Depende de Users)
    -- Nota: Al borrar Accounts, si existiera alguna referencia circular no prevista, CASCADE ayuda.
BEGIN EXECUTE IMMEDIATE 'DROP TABLE ACCOUNTS CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;

    -- 8. Usuarios (Tabla Padre de todo)
BEGIN EXECUTE IMMEDIATE 'DROP TABLE USERS CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN NULL; END;

    DBMS_OUTPUT.PUT_LINE('>> Tablas eliminadas correctamente.');
END;
/

-- ======================================================================
-- 2. ELIMINACIÓN DE SECUENCIAS
-- ======================================================================

BEGIN
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE USER_ID_SEQ'; EXCEPTION WHEN OTHERS THEN NULL; END;
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE ACC_ID_SEQ'; EXCEPTION WHEN OTHERS THEN NULL; END;
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE CAT_ID_SEQ'; EXCEPTION WHEN OTHERS THEN NULL; END;
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE BUD_ID_SEQ'; EXCEPTION WHEN OTHERS THEN NULL; END;
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE TRX_ID_SEQ'; EXCEPTION WHEN OTHERS THEN NULL; END;
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE EXT_ID_SEQ'; EXCEPTION WHEN OTHERS THEN NULL; END;
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE SVG_ID_SEQ'; EXCEPTION WHEN OTHERS THEN NULL; END;
BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE NOT_ID_SEQ'; EXCEPTION WHEN OTHERS THEN NULL; END;

    DBMS_OUTPUT.PUT_LINE('>> Secuencias eliminadas correctamente.');
END;
/

-- PROMPT LIMPIEZA COMPLETADA.