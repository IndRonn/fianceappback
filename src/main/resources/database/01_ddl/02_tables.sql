-- ######################################################################
-- # ARCHIVO: 01_ddl/02_tables.sql
-- # OBJETIVO: Definición de Tablas Core (DDL).
-- # ESTRATEGIA:
-- #    1. Creación de tablas SIN Foreign Keys (se agregan en paso 02).
-- #    2. Definición de Primary Keys (PKs).
-- #    3. Restricciones de Dominio (CHECK Constraints) para simular Enums.
-- #    4. Valores por defecto (DEFAULT) y restricciones NOT NULL.
-- ######################################################################

-- ======================================================================
-- 1. TABLA: USERS
-- ======================================================================
CREATE TABLE USERS (
                       ID              NUMBER          PRIMARY KEY,
                       FIRST_NAME      VARCHAR2(100)   NOT NULL,
                       LAST_NAME       VARCHAR2(100),
                       EMAIL           VARCHAR2(100)   NOT NULL UNIQUE,
                       USERNAME        VARCHAR2(100)   NOT NULL UNIQUE,
                       PASSWORD_HASH   VARCHAR2(255)   NOT NULL,
                       ROLE            VARCHAR2(20)    NOT NULL,
                       CREATED_AT      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Simulación de Enum: Role
                       CONSTRAINT CHK_USER_ROLE CHECK (ROLE IN ('USER', 'ADMIN'))
);

-- ======================================================================
-- 2. TABLA: ACCOUNTS
-- ======================================================================
CREATE TABLE ACCOUNTS (
                          ID              NUMBER          PRIMARY KEY,
                          USER_ID         NUMBER          NOT NULL, -- FK definida en 02_constraints
                          NAME            VARCHAR2(100)   NOT NULL,
                          TYPE            VARCHAR2(20)    NOT NULL,
                          BANK_NAME       VARCHAR2(100),
                          INITIAL_BALANCE NUMBER(12, 2)   DEFAULT 0.00 NOT NULL,
                          CLOSING_DATE    NUMBER(2),      -- Día del mes (1-31)
                          PAYMENT_DATE    NUMBER(2),      -- Día del mes (1-31)
                          IS_ACTIVE       NUMBER(1)       DEFAULT 1 NOT NULL,

    -- Simulación de Enum: AccountType (Alineado con Java)
                          CONSTRAINT CHK_ACC_TYPE CHECK (TYPE IN ('DEBITO', 'CREDITO', 'EFECTIVO')),
    -- Simulación de Boolean: IsActive
                          CONSTRAINT CHK_ACC_ACTIVE CHECK (IS_ACTIVE IN (0, 1))
);

CREATE TABLE ACCOUNTS (
                          ID NUMBER PRIMARY KEY,
                          USER_ID NUMBER NOT NULL,
                          NAME VARCHAR2(100) NOT NULL,
                          TYPE VARCHAR2(20) NOT NULL CHECK (TYPE IN ('DEBITO', 'CREDITO', 'EFECTIVO')),
                          CURRENCY VARCHAR2(3) DEFAULT 'PEN' NOT NULL CHECK (CURRENCY IN ('PEN', 'USD')),
                          BANK_NAME VARCHAR2(100),

    -- El saldo actual (Para crédito: negativo = deuda, positivo = a favor)
                          INITIAL_BALANCE NUMBER(12, 2) DEFAULT 0.00 NOT NULL,

    -- NUEVA COLUMNA: Límite de Crédito (Vital para calcular % de uso)
                          CREDIT_LIMIT NUMBER(12, 2) NULL,

                          CLOSING_DATE NUMBER(2),
                          PAYMENT_DATE NUMBER(2),
                          IS_ACTIVE NUMBER(1) DEFAULT 1 NOT NULL CHECK (IS_ACTIVE IN (0, 1))
);

