-- ######################################################################
-- # ARCHIVO: 02_constraints/02_indexes.sql
-- # OBJETIVO: Optimización del Rendimiento (Performance Tuning).
-- # ESTRATEGIA:
-- #    1. Índices en Foreign Keys (FKs) para evitar bloqueos de tabla
-- #       durante operaciones DML en tablas padre.
-- #    2. Índices en columnas de filtro frecuente (Fechas, Tipos).
-- #    3. Naming Convention: IDX_<TABLA>_<COLUMNA_CLAVE>
-- ######################################################################

-- ======================================================================
-- 1. ÍNDICES EN TRANSACCIONES (Tabla con más volumen)
-- ======================================================================

-- Vital para filtros de rango: "Movimientos de Este Mes"
-- Evita Full Table Scans en la tabla más grande del sistema.
CREATE INDEX IDX_TRX_DATE ON TRANSACTIONS(TRANSACTION_DATE);

-- Optimización para agrupar gastos por usuario (Dashboard principal)
-- También protege contra bloqueos al borrar/modificar USERS.
CREATE INDEX IDX_TRX_USER_ID ON TRANSACTIONS(USER_ID);

-- Optimización para "Ver movimientos de la Cuenta X"
-- Protege contra bloqueos al modificar ACCOUNTS.
CREATE INDEX IDX_TRX_ACCOUNT_ID ON TRANSACTIONS(ACCOUNT_ID);

-- Optimización para "Ver gastos por Categoría" (Gráficos de Torta)
-- Protege contra bloqueos al modificar CATEGORIES.
CREATE INDEX IDX_TRX_CATEGORY_ID ON TRANSACTIONS(CATEGORY_ID);

-- ======================================================================
-- 2. ÍNDICES EN TABLAS MAESTRAS (Soporte a FKs y Búsquedas)
-- ======================================================================

-- ACCOUNTS: Búsquedas rápidas por usuario
CREATE INDEX IDX_ACC_USER_ID ON ACCOUNTS(USER_ID);

-- CATEGORIES: Búsquedas rápidas por usuario
CREATE INDEX IDX_CAT_USER_ID ON CATEGORIES(USER_ID);

-- BUDGETS: Búsquedas por usuario y categoría (Clave compuesta candidata)
CREATE INDEX IDX_BUD_USER_CAT ON BUDGETS(USER_ID, CATEGORY_ID);

-- EXTERNAL_DEBTS: Búsquedas por usuario
CREATE INDEX IDX_EXT_USER_ID ON EXTERNAL_DEBTS(USER_ID);

-- SAVINGS_GOALS: Búsquedas por usuario
CREATE INDEX IDX_SVG_USER_ID ON SAVINGS_GOALS(USER_ID);

-- NOTIFICATIONS: Búsquedas por usuario y estado (Leído/No Leído)
-- Índice compuesto: Ayuda a queries tipo "Dame mis notificaciones no leídas"
CREATE INDEX IDX_NOT_USER_READ ON NOTIFICATIONS(USER_ID, IS_READ);

-- NOTA:
-- No creamos índices para PRIMARY KEYS (ID) ni para columnas UNIQUE (EMAIL),
-- porque Oracle crea índices únicos automáticamente para ellas al definir la tabla.

--PROMPT >> ÍNDICES DE RENDIMIENTO CREADOS CORRECTAMENTE.