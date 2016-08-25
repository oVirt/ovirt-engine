----------------------------------------------------------------------
--  Cpu Profiles
----------------------------------------------------------------------

Create or replace FUNCTION GetCpuProfileByCpuProfileId(v_id UUID)
RETURNS SETOF cpu_profiles STABLE
   AS $procedure$
BEGIN

   RETURN QUERY SELECT *
   FROM cpu_profiles
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION InsertCpuProfile(v_id UUID,
  v_name VARCHAR(50),
  v_cluster_id UUID,
  v_qos_id UUID,
  v_description TEXT)
RETURNS VOID
   AS $procedure$
BEGIN

   INSERT INTO cpu_profiles(id, name, cluster_id, qos_id, description)
       VALUES(v_id, v_name, v_cluster_id, v_qos_id, v_description);

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateCpuProfile(v_id UUID,
  v_name VARCHAR(50),
  v_cluster_id UUID,
  v_qos_id UUID,
  v_description TEXT)
RETURNS VOID
   AS $procedure$
BEGIN

   UPDATE cpu_profiles
   SET id = v_id, name = v_name, cluster_id = v_cluster_id, qos_id = v_qos_id,
       description = v_description, _update_date = LOCALTIMESTAMP
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteCpuProfile(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val UUID;
BEGIN

    DELETE FROM cpu_profiles
    WHERE id = v_id;

    -- Delete the cpu profiles permissions
    DELETE FROM permissions WHERE object_id = v_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromCpuProfiles()
RETURNS SETOF cpu_profiles STABLE
   AS $procedure$
BEGIN

   RETURN QUERY SELECT *
   FROM cpu_profiles;

END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetCpuProfilesByClusterId (
    v_cluster_id UUID,
    v_user_id UUID,
    v_is_filtered boolean,
    v_action_group_id INTEGER
    )
RETURNS SETOF cpu_profiles STABLE AS $PROCEDURE$
DECLARE
    v_everyone_object_id  UUID;
BEGIN
    IF v_is_filtered
    THEN
        RETURN QUERY
        SELECT *
        FROM cpu_profiles
        WHERE cluster_id = v_cluster_id
            AND EXISTS (
                SELECT 1
                FROM get_entity_permissions(v_user_id, v_action_group_id, cpu_profiles.id, 30)
            )
        ORDER BY _create_date;

    ELSE
        RETURN QUERY
        SELECT *
        FROM cpu_profiles
        WHERE cluster_id = v_cluster_id
        ORDER BY _create_date;
    END IF;
END;$PROCEDURE$
LANGUAGE plpgsql;


Create or replace FUNCTION GetCpuProfilesByQosId(v_qos_id UUID)
RETURNS SETOF cpu_profiles STABLE
   AS $procedure$
BEGIN

   RETURN QUERY SELECT *
   FROM cpu_profiles
   WHERE qos_id = v_qos_id;
END; $procedure$
LANGUAGE plpgsql;
