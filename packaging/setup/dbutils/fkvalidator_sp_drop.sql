-- Drop stuff created by fkvalidator_sp.sql
DROP TYPE IF EXISTS fk_info_rs CASCADE;
DROP FUNCTION IF EXISTS fn_db_validate_fks(boolean,boolean) CASCADE;
