

----------------------------------------------------------------
-- [vm_pools] Table
--
CREATE OR REPLACE FUNCTION InsertVm_pools (
    v_vm_pool_description VARCHAR(4000),
    v_vm_pool_comment TEXT,
    v_vm_pool_id UUID,
    v_vm_pool_name VARCHAR(255),
    v_vm_pool_type INT,
    v_stateful BOOLEAN,
    v_parameters VARCHAR(200),
    v_prestarted_vms INT,
    v_cluster_id UUID,
    v_max_assigned_vms_per_user SMALLINT,
    v_spice_proxy VARCHAR(255),
    v_is_auto_storage_select BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO vm_pools (
        vm_pool_id,
        vm_pool_description,
        vm_pool_comment,
        vm_pool_name,
        vm_pool_type,
        stateful,
        parameters,
        prestarted_vms,
        cluster_id,
        max_assigned_vms_per_user,
        spice_proxy,
        is_auto_storage_select
        )
    VALUES (
        v_vm_pool_id,
        v_vm_pool_description,
        v_vm_pool_comment,
        v_vm_pool_name,
        v_vm_pool_type,
        v_stateful,
        v_parameters,
        v_prestarted_vms,
        v_cluster_id,
        v_max_assigned_vms_per_user,
        v_spice_proxy,
        v_is_auto_storage_select
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION UpdateVm_pools (
    v_vm_pool_description VARCHAR(4000),
    v_vm_pool_comment TEXT,
    v_vm_pool_id UUID,
    v_vm_pool_name VARCHAR(255),
    v_vm_pool_type INT,
    v_stateful BOOLEAN,
    v_parameters VARCHAR(200),
    v_prestarted_vms INT,
    v_cluster_id UUID,
    v_max_assigned_vms_per_user SMALLINT,
    v_spice_proxy VARCHAR(255),
    v_is_auto_storage_select BOOLEAN
    )
RETURNS VOID
    --The [vm_pools] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
    AS $PROCEDURE$
BEGIN
    UPDATE vm_pools
    SET vm_pool_description = v_vm_pool_description,
        vm_pool_comment = v_vm_pool_comment,
        vm_pool_name = v_vm_pool_name,
        vm_pool_type = v_vm_pool_type,
        stateful = v_stateful,
        parameters = v_parameters,
        prestarted_vms = v_prestarted_vms,
        cluster_id = v_cluster_id,
        max_assigned_vms_per_user = v_max_assigned_vms_per_user,
        spice_proxy = v_spice_proxy,
        is_auto_storage_select = v_is_auto_storage_select
    WHERE vm_pool_id = v_vm_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteVm_pools (v_vm_pool_id UUID)
RETURNS VOID AS $PROCEDURE$
DECLARE v_val UUID;

BEGIN
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    -- in order to force locking parent before children
    SELECT vm_pool_id
    INTO v_val
    FROM vm_pools
    WHERE vm_pool_id = v_vm_pool_id
    FOR

    UPDATE;

    DELETE
    FROM vm_pools
    WHERE vm_pool_id = v_vm_pool_id;

    -- delete VmPool permissions --
    PERFORM DeletePermissionsByEntityId(v_vm_pool_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION SetVmPoolBeingDestroyed (
    v_vm_pool_id UUID,
    v_is_being_destroyed BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_pools
    SET is_being_destroyed = v_is_being_destroyed
    WHERE vm_pool_id = v_vm_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS GetAllFromVm_pools_rs CASCADE;
    CREATE type GetAllFromVm_pools_rs AS (
        vm_pool_id UUID,
        assigned_vm_count INT,
        vm_running_count INT,
        vm_pool_description VARCHAR(4000),
        vm_pool_comment TEXT,
        vm_pool_name VARCHAR(255),
        vm_pool_type INT,
        stateful BOOLEAN,
        parameters VARCHAR(200),
        prestarted_vms INT,
        cluster_id UUID,
        cluster_name VARCHAR(40),
        max_assigned_vms_per_user SMALLINT,
        spice_proxy VARCHAR(255),
        is_being_destroyed BOOLEAN,
        is_auto_storage_select BOOLEAN
        );

CREATE OR REPLACE FUNCTION GetAllFromVm_pools ()
RETURNS SETOF GetAllFromVm_pools_rs AS $PROCEDURE$
BEGIN
    -- BEGIN TRAN
    BEGIN
        CREATE TEMPORARY TABLE tt_VM_POOL_GROUP (
            vm_pool_id UUID,
            assigned_vm_count INT
            ) ON COMMIT DROP;

        exception when others then

        TRUNCATE TABLE tt_VM_POOL_GROUP;
    END;

    INSERT INTO tt_VM_POOL_GROUP (
        vm_pool_id,
        assigned_vm_count
        )
    SELECT vm_pools_view.vm_pool_id,
        count(vm_pool_map.vm_pool_id)
    FROM vm_pools_view
    LEFT JOIN vm_pool_map
        ON vm_pools_view.vm_pool_id = vm_pool_map.vm_pool_id
    GROUP BY vm_pools_view.vm_pool_id,
        vm_pool_map.vm_pool_id;

    BEGIN
        CREATE TEMPORARY TABLE tt_VM_POOL_RUNNING (
            vm_pool_id UUID,
            vm_running_count INT
            ) ON COMMIT DROP;

        exception when others then

        TRUNCATE TABLE tt_VM_POOL_RUNNING;
    END;

    INSERT INTO tt_VM_POOL_RUNNING (
        vm_pool_id,
        vm_running_count
        )
    SELECT vm_pools_view.vm_pool_id,
        count(vm_pools_view.vm_pool_id)
    FROM vm_pools_view
    LEFT JOIN vm_pool_map
        ON vm_pools_view.vm_pool_id = vm_pool_map.vm_pool_id
    LEFT JOIN vm_dynamic
        ON vm_pool_map.vm_guid = vm_dynamic.vm_guid
    WHERE vm_dynamic.status > 0
    GROUP BY vm_pools_view.vm_pool_id;

    BEGIN
        CREATE TEMPORARY TABLE tt_VM_POOL_PRERESULT (
            vm_pool_id UUID,
            assigned_vm_count INT,
            vm_running_count INT
            ) ON COMMIT DROP;

        exception when others then

        TRUNCATE TABLE tt_VM_POOL_PRERESULT;
    END;

    INSERT INTO tt_VM_POOL_PRERESULT (
        vm_pool_id,
        assigned_vm_count,
        vm_running_count
        )
    SELECT pg.vm_pool_id,
        pg.assigned_vm_count,
        pr.vm_running_count
    FROM tt_VM_POOL_GROUP pg
    LEFT JOIN tt_VM_POOL_RUNNING pr
        ON pg.vm_pool_id = pr.vm_pool_id;

    UPDATE tt_VM_POOL_PRERESULT
    SET vm_running_count = 0
    WHERE vm_running_count IS NULL;

    BEGIN
        CREATE TEMPORARY TABLE tt_VM_POOL_RESULT (
            vm_pool_id UUID,
            assigned_vm_count INT,
            vm_running_count INT,
            vm_pool_description VARCHAR(4000),
            vm_pool_comment TEXT,
            vm_pool_name VARCHAR(255),
            vm_pool_type INT,
            stateful BOOLEAN,
            parameters VARCHAR(200),
            prestarted_vms INT,
            cluster_id UUID,
            cluster_name VARCHAR(40),
            max_assigned_vms_per_user SMALLINT,
            spice_proxy VARCHAR(255),
            is_being_destroyed BOOLEAN,
            is_auto_storage_select BOOLEAN
            ) ON COMMIT DROP;

        exception when others then

        TRUNCATE TABLE tt_VM_POOL_RESULT;
    END;

    INSERT INTO tt_VM_POOL_RESULT (
        vm_pool_id,
        assigned_vm_count,
        vm_running_count,
        vm_pool_description,
        vm_pool_comment,
        vm_pool_name,
        vm_pool_type,
        stateful,
        parameters,
        prestarted_vms,
        cluster_id,
        cluster_name,
        max_assigned_vms_per_user,
        spice_proxy,
        is_being_destroyed,
        is_auto_storage_select
        )
    SELECT ppr.vm_pool_id,
        ppr.assigned_vm_count,
        ppr.vm_running_count,
        p.vm_pool_description,
        p.vm_pool_comment,
        p.vm_pool_name,
        p.vm_pool_type,
        p.stateful,
        p.parameters,
        p.prestarted_vms,
        p.cluster_id,
        p.cluster_name,
        p.max_assigned_vms_per_user,
        p.spice_proxy,
        p.is_being_destroyed,
        p.is_auto_storage_select
    FROM tt_VM_POOL_PRERESULT ppr
    INNER JOIN vm_pools_view p
        ON ppr.vm_pool_id = p.vm_pool_id;

    RETURN QUERY

    SELECT *
    FROM tt_VM_POOL_RESULT;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVm_poolsByvm_pool_id (
    v_vm_pool_id UUID,
    v_user_id UUID,
    v_is_filtered BOOLEAN
    )
RETURNS SETOF vm_pools_full_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_pools_full_view.*
    FROM vm_pools_full_view
    WHERE vm_pool_id = v_vm_pool_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_pool_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_vm_pool_id
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVm_poolsByvm_pool_name (v_vm_pool_name VARCHAR(255))
RETURNS SETOF vm_pools_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vm_pools_view.*
    FROM vm_pools_view
    WHERE vm_pool_name = v_vm_pool_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllVm_poolsByUser_id (v_user_id UUID)
RETURNS SETOF vm_pools_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT vm_pools_view.*
    FROM users_and_groups_to_vm_pool_map_view
    INNER JOIN vm_pools_view
        ON users_and_groups_to_vm_pool_map_view.vm_pool_id = vm_pools_view.vm_pool_id
    WHERE (users_and_groups_to_vm_pool_map_view.user_id = v_user_id);
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromVmPoolsFilteredAndSorted (v_user_id UUID, v_offset int, v_limit int)
RETURNS SETOF vm_pools_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT pools.*
    FROM vm_pools_view pools
    INNER JOIN user_vm_pool_permissions_view
        ON user_id = v_user_id
            AND entity_id = pools.vm_pool_id
    ORDER BY pools.vm_pool_name ASC
    LIMIT v_limit OFFSET v_offset;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVm_poolsByAdGroup_names (v_ad_group_names VARCHAR(4000))
RETURNS SETOF vm_pools_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT vm_pools_view.*
    FROM ad_groups
    INNER JOIN users_and_groups_to_vm_pool_map_view
        ON ad_groups.id = users_and_groups_to_vm_pool_map_view.user_id
    INNER JOIN vm_pools_view
        ON users_and_groups_to_vm_pool_map_view.vm_pool_id = vm_pools_view.vm_pool_id
    WHERE (
            ad_groups.name IN (
                SELECT Id
                FROM fnSplitter(v_ad_group_names)
                )
            );
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetVmDataFromPoolByPoolId (
    v_pool_id uuid,
    v_user_id uuid,
    v_is_filtered boolean
    )
RETURNS SETOF vms STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT vms.*
    FROM vms
    WHERE vm_pool_id = v_pool_id
        AND (
            NOT v_is_filtered
            OR EXISTS (
                SELECT 1
                FROM user_vm_pool_permissions_view
                WHERE user_id = v_user_id
                    AND entity_id = v_pool_id
                )
            )
        -- Limiting results to 1 since we only need a single VM from the pool to retrieve the pool data
        LIMIT 1;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllVm_poolsByUser_id_with_groups_and_UserRoles (v_user_id UUID)
RETURNS SETOF vm_pools_view STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT DISTINCT pools.*
    FROM vm_pools_view pools
    INNER JOIN user_vm_pool_permissions_view
        ON user_id = v_user_id
            AND entity_id = pools.vm_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION BoundVmPoolPrestartedVms (v_vm_pool_id UUID)
RETURNS VOID AS $PROCEDURE$
BEGIN
    UPDATE vm_pools
    SET prestarted_vms = LEAST (
        prestarted_vms, (
            SELECT COUNT (*)
            FROM vm_pool_map
            WHERE vm_pool_id = v_vm_pool_id
            )
        )
    WHERE vm_pool_id = v_vm_pool_id;
END;$PROCEDURE$
LANGUAGE plpgsql;
