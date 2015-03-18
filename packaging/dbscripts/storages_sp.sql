----------------------------------------------------------------
-- [storage_pool] Table
--


Create or replace FUNCTION Insertstorage_pool(v_description VARCHAR(4000),
	v_free_text_comment text,
	v_id UUID,
	v_name VARCHAR(40),
	v_status INTEGER,
	v_is_local BOOLEAN,
	v_master_domain_version INTEGER,
	v_spm_vds_id UUID ,
	v_compatibility_version VARCHAR(40),
	v_quota_enforcement_type INTEGER,
	v_mac_pool_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
  INSERT INTO
      storage_pool(description,
                    free_text_comment,
                    id,
                    name,
                    status,
                    is_local,
                    master_domain_version,
                    spm_vds_id,
                    compatibility_version,
                    quota_enforcement_type,
                    mac_pool_id)
	VALUES(v_description,
          v_free_text_comment,
          v_id,
          v_name,
          v_status,
          v_is_local,
          v_master_domain_version,
          v_spm_vds_id,
          v_compatibility_version,
          v_quota_enforcement_type,
          v_mac_pool_id);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updatestorage_pool(v_description VARCHAR(4000),
	v_free_text_comment text,
	v_id UUID,
	v_name VARCHAR(40),
	v_status INTEGER,
        v_is_local BOOLEAN,
        v_storage_pool_format_type VARCHAR(50),
	v_master_domain_version INTEGER,
	v_spm_vds_id UUID ,
	v_compatibility_version VARCHAR(40),
	v_quota_enforcement_type INTEGER,
	v_mac_pool_id UUID)
RETURNS VOID

	--The [storage_pool] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE
          storage_pool
      SET description = v_description,
          free_text_comment = v_free_text_comment,
          name = v_name,
          is_local = v_is_local,
          status = v_status,
          storage_pool_format_type = v_storage_pool_format_type,
          master_domain_version = v_master_domain_version,
          spm_vds_id = v_spm_vds_id,
          compatibility_version = v_compatibility_version,
          _update_date = LOCALTIMESTAMP,
          quota_enforcement_type=v_quota_enforcement_type,
          mac_pool_id = v_mac_pool_id
      WHERE
          id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Updatestorage_pool_partial(v_description VARCHAR(4000),
	v_free_text_comment text,
	v_id UUID,
	v_name VARCHAR(40),
	v_is_local BOOLEAN,
	v_storage_pool_format_type VARCHAR(50),
	v_compatibility_version VARCHAR(40),
	v_quota_enforcement_type INTEGER,
	v_mac_pool_id UUID )
RETURNS VOID

	--The [storage_pool] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE
        storage_pool
      SET description = v_description,
        free_text_comment = v_free_text_comment,
        name = v_name,
        is_local = v_is_local,
        storage_pool_format_type = v_storage_pool_format_type,
        compatibility_version = v_compatibility_version,
        _update_date = LOCALTIMESTAMP,
        quota_enforcement_type = v_quota_enforcement_type,
        mac_pool_id = v_mac_pool_id
      WHERE
        id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Updatestorage_pool_status(
        v_id UUID,
        v_status INTEGER)
RETURNS VOID

   AS $procedure$
BEGIN
      UPDATE storage_pool
      SET
      status = v_status,
      _update_date = LOCALTIMESTAMP
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION IncreaseStoragePoolMasterVersion(
        v_id UUID)
RETURNS INTEGER
   AS $procedure$
DECLARE v_master_domain_version INTEGER;
BEGIN
      UPDATE storage_pool
      SET
      master_domain_version = master_domain_version + 1
      WHERE id = v_id
      RETURNING master_domain_version into v_master_domain_version;

      RETURN v_master_domain_version;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Deletestorage_pool(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN

         -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    select vm_guid INTO v_val FROM vm_static where vm_guid in (select vm_guid from vms where storage_pool_id = v_id) FOR UPDATE;
    DELETE
    FROM   snapshots
    WHERE  vm_id IN (
        SELECT vm_guid
        FROM   vms
        WHERE  storage_pool_id = v_id);
    delete FROM vm_static where vm_guid in (select vm_guid from vms where storage_pool_id = v_id);

    -- Delete vm pools as empty pools are not supported
    -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    select vm_pool_id INTO v_val FROM vm_pools where vm_pool_id in (select vm_pool_id from vm_pools_view where storage_pool_id = v_id) FOR UPDATE;
    DELETE
    FROM   vm_pools
    WHERE  vm_pool_id IN (
        SELECT vm_pool_id
        FROM   vm_pools_view
        WHERE  storage_pool_id = v_id);

	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
	-- in order to force locking parent before children
   select   id INTO v_val FROM storage_pool  WHERE id = v_id     FOR UPDATE;

   DELETE FROM storage_pool
   WHERE id = v_id;

	-- delete StoragePool permissions --
   DELETE FROM permissions where object_id = v_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromstorage_pool(v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF storage_pool STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool
   WHERE (NOT v_is_filtered OR EXISTS (SELECT 1
                                       FROM   user_storage_pool_permissions_view
                                       WHERE  user_id = v_user_id AND entity_id = id));
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllByStatus(v_status INTEGER) RETURNS SETOF storage_pool STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool
   WHERE status = v_status;
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION Getstorage_poolByid(v_id UUID, v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF storage_pool STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool
   WHERE id = v_id
   AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                     FROM   user_storage_pool_permissions_view
                                     WHERE  user_id = v_user_id AND entity_id = v_id));



END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_poolByName(v_name VARCHAR(40), v_is_case_sensitive BOOLEAN)
RETURNS SETOF storage_pool STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool
   WHERE name = v_name OR (NOT v_is_case_sensitive AND name ilike v_name);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_poolsByStorageDomainId(v_storage_domain_id UUID)
RETURNS SETOF storage_pool STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT storage_pool.*
   FROM storage_pool
   inner join storage_pool_iso_map on storage_pool.id = storage_pool_iso_map.storage_pool_id
   WHERE storage_pool_iso_map.storage_id = v_storage_domain_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetVmAndTemplatesIdsByStorageDomainId(v_storage_domain_id UUID, v_include_shareable BOOLEAN, v_active_only BOOLEAN)
RETURNS SETOF UUID STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT DISTINCT vd.vm_id
      FROM vm_device vd
      INNER JOIN images_storage_domain_view i ON i.image_group_id = vd.device_id
      WHERE i.storage_id = v_storage_domain_id AND i.active = v_active_only AND i.shareable = v_include_shareable;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_poolsByVdsId(v_vdsId UUID)
RETURNS SETOF storage_pool STABLE
   AS $procedure$
   DECLARE
   v_clusterId  UUID;
BEGIN
select   vds_group_id INTO v_clusterId FROM Vds_static WHERE vds_id = v_vdsId;
   RETURN QUERY SELECT *
   FROM storage_pool
   WHERE storage_pool.id in(select storage_pool_id
      FROM vds_groups
      WHERE vds_group_id = v_clusterId);

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_poolsByVdsGroupId(v_clusterId UUID)
RETURNS SETOF storage_pool STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool
   WHERE storage_pool.id in(select storage_pool_id
      FROM vds_groups
      WHERE vds_group_id = v_clusterId);

END; $procedure$
LANGUAGE plpgsql;



----------------------------------------------------------------
-- [storage_domains_ovf_info] Table
--
Create or replace FUNCTION LoadStorageDomainInfoByDomainId(v_storage_domain_id UUID) RETURNS SETOF storage_domains_ovf_info STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT *
   FROM storage_domains_ovf_info ovf
   WHERE ovf.storage_domain_id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION LoadStorageDomainInfoByDiskId(v_disk_id UUID) RETURNS SETOF storage_domains_ovf_info STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT *
   FROM storage_domains_ovf_info ovf
   WHERE ovf.ovf_disk_id = v_disk_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION InsertStorageDomainOvfInfo(v_storage_domain_id UUID, v_status INTEGER, v_ovf_disk_id UUID,
v_stored_ovfs_ids TEXT) RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO storage_domains_ovf_info (storage_domain_id, status, ovf_disk_id, stored_ovfs_ids)
VALUES(v_storage_domain_id, v_status, v_ovf_disk_id, v_stored_ovfs_ids);
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION LoadStorageDomainsForOvfIds(v_ovfs_ids TEXT) RETURNS SETOF UUID
   AS $procedure$
BEGIN
RETURN QUERY SELECT ovf.storage_domain_id
   FROM storage_domains_ovf_info ovf
   WHERE string_to_array(ovf.stored_ovfs_ids,',') && string_to_array(v_ovfs_ids,',');
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION UpdateStorageDomainOvfInfo(v_storage_domain_id UUID, v_status INTEGER, v_ovf_disk_id UUID,
v_stored_ovfs_ids TEXT, v_last_updated TIMESTAMP WITH TIME ZONE) RETURNS VOID
   AS $procedure$
BEGIN
UPDATE storage_domains_ovf_info SET status = v_status, storage_domain_id = v_storage_domain_id,
ovf_disk_id = v_ovf_disk_id, stored_ovfs_ids = v_stored_ovfs_ids, last_updated = v_last_updated
WHERE ovf_disk_id = v_ovf_disk_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION DeleteStorageDomainOvfInfo(v_ovf_disk_id UUID) RETURNS VOID
   AS $procedure$
BEGIN
DELETE FROM storage_domains_ovf_info WHERE ovf_disk_id = v_ovf_disk_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION UpdateOvfUpdatedInfo(v_storage_domains_ids VARCHAR(5000), v_status INTEGER, v_except_status INTEGER)
    RETURNS VOID
    AS $procedure$
DECLARE
curs_storages_ids CURSOR FOR SELECT * FROM fnSplitterUuid(v_storage_domains_ids);
id UUID;
BEGIN
 OPEN curs_storages_ids;
LOOP
    FETCH curs_storages_ids INTO id;
    IF NOT FOUND THEN
     EXIT;
    END IF;
    UPDATE storage_domains_ovf_info
    SET status = v_status WHERE storage_domain_id = id AND status != v_except_status;
END LOOP;
CLOSE curs_storages_ids;
END; $procedure$
LANGUAGE plpgsql;



-- ----------------------------------------------------------------
-- [storage_domain_static] Table
--
--This function is also called during installation. If you change it, please verify
--that functions in inst_sp.sql can be executed successfully.

Create or replace FUNCTION Insertstorage_domain_static(v_id UUID,
	v_storage VARCHAR(250),
	v_storage_name VARCHAR(250),
        v_storage_description VARCHAR(4000),
	v_storage_comment text,
	v_storage_type INTEGER,
	v_storage_domain_type INTEGER,
    v_storage_domain_format_type VARCHAR(50),
    v_last_time_used_as_master BIGINT,
    v_wipe_after_delete BOOLEAN)
RETURNS VOID
   AS $procedure$
   BEGIN
INSERT INTO storage_domain_static(id, storage,storage_name, storage_description, storage_comment, storage_type, storage_domain_type, storage_domain_format_type, last_time_used_as_master, wipe_after_delete)
VALUES(v_id, v_storage, v_storage_name, v_storage_description, v_storage_comment, v_storage_type, v_storage_domain_type, v_storage_domain_format_type, v_last_time_used_as_master, v_wipe_after_delete);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getstorage_domains_List_By_ImageId(v_image_id UUID) RETURNS SETOF storage_domains STABLE
   AS $procedure$
BEGIN
    RETURN QUERY SELECT *
    FROM storage_domains
    WHERE id in (SELECT storage_domain_id
                 FROM image_storage_domain_map
                 WHERE image_id = v_image_id);
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION Updatestorage_domain_static(v_id UUID,
	v_storage VARCHAR(250),
	v_storage_name VARCHAR(250),
	v_storage_description VARCHAR(4000),
	v_storage_comment text,
	v_storage_type INTEGER,
	v_storage_domain_type INTEGER,
	v_storage_domain_format_type INTEGER,
    v_last_time_used_as_master BIGINT,
    v_wipe_after_delete BOOLEAN)
RETURNS VOID

	--The [storage_domain_static] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE storage_domain_static
      SET storage = v_storage, storage_name = v_storage_name, storage_type = v_storage_type,
      storage_domain_type = v_storage_domain_type, _update_date = LOCALTIMESTAMP,
      storage_domain_format_type = v_storage_domain_format_type,
      last_time_used_as_master = v_last_time_used_as_master,
      wipe_after_delete = v_wipe_after_delete,
      storage_description = v_storage_description, storage_comment = v_storage_comment
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletestorage_domain_static(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN

	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
	-- in order to force locking parent before children
   select   id INTO v_val FROM storage_domain_static  WHERE id = v_id     FOR UPDATE;

   DELETE FROM storage_domain_static
   WHERE id = v_id;

	-- delete Storage permissions --
   DELETE FROM permissions where object_id = v_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromstorage_domain_static() RETURNS SETOF storage_domain_static STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domain_static;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_domain_staticByid(v_id UUID)
RETURNS SETOF storage_domain_static STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domain_static
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_domain_staticByName(v_name VARCHAR(250))
RETURNS SETOF storage_domain_static STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domain_static
   WHERE storage_name = v_name;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Getstorage_domain_staticByNameFiltered(v_name VARCHAR(250), v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF storage_domain_static STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domain_static sds
   WHERE storage_name = v_name  AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                     FROM   user_storage_domain_permissions_view
                                     WHERE  user_id = v_user_id AND entity_id = sds.id));
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Getstorage_domain_staticBystorage_pool_id(v_storage_pool_id UUID)
RETURNS SETOF storage_domain_static_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domain_static_view
   WHERE storage_pool_id = v_storage_pool_id;

END; $procedure$
LANGUAGE plpgsql;



DROP TYPE IF EXISTS GetStorageDomainIdsByStoragePoolIdAndStatus_rs CASCADE;
CREATE TYPE GetStorageDomainIdsByStoragePoolIdAndStatus_rs AS (storage_id UUID);
Create or replace FUNCTION GetStorageDomainIdsByStoragePoolIdAndStatus(v_storage_pool_id UUID, v_status INTEGER)
RETURNS SETOF GetStorageDomainIdsByStoragePoolIdAndStatus_rs STABLE
   AS $procedure$
BEGIN
   RETURN QUERY
   SELECT storage_id
   FROM storage_pool_iso_map
   INNER JOIN storage_domain_static on storage_pool_iso_map.storage_id = storage_domain_static.id
   WHERE storage_pool_id = v_storage_pool_id
   AND status = v_status
   AND storage_domain_static.storage_type != 9; -- filter Cinder storage domains

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION Getstorage_domains_By_id(v_id UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF storage_domains_without_storage_pools STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains_without_storage_pools
   WHERE id = v_id
   AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                     FROM   user_storage_domain_permissions_view
                                     WHERE  user_id = v_user_id AND entity_id = v_id));

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Getstorage_domains_by_storage_pool_id_with_permitted_action (v_user_id UUID, v_action_group_id integer, v_storage_pool_id UUID)
RETURNS SETOF storage_domains STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE storage_pool_id = v_storage_pool_id
   AND (SELECT get_entity_permissions(v_user_id, v_action_group_id, id, 11)) IS NOT NULL;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Getstorage_domains_By_id_and_by_storage_pool_id(v_id UUID,
	v_storage_pool_id UUID ) RETURNS SETOF storage_domains STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE id = v_id and storage_pool_id = v_storage_pool_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getstorage_domains_By_storagePoolId(v_storage_pool_id UUID, v_user_id UUID, v_is_filtered BOOLEAN)
RETURNS SETOF storage_domains STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE storage_pool_id = v_storage_pool_id
   AND (NOT v_is_filtered OR EXISTS (SELECT 1
                                     FROM   user_storage_domain_permissions_view
                                     WHERE  user_id = v_user_id AND entity_id = id));



END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getstorage_domain_by_type_storagePoolId_and_status(v_storage_domain_type INTEGER, v_storage_pool_id UUID, v_status INTEGER)
RETURNS SETOF storage_domains STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE storage_pool_id = v_storage_pool_id
   AND storage_domain_type = v_storage_domain_type
   AND (v_status IS NULL OR status = v_status);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getstorage_domains_By_connection(v_connection CHARACTER VARYING)
RETURNS SETOF storage_domains STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE storage IN (
      SELECT id
      FROM storage_server_connections
      WHERE connection = v_connection);

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetAllFromStorageDomainsByConnectionId(v_connection_id CHARACTER VARYING)
RETURNS SETOF storage_domains STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE storage = v_connection_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllFromstorage_domains(v_user_id UUID, v_is_filtered BOOLEAN) RETURNS SETOF storage_domains STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE (NOT v_is_filtered OR EXISTS (SELECT 1
                                       FROM user_storage_domain_permissions_view
                                       WHERE user_id = v_user_id AND entity_id = id));
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Remove_Entities_From_storage_domain(v_storage_domain_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   BEGIN
      -- Creating a temporary table which will give all the images and the disks which resids on only the specified storage domain. (copied template disks on multiple storage domains will not be part of this table)
      CREATE TEMPORARY TABLE STORAGE_DOMAIN_MAP_TABLE AS select image_guid as image_id,disk_id
         from images_storage_domain_view where storage_id = v_storage_domain_id
         except select image_guid as image_id, disk_id from images_storage_domain_view where storage_id != v_storage_domain_id;
      exception when others then
         truncate table STORAGE_DOMAIN_MAP_TABLE;
         insert into STORAGE_DOMAIN_MAP_TABLE select image_guid as image_id,disk_id
         from images_storage_domain_view where storage_id = v_storage_domain_id
         except select image_guid as image_id, disk_id from images_storage_domain_view where storage_id != v_storage_domain_id;
   END;

   BEGIN
      -- All the VMs/Templates which have disks both on the specified domain and other domains.
      CREATE TEMPORARY TABLE ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE AS
      SELECT DISTINCT vm_static.vm_guid
      FROM vm_static
      INNER JOIN (-- Join vm_static only with VMs and Templates that have images on the storage domain v_storage_domain_id
                  SELECT vm_static.vm_guid
                  FROM vm_static
                  INNER JOIN vm_device vd ON vd.vm_id = vm_static.vm_guid
                  INNER JOIN images i ON i.image_group_id = vd.device_id
                  INNER JOIN STORAGE_DOMAIN_MAP_TABLE ON i.image_guid = STORAGE_DOMAIN_MAP_TABLE.image_id) vm_guids_with_disks_on_storage_domain ON vm_static.vm_guid = vm_guids_with_disks_on_storage_domain.vm_guid
      -- With all the VMs which have images on the storage domain, get all of their images and check if there is an image on another storage domain.
      INNER JOIN vm_device vd ON vd.vm_id = vm_static.vm_guid
      INNER JOIN images i ON i.image_group_id = vd.device_id
      INNER JOIN image_storage_domain_map on i.image_guid = image_storage_domain_map.image_id
      WHERE image_storage_domain_map.storage_domain_id != v_storage_domain_id;
	  exception when others then
         truncate table ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE;
         INSERT INTO ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE
         SELECT DISTINCT vm_static.vm_guid
         FROM vm_static
         INNER JOIN (SELECT vm_static.vm_guid
                     FROM vm_static
                     INNER JOIN vm_device vd ON vd.vm_id = vm_static.vm_guid
                     INNER JOIN images i ON i.image_group_id = vd.device_id
                     INNER JOIN STORAGE_DOMAIN_MAP_TABLE ON i.image_guid = STORAGE_DOMAIN_MAP_TABLE.image_id) vm_guids_with_disks_on_storage_domain ON vm_static.vm_guid = vm_guids_with_disks_on_storage_domain.vm_guid
         INNER JOIN vm_device vd ON vd.vm_id = vm_static.vm_guid
         INNER JOIN images i ON i.image_group_id = vd.device_id
         INNER JOIN image_storage_domain_map on i.image_guid = image_storage_domain_map.image_id
         WHERE image_storage_domain_map.storage_domain_id != v_storage_domain_id;
   END;

   BEGIN
      -- Templates with any images residing on only the specified storage domain
      CREATE TEMPORARY TABLE TEMPLATES_IDS_TEMPORARY_TABLE AS select vm_device.vm_id as vm_guid
         from images_storage_domain_view
         JOIN vm_device ON vm_device.device_id = images_storage_domain_view.disk_id
         JOIN STORAGE_DOMAIN_MAP_TABLE ON STORAGE_DOMAIN_MAP_TABLE.image_id = images_storage_domain_view.image_guid
         where entity_type = 'TEMPLATE' and storage_id = v_storage_domain_id
           AND vm_device.vm_id not in (SELECT vm_guid from ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE);
      exception when others then
         truncate table TEMPLATES_IDS_TEMPORARY_TABLE;
         insert into TEMPLATES_IDS_TEMPORARY_TABLE select vm_device.vm_id as vm_guid
         from images_storage_domain_view
         JOIN vm_device ON vm_device.device_id = images_storage_domain_view.disk_id
         JOIN STORAGE_DOMAIN_MAP_TABLE ON STORAGE_DOMAIN_MAP_TABLE.image_id = images_storage_domain_view.image_guid
         where entity_type = 'TEMPLATE' and storage_id = v_storage_domain_id
           AND vm_device.vm_id not in (SELECT vm_guid from ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE);
   END;

   -- Add also Template Versions based on the selected templates
   insert into TEMPLATES_IDS_TEMPORARY_TABLE
     select vm_guid from vm_static
     where vmt_guid in (select vm_guid from TEMPLATES_IDS_TEMPORARY_TABLE)
     and entity_type = 'TEMPLATE';

   BEGIN
     -- Vms which resides on the storage domain
     CREATE TEMPORARY TABLE VM_IDS_TEMPORARY_TABLE AS select vm_id,vm_images_view.entity_type as entity_type from vm_images_view
            JOIN vm_device ON vm_device.device_id = vm_images_view.disk_id
            WHERE v_storage_domain_id in (SELECT * FROM fnsplitteruuid(storage_id))
              AND vm_id not in (SELECT vm_guid from ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE);
     exception when others then
     truncate table VM_IDS_TEMPORARY_TABLE;
     insert into VM_IDS_TEMPORARY_TABLE select vm_id,vm_images_view.entity_type as entity_type from vm_images_view
            JOIN vm_device ON vm_device.device_id = vm_images_view.disk_id
            WHERE v_storage_domain_id in (SELECT * FROM fnsplitteruuid(storage_id))
              AND vm_id not in (SELECT vm_guid from ENTITY_IDS_ON_OTHER_STORAGE_DOMAINS_TEMPORARY_TABLE);
   END;

   delete FROM permissions where object_id in (select vm_id as vm_guid from VM_IDS_TEMPORARY_TABLE where entity_type <> 'TEMPLATE');
   delete FROM snapshots WHERE vm_id in (select vm_id as vm_guid from VM_IDS_TEMPORARY_TABLE);

   delete FROM image_storage_domain_map where storage_domain_id = v_storage_domain_id;
   delete FROM images where image_guid in (select image_id from STORAGE_DOMAIN_MAP_TABLE);
   delete FROM vm_interface where vmt_guid in(select vm_guid from TEMPLATES_IDS_TEMPORARY_TABLE);
   delete FROM permissions where object_id in (select vm_guid from TEMPLATES_IDS_TEMPORARY_TABLE);
   delete FROM vm_static where vm_guid in(select vm_id as vm_guid from VM_IDS_TEMPORARY_TABLE where entity_type <> 'TEMPLATE');

   -- Delete devices which are related to VMs/Templates with Multiple Storage Domain (VMs/Templates which has not removed)
   delete FROM vm_device where device_id in (select disk_id from STORAGE_DOMAIN_MAP_TABLE);

   -- Delete pools and snapshots of pools based on templates from the storage domain to be removed
   delete FROM snapshots where vm_id in (select vm_guid FROM vm_static where vmt_guid in (select vm_guid from TEMPLATES_IDS_TEMPORARY_TABLE));
   delete FROM vm_static where vmt_guid in (select vm_guid from TEMPLATES_IDS_TEMPORARY_TABLE);
   delete FROM vm_static where vm_guid in(select vm_guid from TEMPLATES_IDS_TEMPORARY_TABLE);

   -- Deletes the disks which the only storage domain they are reside on, is the storage domain.
   DELETE FROM base_disks WHERE  disk_id IN (SELECT disk_id FROM STORAGE_DOMAIN_MAP_TABLE);

   -- Deletes the disks's permissions which the only storage domain they are reside on, is the storage domain.
   DELETE FROM permissions WHERE object_id IN (SELECT disk_id FROM STORAGE_DOMAIN_MAP_TABLE);

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Force_Delete_storage_domain(v_storage_domain_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
   PERFORM Remove_Entities_From_storage_domain(v_storage_domain_id);
   delete FROM permissions where object_id = v_storage_domain_id;
   delete FROM storage_domain_dynamic where id = v_storage_domain_id;
   delete FROM storage_domain_static where id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getstorage_domains_List_By_storageDomainId(v_storage_domain_id UUID)
RETURNS SETOF storage_domains STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE storage_domains.id = v_storage_domain_id;

END; $procedure$
LANGUAGE plpgsql;

--This SP returns all data centers containing clusters with permissions to run the given action by user
Create or replace FUNCTION fn_perms_get_storage_pools_with_permitted_action_on_vds_groups(v_user_id UUID, v_action_group_id integer, v_supports_virt_service boolean, v_supports_gluster_service boolean) RETURNS SETOF storage_pool STABLE
   AS $procedure$
BEGIN
      RETURN QUERY SELECT sp.*
      FROM storage_pool sp
      WHERE sp.id in
        (SELECT vg.storage_pool_id
         FROM vds_groups vg
         WHERE (SELECT get_entity_permissions(v_user_id, v_action_group_id, vg.vds_group_id, 9)) IS NOT NULL
         AND ((v_supports_virt_service = TRUE AND vg.virt_service = TRUE) OR (v_supports_gluster_service = TRUE AND vg.gluster_service = TRUE))
        );
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Getstorage_domains_By_storage_pool_id_and_connection(v_storage_pool_id UUID, v_connection CHARACTER VARYING)
RETURNS SETOF storage_domains STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE storage_pool_id = v_storage_pool_id
   AND storage IN (
      SELECT id
      FROM storage_server_connections
      WHERE connection = v_connection);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetFailingStorage_domains()
RETURNS SETOF storage_domains STABLE
   AS $procedure$
BEGIN
   RETURN QUERY
    SELECT * FROM storage_domains WHERE recoverable AND status = 4; --inactive
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetFailingVdss()
RETURNS SETOF vds STABLE
   AS $procedure$
BEGIN
   RETURN QUERY
    SELECT * FROM vds WHERE recoverable AND status = 10; --non operational
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetStoragePoolsByClusterService(
    v_supports_virt_service BOOLEAN,
    v_supports_gluster_service BOOLEAN) RETURNS SETOF storage_pool STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT sp.* FROM storage_pool SP
   WHERE EXISTS (SELECT 1 FROM vds_groups vg
       WHERE ((v_supports_virt_service = TRUE AND vg.virt_service = TRUE) OR
              (v_supports_gluster_service = TRUE AND vg.gluster_service = TRUE)) AND vg.storage_pool_id = sp.id);
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetStorageServerConnectionsForDomain(v_storage_domain_id UUID)
 RETURNS SETOF storage_server_connections STABLE
 AS $procedure$
 BEGIN
RETURN QUERY SELECT *
FROM storage_server_connections
WHERE EXISTS  ( SELECT 1
		FROM    storage_domain_static
		WHERE   storage_domain_static.id = v_storage_domain_id
		AND     storage_domain_static.storage_type in (1,4,6) -- file storage domains - nfs,posix,local
		AND     storage_server_connections.id  = storage_domain_static.storage
		UNION ALL
		SELECT 1
		FROM   storage_domain_static
		JOIN   luns ON storage_domain_static.storage  = luns.volume_group_id
		JOIN   lun_storage_server_connection_map ON  luns.lun_id = lun_storage_server_connection_map.lun_id
							 AND storage_server_connections.id  = lun_storage_server_connection_map.storage_server_connection
		WHERE  storage_domain_static.id = v_storage_domain_id
		AND    storage_domain_static.storage_type = 3  -- storage type = iscsi
		);
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetDcIdByExternalNetworkId(v_external_id text)
RETURNS SETOF UUID STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT storage_pool_id
    FROM network
    WHERE provider_network_external_id = v_external_id;
END; $procedure$
LANGUAGE plpgsql;




-- This SP returns a distinct list of the storage types of the data domains in the pool (ignoring ISO/Export etc.)
Create or replace FUNCTION GetStorageTypesInPoolByPoolId(v_storage_pool_id UUID)
RETURNS SETOF INTEGER STABLE
   AS $procedure$
BEGIN
   RETURN QUERY
   SELECT DISTINCT storage_type
   FROM   storage_domains
   WHERE  storage_pool_id = v_storage_pool_id
   AND    storage_domain_type IN (0,1);  -- 0 = MASTER, 1 = DATA
END; $procedure$
LANGUAGE plpgsql;




-- This SP returns the number of images in the specified storage domain
Create or replace FUNCTION GetNumberOfImagesInStorageDomain(v_storage_domain_id UUID)
  RETURNS SETOF BIGINT STABLE
AS $procedure$
BEGIN
   RETURN QUERY
   SELECT COUNT(*)
   FROM image_storage_domain_map
   WHERE storage_domain_id = v_storage_domain_id;
END; $procedure$
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION GetAllDataCentersByMacPoolId(v_id UUID)
  RETURNS SETOF storage_pool STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT sp.*
   FROM storage_pool sp
   WHERE sp.mac_pool_id=v_id;

END; $procedure$
LANGUAGE plpgsql;