-- ======================================================================
-- 3. TABLA: CATEGORIES
-- ======================================================================
CREATE TABLE CATEGORIES (
                            ID              NUMBER          PRIMARY KEY,
                            USER_ID         NUMBER          NOT NULL, -- FK definida en 02_constraints
                            NAME            VARCHAR2(100)   NOT NULL,
                            TYPE            VARCHAR2(10)    NOT NULL,
                            MANAGEMENT_TYPE VARCHAR2(20)    NOT NULL,

    -- Simulación de Enum: CategoryType
                            CONSTRAINT CHK_CAT_TYPE CHECK (TYPE IN ('INGRESO', 'GASTO')),
    -- Simulación de Enum: ManagementType
                            CONSTRAINT CHK_CAT_MGMT CHECK (MANAGEMENT_TYPE IN ('PLANIFICADO_MENSUAL', 'DIA_A_DIA'))
);

-- ======================================================================
-- 4. TABLA: BUDGETS
-- ======================================================================
CREATE TABLE BUDGETS (
                         ID              NUMBER          PRIMARY KEY,
                         USER_ID         NUMBER          NOT NULL, -- FK definida en 02_constraints
                         CATEGORY_ID     NUMBER          NOT NULL, -- FK definida en 02_constraints
                         AMOUNT          NUMBER(12, 2)   NOT NULL,
                         MONTH           NUMBER(2)       NOT NULL,
                         YEAR            NUMBER(4)       NOT NULL,

    -- Validación básica de mes
                         CONSTRAINT CHK_BUD_MONTH CHECK (MONTH BETWEEN 1 AND 12)
    );

-- ======================================================================
-- 5. TABLA: TRANSACTIONS
-- ======================================================================
CREATE TABLE TRANSACTIONS (
                              ID                      NUMBER          PRIMARY KEY,
                              USER_ID                 NUMBER          NOT NULL, -- FK definida en 02_constraints
                              ACCOUNT_ID              NUMBER          NOT NULL, -- FK definida en 02_constraints
                              CATEGORY_ID             NUMBER,         -- Nullable (Transferencias)
                              TYPE                    VARCHAR2(20)    NOT NULL,
                              AMOUNT                  NUMBER(12, 2)   NOT NULL,
                              DESCRIPTION             VARCHAR2(4000),
                              TRANSACTION_DATE        DATE            DEFAULT SYSDATE NOT NULL,
                              DESTINATION_ACCOUNT_ID  NUMBER,         -- Nullable (Solo Transferencias)

    -- Simulación de Enum: TransactionType
                              CONSTRAINT CHK_TRX_TYPE CHECK (TYPE IN ('INGRESO', 'GASTO', 'TRANSFERENCIA'))
);

-- ======================================================================
-- 6. TABLA: EXTERNAL_DEBTS
-- ======================================================================
CREATE TABLE EXTERNAL_DEBTS (
                                ID              NUMBER          PRIMARY KEY,
                                USER_ID         NUMBER          NOT NULL, -- FK definida en 02_constraints
                                NAME            VARCHAR2(150)   NOT NULL,
                                CREDITOR        VARCHAR2(100),
                                TOTAL_AMOUNT    NUMBER(12, 2)   NOT NULL,
                                CURRENT_BALANCE NUMBER(12, 2)   NOT NULL
);

-- ======================================================================
-- 7. TABLA: SAVINGS_GOALS
-- ======================================================================
CREATE TABLE SAVINGS_GOALS (
                               ID              NUMBER          PRIMARY KEY,
                               USER_ID         NUMBER          NOT NULL, -- FK definida en 02_constraints
                               NAME            VARCHAR2(150)   NOT NULL,
                               TARGET_AMOUNT   NUMBER(12, 2),
                               CURRENT_AMOUNT  NUMBER(12, 2)   DEFAULT 0.00 NOT NULL
);

-- ======================================================================
-- 8. TABLA: NOTIFICATIONS
-- ======================================================================
CREATE TABLE NOTIFICATIONS (
                               ID          NUMBER          PRIMARY KEY,
                               USER_ID     NUMBER          NOT NULL, -- FK definida en 02_constraints
                               MESSAGE     VARCHAR2(255)   NOT NULL,
                               IS_READ     NUMBER(1)       DEFAULT 0 NOT NULL,
                               CREATED_AT  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,

    -- Simulación de Boolean: IsRead
                               CONSTRAINT CHK_NOT_READ CHECK (IS_READ IN (0, 1))
);

PROMPT >> TABLAS (CORE) CREADAS CORRECTAMENTE.