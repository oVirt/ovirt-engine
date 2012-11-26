--#source action_version_map_sp.sql
------------------------------------------------------------------------------------
--              Cleanup deprecated action version section
------------------------------------------------------------------------------------
select fn_db_delete_version_map('2.2','2.2');
