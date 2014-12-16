----------------------------------------------------------------
-- [LUNs] Table
--


Create or replace FUNCTION InsertLUNs(v_LUN_id VARCHAR(50),
	v_physical_volume_id VARCHAR(50) ,
	v_volume_group_id VARCHAR(50) ,
	v_serial VARCHAR(4000) ,
	v_lun_mapping INTEGER ,
	v_vendor_id VARCHAR(50) ,
	v_product_id VARCHAR(50) ,
	v_device_size INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO LUNs(LUN_id, physical_volume_id, volume_group_id, serial, lun_mapping, vendor_id, product_id, device_size)
	VALUES(v_LUN_id, v_physical_volume_id, v_volume_group_id, v_serial, v_lun_mapping, v_vendor_id, v_product_id, v_device_size);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateLUNs(v_LUN_id VARCHAR(50),
    v_physical_volume_id VARCHAR(50) ,
    v_volume_group_id VARCHAR(50) ,
    v_serial VARCHAR(4000) ,
    v_lun_mapping INTEGER ,
    v_vendor_id VARCHAR(50) ,
    v_product_id VARCHAR(50) ,
    v_device_size INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE LUNs
      SET LUN_id = v_LUN_id, physical_volume_id = v_physical_volume_id, volume_group_id = v_volume_group_id,
          serial = v_serial, lun_mapping = v_lun_mapping, vendor_id = v_vendor_id,
          product_id = v_product_id, device_size = v_device_size
      WHERE LUN_id = v_LUN_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteLUN(v_LUN_id VARCHAR(50))
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  VARCHAR(50);
BEGIN

	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
	-- in order to force locking parent before children
   select   LUN_id INTO v_val FROM LUNs  WHERE LUN_id = v_LUN_id     FOR UPDATE;

   DELETE FROM LUNs
   WHERE LUN_id = v_LUN_id;

END; $procedure$
LANGUAGE plpgsql;









Create or replace FUNCTION GetAllFromLUNs() RETURNS SETOF luns_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM luns_view;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetLUNsBystorage_server_connection(v_storage_server_connection VARCHAR(50)) RETURNS SETOF luns_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT luns_view.*
   FROM luns_view INNER JOIN
   LUN_storage_server_connection_map
   ON LUN_storage_server_connection_map.LUN_id = luns_view.LUN_id
   WHERE LUN_storage_server_connection_map.storage_server_connection = v_storage_server_connection;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetLUNsByVolumeGroupId(v_volume_group_id VARCHAR(50))
RETURNS SETOF luns_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM luns_view
   WHERE volume_group_id = v_volume_group_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetLUNByLUNId(v_LUN_id VARCHAR(50))
RETURNS SETOF luns_view STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM luns_view
   WHERE LUN_id = v_LUN_id;

END; $procedure$
LANGUAGE plpgsql;


----------------------------------------------------------------
-- [storage_domain_dynamic] Table
--

--This function is also called during installation. If you change it, please verify
--that functions in inst_sp.sql can be executed successfully.

Create or replace FUNCTION Insertstorage_domain_dynamic(v_available_disk_size INTEGER ,
	v_id UUID,
	v_used_disk_size INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO storage_domain_dynamic(available_disk_size, id, used_disk_size)
	VALUES(v_available_disk_size, v_id, v_used_disk_size);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updatestorage_domain_dynamic(v_available_disk_size INTEGER ,
	v_id UUID,
	v_used_disk_size INTEGER)
RETURNS VOID

	--The [storage_domain_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE storage_domain_dynamic
      SET available_disk_size = v_available_disk_size,used_disk_size = v_used_disk_size, _update_date = LOCALTIMESTAMP
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletestorage_domain_dynamic(v_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM storage_domain_dynamic
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromstorage_domain_dynamic() RETURNS SETOF storage_domain_dynamic STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domain_dynamic;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_domain_dynamicByid(v_id UUID)
RETURNS SETOF storage_domain_dynamic STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_domain_dynamic
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;




--The GetByFK stored procedure cannot be created because the [storage_domain_dynamic] table doesn't have at least one foreign key column or the foreign keys are also primary keys.

----------------------------------------------------------------
-- [storage_pool_iso_map] Table
--


Create or replace FUNCTION Insertstorage_pool_iso_map(v_storage_id UUID,
	v_storage_pool_id UUID,
	v_status INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO storage_pool_iso_map(storage_id, storage_pool_id, status)
	VALUES(v_storage_id, v_storage_pool_id, v_status);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletestorage_pool_iso_map(v_storage_id UUID,
	v_storage_pool_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM storage_pool_iso_map
   WHERE storage_id = v_storage_id AND storage_pool_id = v_storage_pool_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromstorage_pool_iso_map() RETURNS SETOF storage_pool_iso_map STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool_iso_map;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_pool_iso_mapBystorage_idAndBystorage_pool_id(v_storage_id UUID,v_storage_pool_id UUID) RETURNS SETOF storage_pool_iso_map STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool_iso_map
   WHERE storage_id = v_storage_id AND storage_pool_id = v_storage_pool_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_pool_iso_mapsBystorage_id(v_storage_id UUID)
RETURNS SETOF storage_pool_iso_map STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool_iso_map
   WHERE storage_id = v_storage_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_pool_iso_mapsByBystorage_pool_id(v_storage_id UUID,
	v_storage_pool_id UUID) RETURNS SETOF storage_pool_iso_map STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_pool_iso_map
   WHERE storage_pool_id = v_storage_pool_id;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Updatestorage_pool_iso_map_status(v_storage_id UUID,
        v_storage_pool_id UUID,
        v_status INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
   UPDATE storage_pool_iso_map
   SET status = v_status
   WHERE storage_pool_id = v_storage_pool_id AND storage_id = v_storage_id;
END; $procedure$
LANGUAGE plpgsql;



--The GetByFK stored procedure cannot be created because the [storage_pool_iso_map] table doesn't have at least one foreign key column or the foreign keys are also primary keys.

----------------------------------------------------------------
-- [storage_server_connections] Table
--

--This function is also called during installation. If you change it, please verify
--that functions in inst_sp.sql can be executed successfully.

Create or replace FUNCTION Insertstorage_server_connections(v_connection VARCHAR(250),
	v_id VARCHAR(50),
	v_iqn VARCHAR(128) ,
	v_port VARCHAR(50) ,
	v_portal VARCHAR(50) ,
	v_password text,
	v_storage_type INTEGER,
	v_user_name VARCHAR(50),
	v_mount_options VARCHAR(500),
	v_vfs_type VARCHAR(128),
	v_nfs_version VARCHAR(4),
	v_nfs_timeo smallint,
	v_nfs_retrans smallint)


RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO storage_server_connections(connection, id, iqn, port,portal,
	password, storage_type, user_name,mount_options,vfs_type,nfs_version,nfs_timeo,nfs_retrans)
	VALUES(v_connection, v_id, v_iqn,v_port,v_portal, v_password, v_storage_type, v_user_name,v_mount_options,v_vfs_type,v_nfs_version,v_nfs_timeo,v_nfs_retrans);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updatestorage_server_connections(v_connection VARCHAR(250),
	v_id VARCHAR(50),
	v_iqn VARCHAR(128) ,
	v_password text,
	v_storage_type INTEGER,
	v_port VARCHAR(50) ,
	v_portal VARCHAR(50) ,
	v_user_name VARCHAR(50),
	v_mount_options VARCHAR(500),
	v_vfs_type VARCHAR(128),
	v_nfs_version VARCHAR(4),
	v_nfs_timeo smallint,
	v_nfs_retrans smallint)

RETURNS VOID

	--The [storage_server_connections] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE storage_server_connections
      SET connection = v_connection,iqn = v_iqn,password = v_password,port = v_port,
      portal = v_portal,storage_type = v_storage_type,user_name = v_user_name,mount_options = v_mount_options, vfs_type = v_vfs_type, nfs_version = v_nfs_version, nfs_timeo = v_nfs_timeo, nfs_retrans = v_nfs_retrans
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Deletestorage_server_connections(v_id VARCHAR(50))
RETURNS VOID
   AS $procedure$
   DECLARE
   v_val  VARCHAR(50);
BEGIN

	-- Get (and keep) a shared lock with "right to upgrade to exclusive"
	-- in order to force locking parent before children
   select   id INTO v_val FROM storage_server_connections  WHERE id = v_id     FOR UPDATE;

   DELETE FROM storage_server_connections
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_server_connectionsByid(v_id VARCHAR(50))
RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_server_connections
   WHERE id = v_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_server_connectionsByConnection(v_connection VARCHAR(250))
RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_server_connections
   WHERE connection = v_connection;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getstorage_server_connectionsByIqn(v_iqn VARCHAR(128))
RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_server_connections
   WHERE iqn = v_iqn OR iqn IS NULL AND v_iqn IS NULL;

END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION Getstorage_server_connectionsByIqnAndConnection(v_iqn VARCHAR(128) ,
	v_connection VARCHAR(250)) RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_server_connections
   WHERE iqn = v_iqn and (connection = v_connection or connection is NULL);

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION Getstorage_server_connectionsByKey(v_iqn VARCHAR(128) ,
	v_connection VARCHAR(250),
	v_port VARCHAR(50) ,
	v_portal VARCHAR(50) ,
	v_username VARCHAR(50)) RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_server_connections
   WHERE (iqn = v_iqn or (iqn IS NULL AND v_iqn IS NULL)) AND
			(connection = v_connection) AND
			(port = v_port or (port IS NULL AND v_port IS NULL)) AND
			(portal = v_portal or (portal is NULL AND v_portal IS NULL)) AND
			(user_name = v_username or (user_name is NULL AND v_username IS NULL));

END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION Getstorage_server_connectionsByStorageType(v_storage_type INTEGER)
RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_server_connections
   WHERE storage_type = v_storage_type;

END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllstorage_server_connections()
RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_server_connections;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetStorageServerConnectionsByIds(v_ids TEXT)
RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM storage_server_connections WHERE id = any(string_to_array(v_ids,',')::VARCHAR[]);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Getstorage_server_connectionsByVolumeGroupId(v_volume_group_id VARCHAR(50)) RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT DISTINCT storage_server_connections.*
   FROM
   LUN_storage_server_connection_map  LUN_storage_server_connection_map
   INNER JOIN
   LUNs ON LUN_storage_server_connection_map.LUN_id = LUNs.LUN_id INNER JOIN
   storage_domain_static ON LUNs.volume_group_id = storage_domain_static.storage INNER JOIN
   storage_server_connections ON
   LUN_storage_server_connection_map.storage_server_connection = storage_server_connections.id
   WHERE     (storage_domain_static.storage = v_volume_group_id);
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetStorageConnectionsByStorageTypeAndStatus(v_storage_pool_id UUID, v_storage_type integer, v_statuses varchar(20))
RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
DECLARE
  statuses int[];
BEGIN
statuses := string_to_array(v_statuses,',')::integer[];
RETURN QUERY SELECT * FROM (SELECT distinct storage_server_connections.*
   FROM
   LUN_storage_server_connection_map LUN_storage_server_connection_map
   INNER JOIN  LUNs ON LUN_storage_server_connection_map.LUN_id = LUNs.LUN_id
   INNER JOIN  storage_domains ON LUNs.volume_group_id = storage_domains.storage
   INNER JOIN  storage_server_connections ON LUN_storage_server_connection_map.storage_server_connection = storage_server_connections.id
   WHERE     (storage_domains.storage_pool_id = v_storage_pool_id  and storage_domains.status = any(statuses))
   UNION
   SELECT distinct storage_server_connections.*
   FROM         storage_server_connections
   INNER JOIN  storage_domains ON storage_server_connections.id = storage_domains.storage
   WHERE     (storage_domains.storage_pool_id = v_storage_pool_id and storage_domains.status = any(statuses))
   ) connections WHERE (v_storage_type is NULL or connections.storage_type = v_storage_type);
END; $procedure$
LANGUAGE plpgsql;




Create or replace FUNCTION GetNFSstorage_server_connectionsByStoragePoolId(v_storage_pool_id UUID)
RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT distinct storage_server_connections.*
   FROM    storage_server_connections storage_server_connections
   INNER JOIN
   storage_domain_static_view ON
   storage_server_connections.id = storage_domain_static_view.storage
   WHERE     (storage_domain_static_view.storage_pool_id = v_storage_pool_id);
END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION Getstorage_server_connectionsByLunId(v_lunId VARCHAR(50))
RETURNS SETOF storage_server_connections STABLE
   AS $procedure$
BEGIN
RETURN QUERY SELECT storage_server_connections.*
   FROM  storage_server_connections storage_server_connections
   INNER JOIN lun_storage_server_connection_map ON
   lun_storage_server_connection_map.storage_server_connection = storage_server_connections.id
   WHERE     (lun_storage_server_connection_map.lun_id = v_lunId);
END; $procedure$
LANGUAGE plpgsql;

--The GetByFK stored procedure cannot be created because the [storage_server_connections] table doesn't have at least one foreign key column or the foreign keys are also primary keys.


----------------------------------------------------------------
-- [LUN_storage_server_connection_map] Table
--


Create or replace FUNCTION InsertLUN_storage_server_connection_map(v_LUN_id VARCHAR(50),
	v_storage_server_connection VARCHAR(50))
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO LUN_storage_server_connection_map(LUN_id, storage_server_connection)
	VALUES(v_LUN_id, v_storage_server_connection);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION UpdateLUN_storage_server_connection_map(v_LUN_id VARCHAR(50),
	v_storage_server_connection VARCHAR(50))
RETURNS VOID

	--The [LUN_storage_server_connection_map] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION DeleteLUN_storage_server_connection_map(v_LUN_id VARCHAR(50),
	v_storage_server_connection VARCHAR(50))
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM LUN_storage_server_connection_map
   WHERE LUN_id = v_LUN_id AND storage_server_connection = v_storage_server_connection;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromLUN_storage_server_connection_map()
RETURNS SETOF LUN_storage_server_connection_map STABLE
   AS $procedure$
BEGIN

   RETURN QUERY SELECT *
   FROM LUN_storage_server_connection_map lUN_storage_server_connection_map;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetLUN_storage_server_connection_mapByLUNBystorage_server_conn(v_LUN_id VARCHAR(50),v_storage_server_connection VARCHAR(50)) RETURNS SETOF LUN_storage_server_connection_map STABLE
   AS $procedure$
BEGIN

   RETURN QUERY SELECT *
   FROM LUN_storage_server_connection_map lUN_storage_server_connection_map
   WHERE LUN_id = v_LUN_id AND storage_server_connection = v_storage_server_connection;

END; $procedure$
LANGUAGE plpgsql;





