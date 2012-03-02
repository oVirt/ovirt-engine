--Add virt_service and gluster_service fields into vds_group
SELECT fn_db_add_column('vds_groups', 'virt_service', 'boolean not null default true');
SELECT fn_db_add_column('vds_groups', 'gluster_service', 'boolean not null default false');
