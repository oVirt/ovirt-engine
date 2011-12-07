----------------------------------------------------------------
-- [storage_pool] Table
--


Create or replace FUNCTION Insertstorage_pool(v_description VARCHAR(4000),
	v_id UUID,
	v_name VARCHAR(40),
	v_storage_pool_type INTEGER,
	v_status INTEGER,
	v_master_domain_version INTEGER,
	v_spm_vds_id UUID ,
	v_compatibility_version VARCHAR(40))
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO storage_pool(description, id, name, storage_pool_type,status,master_domain_version,spm_vds_id,compatibility_version)
	VALUES(v_description, v_id, v_name, v_storage_pool_type,v_status,v_master_domain_version,v_spm_vds_id,v_compatibility_version);
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION Updatestorage_pool(v_description VARCHAR(4000),
	v_id UUID,
	v_name VARCHAR(40),
	v_storage_pool_type INTEGER,
	v_status INTEGER,
        v_storage_pool_format_type VARCHAR(50),
	v_master_domain_version INTEGER,
	v_spm_vds_id UUID ,
	v_compatibility_version VARCHAR(40))
RETURNS VOID

	--The [storage_pool] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE storage_pool
      SET description = v_description,name = v_name,storage_pool_type = v_storage_pool_type, 
      status = v_status,storage_pool_format_type = v_storage_pool_format_type,master_domain_version = v_master_domain_version, 
      spm_vds_id = v_spm_vds_id,compatibility_version = v_compatibility_version, 
      _update_date = LOCALTIMESTAMP
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Updatestorage_pool_partial(v_description VARCHAR(4000),
	v_id UUID,
	v_name VARCHAR(40),
	v_storage_pool_type INTEGER,
	v_compatibility_version VARCHAR(40))
RETURNS VOID

	--The [storage_pool] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE storage_pool
      SET description = v_description,name = v_name,storage_pool_type = v_storage_pool_type,compatibility_version = v_compatibility_version, 
      _update_date = LOCALTIMESTAMP
      WHERE id = v_id;
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

Create or replace FUNCTION Deletestorage_pool(v_id UUID)
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  UUID;
BEGIN
	
         -- Get (and keep) a shared lock with "right to upgrade to exclusive"
    select vm_guid INTO v_val FROM vm_static where vm_guid in (select vm_guid from vms where storage_pool_id = v_id) FOR UPDATE;
    delete FROM vm_static where vm_guid in (select vm_guid from vms where storage_pool_id = v_id);

	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
	-- in order to force locking parent before children 
   select   id INTO v_val FROM storage_pool  WHERE id = v_id     FOR UPDATE;
	
   DELETE FROM storage_pool
   WHERE id = v_id;

	-- delete StoragePool permissions --
   DELETE FROM permissions where object_id = v_id; 
    
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromstorage_pool() RETURNS SETOF storage_pool
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_poolByid(v_id UUID) RETURNS SETOF storage_pool
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_poolByName(v_name VARCHAR(40)) 
RETURNS SETOF storage_pool
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool
   WHERE name = v_name;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_poolsByType(v_storage_pool_type INTEGER) 
RETURNS SETOF storage_pool
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool
   WHERE storage_pool_type = v_storage_pool_type;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_poolsByStorageDomainId(v_storage_domain_id UUID)
RETURNS SETOF storage_pool
   AS $procedure$
BEGIN
   RETURN QUERY SELECT storage_pool.*
   FROM storage_pool
   inner join storage_pool_iso_map on storage_pool.id = storage_pool_iso_map.storage_pool_id
   WHERE storage_pool_iso_map.storage_id = v_storage_domain_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_poolsByVdsId(v_vdsId UUID)
RETURNS SETOF storage_pool
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
RETURNS SETOF storage_pool
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
-- [storage_domain_static] Table
--

--This function is also called during installation. If you change it, please verify
--that functions in inst_sp.sql can be executed successfully.

Create or replace FUNCTION Insertstorage_domain_static(v_id UUID,
	v_storage VARCHAR(250),
	v_storage_name VARCHAR(250),
	v_storage_type INTEGER,
	v_storage_domain_type INTEGER,
    v_storage_domain_format_type VARCHAR(50))
