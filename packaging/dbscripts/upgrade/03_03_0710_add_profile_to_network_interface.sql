---------------------------------------------------------------------
--  table vnic_profiles
---------------------------------------------------------------------
CREATE TABLE vnic_profiles
(
  id UUID NOT NULL CONSTRAINT pk_vnic_profiles_id PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  network_id UUID NOT NULL,
  port_mirroring BOOLEAN NOT NULL,
  custom_properties TEXT,
  description TEXT,
  _create_date TIMESTAMP WITH TIME ZONE default LOCALTIMESTAMP,
  _update_date TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (network_id) REFERENCES network(id) ON DELETE CASCADE
) WITH OIDS;

DROP INDEX IF EXISTS IDX_vnic_profiles_network_id;
CREATE INDEX IDX_vnic_profiles_network_id ON vnic_profiles(network_id);

--Add vnic_profile id and name into vm_interface
SELECT fn_db_add_column('vm_interface', 'vnic_profile_id', 'UUID');

DROP INDEX IF EXISTS IDX_vm_interface_vnic_profile_id;
CREATE INDEX IDX_vm_interface_vnic_profile_id ON vm_interface(vnic_profile_id);

ALTER TABLE vm_interface ADD CONSTRAINT FK_vm_interface_vnic_profile_id FOREIGN KEY(vnic_profile_id)
REFERENCES vnic_profiles(id);

Create or replace FUNCTION __temp_has_port_mirroring_vm_interfaces(v_network_id UUID) RETURNS BOOLEAN
   AS $procedure$
BEGIN
   RETURN ((SELECT COUNT(1)
   FROM vm_interface
   INNER JOIN vm_static
   ON vm_static.vm_guid = vm_interface.vm_guid
   INNER JOIN network_cluster
   ON network_cluster.cluster_id = vm_static.vds_group_id
   INNER JOIN network
   ON network.id = network_cluster.network_id
   AND network.name = vm_interface.network_name
   WHERE network.id = v_network_id
   AND port_mirroring = TRUE) > 0);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION __temp_has_port_mirroring_template_interfaces(v_network_id UUID) RETURNS BOOLEAN
   AS $procedure$
BEGIN
   RETURN ((SELECT COUNT(1)
   FROM vm_interface
   INNER JOIN vm_static
   ON vm_static.vm_guid = vm_interface.vmt_guid
   INNER JOIN network_cluster
   ON network_cluster.cluster_id = vm_static.vds_group_id
   INNER JOIN network
   ON network.id = network_cluster.network_id
   AND network.name = vm_interface.network_name
   WHERE network.id = v_network_id
   AND port_mirroring = TRUE) > 0);
END; $procedure$
LANGUAGE plpgsql;

-- create profiles for every network, with no port mirroring support
INSERT INTO vnic_profiles(id, name, network_id, port_mirroring)
       SELECT uuid_generate_v1(),
        network.name,
        network.id,
        FALSE
       FROM network
       WHERE network.vm_network IS TRUE;

-- create profiles with port_mirroring support for networks with such such VNICs
INSERT INTO vnic_profiles(id, name, network_id, port_mirroring)
       SELECT uuid_generate_v1(),
        network.name || '_pm',
        network.id,
        TRUE
       FROM network
       WHERE network.vm_network IS TRUE
       AND (__temp_has_port_mirroring_template_interfaces(network.id) OR __temp_has_port_mirroring_vm_interfaces(network.id));

-- add correct profile to each VM/Template vnic
UPDATE vm_interface
SET vnic_profile_id = vnic_profiles.id
       FROM vnic_profiles
       JOIN network ON network.id = vnic_profiles.network_id
       JOIN vds_groups ON network.storage_pool_id = vds_groups.storage_pool_id
       JOIN vm_static ON vm_static.vds_group_id = vds_groups.vds_group_id
       WHERE (vm_interface.vm_guid = vm_static.vm_guid
              OR vm_interface.vmt_guid = vm_static.vm_guid)
       AND vm_interface.port_mirroring = vnic_profiles.port_mirroring
       AND vm_interface.network_name = network.name;

-- The following drop column was commented since it is done also in 03_03_0720
-- drop the port_mirroring column from vm_interface
--SELECT fn_db_drop_column ('port_mirroring', 'vm_interface');

