SELECT fn_db_add_column('storage_pool', 'managed', 'BOOLEAN DEFAULT TRUE');
SELECT fn_db_add_column('cluster', 'managed', 'BOOLEAN DEFAULT TRUE');
SELECT fn_db_add_column('vm_static', 'namespace', 'VARCHAR(253)');

ALTER TABLE vds_static
DROP CONSTRAINT vds_static_vds_name_unique,
ADD CONSTRAINT vds_static_vds_name_unique UNIQUE (vds_name, cluster_id);

ALTER TABLE vds_static
DROP CONSTRAINT vds_static_host_name_unique,
ADD CONSTRAINT vds_static_host_name_unique UNIQUE (host_name, cluster_id);

