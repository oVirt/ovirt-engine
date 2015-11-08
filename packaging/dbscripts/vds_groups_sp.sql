

----------------------------------------------------------------
-- [vds_groups] Table
--
CREATE OR REPLACE FUNCTION InsertVdsGroups (
    v_vds_group_id UUID,
    v_description VARCHAR(4000),
    v_free_text_comment TEXT,
    v_name VARCHAR(40),
    v_cpu_name VARCHAR(255),
    v_storage_pool_id UUID,
    v_max_vds_memory_over_commit INT,
    v_count_threads_as_cores BOOLEAN,
    v_compatibility_version VARCHAR(40),
    v_transparent_hugepages BOOLEAN,
    v_migrate_on_error INT,
    v_virt_service BOOLEAN,
    v_gluster_service BOOLEAN,
    v_tunnel_migration BOOLEAN,
    v_emulated_machine VARCHAR(40),
    v_detect_emulated_machine BOOLEAN,
    v_trusted_service BOOLEAN,
    v_ha_reservation BOOLEAN,
    v_optional_reason BOOLEAN,
    v_maintenance_reason_required BOOLEAN,
    v_cluster_policy_id UUID,
    v_cluster_policy_custom_properties TEXT,
    v_enable_balloon BOOLEAN,
    v_architecture INT,
    v_optimization_type SMALLINT,
    v_spice_proxy VARCHAR(255),
    v_enable_ksm BOOLEAN,
    v_serial_number_policy SMALLINT,
    v_custom_serial_number VARCHAR(255),
    v_required_rng_sources VARCHAR(255),
    v_skip_fencing_if_sd_active BOOLEAN,
    v_skip_fencing_if_connectivity_broken BOOLEAN,
    v_hosts_with_broken_connectivity_threshold SMALLINT,
    v_fencing_enabled BOOLEAN,
    v_is_auto_converge BOOLEAN,
    v_is_migrate_compressed BOOLEAN,
    v_gluster_tuned_profile VARCHAR(50),
    v_ksm_merge_across_nodes BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vds_groups (
        vds_group_id,
        description,
        name,
        free_text_comment,
        cpu_name,
        storage_pool_id,
        max_vds_memory_over_commit,
        count_threads_as_cores,
        compatibility_version,
        transparent_hugepages,
        migrate_on_error,
        virt_service,
        gluster_service,
        tunnel_migration,
        emulated_machine,
        detect_emulated_machine,
        trusted_service,
        ha_reservation,
        optional_reason,
        maintenance_reason_required,
        cluster_policy_id,
        cluster_policy_custom_properties,
        enable_balloon,
        architecture,
        optimization_type,
        spice_proxy,
        enable_ksm,
        serial_number_policy,
        custom_serial_number,
        required_rng_sources,
        skip_fencing_if_sd_active,
        skip_fencing_if_connectivity_broken,
        hosts_with_broken_connectivity_threshold,
        fencing_enabled,
        is_auto_converge,
        is_migrate_compressed,
        gluster_tuned_profile,
        ksm_merge_across_nodes
        )
    VALUES (
        v_vds_group_id,
        v_description,
        v_name,
        v_free_text_comment,
        v_cpu_name,
        v_storage_pool_id,
        v_max_vds_memory_over_commit,
        v_count_threads_as_cores,
        v_compatibility_version,
        v_transparent_hugepages,
        v_migrate_on_error,
        v_virt_service,
        v_gluster_service,
        v_tunnel_migration,
        v_emulated_machine,
        v_detect_emulated_machine,
        v_trusted_service,
        v_ha_reservation,
        v_optional_reason,
        v_maintenance_reason_required,
        v_cluster_policy_id,
        v_cluster_policy_custom_properties,
        v_enable_balloon,
        v_architecture,
        v_optimization_type,
        v_spice_proxy,
        v_enable_ksm,
        v_serial_number_policy,
        v_custom_serial_number,
        v_required_rng_sources,
        v_skip_fencing_if_sd_active,
        v_skip_fencing_if_connectivity_broken,
        v_hosts_with_broken_connectivity_threshold,
        v_fencing_enabled,
        v_is_auto_converge,
        v_is_migrate_compressed,
        v_gluster_tuned_profile,
        v_ksm_merge_across_nodes
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVdsGroup (
    v_description VARCHAR(4000),
    v_free_text_comment TEXT,
    v_name VARCHAR(40),
    v_vds_group_id UUID,
    v_cpu_name VARCHAR(255),
    v_storage_pool_id UUID,
    v_max_vds_memory_over_commit INT,
    v_count_threads_as_cores BOOLEAN,
    v_compatibility_version VARCHAR(40),
    v_transparent_hugepages BOOLEAN,
    v_migrate_on_error INT,
    v_virt_service BOOLEAN,
    v_gluster_service BOOLEAN,
    v_gluster_cli_based_snapshot_scheduled BOOLEAN,
    v_tunnel_migration BOOLEAN,
    v_emulated_machine VARCHAR(40),
    v_detect_emulated_machine BOOLEAN,
    v_trusted_service BOOLEAN,
    v_ha_reservation BOOLEAN,
    v_optional_reason BOOLEAN,
    v_maintenance_reason_required BOOLEAN,
    v_cluster_policy_id UUID,
    v_cluster_policy_custom_properties TEXT,
    v_enable_balloon BOOLEAN,
    v_architecture INT,
    v_optimization_type SMALLINT,
    v_spice_proxy VARCHAR(255),
    v_enable_ksm BOOLEAN,
    v_serial_number_policy SMALLINT,
    v_custom_serial_number VARCHAR(255),
    v_required_rng_sources VARCHAR(255),
    v_skip_fencing_if_sd_active BOOLEAN,
    v_skip_fencing_if_connectivity_broken BOOLEAN,
    v_hosts_with_broken_connectivity_threshold SMALLINT,
    v_fencing_enabled BOOLEAN,
    v_is_auto_converge BOOLEAN,
    v_is_migrate_compressed BOOLEAN,
    v_gluster_tuned_profile VARCHAR(50),
    v_ksm_merge_across_nodes BOOLEAN
    )
RETURNS VOID
    --The [vds_groups] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE vds_groups
    SET description = v_description,
        free_text_comment = v_free_text_comment,
        name = v_name,
        cpu_name = v_cpu_name,
        storage_pool_id = v_storage_pool_id,
        _update_date = LOCALTIMESTAMP,
        max_vds_memory_over_commit = v_max_vds_memory_over_commit,
        count_threads_as_cores = v_count_threads_as_cores,
        compatibility_version = v_compatibility_version,
        transparent_hugepages = v_transparent_hugepages,
        migrate_on_error = v_migrate_on_error,
        virt_service = v_virt_service,
        gluster_service = v_gluster_service,
        tunnel_migration = v_tunnel_migration,
        gluster_cli_based_snapshot_scheduled = v_gluster_cli_based_snapshot_scheduled,
        emulated_machine = v_emulated_machine,
        detect_emulated_machine = v_detect_emulated_machine,
        trusted_service = v_trusted_service,
        ha_reservation = v_ha_reservation,
        optional_reason = v_optional_reason,
        maintenance_reason_required = v_maintenance_reason_required,
        cluster_policy_id = v_cluster_policy_id,
        cluster_policy_custom_properties = v_cluster_policy_custom_properties,
        enable_balloon = v_enable_balloon,
        architecture = v_architecture,
        optimization_type = v_optimization_type,
        spice_proxy = v_spice_proxy,
        enable_ksm = v_enable_ksm,
        serial_number_policy = v_serial_number_policy,
        custom_serial_number = v_custom_serial_number,
        required_rng_sources = v_required_rng_sources,
        skip_fencing_if_sd_active = v_skip_fencing_if_sd_active,
        skip_fencing_if_connectivity_broken = v_skip_fencing_if_connectivity_broken,
        hosts_with_broken_connectivity_threshold = v_hosts_with_broken_connectivity_threshold,
        fencing_enabled = v_fencing_enabled,
        is_auto_converge = v_is_auto_converge,
        is_migrate_compressed = v_is_migrate_compressed,
        gluster_tuned_profile = v_gluster_tuned_profile,
        ksm_merge_across_nodes = v_ksm_merge_across_nodes
    WHERE vds_group_id = v_vds_group_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVdsGroup (v_vds_group_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT vds_group_id
    INTO v_val
    FROM vds_groups
    WHERE vds_group_id = v_vds_group_id
    FOR UPDATE;

    DELETE
    FROM vds_groups
    WHERE vds_group_id = v_vds_group_id;

    -- delete VDS group permissions --
    DELETE
    FROM permissions
    WHERE object_id = v_vds_group_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVdsGroups (
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vds_groups_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vds_group_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsGroupByVdsGroupId (
    v_vds_group_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE vds_group_id = v_vds_group_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vds_groups_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vds_group_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsGroupByVdsGroupName (
    v_vds_group_name VARCHAR(40),
    v_is_case_sensitive BOOLEAN
    )
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE name = v_vds_group_name
        OR (
            NOT v_is_case_sensitive
            AND name ilike v_vds_group_name
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsGroupForUserByVdsGroupName (
    v_vds_group_name VARCHAR(40),
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE name = v_vds_group_name
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vds_groups_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vds_group_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsGroupsByStoragePoolId (
    v_storage_pool_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE storage_pool_id = v_storage_pool_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vds_groups_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = vds_group_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

--This SP returns the VDS group if it has running vms
CREATE OR REPLACE FUNCTION GetVdsGroupWithRunningVms (v_vds_group_id UUID)
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE vds_group_id = v_vds_group_id
        AND vds_group_id IN (
            SELECT vds_group_id
            FROM vms
            WHERE vms.status NOT IN (
                    0,
                    13,
                    14
                    )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

--This SP returns all VDS groups where currently no migration is going on
CREATE OR REPLACE FUNCTION GetVdsGroupsWithoutMigratingVms ()
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE vds_group_id NOT IN (
            SELECT s.vds_group_id
            FROM vm_static s
            INNER JOIN vm_dynamic d
                ON s.vm_guid = d.vm_guid
            WHERE d.status IN (
                    5,
                    6
                    )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

--This SP returns if the VDS group does not have any hosts or VMs
CREATE OR REPLACE FUNCTION GetIsVdsGroupEmpty (v_vds_group_id UUID)
RETURNS BOOLEAN AS $PROCEDURE$
BEGIN
    RETURN NOT EXISTS (
            SELECT 1
            FROM vm_static
            WHERE vds_group_id = v_vds_group_id
                AND vm_guid != '00000000-0000-0000-0000-000000000000'
            )
        AND NOT EXISTS (
            SELECT 1
            FROM vds_static
            WHERE vds_group_id = v_vds_group_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

--This SP returns all clusters with permissions to run the given action by user
CREATE OR REPLACE FUNCTION fn_perms_get_vds_groups_with_permitted_action (
    v_user_id UUID,
    v_action_group_id INT
    )
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE (
            SELECT 1
            FROM get_entity_permissions(v_user_id, v_action_group_id, vds_groups_view.vds_group_id, 9)
            ) IS NOT NULL;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- This SP returns all clusters which have valid hosts attached to them
CREATE OR REPLACE FUNCTION GetClustersHavingHosts ()
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE EXISTS (
            SELECT 1
            FROM vds_static
            WHERE vds_group_id = vds_groups_view.vds_group_id
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

--This SP updates the vds_group emulated machine and the detection mode
CREATE OR REPLACE FUNCTION UpdateVdsGroupEmulatedMachine (
    v_vds_group_id UUID,
    v_emulated_machine VARCHAR(40),
    v_detect_emulated_machine BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vds_groups
    SET emulated_machine = v_emulated_machine,
        detect_emulated_machine = v_detect_emulated_machine
    WHERE vds_group_id = v_vds_group_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetTrustedVdsGroups ()
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE trusted_service;
END;$PROCEDURE$
LANGUAGE plpgsql;

-- returns all clusters attached to a specific cluster policy (given as a parameter to the SP)
CREATE OR REPLACE FUNCTION GetVdsGroupsByClusterPolicyId (v_cluster_policy_id UUID)
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE cluster_policy_id = v_cluster_policy_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetNumberOfVmsInCluster (v_vds_group_id UUID)
RETURNS SETOF BIGINT STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT COUNT(vms.*)
    FROM vm_static vms
    WHERE vms.vds_group_id = v_vds_group_id
        AND vms.entity_type = 'VM';
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS host_vm_cluster_rs CASCADE;
CREATE TYPE host_vm_cluster_rs AS (
        vds_group_id UUID,
        hosts BIGINT,
        vms BIGINT
        );

CREATE OR REPLACE FUNCTION GetHostsAndVmsForClusters (v_vds_group_ids UUID [])
RETURNS SETOF host_vm_cluster_rs STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT groups.vds_group_id,
        (
            SELECT COUNT(DISTINCT vds.vds_id)
            FROM vds_static vds
            WHERE vds.vds_group_id = groups.vds_group_id
            ) AS host_count,
        (
            SELECT COUNT(DISTINCT vms.vm_guid)
            FROM vm_static vms
            WHERE vms.vds_group_id = groups.vds_group_id
                AND vms.entity_type::TEXT = 'VM'::TEXT
            ) AS vm_count
    FROM vds_groups groups
    WHERE groups.vds_group_id = ANY (v_vds_group_ids)
    GROUP BY groups.vds_group_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVdsGroupsByServiceAndCompatibilityVersion (
    v_gluster_service BOOLEAN,
    v_virt_service BOOLEAN,
    v_compatibility_version VARCHAR(40)
    )
RETURNS SETOF vds_groups_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vds_groups_view.*
    FROM vds_groups_view
    WHERE virt_service = v_virt_service
        AND gluster_service = v_gluster_service
        AND compatibility_version = v_compatibility_version;
END;$PROCEDURE$
LANGUAGE plpgsql;


