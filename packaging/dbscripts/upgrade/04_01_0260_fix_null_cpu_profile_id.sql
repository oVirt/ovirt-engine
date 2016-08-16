CREATE VIEW __tmp_permissions_view (
    entity_id,
    user_id,
    role_id
) AS

  SELECT object_id,
    ad_element_id,
    role_id
  FROM permissions
  WHERE object_type_id = 30 -- 30 = Cpu Profile
  UNION ALL
  SELECT cpu_profiles.id,
    permissions.ad_element_id,
    permissions.role_id
  FROM cpu_profiles
    INNER JOIN permissions
      ON object_id=cluster_id
         AND object_type_id = 9 -- 9  = Cluster
  UNION ALL
  SELECT cpu_profiles.id,
    permissions.ad_element_id,
    permissions.role_id
  FROM cpu_profiles
    INNER JOIN cluster
      ON cluster.cluster_id=cpu_profiles.cluster_id
    INNER JOIN permissions
      ON object_id=cluster.storage_pool_id
         AND object_type_id = 14  -- 14 = Data Center
  UNION ALL
  SELECT cpu_profiles.id,
    permissions.ad_element_id,
    permissions.role_id
  FROM cpu_profiles
    CROSS JOIN permissions
  WHERE object_type_id = 1; -- 1 = System


CREATE OR REPLACE FUNCTION __tmp_add_cpu_profile_with_permissions(v_cl cluster)
  RETURNS VOID AS $FUNCTION$
DECLARE
  v_CPU_PROFILE_OPERATOR_ID UUID;
  v_cpu_profile_id UUID;
BEGIN
  v_CPU_PROFILE_OPERATOR_ID := 'DEF00017-0000-0000-0000-DEF000000017';
  v_cpu_profile_id := uuid_generate_v1();

  INSERT INTO cpu_profiles(
    id,
    name,
    cluster_id
  )
  VALUES(
    v_cpu_profile_id,
    v_cl.name,
    v_cl.cluster_id
  );

  INSERT INTO permissions (
    id,
    role_id,
    ad_element_id,
    object_id,
    object_type_id
  )
  VALUES (
    uuid_generate_v1(),
    v_CPU_PROFILE_OPERATOR_ID,
    getGlobalIds('everyone'),
    v_cpu_profile_id,
    30 -- cpu profile object id
  );

END; $FUNCTION$
LANGUAGE plpgsql;

DO $$
  DECLARE
    v_CPU_PROFILE_OPERATOR_ID UUID;
  BEGIN

    -- create cpu profile for clusters without it
    PERFORM __tmp_add_cpu_profile_with_permissions(cluster.*)
      FROM cluster
      WHERE NOT EXISTS(
        SELECT 1
        FROM cpu_profiles
        WHERE cpu_profiles.cluster_id = cluster.cluster_id
      );

    -- For each VM with null cpu_profile_id, set it to
    -- some cpu profile id from the cluster with permissions for everyone

    v_CPU_PROFILE_OPERATOR_ID := 'DEF00017-0000-0000-0000-DEF000000017';

    UPDATE vm_static
    SET cpu_profile_id = cpu_profiles.id
    FROM cpu_profiles
      JOIN __tmp_permissions_view AS perm
        ON cpu_profiles.id = perm.entity_id
    WHERE vm_static.cpu_profile_id IS NULL
          AND vm_static.cluster_id = cpu_profiles.cluster_id
          AND perm.user_id = getGlobalIds('everyone')
          AND perm.role_id = v_CPU_PROFILE_OPERATOR_ID;

    -- If there are no cpu profiles with permissions for everyone
    -- use any cpu profile

    UPDATE vm_static
    SET cpu_profile_id = cpu_profiles.id
    FROM cpu_profiles
    WHERE vm_static.cpu_profile_id IS NULL
          AND vm_static.cluster_id = cpu_profiles.cluster_id;

    ALTER TABLE vm_static ADD CONSTRAINT vm_static_cpu_profile_not_null
      CHECK ((cluster_id IS NULL) OR (cpu_profile_id IS NOT NULL));

  END;
$$;

DROP FUNCTION __tmp_add_cpu_profile_with_permissions(cluster);
DROP VIEW __tmp_permissions_view;