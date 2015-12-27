CREATE OR REPLACE FUNCTION __temp_set_cpu_profiles_permissions ()

RETURNS VOID AS $PROCEDURE$

DECLARE
    -- role ids declarations
    v_CPU_PROFILE_CREATOR_ID UUID;
    v_CPU_PROFILE_OPERATOR_ID UUID;
    v_SUPER_USER_ROLE_ID UUID;
    v_POWER_USER_ROLE_ID UUID;
    v_CLUSTER_ADMIN_ROLE_ID UUID;
    v_DATA_CENTER_ADMIN_ROLE_ID UUID;
    v_USER_VM_MANAGER_ROLE_ID UUID;
    v_VM_POOL_ADMIN_ROLE_ID UUID;
    v_VM_CREATOR_ROLE_ID UUID;
    v_USER_TEMPLATE_BASED_VM_ROLE_ID UUID;
    v_USER_VM_RUNTIME_MANAGER_ROLE_ID UUID;
    v_EVERYONE_ROLE_ID UUID;

    --  action groups declarations
    v_delete_cpu_profile_action_group INT;
    v_update_cpu_profile_action_group INT;
    v_create_cpu_profile_action_group INT;
    v_assign_cpu_profile_action_group INT;

BEGIN
    -- role ids
    v_CPU_PROFILE_CREATOR_ID := 'DEF00016-0000-0000-0000-DEF000000016';
    v_CPU_PROFILE_OPERATOR_ID := 'DEF00017-0000-0000-0000-DEF000000017';
    v_SUPER_USER_ROLE_ID := '00000000-0000-0000-0000-000000000001';
    v_POWER_USER_ROLE_ID := '00000000-0000-0000-0001-000000000002';
    v_CLUSTER_ADMIN_ROLE_ID := 'DEF00001-0000-0000-0000-DEF000000001';
    v_DATA_CENTER_ADMIN_ROLE_ID := 'DEF00002-0000-0000-0000-DEF000000002';
    v_USER_VM_MANAGER_ROLE_ID := 'DEF00006-0000-0000-0000-DEF000000006';
    v_VM_POOL_ADMIN_ROLE_ID := 'DEF00007-0000-0000-0000-DEF000000007';
    v_VM_CREATOR_ROLE_ID := 'DEF0000A-0000-0000-0000-DEF00000000D';
    v_USER_TEMPLATE_BASED_VM_ROLE_ID := 'DEF00009-0000-0000-0000-DEF000000009';
    v_USER_VM_RUNTIME_MANAGER_ROLE_ID := 'DEF00006-0000-0000-0000-DEF000000011';
    v_EVERYONE_ROLE_ID := 'EEE00000-0000-0000-0000-123456789EEE';

    --  action groups declarations
    v_delete_cpu_profile_action_group := 1665;
    v_update_cpu_profile_action_group := 1666;
    v_create_cpu_profile_action_group := 1667;
    v_assign_cpu_profile_action_group := 1668;

    -- Add cpu_profile_creator role
    INSERT INTO roles (
        id,
        name,
        description,
        is_readonly,
        role_type,
        app_mode
        )
    VALUES (
        v_CPU_PROFILE_CREATOR_ID,
        'CpuProfileCreator',
        'Cpu Profile Creation/Deletion/Updating and Operation',
        true,
        1,
        1);

    -- Add cpu_profile_operator role
    INSERT INTO roles (
        id,
        name,
        description,
        is_readonly,
        role_type,
        app_mode
        )
    VALUES (
        v_CPU_PROFILE_OPERATOR_ID,
        'CpuProfileOperator',
        'Cpu Profile Operation',
        true,
        2,
        1);

    -- Adding Create/Update/Delete permissions to roles:
    PERFORM fn_db_add_action_group_to_role(v_CPU_PROFILE_CREATOR_ID, v_update_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_CPU_PROFILE_CREATOR_ID, v_delete_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_CPU_PROFILE_CREATOR_ID, v_create_cpu_profile_action_group);

    PERFORM fn_db_add_action_group_to_role(v_SUPER_USER_ROLE_ID, v_update_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_SUPER_USER_ROLE_ID , v_delete_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_SUPER_USER_ROLE_ID , v_create_cpu_profile_action_group);

    PERFORM fn_db_add_action_group_to_role(v_POWER_USER_ROLE_ID, v_update_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_POWER_USER_ROLE_ID, v_delete_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_POWER_USER_ROLE_ID, v_create_cpu_profile_action_group);

    PERFORM fn_db_add_action_group_to_role(v_CLUSTER_ADMIN_ROLE_ID, v_delete_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_CLUSTER_ADMIN_ROLE_ID, v_update_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_CLUSTER_ADMIN_ROLE_ID, v_create_cpu_profile_action_group);

    PERFORM fn_db_add_action_group_to_role(v_DATA_CENTER_ADMIN_ROLE_ID, v_delete_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_DATA_CENTER_ADMIN_ROLE_ID, v_update_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_DATA_CENTER_ADMIN_ROLE_ID, v_create_cpu_profile_action_group);

    -- Adding use permissions to roles:
    PERFORM fn_db_add_action_group_to_role(v_CPU_PROFILE_OPERATOR_ID, v_assign_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_CPU_PROFILE_CREATOR_ID, v_assign_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_SUPER_USER_ROLE_ID, v_assign_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_POWER_USER_ROLE_ID , v_assign_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_CLUSTER_ADMIN_ROLE_ID, v_assign_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_DATA_CENTER_ADMIN_ROLE_ID, v_assign_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_USER_VM_MANAGER_ROLE_ID, v_assign_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_VM_POOL_ADMIN_ROLE_ID, v_assign_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_VM_CREATOR_ROLE_ID, v_assign_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_USER_TEMPLATE_BASED_VM_ROLE_ID, v_assign_cpu_profile_action_group);
    PERFORM fn_db_add_action_group_to_role(v_USER_VM_RUNTIME_MANAGER_ROLE_ID, v_assign_cpu_profile_action_group);

    -- Add role CpuProfileOperator to Everyone on all the cpu profiles that exist
    -- in the system at start(That means that everyone can use the default cpu
    -- profiles in the system).
    INSERT INTO permissions (
        id,
        role_id,
        ad_element_id,
        object_id,
        object_type_id
       )
    SELECT
        uuid_generate_v1(),
        v_CPU_PROFILE_OPERATOR_ID,
        v_EVERYONE_ROLE_ID, -- Everyone
       cpu_profiles.id,
        30 -- cpu profile object id
        FROM cpu_profiles order by _create_date LIMIT 1;

END; $PROCEDURE$
LANGUAGE plpgsql;

SELECT __temp_set_cpu_profiles_permissions();
DROP FUNCTION __temp_set_cpu_profiles_permissions();

