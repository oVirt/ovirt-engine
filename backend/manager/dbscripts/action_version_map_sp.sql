

----------------------------------------------------------------
-- [action_version_map] Table
--




Create or replace FUNCTION Insertaction_version_map(v_action_type INTEGER,
	v_cluster_minimal_version VARCHAR(40),
	v_storage_pool_minimal_version VARCHAR(40))
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO action_version_map(action_type, cluster_minimal_version, storage_pool_minimal_version)
	VALUES(v_action_type, v_cluster_minimal_version, v_storage_pool_minimal_version);
END; $procedure$
LANGUAGE plpgsql;    




Create or replace FUNCTION Deleteaction_version_map(v_action_type INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
	
   DELETE FROM action_version_map
   WHERE action_type = v_action_type;
    
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetAllFromaction_version_map() RETURNS SETOF action_version_map
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM action_version_map;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION Getaction_version_mapByaction_type(v_action_type INTEGER) RETURNS SETOF action_version_map
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM action_version_map
   WHERE action_type = v_action_type;

END; $procedure$
LANGUAGE plpgsql;

