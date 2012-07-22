/**********************************************************************************************
   Currently we have a bug that upgarde 03010250 was cherry-picked and installed after 03010130
   This caused all scripts between 03010140 and 03010240 not to run.
   This scripts removes the 03010250 from schema_version and updates 03010130 to be the curent version
   So, after that , all scripts from 03010140 and up will run
   Since 03010250 was a configuration change that is already handled in the new config.sql, no other handling is needed.
**********************************************************************************************/

update schema_version set current = true where version = '03010130'
and exists(select 1 from schema_version where version = '03010250' and current = true);

delete from schema_version
where version = '03010250' and current = true;

-- Add a supported_engines column to vds_dynamic reolving a 3.0 to 3.1 upgrade issue
-- This column was probably added to the create_tables.sql and not to the upgrade so
-- it exists on any 3.1 based DB but is missing from 3.0 based DB that is upgraded to 3.1

select fn_db_add_column('vds_dynamic','supported_engines','varchar(40)');
