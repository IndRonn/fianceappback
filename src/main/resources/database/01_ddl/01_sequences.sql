-- ######################################################################
-- # ARCHIVO: 01_ddl/01_sequences.sql
-- # OBJETIVO: Generación de Identificadores Únicos (Primary Keys).
-- # ESTRATEGIA: Uso explícito de SEQUENCE.
-- #             Configurado como NOCACHE para desarrollo (evita saltos
-- #             de numeración al reiniciar la instancia Docker).
-- ######################################################################


-- 1. Usuarios (USERS)
CREATE SEQUENCE USER_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 2. Cuentas Financieras (ACCOUNTS)
CREATE SEQUENCE ACC_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 3. Categorías (CATEGORIES)
CREATE SEQUENCE CAT_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 4. Presupuestos (BUDGETS)
CREATE SEQUENCE BUD_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 5. Transacciones (TRANSACTIONS)
-- Nota: Esta secuencia tendrá alto tráfico. En PROD evaluar CACHE 20.
CREATE SEQUENCE TRX_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 6. Deudas Externas (EXTERNAL_DEBTS)
CREATE SEQUENCE EXT_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 7. Metas de Ahorro (SAVINGS_GOALS)
CREATE SEQUENCE SVG_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

-- 8. Notificaciones (NOTIFICATIONS)
CREATE SEQUENCE NOT_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

PROMPT >> SECUENCIAS CREADAS CORRECTAMENTE.