RETURNS VOID
   AS $procedure$
   BEGIN
INSERT INTO storage_domain_static(id, storage,storage_name, storage_type, storage_domain_type, storage_domain_format_type)
	VALUES(v_id, v_storage, v_storage_name, v_storage_type, v_storage_domain_type, v_storage_domain_format_type);
END; $procedure$
LANGUAGE plpgsql;    





Create or replace FUNCTION Updatestorage_domain_static(v_id UUID,
	v_storage VARCHAR(250),
	v_storage_name VARCHAR(250),
	v_storage_type INTEGER,
	v_storage_domain_type INTEGER)
RETURNS VOID

	--The [storage_domain_static] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE storage_domain_static
      SET storage = v_storage,storage_name = v_storage_name,storage_type = v_storage_type, 
      storage_domain_type = v_storage_domain_type,_update_date = LOCALTIMESTAMP
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





Create or replace FUNCTION GetAllFromstorage_domain_static() RETURNS SETOF storage_domain_static
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domain_static;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_domain_staticByid(v_id UUID)
RETURNS SETOF storage_domain_static
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domain_static
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_domain_staticByName(v_name VARCHAR(250))
RETURNS SETOF storage_domain_static
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domain_static
   WHERE storage_name = v_name;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_domain_staticBystorage_pool_id(v_storage_pool_id UUID)
RETURNS SETOF storage_domain_static_view
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
RETURNS SETOF GetStorageDomainIdsByStoragePoolIdAndStatus_rs
   AS $procedure$
BEGIN
   RETURN QUERY
   SELECT storage_id
   FROM   storage_pool_iso_map
   WHERE  storage_pool_id = v_storage_pool_id
   AND    status = v_status;

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION Getstorage_domains_By_id(v_id UUID)
RETURNS SETOF storage_domains_without_storage_pools
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains_without_storage_pools
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getstorage_domains_By_id_and_by_storage_pool_id(v_id UUID,
	v_storage_pool_id UUID ) RETURNS SETOF storage_domains
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE id = v_id and storage_pool_id = v_storage_pool_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getstorage_domains_By_storagePoolId(v_storage_pool_id UUID)
RETURNS SETOF storage_domains
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE storage_pool_id = v_storage_pool_id;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getstorage_domains_By_connection(v_connection CHARACTER VARYING)
RETURNS SETOF storage_domains
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


