

----------------------------------------------------------------
-- [vds_spm_id_map] Table
--





Create or replace FUNCTION Insertvds_spm_id_map(v_storage_pool_id UUID,
 v_vds_id UUID,
 v_vds_spm_id INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO vds_spm_id_map(storage_pool_id, vds_id, vds_spm_id)
	VALUES(v_storage_pool_id, v_vds_id, v_vds_spm_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletevds_spm_id_map(v_vds_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM vds_spm_id_map
   WHERE vds_id = v_vds_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteByPoolvds_spm_id_map(v_vds_id UUID, v_storage_pool_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM vds_spm_id_map
   WHERE vds_id = v_vds_id AND storage_pool_id = v_storage_pool_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromvds_spm_id_map() RETURNS SETOF vds_spm_id_map
   AS $procedure$
BEGIN
RETURN QUERY SELECT vds_spm_id_map.*
   FROM vds_spm_id_map;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getvds_spm_id_mapBystorage_pool_idAndByvds_spm_id(v_storage_pool_id UUID,
 v_vds_spm_id INTEGER) RETURNS SETOF vds_spm_id_map
   AS $procedure$
BEGIN
RETURN QUERY SELECT vds_spm_id_map.*
   FROM vds_spm_id_map
   WHERE storage_pool_id = v_storage_pool_id AND vds_spm_id = v_vds_spm_id;

END; $procedure$
LANGUAGE plpgsql;






Create or replace FUNCTION Getvds_spm_id_mapBystorage_pool_id(v_storage_pool_id UUID) RETURNS SETOF vds_spm_id_map
   AS $procedure$
BEGIN
RETURN QUERY SELECT vds_spm_id_map.*
   FROM vds_spm_id_map
   WHERE storage_pool_id = v_storage_pool_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getvds_spm_id_mapByvds_id(v_vds_id UUID) RETURNS SETOF vds_spm_id_map
   AS $procedure$
BEGIN
RETURN QUERY SELECT vds_spm_id_map.*
   FROM vds_spm_id_map
   WHERE vds_id = v_vds_id;

END; $procedure$
LANGUAGE plpgsql;


