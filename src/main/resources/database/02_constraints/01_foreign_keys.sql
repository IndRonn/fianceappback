-- ######################################################################
-- # ARCHIVO: 02_constraints/01_foreign_keys.sql
-- # OBJETIVO: Establecer la Integridad Referencial (Relaciones).
-- # ESTRATEGIA:
-- #    1. Definición explícita de nombres de constraints (FK_Tabla_Ref).
-- #    2. Reglas de borrado:
-- #       - ON DELETE CASCADE: Para datos propiedad estricta del Usuario.
-- #       - RESTRICT (Default): Para datos financieros críticos (Transacciones).
-- ######################################################################

-- ======================================================================
-- 1. RELACIONES DE CUENTAS (ACCOUNTS)
-- ======================================================================
-- Si se borra el usuario, sus cuentas desaparecen.
ALTER TABLE ACCOUNTS
    ADD CONSTRAINT FK_ACCOUNT_USER
        FOREIGN KEY (USER_ID)
            REFERENCES USERS (ID)
            ON DELETE CASCADE;

-- ======================================================================
-- 2. RELACIONES DE CATEGORÍAS (CATEGORIES)
-- ======================================================================
ALTER TABLE CATEGORIES
    ADD CONSTRAINT FK_CAT_USER
        FOREIGN KEY (USER_ID)
            REFERENCES USERS (ID)
            ON DELETE CASCADE;

-- ======================================================================
-- 3. RELACIONES DE PRESUPUESTOS (BUDGETS)
-- ======================================================================
ALTER TABLE BUDGETS
    ADD CONSTRAINT FK_BUDGET_USER
        FOREIGN KEY (USER_ID)
            REFERENCES USERS (ID)
            ON DELETE CASCADE;

ALTER TABLE BUDGETS
    ADD CONSTRAINT FK_BUDGET_CATEGORY
        FOREIGN KEY (CATEGORY_ID)
            REFERENCES CATEGORIES (ID)
            ON DELETE CASCADE;

-- ======================================================================
-- 4. RELACIONES DE TRANSACCIONES (TRANSACTIONS)
-- ======================================================================
ALTER TABLE TRANSACTIONS
    ADD CONSTRAINT FK_TRX_USER
        FOREIGN KEY (USER_ID)
            REFERENCES USERS (ID)
            ON DELETE CASCADE;

-- IMPORTANTE: Sin CASCADE. Protegemos el historial financiero.
-- Si intentas borrar una cuenta con movimientos, Oracle lanzará error.
ALTER TABLE TRANSACTIONS
    ADD CONSTRAINT FK_TRX_ACCOUNT
        FOREIGN KEY (ACCOUNT_ID)
            REFERENCES ACCOUNTS (ID);

ALTER TABLE TRANSACTIONS
    ADD CONSTRAINT FK_TRX_CAT
        FOREIGN KEY (CATEGORY_ID)
            REFERENCES CATEGORIES (ID);

ALTER TABLE TRANSACTIONS
    ADD CONSTRAINT FK_TRX_DEST_ACC
        FOREIGN KEY (DESTINATION_ACCOUNT_ID)
            REFERENCES ACCOUNTS (ID);

-- ======================================================================
-- 5. RELACIONES DE DEUDAS EXTERNAS (EXTERNAL_DEBTS)
-- ======================================================================
ALTER TABLE EXTERNAL_DEBTS
    ADD CONSTRAINT FK_EXT_USER
        FOREIGN KEY (USER_ID)
            REFERENCES USERS (ID)
            ON DELETE CASCADE;

-- ======================================================================
-- 6. RELACIONES DE METAS DE AHORRO (SAVINGS_GOALS)
-- ======================================================================
ALTER TABLE SAVINGS_GOALS
    ADD CONSTRAINT FK_SVG_USER
        FOREIGN KEY (USER_ID)
            REFERENCES USERS (ID)
            ON DELETE CASCADE;

-- ======================================================================
-- 7. RELACIONES DE NOTIFICACIONES (NOTIFICATIONS)
-- ======================================================================
ALTER TABLE NOTIFICATIONS
    ADD CONSTRAINT FK_NOT_USER
        FOREIGN KEY (USER_ID)
            REFERENCES USERS (ID)
            ON DELETE CASCADE;

PROMPT >> FOREIGN KEYS APLICADAS CORRECTAMENTE.