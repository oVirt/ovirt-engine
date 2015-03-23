-- The following stored procedures are relevant to oVirt Installer only

Create or replace FUNCTION inst_update_default_storage_pool_type(v_is_local boolean)
RETURNS VOID
   AS $procedure$
BEGIN
         UPDATE storage_pool
         SET is_local = v_is_local, _update_date = LOCALTIMESTAMP
         WHERE storage_pool.name = 'Default' and not exists
         (select 1 from storage_domains where storage_domains.storage_pool_name = 'Default');
END; $procedure$
LANGUAGE plpgsql;




-- This function calls insert_server_connections, insertstorage_domain_static,insertstorage_domain_dynamic
-- Any change to these functions may effect correctness of the installion.

Create or replace FUNCTION inst_add_iso_storage_domain(v_storage_domain_id UUID, v_name VARCHAR(250), v_connection_id uuid, v_connection VARCHAR(250),v_available int, v_used int)
RETURNS VOID
   AS $procedure$
   DECLARE
BEGIN
    if not exists (select 1 from storage_server_connections where connection = v_connection) then
        -- Insert storage server connection info
        perform Insertstorage_server_connections(v_connection,cast(v_connection_id as varchar(250)),NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,NULL,NULL);
        -- Insert storage domain static info
        perform Insertstorage_domain_static(v_storage_domain_id,cast(v_connection_id as varchar(250)),v_name,v_name,'',1,2,'0',0, FALSE);
        -- Insert storage domain dynamic  info
        perform Insertstorage_domain_dynamic(v_available,v_storage_domain_id,v_used);
    end if;
    exception
        when others then
	    RAISE EXCEPTION 'NUM:%, DETAILS:%', SQLSTATE, SQLERRM;
END; $procedure$
LANGUAGE plpgsql;

-- Updates service types(gluster and virt) in vds_groups table
create or replace FUNCTION inst_update_service_type(v_cluster_id uuid, v_virt_service boolean,
                                                     v_gluster_service boolean)
returns void
AS $procedure$
begin
    update  vds_groups set virt_service = v_virt_service, gluster_service = v_gluster_service
    where vds_group_id = v_cluster_id;
END; $procedure$
LANGUAGE plpgsql;

-- Adds a new glance provider, according to the specified arguments
CREATE OR REPLACE FUNCTION inst_add_glance_provider(
    v_provider_id UUID,
    v_provider_name VARCHAR(128),
    v_provider_description VARCHAR(4000),
    v_provider_url VARCHAR(512),
    v_storage_domain_id UUID
)
RETURNS VOID
AS $procedure$
BEGIN
    -- Adding the Glance provider
    insert into providers(id, name, description, url, provider_type, auth_required)
    select v_provider_id, v_provider_name, v_provider_description, v_provider_url, 'OPENSTACK_IMAGE', false
    where not exists (select id from providers where id = v_provider_id);

    -- Adding a proper storage domain static entry
    insert into storage_domain_static(id, storage, storage_name, storage_domain_type, storage_type, storage_domain_format_type, recoverable)
    select v_storage_domain_id, v_provider_id, v_provider_name, 4, 8, 0, true
    where not exists (select id from storage_domain_static where id = v_storage_domain_id);

    -- Adding a proper storage domain dynamic entry
    insert into storage_domain_dynamic(id, available_disk_size, used_disk_size)
    select v_storage_domain_id, NULL, NULL
    where not exists (select id from storage_domain_dynamic where id = v_storage_domain_id);

END; $procedure$
LANGUAGE plpgsql;


