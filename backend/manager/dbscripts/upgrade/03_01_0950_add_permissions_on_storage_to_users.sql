Create or replace FUNCTION __temp_insert_storage_permissions()
RETURNS VOID
   AS $procedure$
DECLARE
    v_VM_ADMIN_ID UUID;
    v_DISK_OPERATOR_ROLE_ID UUID;
    v_DISK_CREATOR_ROLE_ID UUID;
    v_CREATE_VM_GROUP_ID INTEGER;
    v_CLUSTER_OBJECT_TYPE INTEGER;
    v_DATA_CENTER_OBJECT_TYPE INTEGER;
    v_SYSTEM_OBJECT_TYPE INTEGER;
    v_STORAGE_OBJECT_TYPE INTEGER;
    v_DISK_OBJECT_TYPE INTEGER;
    v_VmDisks RECORD;
    v_permissions RECORD;

BEGIN
    v_VM_ADMIN_ID := 'DEF00006-0000-0000-0000-DEF000000006';
    v_DISK_OPERATOR_ROLE_ID := 'DEF0000A-0000-0000-0000-DEF00000000B';
    v_DISK_CREATOR_ROLE_ID := 'DEF0000A-0000-0000-0000-DEF00000000C';
    v_CREATE_VM_GROUP_ID := 1;
    v_CLUSTER_OBJECT_TYPE := 9;
    v_DATA_CENTER_OBJECT_TYPE := 14;
    v_STORAGE_OBJECT_TYPE := 11;
    v_SYSTEM_OBJECT_TYPE := 1;
    v_DISK_OBJECT_TYPE := 19;

    -- Add Disk Operator permissions on the relevant disks to users with VM Operator for their disks
    FOR v_VmDisks IN
      SELECT DISTINCT vm_device.vm_id, base_disks.disk_id
      FROM images
      LEFT OUTER JOIN disk_image_dynamic ON images.image_guid = disk_image_dynamic.image_id
      LEFT OUTER JOIN base_disks ON images.image_group_id = base_disks.disk_id
      LEFT OUTER JOIN vm_device ON vm_device.device_id = images.image_group_id
      LEFT OUTER JOIN vm_static ON vm_static.vm_guid = vm_device.vm_id
    LOOP

      INSERT INTO permissions (id,
                            role_id,
                            ad_element_id,
                            object_id,
                            object_type_id)
      (SELECT uuid_generate_v1(),
             v_DISK_OPERATOR_ROLE_ID,
             ad_element_id,
             v_VmDisks.disk_id,
             v_DISK_OBJECT_TYPE
       FROM permissions
       WHERE role_id = v_VM_ADMIN_ID
       AND object_id = v_VmDisks.vm_id);

    END LOOP;

    FOR v_permissions IN SELECT ad_element_id, object_id, object_type_id
                         FROM permissions
                         INNER JOIN roles_groups ON permissions.role_id = roles_groups.role_id
                         WHERE roles_groups.action_group_id = v_CREATE_VM_GROUP_ID
    LOOP

        -- CREATE_VM on Cluster will allow creating Disks on the Storage Domains of the Data Center which the Cluster belongs to.
        IF (v_permissions.object_type_id = v_CLUSTER_OBJECT_TYPE) THEN

            INSERT INTO permissions (id,
                            role_id,
                            ad_element_id,
                            object_id,
                            object_type_id)
            (SELECT uuid_generate_v1(),
                    v_DISK_CREATOR_ROLE_ID,
                    v_permissions.ad_element_id,
                    storage_pool_iso_map.storage_id,
                    v_STORAGE_OBJECT_TYPE
             FROM vds_groups
             INNER JOIN storage_pool_iso_map ON vds_groups.storage_pool_id = storage_pool_iso_map.storage_pool_id
             WHERE vds_groups.vds_group_id = v_permissions.object_id and
             cast(v_DISK_CREATOR_ROLE_ID as VARCHAR) || cast(v_permissions.ad_element_id as VARCHAR) ||
                  cast(storage_pool_iso_map.storage_id as VARCHAR) not in
             ( select cast(role_id as VARCHAR) || cast(ad_element_id as VARCHAR) ||
                      cast(object_id as VARCHAR) from permissions));

        -- CREATE_VM on Data Center will allow creating Disks on the Storage Domains of the Data Center.
        ELSIF (v_permissions.object_type_id = v_DATA_CENTER_OBJECT_TYPE) THEN
            INSERT INTO permissions (id,
                            role_id,
                            ad_element_id,
                            object_id,
                            object_type_id)
            (SELECT uuid_generate_v1(),
                    v_DISK_CREATOR_ROLE_ID,
                    v_permissions.ad_element_id,
                    storage_pool_iso_map.storage_id,
                    v_STORAGE_OBJECT_TYPE
             FROM storage_pool_iso_map
             WHERE storage_pool_iso_map.storage_pool_id = v_permissions.object_id and
             cast(v_DISK_CREATOR_ROLE_ID as VARCHAR) || cast(v_permissions.ad_element_id as VARCHAR) ||
                  cast(storage_pool_iso_map.storage_id as VARCHAR) not in
             ( select cast(role_id as VARCHAR) || cast(ad_element_id as VARCHAR) ||
                      cast(object_id as VARCHAR) from permissions));

        -- CREATE_VM on System will allow creating Disks on all Storage Domains in the System.
        ELSEIF (v_permissions.object_type_id = v_SYSTEM_OBJECT_TYPE) THEN
            INSERT INTO permissions (id,
                            role_id,
                            ad_element_id,
                            object_id,
                            object_type_id)
            (SELECT uuid_generate_v1(),
                    v_DISK_CREATOR_ROLE_ID,
                    v_permissions.ad_element_id,
                    storage_domain_static.id,
                    v_STORAGE_OBJECT_TYPE
             FROM storage_domain_static
             WHERE cast(v_DISK_CREATOR_ROLE_ID as VARCHAR) || cast(v_permissions.ad_element_id as VARCHAR) ||
                  cast(storage_domain_static.id as VARCHAR) not in
             ( select cast(role_id as VARCHAR) || cast(ad_element_id as VARCHAR) ||
                      cast(object_id as VARCHAR) from permissions));

        END IF;
    END LOOP;


 RETURN;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_insert_storage_permissions();
DROP function __temp_insert_storage_permissions();