Create or replace FUNCTION GetAllFromstorage_domains() RETURNS SETOF storage_domains
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getstorage_domain_staticBystorage_pool_type(v_storage_pool_type INTEGER)
RETURNS SETOF storage_domain_static
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domain_static
   WHERE storage_type = v_storage_pool_type;

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION Getstorage_domain_staticBystorage_type_and_storage_pool_id(v_storage_type INTEGER, v_storage_pool_id UUID) RETURNS SETOF storage_domains
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE storage_pool_id = v_storage_pool_id and storage_type = v_storage_type;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Force_Delete_storage_domain(v_storage_domain_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
	
	-- delete the map between the domain to templates that was copied to it
   delete FROM image_group_storage_domain_map where storage_domain_id = v_storage_domain_id;

	-- update images that their first storage domain was this and are on other domains (set the storage_id field to any other domain)
   update images set storage_id =(select  storage_domain_id from image_group_storage_domain_map map where images.image_group_id = map.image_group_id LIMIT 1)
   where storage_id = v_storage_domain_id and image_group_id in(select image_group_id from image_group_storage_domain_map);

	-- delete relations that are now duplicate because of the previous update action
   delete FROM image_group_storage_domain_map where image_group_id in(select images.image_group_id from images, image_group_storage_domain_map imap
   where images.image_group_id = imap.image_group_id and images.storage_id = imap.storage_domain_id);
	
   BEGIN
      CREATE GLOBAL TEMPORARY TABLE tt_TEMPTEMPLATESTABLE AS select vmt_guid
	
         from vm_template_disk where vtim_it_guid in(select image_guid from images where storage_id = v_storage_domain_id);
      exception when others then
         truncate table tt_TEMPTEMPLATESTABLE;
         insert into tt_TEMPTEMPLATESTABLE select vmt_guid
	
         from vm_template_disk where vtim_it_guid in(select image_guid from images where storage_id = v_storage_domain_id);
   END;

   BEGIN
      CREATE GLOBAL TEMPORARY TABLE tt_TEMPTEMPLATEIMAGESTABLE AS select image_id
	
         from image_vm_map AS vm_template_image_map where vm_id in(select vmt_guid from tt_TEMPTEMPLATESTABLE);
      exception when others then
         truncate table tt_TEMPTEMPLATEIMAGESTABLE;
         insert into tt_TEMPTEMPLATEIMAGESTABLE select image_id
	
         from image_vm_map AS vm_template_image_map where vm_id in(select vmt_guid from tt_TEMPTEMPLATESTABLE);
   END;

   delete FROM permissions where object_id in (select vm_guid from vm_images_view where storage_id = v_storage_domain_id);
   delete FROM vm_static where vm_guid in(select vm_guid from vm_images_view where storage_id = v_storage_domain_id)
   or vmt_guid in(select vmt_guid from tt_TEMPTEMPLATESTABLE);

   delete FROM images where storage_id = v_storage_domain_id;

   delete FROM image_vm_map where vm_id in(select vmt_guid from tt_TEMPTEMPLATESTABLE);

   delete FROM image_templates where it_guid in(select it_guid from tt_TEMPTEMPLATEIMAGESTABLE);

   delete FROM vm_interface where vmt_guid in(select vmt_guid from tt_TEMPTEMPLATESTABLE);
   delete FROM permissions where object_id in (select vmt_guid from tt_TEMPTEMPLATESTABLE);
   delete FROM permissions where object_id = v_storage_domain_id;
   delete from vm_static where vm_guid in(select vmt_guid from tt_TEMPTEMPLATESTABLE);
   delete FROM storage_domain_dynamic where id  = v_storage_domain_id;
   delete FROM storage_domain_static where id  = v_storage_domain_id;
    
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION Getstorage_domains_By_imageGroupId(v_image_group_id UUID)
RETURNS SETOF storage_domains
   AS $procedure$
BEGIN
   RETURN QUERY SELECT storage_domains.*
   FROM storage_domains
   INNER JOIN image_group_storage_domain_map as map on storage_domains.id = map.storage_domain_id
   WHERE map.image_group_id = v_image_group_id;

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION Getstorage_domains_List_By_storageDomainId(v_storage_domain_id UUID)
RETURNS SETOF storage_domains
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domains
   WHERE storage_domains.id = v_storage_domain_id;

END; $procedure$
LANGUAGE plpgsql;

DROP TYPE IF EXISTS Getstorage_domainsId_By_imageGroupId_rs CASCADE;
CREATE TYPE Getstorage_domainsId_By_imageGroupId_rs AS (storage_id UUID);
Create or replace FUNCTION Getstorage_domainsId_By_imageGroupId(v_image_group_id UUID)
RETURNS SETOF Getstorage_domainsId_By_imageGroupId_rs
   AS $procedure$
BEGIN
   RETURN QUERY SELECT 
   images.storage_id AS storage_id FROM images
   WHERE  images.image_group_id = v_image_group_id
   UNION
   SELECT image_group_storage_domain_map.storage_domain_id AS storage_id FROM image_group_storage_domain_map
   WHERE image_group_storage_domain_map.image_group_id = v_image_group_id;


END; $procedure$
LANGUAGE plpgsql;


--This SP returns all data centers containing clusters with permissions to run the given action by user
Create or replace FUNCTION fn_perms_get_storage_pools_with_permitted_action_on_vds_groups(v_user_id UUID, v_action_group_id integer) RETURNS SETOF storage_pool
   AS $procedure$
BEGIN
      RETURN QUERY SELECT * 
      FROM storage_pool
      WHERE storage_pool.id in 
	(SELECT storage_pool_id 
	 FROM vds_groups 
	 WHERE (SELECT get_entity_permissions(v_user_id, v_action_group_id, vds_groups.vds_group_id, 9)) IS NOT NULL);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getstorage_domains_By_storage_pool_id_and_connection(v_storage_pool_id UUID, v_connection CHARACTER VARYING)
RETURNS SETOF storage_domains
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