-- add permissions to vnic profile (according to existing permissions on networks.
Create or replace FUNCTION __temp_set_vnic_profiles_permissions()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_NETWORK_ADMIN_ID UUID;
   v_VNIC_PROFILE_USER_ID UUID;
   v_NETWORK_USER_ID UUID;

BEGIN
   v_NETWORK_ADMIN_ID := 'DEF00005-0000-0000-0000-DEF000000005';
   v_VNIC_PROFILE_USER_ID := 'DEF00020-0000-0000-0000-DEF000000010';
   v_NETWORK_USER_ID := 'DEF0000A-0000-0000-0000-DEF000000010';

------------------------------------------------
--- Update existing roles with new Action Groups
------------------------------------------------
-- Add ActionGroup 1203 (CONFIGURE_NETWORK_VNIC_PROFILE) to any role which contains ActionGroup 703 (CONFIGURE_STORAGE_POOL_NETWORK)
    INSERT INTO roles_groups (role_id, action_group_id)
    SELECT DISTINCT role_id, 1203
    FROM roles_groups a
    WHERE NOT EXISTS (SELECT 1
                      FROM roles_groups b
                      WHERE b.role_id = a.role_id
                      AND b.action_group_id = 1203)
    AND EXISTS (SELECT 1
                FROM roles_groups b
                WHERE b.role_id = a.role_id
                AND b.action_group_id = 703);

-- Add ActionGroup 1204 (CREATE_NETWORK_VNIC_PROFILE) to any role which contains ActionGroup 704 (CREATE_STORAGE_POOL_NETWORK)
    INSERT INTO roles_groups (role_id, action_group_id)
    SELECT DISTINCT role_id, 1204
    FROM roles_groups a
    WHERE NOT EXISTS (SELECT 1
                      FROM roles_groups b
                      WHERE b.role_id = a.role_id
                      AND b.action_group_id = 1204)
    AND EXISTS (SELECT 1
                FROM roles_groups b
                WHERE b.role_id = a.role_id
                AND b.action_group_id = 704);

-- Add ActionGroup 1205 (DELETE_NETWORK_VNIC_PROFILE) to any role which contains ActionGroup 705 (DELETE_STORAGE_POOL_NETWORK)
    INSERT INTO roles_groups (role_id, action_group_id)
    SELECT DISTINCT role_id, 1205
    FROM roles_groups a
    WHERE NOT EXISTS (SELECT 1
                      FROM roles_groups b
                      WHERE b.role_id = a.role_id
                      AND b.action_group_id = 1205)
    AND EXISTS (SELECT 1
                FROM roles_groups b
                WHERE b.role_id = a.role_id
                AND b.action_group_id = 705);

----------------------------
-- UPDATE NETWORK_ADMIN role
----------------------------

-- Add CONFIGURE_NETWORK_VNIC_PROFILE
    PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID, 1203);

-- Add CREATE_NETWORK_VNIC_PROFILE
    PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID, 1204);

-- Add DELETE_NETWORK_VNIC_PROFILE
    PERFORM fn_db_add_action_group_to_role(v_NETWORK_ADMIN_ID ,1205);

------------------------
-- ADD VNIC_PROFILE_USER role
------------------------
    INSERT INTO roles(id,name,description,is_readonly,role_type) SELECT v_VNIC_PROFILE_USER_ID, 'VnicProfileUser', 'VM Network Interface Profile User', true, 2
    WHERE NOT EXISTS (SELECT id,name,description,is_readonly,role_type
                      FROM roles
                      WHERE id = v_VNIC_PROFILE_USER_ID
                      AND name='VnicProfileUser'
                      AND description='VM Network Interface Profile User'
                      AND is_readonly=true
                      AND role_type=2);

-- Add CONFIGURE_VM_NETWORK
    PERFORM fn_db_add_action_group_to_role(v_VNIC_PROFILE_USER_ID, 9);

-- Add CONFIGURE_TEMPLATE_NETWORK
    PERFORM fn_db_add_action_group_to_role(v_VNIC_PROFILE_USER_ID, 204);

-- Add LOGIN
    PERFORM fn_db_add_action_group_to_role(v_VNIC_PROFILE_USER_ID, 1300);

---------------------------
-- Grant permission to use the created VNIC profiles to each user which have NetworkUser permission to use the network of the profile
---------------------------
    INSERT INTO permissions (id, role_id, ad_element_id, object_id, object_type_id)
          (SELECT uuid_generate_v1(),
                 v_VNIC_PROFILE_USER_ID,
                 ad_element_id,
                 vnic_profiles.id,
                 27
           FROM permissions
           INNER JOIN vnic_profiles ON vnic_profiles.network_id = permissions.object_id
           WHERE permissions.object_type_id = 20
           AND permissions.role_id = v_NETWORK_USER_ID
           AND NOT EXISTS (SELECT 1
                           FROM permissions p
                           WHERE p.role_id = v_VNIC_PROFILE_USER_ID
                           AND p.ad_element_id = permissions.ad_element_id
                           AND p.object_id = vnic_profiles.id
                           AND object_type_id = 27));


---------------------------
-- Replace any NetworkUser role with VNICProfileUser, on all object types, except networks (which were handeled in the INSERT above)
---------------------------
    UPDATE permissions
    SET role_id = v_VNIC_PROFILE_USER_ID
    WHERE role_id = v_NETWORK_USER_ID AND object_type_id != 20;

---------------------------
-- Delete NetworkUser permissions
---------------------------
    DELETE FROM permissions
    WHERE role_id = v_NETWORK_USER_ID;

---------------------------
-- Delete NetworkUser role
---------------------------
    DELETE FROM roles
    WHERE id = v_NETWORK_USER_ID;

------------------------------------------------
-- Delete Port Mirroring Action Group from Roles
------------------------------------------------
    DELETE FROM roles_groups
    WHERE action_group_id = 1200;


END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_set_vnic_profiles_permissions();
DROP function __temp_set_vnic_profiles_permissions();
DROP function __temp_has_port_mirroring_vm_interfaces(UUID);
DROP function __temp_has_port_mirroring_template_interfaces(UUID);
