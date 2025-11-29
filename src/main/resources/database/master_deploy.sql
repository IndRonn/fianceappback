-- ######################################################################
-- # ARCHIVO: master_deploy.sql
-- # OBJETIVO: Script Maestro de Despliegue (Orquestador).
-- # EJECUCIÓN: Desde SQL*Plus o SQL Developer conectados a la base de datos.
-- #            Se asume ejecución desde la carpeta raíz 'database/'.
-- ######################################################################

-- Configuración de entorno SQL*Plus
SET DEFINE OFF;   -- Evita que el caracter '&' pida sustitución de variables
SET ECHO ON;      -- Muestra los comandos que se van ejecutando
SET SERVEROUTPUT ON; -- Habilita los mensajes DBMS_OUTPUT.PUT_LINE

-- Iniciar grabación de Log (Forense)
SPOOL deploy_log.txt

PROMPT ======================================================================
PROMPT   INICIANDO DESPLIEGUE DE FINANCEDB (HARD MODE EDITION)
PROMPT ======================================================================
PROMPT

-- 1. LIMPIEZA (DDL)
PROMPT [PASO 1/6] Limpiando esquema anterior...
@@01_ddl/00_drop_all.sql

-- 2. ESTRUCTURA BASE (DDL)
PROMPT [PASO 2/6] Creando Secuencias...
@@01_ddl/01_sequences.sql

PROMPT [PASO 3/6] Creando Tablas Core...
@@01_ddl/02_tables.sql

-- 3. RESTRICCIONES E INDICES
PROMPT [PASO 4/6] Aplicando Integridad Referencial e Indices...
@@02_constraints/01_foreign_keys.sql
@@02_constraints/02_indexes.sql

-- 4. DATOS MAESTROS (DML)
PROMPT [PASO 5/6] Cargando Datos Maestros (Bootstrap)...
@@03_dml_static/01_bootstrap.sql

-- 5. DATOS DE PRUEBA (SEED - OPCIONAL)
-- Comenta esta línea si vas a desplegar a PRODUCCIÓN REAL.
PROMPT [PASO 6/6] Generando Datos de Prueba (Seed)...
@@05_seed_dev/01_mock_data.sql

PROMPT
PROMPT ======================================================================
PROMPT   DESPLIEGUE EXITOSO. SISTEMA LISTO PARA OPERAR.
PROMPT ======================================================================

-- Cerrar Log
SPOOL OFF